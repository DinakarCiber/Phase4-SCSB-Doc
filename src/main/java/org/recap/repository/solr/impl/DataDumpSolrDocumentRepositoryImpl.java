package org.recap.repository.solr.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.recap.RecapConstants;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.model.search.resolver.BibValueResolver;
import org.recap.model.search.resolver.ItemValueResolver;
import org.recap.model.search.resolver.impl.Bib.BibIdValueResolver;
import org.recap.model.search.resolver.impl.Bib.DocTypeValueResolver;
import org.recap.model.search.resolver.impl.Bib.IdValueResolver;
import org.recap.model.search.resolver.impl.Bib.RootValueResolver;
import org.recap.model.search.resolver.impl.item.ItemIdValueResolver;
import org.recap.model.search.resolver.impl.item.ItemRootValueResolver;
import org.recap.model.solr.BibItem;
import org.recap.model.solr.Item;
import org.recap.repository.solr.main.CustomDocumentRepository;
import org.recap.util.SolrQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by angelind on 26/10/16.
 */
@Repository
public class DataDumpSolrDocumentRepositoryImpl implements CustomDocumentRepository {

    Logger log = Logger.getLogger(DataDumpSolrDocumentRepositoryImpl.class);

    @Resource
    private SolrTemplate solrTemplate;

    @Autowired
    private SolrQueryBuilder solrQueryBuilder;

    List<BibValueResolver> bibValueResolvers;
    List<ItemValueResolver> itemValueResolvers;

    @Override
    public Map<String, Object> search(SearchRecordsRequest searchRecordsRequest) {
        List<BibItem> bibItems = new ArrayList<>();
        Map<String, Object> response = new HashMap<>();
        try {
            searchRecordsRequest.setShowTotalCount(true);
            searchRecordsRequest.setFieldName(RecapConstants.ALL_FIELDS);
            bibItems = searchByBib(searchRecordsRequest);
            response.put(RecapConstants.SEARCH_SUCCESS_RESPONSE, bibItems);
        } catch (SolrServerException e) {
            log.error(e.getMessage());
            response.put(RecapConstants.SEARCH_ERROR_RESPONSE, e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
            response.put(RecapConstants.SEARCH_ERROR_RESPONSE, e.getMessage());
        }
        return response;
    }

    private List<BibItem> searchByBib(SearchRecordsRequest searchRecordsRequest) throws SolrServerException, IOException {
        List<BibItem> bibItems = new ArrayList<>();
        SolrQuery queryForParentAndChildCriteria = solrQueryBuilder.getQueryForParentAndChildCriteria(searchRecordsRequest);
        queryForParentAndChildCriteria.setStart(searchRecordsRequest.getPageNumber() * searchRecordsRequest.getPageSize());
        queryForParentAndChildCriteria.setRows(searchRecordsRequest.getPageSize());
        queryForParentAndChildCriteria.setSort(RecapConstants.TITLE_SORT, SolrQuery.ORDER.asc);
        QueryResponse queryResponse = solrTemplate.getSolrClient().query(queryForParentAndChildCriteria);
        SolrDocumentList bibSolrDocumentList = queryResponse.getResults();
        if(CollectionUtils.isNotEmpty(bibSolrDocumentList)) {
            long numFound = bibSolrDocumentList.getNumFound();
            String totalBibCount = String.valueOf(numFound);
            searchRecordsRequest.setTotalBibRecordsCount(totalBibCount);
            searchRecordsRequest.setTotalRecordsCount(totalBibCount);
            int totalPagesCount = (int) Math.ceil((double) numFound / (double) searchRecordsRequest.getPageSize());
            searchRecordsRequest.setTotalPageCount(totalPagesCount);
            for (Iterator<SolrDocument> iterator = bibSolrDocumentList.iterator(); iterator.hasNext(); ) {
                SolrDocument bibSolrDocument = iterator.next();
                BibItem bibItem = new BibItem();
                populateBib(bibSolrDocument, bibItem);
                populateItemInfo(bibItem, searchRecordsRequest);
                bibItems.add(bibItem);
            }
        }
        return bibItems;
    }

    public void populateItemInfo(BibItem bibItem, SearchRecordsRequest searchRecordsRequest) {
        String queryStringForMatchParentReturnChild = solrQueryBuilder.getQueryStringForMatchParentReturnChild(searchRecordsRequest);
        SolrQuery solrQueryForItem = solrQueryBuilder.getSolrQueryForBibItem("_root_:" + bibItem.getRoot() + " AND " + RecapConstants.DOCTYPE + ":" + RecapConstants.ITEM + " AND " + queryStringForMatchParentReturnChild);
        QueryResponse queryResponse = null;
        try {
            queryResponse = solrTemplate.getSolrClient().query(solrQueryForItem);
            SolrDocumentList solrDocuments = queryResponse.getResults();
            if(solrDocuments.getNumFound() > 10 ) {
                solrQueryForItem.setRows((int) solrDocuments.getNumFound());
                queryResponse = solrTemplate.getSolrClient().query(solrQueryForItem);
                solrDocuments = queryResponse.getResults();
            }
            for (Iterator<SolrDocument> iterator = solrDocuments.iterator(); iterator.hasNext(); ) {
                SolrDocument solrDocument = iterator.next();
                Item item = getItem(solrDocument);
                bibItem.addItem(item);
            }
        } catch (SolrServerException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void populateBib(SolrDocument solrDocument, BibItem bibItem) {
        Collection<String> fieldNames = solrDocument.getFieldNames();
        for (Iterator<String> stringIterator = fieldNames.iterator(); stringIterator.hasNext(); ) {
            String fieldName = stringIterator.next();
            Object fieldValue = solrDocument.getFieldValue(fieldName);
            for (Iterator<BibValueResolver> valueResolverIterator = getBibValueResolversForDataDump().iterator(); valueResolverIterator.hasNext(); ) {
                BibValueResolver valueResolver = valueResolverIterator.next();
                if (valueResolver.isInterested(fieldName)) {
                    valueResolver.setValue(bibItem, fieldValue);
                }
            }
        }
    }

    private Item getItem(SolrDocument itemSolrDocument) {
        Item item = new Item();
        Collection<String> fieldNames = itemSolrDocument.getFieldNames();
        for (Iterator<String> iterator = fieldNames.iterator(); iterator.hasNext(); ) {
            String fieldName = iterator.next();
            Object fieldValue = itemSolrDocument.getFieldValue(fieldName);
            for (Iterator<ItemValueResolver> itemValueResolverIterator = getItemValueResolversForDataDump().iterator(); itemValueResolverIterator.hasNext(); ) {
                ItemValueResolver itemValueResolver = itemValueResolverIterator.next();
                if (itemValueResolver.isInterested(fieldName)) {
                    itemValueResolver.setValue(item, fieldValue);
                }
            }
        }
        return item;
    }

    @Override
    public Integer getPageNumberOnPageSizeChange(SearchRecordsRequest searchRecordsRequest) {
        return null;
    }

    public List<BibValueResolver> getBibValueResolversForDataDump() {
        if (null == bibValueResolvers) {
            bibValueResolvers = new ArrayList<>();
            bibValueResolvers.add(new RootValueResolver());
            bibValueResolvers.add(new BibIdValueResolver());
            bibValueResolvers.add(new DocTypeValueResolver());
            bibValueResolvers.add(new IdValueResolver());
        }
        return bibValueResolvers;
    }

    public List<ItemValueResolver> getItemValueResolversForDataDump() {
        if (null == itemValueResolvers) {
            itemValueResolvers = new ArrayList<>();
            itemValueResolvers.add(new org.recap.model.search.resolver.impl.item.DocTypeValueResolver());
            itemValueResolvers.add(new ItemRootValueResolver());
            itemValueResolvers.add(new ItemIdValueResolver());
            itemValueResolvers.add(new org.recap.model.search.resolver.impl.item.IdValueResolver());
        }
        return itemValueResolvers;
    }
}
