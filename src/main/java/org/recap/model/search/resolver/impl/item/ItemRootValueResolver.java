package org.recap.model.search.resolver.impl.item;

import org.recap.model.search.resolver.ItemValueResolver;
import org.recap.model.solr.Item;

/**
 * Created by peris on 9/29/16.
 */
public class ItemRootValueResolver implements ItemValueResolver {
    @Override
    public Boolean isInterested(String field) {
        return "_version_".equals(field);
    }

    @Override
    public void setValue(Item item, Object value) {
        item.setVersion(String.valueOf(value));
    }
}
