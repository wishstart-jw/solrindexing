package org.wishstart.solrindexing;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class IndexingJobBuilder {

	@Bean
	public Job indexingJob(JobRepository jobRepository, JobExecutionListener listener, Step step1) {
		return new JobBuilder("indexingJob", jobRepository)
			.listener(listener)
			.start(step1)
			.build();
	}

	@Bean
	public Step step1(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
					FlatFileItemReader<Map<String, Object>> reader, MapToSolrDocumentProcessor processor, SolrItemWriter writer) {
		return new StepBuilder("step1", jobRepository)
			.<Map<String, Object>,SolrInputDocument> chunk(10, transactionManager)
			.reader(reader)
			.processor(processor)
			.writer(writer)
			.build();
	}


	//private String [] names = {"availability","condition","issale","shipping","manufacturernumber","name","weight","price","asin","id","url","image_url","brand_id"};

	@Bean
	public FlatFileItemReader<Map<String, Object>> reader() {
		log.info("Building item reader");
		return new FlatFileItemReaderBuilder<Map<String, Object>>()
			.name("itemReader")
			.resource(new FileSystemResource("items.csv"))
			.linesToSkip(0)
			.lineMapper(new LineMapper<Map<String, Object>>() {
				DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(",");

				@Override
				public Map<String, Object> mapLine(String line, int lineNumber) throws Exception {
					if(!tokenizer.hasNames() && lineNumber == 1) {
						tokenizer.setNames(line.split(","));
						return null;
					}
					FieldSet fs = tokenizer.tokenize(line);
					Map<String, Object> map = new HashMap<>();
					fs.getProperties().entrySet().forEach(e -> map.put((String)e.getKey(), e.getValue()));
					return map;
				}
			})
			.build();
	}
	/*
	@Bean JdbcCursorItemReader reader() {
		return new JdbcCursorItemReader(){
			
		};
	}*/
}
