package org.recap.model.search.resolver.impl.Bib;

import org.recap.model.search.resolver.BibValueResolver;
import org.recap.model.solr.BibItem;

/**
 * Created by rajeshbabuk on 27/10/16.
 */
public class BibLastUpdatedByValueResolver implements BibValueResolver {
    @Override
    public Boolean isInterested(String field) {
        return field.equalsIgnoreCase("BibLastUpdatedBy");
    }

    @Override
    public void setValue(BibItem bibItem, Object value) {
        bibItem.setBibLastUpdatedBy((String) value);
    }
}