package org.wishstart.solrindexing;

import java.util.Collection;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SolrItemWriter implements ItemWriter<SolrInputDocument> {

	private static final int DOCCOUNT_DELAY = 500;

	@Autowired
	private SolrClient solrClient;

	@Override
	public void write(Chunk<? extends SolrInputDocument> chunk) throws Exception {
		solrClient.add((Collection<SolrInputDocument>) chunk.getItems(), DOCCOUNT_DELAY);
		log.debug("Wrote items to solr");
	}

}
