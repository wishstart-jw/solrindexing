package org.wishstart.solrindexing;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "solr")
public class IndexingJobBuilder {

	@Getter @Setter
	private String solrUrl;

	@Bean
	public SolrClient solrClient() {
		return new Http2SolrClient.Builder(solrUrl)
				.withConnectionTimeout(10000, TimeUnit.MILLISECONDS)
				.withRequestTimeout(60000, TimeUnit.MILLISECONDS)
				.build();
	}

	@Bean
	public Job indexingJob(JobRepository jobRepository, JobExecutionListener listener, Step step1,
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
	public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager, Tasklet clearSolrCollectiontasklet) {
		return new StepBuilder("step1", jobRepository)
			.tasklet(clearSolrCollectiontasklet, transactionManager)
			.build();
	}

	@Bean
	public Step step2(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
			ItemReader<Map<String, Object>> itemReader, MapToSolrDocumentProcessor processor, SolrItemWriter writer) {
		return stepBuilder("step_add_items", jobRepository, transactionManager, itemReader, processor, writer);	
	}

	@Bean
	public Step step3(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
			ItemReader<Map<String, Object>> brandReader, MapToSolrDocumentUpdateProcessor processor, SolrItemWriter writer) {
		return stepBuilder("step_add_brands", jobRepository, transactionManager, brandReader, processor, writer);	}

	@Bean
	public Step step4(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
			ItemReader<Map<String, Object>> categoryReader, MapToSolrDocumentUpdateProcessor processor, SolrItemWriter writer) {
		return stepBuilder("step_add_categories", jobRepository, transactionManager, categoryReader, processor, writer);
	}


	private Step stepBuilder(String name, JobRepository jobRepository, DataSourceTransactionManager transactionManager,
		ItemReader<Map<String, Object>> reader, ItemProcessor processor, ItemWriter writer) {
		return new StepBuilder(name, jobRepository)
			.<Map<String, Object>,SolrInputDocument> chunk(100, transactionManager)
			.reader(reader)
			.processor(processor)
			.writer(writer)
			.build();
	}

	@Bean
	public JdbcCursorItemReader<Map<String, Object>> itemReader(DataSource dataSource) {
		String sql = """
				select * from items
				""";
		return readerBuilder(dataSource, sql);
	}

	@Bean
	public JdbcCursorItemReader<Map<String, Object>> brandReader(DataSource dataSource) {
		String sql = """
				select id, brand_name 
				from items
					join brands using (brand_id)
				""";
		return readerBuilder(dataSource, sql);
	}

	@Bean
	public JdbcCursorItemReader<Map<String, Object>> categoryReader(DataSource dataSource) {
		String sql = """
				select id, category_name
				from item_categories 
					join categories using(category_id)
				""";
		return readerBuilder(dataSource, sql);
	}

	private JdbcCursorItemReader<Map<String, Object>> readerBuilder(DataSource dataSource, String sql) {
		JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
		reader.setDataSource(dataSource);
		reader.setSql(sql);
		reader.setFetchSize(1000);
		reader.setRowMapper(new ColumnMapRowMapper());
		return reader;
		
	}
}
