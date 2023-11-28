package org.wishstart.solrindexing;

import org.apache.solr.client.solrj.SolrClient;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@ConfigurationProperties(prefix = "solr")
@Slf4j
public class ClearSolrCollectionTasklet implements Tasklet {

	@Autowired
	private SolrClient solrClient;
			
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		String queryString = "*:*";
		log.info("About to empty collection");
		solrClient.deleteByQuery(queryString);
		solrClient.commit();
		log.info("Emptied solr collection");
		return RepeatStatus.FINISHED;
	}

}
