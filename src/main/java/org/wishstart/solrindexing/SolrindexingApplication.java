package org.wishstart.solrindexing;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SolrindexingApplication implements CommandLineRunner{

	@Autowired
	private JobLauncher jobLauncher;

	public static void main(String[] args) {
		SpringApplication.run(SolrindexingApplication.class, args).close();
	}

	@Autowired
	private Job job;

	@Override
	public void run(String... args) throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("date", System.currentTimeMillis())
				.toJobParameters();

		jobLauncher.run(job, jobParameters);
	}
}
