package org.wishstart.solrindexing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SolrindexingApplication {

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(SolrindexingApplication.class, args)));
	}

}
