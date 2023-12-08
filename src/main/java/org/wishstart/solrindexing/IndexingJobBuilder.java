package org.wishstart.solrindexing;

import java.util.Map;

import javax.sql.DataSource;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
public class IndexingJobBuilder {

	private JobRepository jobRepository;
	private DataSourceTransactionManager transactionManager;
	private DataSource dataSource;
	private MapToSolrDocumentProcessor insertProcessor;
	private MapToSolrDocumentUpdateProcessor updateProcessor;
	private SolrItemWriter solrItemWriter;
	private QueryManager queryManager;

	public IndexingJobBuilder(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
			DataSource dataSource, MapToSolrDocumentProcessor insertProcessor, MapToSolrDocumentUpdateProcessor updateProcessor, 
			QueryManager queryManager, SolrItemWriter solrItemWriter) {
		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
		this.dataSource = dataSource;
		this.insertProcessor = insertProcessor;
		this.updateProcessor = updateProcessor;
		this.queryManager = queryManager;
		this.solrItemWriter = solrItemWriter;
	}

	@Bean
	public Job indexingJob(JobExecutionListener listener, Flow updateFlow) {
		return new JobBuilder("indexingJob", jobRepository)
			.listener(listener)
			.start(updateFlow)
			.end()
			.build();
	}

	@Bean
	public Step clearCollectionStep(Tasklet clearSolrCollectiontasklet) {
		return new StepBuilder("step1", jobRepository)
			.tasklet(clearSolrCollectiontasklet, transactionManager)
			.build();
	}

	@Bean
	public Flow updateFlow(Step clearCollectionStep) {
		FlowBuilder<SimpleFlow> builder = new FlowBuilder<SimpleFlow>("flow_update")
				.start(clearCollectionStep);
		queryManager.getInsertQueries()
			.entrySet()
			.forEach(q -> builder.next(stepBuilder("step_" + q.getKey().toLowerCase(), readerBuilder(q.getValue()), insertProcessor)));
		queryManager.getUpdateQueries()
			.entrySet()
			.forEach(q -> builder.next(stepBuilder("step_" + q.getKey().toLowerCase(), readerBuilder(q.getValue()), updateProcessor)));

		return builder.build();
	}

	private Step stepBuilder(String name, ItemReader<Map<String, Object>> reader, ItemProcessor<Map<String, Object>, SolrInputDocument> processor) {
		return new StepBuilder(name, jobRepository)
			.<Map<String, Object>,SolrInputDocument> chunk(100, transactionManager)
			.reader(reader)
			.processor(processor)
			.writer(solrItemWriter)
			.build();
	}

	private JdbcCursorItemReader<Map<String, Object>> readerBuilder(String sql) {
		JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
		reader.setDataSource(dataSource);
		reader.setSql(sql);
		reader.setFetchSize(1000);
		reader.setRowMapper(new ColumnMapRowMapper());
		return reader;
		
	}
}
