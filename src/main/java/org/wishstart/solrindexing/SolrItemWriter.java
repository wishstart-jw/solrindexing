package org.wishstart.solrindexing;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class SolrItemWriter implements ItemWriter<SolrInputDocument>{

	private final String solrUrl = "http://localhost:8983/solr/sample_tech";
	private static final int DOCCOUNT_DELAY = 500;

	private SolrClient solrClient = getSolrClient();
			
	private SolrClient getSolrClient() {
		return new Http2SolrClient.Builder(solrUrl)
				.withConnectionTimeout(10000, TimeUnit.MILLISECONDS)
				.withRequestTimeout(60000, TimeUnit.MILLISECONDS)
				.build();
	}

	@Override
	public void write(Chunk<? extends SolrInputDocument> chunk) throws Exception {
		// TODO Auto-generated method stub
		solrClient.add((Collection<SolrInputDocument>) chunk.getItems(), DOCCOUNT_DELAY);
	}

}
