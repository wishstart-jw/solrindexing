package org.wishstart.solrindexing;

import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class QueryManager {

	private Map<String, String> insertQueries = 
			Map.of("INSERT_ITEMS", """
				select * from items
				""");

	private Map<String, String> updateQueries = 
			Map.of("UPDATE_BRANDS",
					 """
					select id, brand_name 
					from items
						join brands using (brand_id)
					""",

				"UPDATE_CATEGORIES",
				"""
				select id, category_name
				from item_categories 
					join categories using(category_id)
				""");
}
