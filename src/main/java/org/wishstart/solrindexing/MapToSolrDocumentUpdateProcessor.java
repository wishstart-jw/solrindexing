package org.wishstart.solrindexing;

import java.util.Map;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class MapToSolrDocumentUpdateProcessor implements ItemProcessor<Map<String, Object>, SolrInputDocument>{

	@Override
	public SolrInputDocument process(Map<String, Object> item) throws Exception {
		SolrInputDocument doc = new SolrInputDocument();
		Object id = item.remove("id");
		doc.addField("id", id);
		doc.addField("_version_", 1);
		item.entrySet()
			.stream()
			.filter(e -> e.getValue() != null)
			.filter(e -> ! e.getValue().equals(""))
			.forEach(e -> doc.addField(e.getKey().toLowerCase(), Map.of("add", e.getValue())));
		return doc;
	}
}
