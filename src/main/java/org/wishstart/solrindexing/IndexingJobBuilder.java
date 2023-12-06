package org.wishstart.solrindexing;

import java.util.HashMap;
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
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConfigurationProperties(prefix = "solr")
@Slf4j
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
		return new StepBuilder("step_add_items", jobRepository)
			.<Map<String, Object>,SolrInputDocument> chunk(10, transactionManager)
			.reader(itemReader)
			.processor(processor)
			.writer(writer)
			.build();
	}

	@Bean
	public Step step3(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
					ItemReader<Map<String, Object>> brandReader, MapToSolrDocumentUpdateProcessor processor, SolrItemWriter writer) {
		return new StepBuilder("step_add_brands", jobRepository)
			.<Map<String, Object>,SolrInputDocument> chunk(10, transactionManager)
			.reader(brandReader)
			.processor(processor)
			.writer(writer)
			.build();
	}

	@Bean
	public Step step4(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
					ItemReader<Map<String, Object>> categoryReader, MapToSolrDocumentUpdateProcessor processor, SolrItemWriter writer) {
		return new StepBuilder("step_add_categories", jobRepository)
			.<Map<String, Object>,SolrInputDocument> chunk(10, transactionManager)
			.reader(categoryReader)
			.processor(processor)
			.writer(writer)
			.build();
	}

	private String [] names = {"availability","condition","issale","shipping","manufacturernumber","name","weight","price","asin","id","url","image_url","brand_id"};

	@Bean
	public FlatFileItemReader<Map<String, Object>> reader() {
		log.info("Building item reader");
		return new FlatFileItemReaderBuilder<Map<String, Object>>()
			.name("itemReader")
			.resource(new FileSystemResource("items.csv"))
			.linesToSkip(1)
			.lineMapper(new LineMapper<Map<String, Object>>() {
				DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(",");

				@Override
				public Map<String, Object> mapLine(String line, int lineNumber) throws Exception {
					if(!tokenizer.hasNames()) {
						tokenizer.setNames(names);
					}
					FieldSet fs = tokenizer.tokenize(line);
					Map<String, Object> map = new HashMap<>();
					fs.getProperties().entrySet().forEach(e -> map.put((String)e.getKey(), e.getValue()));
					return map;
				}
			})
			.build();
	}

	@Bean
	public JdbcCursorItemReader<Map<String, Object>> itemReader(DataSource dataSource) {
		String sql = """
				select * from items
				""";
		JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
		reader.setDataSource(dataSource);
		reader.setSql(sql);
		reader.setFetchSize(1000);
		reader.setRowMapper(new ColumnMapRowMapper());
		return reader;
	}

	@Bean
	public JdbcCursorItemReader<Map<String, Object>> brandReader(DataSource dataSource) {
		String sql = """
				select id, brand_name 
				from items
					join brands using (brand_id)
				""";
		JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
		reader.setDataSource(dataSource);
		reader.setSql(sql);
		reader.setFetchSize(1000);
		reader.setRowMapper(new ColumnMapRowMapper());
		return reader;
	}

	@Bean
	public JdbcCursorItemReader<Map<String, Object>> categoryReader(DataSource dataSource) {
		String sql = """
				select id, category_name
				from item_categories 
					join categories using(category_id)
				""";
		JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
		reader.setDataSource(dataSource);
		reader.setSql(sql);
		reader.setFetchSize(1000);
		reader.setRowMapper(new ColumnMapRowMapper());
		return reader;
	}
}
