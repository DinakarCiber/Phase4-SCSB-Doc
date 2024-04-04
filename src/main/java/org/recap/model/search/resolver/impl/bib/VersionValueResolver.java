package org.recap.model.search.resolver.impl.bib;

import org.recap.model.search.resolver.BibValueResolver;
import org.recap.model.solr.BibItem;

/**
 * @author Dinakar N created on 21/08/23
 */
public class VersionValueResolver implements BibValueResolver {
    @Override
    public Boolean isInterested(String field) {
        return "_version_".equalsIgnoreCase(field);
    }

    @Override
    public void setValue(BibItem bibItem, Object value) {
        bibItem.setVersion(String.valueOf(value));
    }
}
