package org.recap.repository.solr.main;

import org.recap.model.solr.Item;
import org.springframework.data.solr.repository.SolrCrudRepository;

/**
 * Created by angelind on 15/6/16.
 */
public interface ItemCrudRepository extends SolrCrudRepository<Item, String> {

    Item findByBarcode(String barcode);
}
