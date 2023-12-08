package org.wishstart.solrindexing;

import java.util.concurrent.TimeUnit;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "solr")
public class SupportComponents {

	@Getter @Setter
	private String solrUrl;

	@Bean
	public SolrClient solrClient() {
		return new Http2SolrClient.Builder(solrUrl)
				.withConnectionTimeout(10000, TimeUnit.MILLISECONDS)
				.withRequestTimeout(60000, TimeUnit.MILLISECONDS)
				.build();
	}

}
