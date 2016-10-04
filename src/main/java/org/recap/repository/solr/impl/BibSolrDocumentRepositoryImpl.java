package org.recap.repository.solr.impl;

import org.apache.commons.lang3.StringUtils;
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
import org.recap.model.search.resolver.impl.Bib.*;
import org.recap.model.search.resolver.impl.Bib.DocTypeValueResolver;
import org.recap.model.search.resolver.impl.Bib.IdValueResolver;
import org.recap.model.search.resolver.impl.item.*;
import org.recap.model.solr.BibItem;
import org.recap.model.solr.Item;
import org.recap.repository.solr.main.CustomDocumentRepository;
import org.recap.util.SolrQueryBuilder;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.NumberFormat;
import java.text.StringCharacterIterator;
import java.util.*;

/**
 * Created by rajeshbabuk on 8/7/16.
 */
@Repository
public class BibSolrDocumentRepositoryImpl implements CustomDocumentRepository {

    Logger log = Logger.getLogger(BibSolrDocumentRepositoryImpl.class);

    @Resource
    private SolrTemplate solrTemplate;

    List<BibValueResolver> bibValueResolvers;
    List<ItemValueResolver> itemValueResolvers;
    private SolrQueryBuilder solrQueryBuilder;

    public SolrQueryBuilder getSolrQueryBuilder() {
        if (null == solrQueryBuilder) {
            solrQueryBuilder = new SolrQueryBuilder();
        }
        return solrQueryBuilder;
    }

    public void setSolrQueryBuilder(SolrQueryBuilder solrQueryBuilder) {
        this.solrQueryBuilder = solrQueryBuilder;
    }

    @Override
    public List<BibItem> search(SearchRecordsRequest searchRecordsRequest) {
        List<BibItem> bibItems = new ArrayList<>();
        try {
            if (isItemField(searchRecordsRequest)) {
                bibItems = searchByItem(searchRecordsRequest);
            } else {
                bibItems = searchByBib(searchRecordsRequest);
            }
        } catch (SolrServerException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return bibItems;
    }

    private List<BibItem> searchByItem(SearchRecordsRequest searchRecordsRequest) throws SolrServerException, IOException {
        List<BibItem> bibItems = new ArrayList<>();
        SolrQuery solrQuery = getSolrQueryBuilder().getItemSolrQueryForCriteria(null, searchRecordsRequest);
        if (null != solrQuery) {
            QueryResponse queryResponse = solrTemplate.getSolrClient().query(solrQuery);
            SolrDocumentList itemSolrDocuments = queryResponse.getResults();
            String totalItemCount = NumberFormat.getNumberInstance().format(itemSolrDocuments.getNumFound());
            searchRecordsRequest.setTotalItemRecordsCount(totalItemCount);
            for (Iterator<SolrDocument> iterator = itemSolrDocuments.iterator(); iterator.hasNext(); ) {
                SolrDocument solrDocument = iterator.next();
                Item item = getItem(solrDocument);
                bibItems.addAll(getBibItems(item, searchRecordsRequest));
            }
        }

        return bibItems;
    }

    private List<BibItem> getBibItems(Item item, SearchRecordsRequest searchRecordsRequest) {
        List<BibItem> bibItems = new ArrayList<>();
        String queryStringForBibCriteria = solrQueryBuilder.getQueryStringForBibCriteria(searchRecordsRequest);
        if (StringUtils.isNotBlank(queryStringForBibCriteria)) {
            SolrQuery itemSolrQuery = new SolrQuery("_root_:" + item.getRoot() + " AND " + queryStringForBibCriteria);
            QueryResponse queryResponse = null;
            try {
                queryResponse = solrTemplate.getSolrClient().query(itemSolrQuery);
                SolrDocumentList solrDocuments = queryResponse.getResults();
                for (Iterator<SolrDocument> iterator = solrDocuments.iterator(); iterator.hasNext(); ) {
                    SolrDocument solrDocument = iterator.next();
                    BibItem bibItem = new BibItem();
                    String docType = (String) solrDocument.getFieldValue("DocType");
                    if (docType.equalsIgnoreCase("Bib")) {
                        populateBibItem(solrDocument, bibItem);
                        bibItem.setItems(Arrays.asList(item));
                        bibItems.add(bibItem);
                    }
                }
            } catch (SolrServerException e) {
                log.error(e.getMessage());
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        } else {
            //TODO: If Item criteria is selected and no bib facets selected
        }
        return bibItems;
    }

    private List<BibItem> searchByBib(SearchRecordsRequest searchRecordsRequest) throws SolrServerException, IOException {
        List<BibItem> bibItems = new ArrayList<>();
        if (null != getItemCountForItemCriteria(searchRecordsRequest) && 0 != getItemCountForItemCriteria(searchRecordsRequest)) {
            SolrQuery solrQuery = getSolrQueryBuilder().getSolrQueryForCriteria(searchRecordsRequest);
            solrQuery.setSort(RecapConstants.TITLE_SORT, SolrQuery.ORDER.asc);
            if (null != solrQuery) {
                bibItems = getBibItemsForBib(searchRecordsRequest, solrQuery);
            }
        }
        return bibItems;
    }

    private List<BibItem> getBibItemsForBib(SearchRecordsRequest searchRecordsRequest, SolrQuery solrQuery) throws SolrServerException, IOException {
        List<BibItem> bibItems = new ArrayList<>();
        QueryResponse queryResponse = solrTemplate.getSolrClient().query(solrQuery);
        SolrDocumentList bibSolrDocuments = queryResponse.getResults();
        setCounts(searchRecordsRequest, bibSolrDocuments);
        resolveBibItems(searchRecordsRequest, bibItems, bibSolrDocuments);

        if (bibItems.size() == searchRecordsRequest.getPageSize()) {
            return bibItems;
        } else {
            solrQuery.setStart(searchRecordsRequest.getPageNumber() + 1 * searchRecordsRequest.getPageSize());
            return getBibItemsForBib(bibItems, searchRecordsRequest, solrQuery);
        }
    }


    private List<BibItem> getBibItemsForBib(List<BibItem> bibItems, SearchRecordsRequest searchRecordsRequest, SolrQuery solrQuery) throws SolrServerException, IOException {
        QueryResponse queryResponse = solrTemplate.getSolrClient().query(solrQuery);
        SolrDocumentList bibSolrDocuments = queryResponse.getResults();
        resolveBibItems(searchRecordsRequest, bibItems, bibSolrDocuments);

        if (bibItems.size() > 0 && bibItems.size() <= searchRecordsRequest.getPageSize()) {
            return bibItems;
        } else {
            int pageNum = searchRecordsRequest.getPageNumber() + 1;
            searchRecordsRequest.setPageNumber(pageNum);
            solrQuery.setStart(pageNum * searchRecordsRequest.getPageSize());
            getBibItemsForBib(bibItems, searchRecordsRequest, solrQuery);
        }

        return bibItems;
    }


    private void resolveBibItems(SearchRecordsRequest searchRecordsRequest, List<BibItem> bibItems, SolrDocumentList bibSolrDocuments) {
        for (Iterator<SolrDocument> iterator = bibSolrDocuments.iterator(); iterator.hasNext(); ) {
            if (bibItems.size() == searchRecordsRequest.getPageSize()) {
                break;
            }
            SolrDocument solrDocument = iterator.next();
            BibItem bibItem = new BibItem();
            populateBibItem(solrDocument, bibItem);
            populateItemInfo(bibItem, searchRecordsRequest);
            if (!CollectionUtils.isEmpty(bibItem.getItems())) {
                bibItems.add(bibItem);
            }
        }
    }

    private boolean isItemField(SearchRecordsRequest searchRecordsRequest) {
        if (StringUtils.isNotBlank(searchRecordsRequest.getFieldName())
                && (searchRecordsRequest.getFieldName().equalsIgnoreCase(RecapConstants.BARCODE) || searchRecordsRequest.getFieldName().equalsIgnoreCase(RecapConstants.CALL_NUMBER))) {
            return true;
        }
        return false;
    }

    private void setCounts(SearchRecordsRequest searchRecordsRequest, SolrDocumentList bibSolrDocuments) {
        long numFound = bibSolrDocuments.getNumFound();
        String totalBibCount = NumberFormat.getNumberInstance().format(numFound);
        //TODO: Need to populate item counts as well.
        searchRecordsRequest.setTotalBibRecordsCount(totalBibCount);
        int totalPagesCount = (int) Math.ceil((double) numFound / (double) searchRecordsRequest.getPageSize());
        searchRecordsRequest.setTotalPageCount(totalPagesCount);
    }

    private void populateItemInfo(BibItem bibItem, SearchRecordsRequest searchRecordsRequest) {
        SolrQuery itemSolrQueryForCriteria = getSolrQueryBuilder().getItemSolrQueryForCriteria("_root_:" + bibItem.getRoot(), searchRecordsRequest);
        QueryResponse queryResponse = null;
        try {
            queryResponse = solrTemplate.getSolrClient().query(itemSolrQueryForCriteria);
            SolrDocumentList solrDocuments = queryResponse.getResults();
            for (Iterator<SolrDocument> iterator = solrDocuments.iterator(); iterator.hasNext(); ) {
                SolrDocument solrDocument = iterator.next();
                String docType = (String) solrDocument.getFieldValue("DocType");
                if (docType.equalsIgnoreCase("Item")) {
                    Item item = getItem(solrDocument);
                    bibItem.addItem(item);
                }
            }
        } catch (SolrServerException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private Item getItem(SolrDocument itemSolrDocument) {
        Item item = new Item();

        Collection<String> fieldNames = itemSolrDocument.getFieldNames();
        List<ItemValueResolver> itemValueResolvers = getItemValueResolvers();
        for (Iterator<String> iterator = fieldNames.iterator(); iterator.hasNext(); ) {
            String fieldName = iterator.next();
            Object fieldValue = itemSolrDocument.getFieldValue(fieldName);
            for (Iterator<ItemValueResolver> itemValueResolverIterator = itemValueResolvers.iterator(); itemValueResolverIterator.hasNext(); ) {
                ItemValueResolver itemValueResolver = itemValueResolverIterator.next();
                if (itemValueResolver.isInterested(fieldName)) {
                    itemValueResolver.setValue(item, fieldValue);
                }
            }
        }

        return item;

    }

    private void populateBibItem(SolrDocument solrDocument, BibItem bibItem) {
        Collection<String> fieldNames = solrDocument.getFieldNames();
        for (Iterator<String> stringIterator = fieldNames.iterator(); stringIterator.hasNext(); ) {
            String fieldName = stringIterator.next();
            Object fieldValue = solrDocument.getFieldValue(fieldName);
            for (Iterator<BibValueResolver> valueResolverIterator = getBibValueResolvers().iterator(); valueResolverIterator.hasNext(); ) {
                BibValueResolver valueResolver = valueResolverIterator.next();
                if (valueResolver.isInterested(fieldName)) {
                    valueResolver.setValue(bibItem, fieldValue);
                }
            }
        }
    }

    public Criteria getCriteriaForFieldName(SearchRecordsRequest searchRecordsRequest) {
        Criteria criteria = null;
        String fieldName = searchRecordsRequest.getFieldName();
        String fieldValue = getModifiedText(searchRecordsRequest.getFieldValue().trim());

        if (StringUtils.isBlank(fieldName) && StringUtils.isBlank(fieldValue)) {
            criteria = new Criteria().expression(RecapConstants.ALL);
        } else if (StringUtils.isBlank(fieldName)) {
            fieldValue = "(" + StringUtils.join(fieldValue.split("\\s+"), " " + RecapConstants.AND + " ") + ")";
            criteria = new Criteria().expression(fieldValue)
                    .or(RecapConstants.TITLE_SEARCH).expression(fieldValue)
                    .or(RecapConstants.AUTHOR_SEARCH).expression(fieldValue)
                    .or(RecapConstants.PUBLISHER).expression(fieldValue);
        } else if (StringUtils.isBlank(fieldValue)) {
            if (RecapConstants.TITLE_STARTS_WITH.equals(fieldName)) {
                fieldName = RecapConstants.TITLE_SEARCH;
            }
            criteria = new Criteria(fieldName).expression(RecapConstants.ALL);
        } else {
            if (RecapConstants.TITLE_STARTS_WITH.equals(fieldName)) {
                String[] splitedTitle = fieldValue.split(" ");
                criteria = new Criteria(RecapConstants.TITLE_STARTS_WITH).startsWith(splitedTitle[0]);
            } else {
                String[] splitValues = fieldValue.split("\\s+");
                for (String splitValue : splitValues) {
                    if (null == criteria) {
                        criteria = new Criteria().and(fieldName).expression(splitValue);
                    } else {
                        criteria.and(fieldName).expression(splitValue);
                    }
                }
            }
        }
        return criteria;
    }

    private Long getItemCountForItemCriteria(SearchRecordsRequest searchRecordsRequest) {
        SolrQuery itemSolrQueryForCriteria = new SolrQuery(getSolrQueryBuilder().getQueryStringForItemCriteria(searchRecordsRequest));
        itemSolrQueryForCriteria.setRows(1);
        QueryResponse queryResponse = null;
        try {
            queryResponse = solrTemplate.getSolrClient().query(itemSolrQueryForCriteria);
            SolrDocumentList solrDocuments = queryResponse.getResults();
            long totalItemCount = solrDocuments.getNumFound();
            return Long.valueOf(totalItemCount);
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getModifiedText(String searchText) {
        StringBuffer modifiedText = new StringBuffer();
        StringCharacterIterator stringCharacterIterator = new StringCharacterIterator(searchText);
        char character = stringCharacterIterator.current();
        while (character != CharacterIterator.DONE) {
            if (character == '\\') {
                modifiedText.append("\\\\");
            } else if (character == '?') {
                modifiedText.append("\\?");
            } else if (character == '*') {
                modifiedText.append("\\*");
            } else if (character == '+') {
                modifiedText.append("\\+");
            } else if (character == ':') {
                modifiedText.append("\\:");
            } else if (character == '{') {
                modifiedText.append("\\{");
            } else if (character == '}') {
                modifiedText.append("\\}");
            } else if (character == '[') {
                modifiedText.append("\\[");
            } else if (character == ']') {
                modifiedText.append("\\]");
            } else if (character == '(') {
                modifiedText.append("\\(");
            } else if (character == ')') {
                modifiedText.append("\\)");
            } else if (character == '^') {
                modifiedText.append("\\^");
            } else if (character == '~') {
                modifiedText.append("\\~");
            } else if (character == '-') {
                modifiedText.append("\\-");
            } else if (character == '!') {
                modifiedText.append("\\!");
            } else if (character == '\'') {
                modifiedText.append("\\'");
            } else if (character == '@') {
                modifiedText.append("\\@");
            } else if (character == '#') {
                modifiedText.append("\\#");
            } else if (character == '$') {
                modifiedText.append("\\$");
            } else if (character == '%') {
                modifiedText.append("\\%");
            } else if (character == '/') {
                modifiedText.append("\\/");
            } else if (character == '"') {
                modifiedText.append("\\\"");
            } else if (character == '.') {
                modifiedText.append("\\.");
            } else {
                modifiedText.append(character);
            }
            character = stringCharacterIterator.next();
        }
        return modifiedText.toString();
    }

    public List<BibValueResolver> getBibValueResolvers() {
        if (null == bibValueResolvers) {
            bibValueResolvers = new ArrayList<>();
            bibValueResolvers.add(new RootValueResolver());
            bibValueResolvers.add(new AuthorDisplayValueResolver());
            bibValueResolvers.add(new AuthorSearchValueResolver());
            bibValueResolvers.add(new BibIdValueResolver());
            bibValueResolvers.add(new DocTypeValueResolver());
            bibValueResolvers.add(new IdValueResolver());
            bibValueResolvers.add(new ImprintValueResolver());
            bibValueResolvers.add(new ISBNValueResolver());
            bibValueResolvers.add(new ISSNValueResolver());
            bibValueResolvers.add(new LCCNValueResolver());
            bibValueResolvers.add(new LeaderMaterialTypeValueResolver());
            bibValueResolvers.add(new MaterialTypeValueResolver());
            bibValueResolvers.add(new NotesValueResolver());
            bibValueResolvers.add(new OCLCValueResolver());
            bibValueResolvers.add(new OwningInstitutionBibIdValueResolver());
            bibValueResolvers.add(new OwningInstitutionValueResolver());
            bibValueResolvers.add(new PublicationDateValueResolver());
            bibValueResolvers.add(new PublicationPlaceValueResolver());
            bibValueResolvers.add(new PublisherValueResolver());
            bibValueResolvers.add(new SubjectValueResolver());
            bibValueResolvers.add(new TitleDisplayValueResolver());
            bibValueResolvers.add(new TitleSearchValueResolver());
            bibValueResolvers.add(new TitleSortValueResolver());
        }
        return bibValueResolvers;
    }

    public List<ItemValueResolver> getItemValueResolvers() {
        if (null == itemValueResolvers) {
            itemValueResolvers = new ArrayList<>();
            itemValueResolvers.add(new AvailabilityValueResolver());
            itemValueResolvers.add(new BarcodeValueResolver());
            itemValueResolvers.add(new CallNumberValueResolver());
            itemValueResolvers.add(new CollectionGroupDesignationValueResolver());
            itemValueResolvers.add(new CustomerCodeValueResolver());
            itemValueResolvers.add(new org.recap.model.search.resolver.impl.item.DocTypeValueResolver());
            itemValueResolvers.add(new ItemOwningInstitutionValueResolver());
            itemValueResolvers.add(new UseRestrictionValueResolver());
            itemValueResolvers.add(new VolumePartYearValueResolver());
            itemValueResolvers.add(new ItemRootValueResolver());
            itemValueResolvers.add(new ItemIdValueResolver());
            itemValueResolvers.add(new org.recap.model.search.resolver.impl.item.IdValueResolver());
        }
        return itemValueResolvers;
    }
}
