package org.wishstart.solrindexing;

import java.util.Map;

import javax.sql.DataSource;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
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

	public IndexingJobBuilder(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
			DataSource dataSource, MapToSolrDocumentProcessor insertProcessor, MapToSolrDocumentUpdateProcessor updateProcessor, 
			SolrItemWriter solrItemWriter) {
		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
		this.dataSource = dataSource;
		this.insertProcessor = insertProcessor;
		this.updateProcessor = updateProcessor;
		this.solrItemWriter = solrItemWriter;
	}

	@Bean
	public Job indexingJob(JobExecutionListener listener, Step step1,
			Step step2, Step step3, Step step4) {
		return new JobBuilder("indexingJob", jobRepository)
			.listener(listener)
			.start(step1)
			.next(step2)
			.next(step3)
			.next(step4)
			.build();
	}

	@Bean
	public Step step1(Tasklet clearSolrCollectiontasklet) {
		return new StepBuilder("step1", jobRepository)
			.tasklet(clearSolrCollectiontasklet, transactionManager)
			.build();
	}

	@Bean
	public Step step2(ItemReader<Map<String, Object>> itemReader) {
		return stepBuilder("step_add_items", itemReader, insertProcessor);	
	}

	@Bean
	public Step step3(ItemReader<Map<String, Object>> brandReader) {
		return stepBuilder("step_add_brands", brandReader, updateProcessor);	}

	@Bean
	public Step step4(ItemReader<Map<String, Object>> categoryReader) {
		return stepBuilder("step_add_categories", categoryReader, updateProcessor);
	}

	private Step stepBuilder(String name, ItemReader<Map<String, Object>> reader, ItemProcessor<Map<String, Object>, SolrInputDocument> processor) {
		return new StepBuilder(name, jobRepository)
			.<Map<String, Object>,SolrInputDocument> chunk(100, transactionManager)
			.reader(reader)
			.processor(processor)
			.writer(solrItemWriter)
			.build();
	}

	@Bean
	public JdbcCursorItemReader<Map<String, Object>> itemReader() {
		String sql = """
				select * from items
				""";
		return readerBuilder(sql);
	}

	@Bean
	public JdbcCursorItemReader<Map<String, Object>> brandReader() {
		String sql = """
				select id, brand_name 
				from items
					join brands using (brand_id)
				""";
		return readerBuilder(sql);
	}

	@Bean
	public JdbcCursorItemReader<Map<String, Object>> categoryReader() {
		String sql = """
				select id, category_name
				from item_categories 
					join categories using(category_id)
				""";
		return readerBuilder(sql);
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
