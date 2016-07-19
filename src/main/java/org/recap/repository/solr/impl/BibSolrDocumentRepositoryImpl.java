package org.recap.repository.solr.impl;

import org.apache.commons.lang3.StringUtils;
import org.recap.RecapConstants;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.model.solr.BibItem;
import org.recap.model.solr.Item;
import org.recap.repository.solr.main.CustomDocumentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Join;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by rajeshbabuk on 8/7/16.
 */
@Repository
public class BibSolrDocumentRepositoryImpl implements CustomDocumentRepository {

    @Resource
    private SolrTemplate solrTemplate;

    @Override
    public List<BibItem> search(SearchRecordsRequest searchRecordsRequest, Pageable page) {

        SimpleQuery query = new SimpleQuery();
        query.setPageRequest(page);
        query.addSort(new Sort(Sort.Direction.ASC, RecapConstants.TITLE));
        query.addCriteria(getCriteriaForFieldName(searchRecordsRequest));

        SimpleFilterQuery filterQuery = new SimpleFilterQuery();
        String fieldName = searchRecordsRequest.getFieldName();
        if (RecapConstants.NOTES.equals(fieldName) || RecapConstants.CALL_NUMBER.equals(fieldName) || RecapConstants.BARCODE.equals(fieldName)) {
            query.setJoin(Join.from(RecapConstants.HOLDINGS_ID).to(RecapConstants.HOLDINGS_ID));
            query.addCriteria(new Criteria(RecapConstants.COLLECTION_GROUP_DESIGNATION).in(searchRecordsRequest.getCollectionGroupDesignations()));
            query.addCriteria(new Criteria(RecapConstants.AVAILABILITY).in(searchRecordsRequest.getAvailability()));

            if (!CollectionUtils.isEmpty(searchRecordsRequest.getOwningInstitutions()) || !CollectionUtils.isEmpty(searchRecordsRequest.getMaterialTypes())) {
                filterQuery.setJoin(Join.from(RecapConstants.HOLDINGS_ID).to(RecapConstants.HOLDINGS_ID));
            }
            if (!CollectionUtils.isEmpty(searchRecordsRequest.getOwningInstitutions())) {
                filterQuery.addCriteria(new Criteria(RecapConstants.OWNING_INSTITUTION).in(searchRecordsRequest.getOwningInstitutions()));
            }
            if (!CollectionUtils.isEmpty(searchRecordsRequest.getMaterialTypes())) {
                filterQuery.addCriteria(new Criteria(RecapConstants.LEADER_MATERIAL_TYPE).in(searchRecordsRequest.getMaterialTypes()));
            }
        } else {
            if (!CollectionUtils.isEmpty(searchRecordsRequest.getOwningInstitutions())) {
                query.addCriteria(new Criteria(RecapConstants.OWNING_INSTITUTION).in(searchRecordsRequest.getOwningInstitutions()));
            }
            if (!CollectionUtils.isEmpty(searchRecordsRequest.getMaterialTypes())) {
                query.addCriteria(new Criteria(RecapConstants.LEADER_MATERIAL_TYPE).in(searchRecordsRequest.getMaterialTypes()));
            }
            if (!CollectionUtils.isEmpty(searchRecordsRequest.getCollectionGroupDesignations()) || !CollectionUtils.isEmpty(searchRecordsRequest.getAvailability())) {
                filterQuery.setJoin(Join.from(RecapConstants.HOLDINGS_ID).to(RecapConstants.HOLDINGS_ID));
            }
            if (!CollectionUtils.isEmpty(searchRecordsRequest.getCollectionGroupDesignations())) {
                filterQuery.addCriteria(new Criteria(RecapConstants.COLLECTION_GROUP_DESIGNATION).in(searchRecordsRequest.getCollectionGroupDesignations()));
            }
            if (!CollectionUtils.isEmpty(searchRecordsRequest.getAvailability())) {
                filterQuery.addCriteria(new Criteria(RecapConstants.AVAILABILITY).in(searchRecordsRequest.getAvailability()));
            }
        }

        query.addFilterQuery(filterQuery);
        query.addFilterQuery(new SimpleFilterQuery(new Criteria(RecapConstants.DOCTYPE).is(RecapConstants.BIB)));

        Page results = solrTemplate.queryForPage(query, BibItem.class);
        searchRecordsRequest.setPageNumber(results.getNumber());
        searchRecordsRequest.setTotalPageCount(results.getTotalPages());
        searchRecordsRequest.setTotalRecordsCount(results.getTotalElements());
        List<BibItem> bibItems = buildBibItems(results);
        return bibItems;
    }

    private Criteria getCriteriaForFieldName(SearchRecordsRequest searchRecordsRequest) {
        Criteria criteria = null;
        String fieldValue = StringUtils.isBlank(searchRecordsRequest.getFieldValue()) ? RecapConstants.ALL : searchRecordsRequest.getFieldValue();
        if (StringUtils.isBlank(searchRecordsRequest.getFieldName())) {
            criteria = new Criteria().is(fieldValue);
        } else if (RecapConstants.TITLE_STARTS_WITH.equals(searchRecordsRequest.getFieldName())) {
            criteria = new Criteria(RecapConstants.TITLE).startsWith(fieldValue);
        } else {
            criteria = new Criteria(searchRecordsRequest.getFieldName()).is(fieldValue);
        }
        return criteria;
    }

    private List<BibItem> buildBibItems(Page results) {
        List<BibItem> bibItems = results.getContent();
        Set<Integer> itemIds = new HashSet<>();
        if (!CollectionUtils.isEmpty(bibItems)) {
            for (BibItem bibItem : bibItems) {
                if (!CollectionUtils.isEmpty(bibItem.getBibItemIdList())) {
                        itemIds.addAll(bibItem.getBibItemIdList());
                }
            }
        }
        Map<Integer, Item> itemMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(itemIds)) {
            SimpleQuery query = new SimpleQuery(new Criteria(RecapConstants.ITEM_ID).in(itemIds));
            query.setRows(itemIds.size());
            ScoredPage<Item> itemsPage = solrTemplate.queryForPage(query, Item.class);
            if (itemsPage.getTotalElements() > itemIds.size()) {
                query.setRows(Math.toIntExact(itemsPage.getTotalElements()));
                itemsPage = solrTemplate.queryForPage(query, Item.class);
            }
            List<Item> items = itemsPage.getContent();
            if (!CollectionUtils.isEmpty(items)) {
                for (Item item : items) {
                    itemMap.put(item.getItemId(), item);
                }
            }
        }
        if (!CollectionUtils.isEmpty(bibItems)) {
            for (BibItem bibItem : bibItems) {
                if (!CollectionUtils.isEmpty(bibItem.getBibItemIdList())) {
                        for (Integer itemId : bibItem.getBibItemIdList()) {
                            bibItem.getItems().add(itemMap.get(itemId));
                        }
                }
            }
        }
        return bibItems;
    }
}
