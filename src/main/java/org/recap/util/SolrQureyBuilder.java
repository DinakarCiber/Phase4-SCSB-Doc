package org.recap.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.recap.RecapConstants;
import org.recap.model.search.SearchRecordsRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peris on 9/30/16.
 */
public class SolrQureyBuilder {

    String all = "*:*";

    String and = " AND ";

    String coreFilterQuery = "{!parent which=\"DocType:Bib\"}";

    public SolrQuery getQuryForBibSpecificFieldSpecificValue(SearchRecordsRequest searchRecordsRequest) {
        StringBuilder strBuilder = new StringBuilder();
        String queryStringForBibCriteria = getQueryStringForBibCriteria(searchRecordsRequest);
        strBuilder.append(searchRecordsRequest.getFieldName())
                .append(":")
                .append(searchRecordsRequest.getFieldValue())
                .append(and).append(queryStringForBibCriteria);

        return getSolrQuery(searchRecordsRequest, strBuilder.toString() );
    }

    public SolrQuery getQuryForItemSpecificFieldSpecificValue(SearchRecordsRequest searchRecordsRequest) {
        StringBuilder strBuilder = new StringBuilder();
        String queryStringForBibCriteria = getQueryStringForBibCriteria(searchRecordsRequest);
        String queryStringForItemCriteria = getQueryStringForItemCriteria(searchRecordsRequest);
        strBuilder
                .append(coreFilterQuery)
                .append(searchRecordsRequest.getFieldName())
                .append(":")
                .append(searchRecordsRequest.getFieldValue());
                if(StringUtils.isNotBlank(queryStringForItemCriteria)){
                    strBuilder.append(and).append(queryStringForItemCriteria);
                }

        SolrQuery solrQurey = new SolrQuery(strBuilder.toString());
        solrQurey.addFilterQuery(queryStringForBibCriteria);
        return solrQurey;
    }

    public SolrQuery getQuryForAllFieldsSpecificValue(SearchRecordsRequest searchRecordsRequest) {
        StringBuilder strBuilder = new StringBuilder();
        String queryStringForBibCriteria = getQueryStringForBibCriteria(searchRecordsRequest);
        strBuilder.append(searchRecordsRequest.getFieldValue()).append(and).append(queryStringForBibCriteria);

        return getSolrQuery(searchRecordsRequest, strBuilder.toString() );
    }

    public SolrQuery getQuryForAllFieldsNoValue(SearchRecordsRequest searchRecordsRequest) {
        StringBuilder strBuilder = new StringBuilder();
        String queryStringForBibCriteria = getQueryStringForBibCriteria(searchRecordsRequest);
        strBuilder.append(all).append(and).append(queryStringForBibCriteria);

        return getSolrQuery(searchRecordsRequest, strBuilder.toString() );
    }

    private SolrQuery getSolrQuery(SearchRecordsRequest searchRecordsRequest, String queryString) {
        SolrQuery solrQuery = new SolrQuery(queryString);
        solrQuery.addFilterQuery(coreFilterQuery + getQueryStringForItemCriteria(searchRecordsRequest));

        return solrQuery;
    }

    private String getQueryStringForItemCriteria(SearchRecordsRequest searchRecordsRequest) {
        StringBuilder stringBuilder = new StringBuilder();

        List<String> availability = searchRecordsRequest.getAvailability();
        List<String> collectionGroupDesignations = searchRecordsRequest.getCollectionGroupDesignations();
        List<String> useRestrictions = searchRecordsRequest.getUseRestrictions();

        if (CollectionUtils.isNotEmpty(availability)) {
            stringBuilder.append(buildQueryForCriteriaField(RecapConstants.AVAILABILITY, availability));
        }
        if (StringUtils.isNotBlank(stringBuilder.toString())) {
            stringBuilder.append(and);
        }
        if (CollectionUtils.isNotEmpty(collectionGroupDesignations)) {
            stringBuilder.append(buildQueryForCriteriaField(RecapConstants.COLLECTION_GROUP_DESIGNATION, collectionGroupDesignations));
        }
        if (StringUtils.isNotBlank(stringBuilder.toString())) {
            stringBuilder.append(and);
        }
        if (CollectionUtils.isNotEmpty(useRestrictions)) {
            stringBuilder.append(buildQueryForCriteriaField(RecapConstants.USE_RESTRICTION, useRestrictions));
        }

        return stringBuilder.toString();
    }

    private String getQueryStringForBibCriteria(SearchRecordsRequest searchRecordsRequest) {
        StringBuilder strinBuilder = new StringBuilder();
        List<String> owningInstitutions = searchRecordsRequest.getOwningInstitutions();
        if (CollectionUtils.isNotEmpty(owningInstitutions)) {
            strinBuilder.append(buildQueryForCriteriaField(RecapConstants.BIB_OWNING_INSTITUTION, owningInstitutions));
        }
        if (!StringUtils.isNotBlank(strinBuilder.toString())) {
            strinBuilder.append(and);
        }
        List<String> materialTypes = searchRecordsRequest.getMaterialTypes();
        if (CollectionUtils.isNotEmpty(materialTypes)) {
            strinBuilder.append(buildQueryForCriteriaField(RecapConstants.LEADER_MATERIAL_TYPE, materialTypes));
        }
        return strinBuilder.toString();
    }


    private String buildQueryForCriteriaField(String fieldName, List<String> values) {
        List<String> modifiedValues = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(values)) {
            for (String value : values) {
                modifiedValues.add("\"" + value + "\"");
            }
        }
        return fieldName + ":" + "(" + StringUtils.join(modifiedValues, " ") + ")";
    }

    public SolrQuery getSolrQueryForCriteria(SearchRecordsRequest searchRecordsRequest) {
        if(StringUtils.isBlank(searchRecordsRequest.getFieldName()) && StringUtils.isBlank(searchRecordsRequest.getFieldValue())){
            return getQuryForAllFieldsNoValue(searchRecordsRequest);
        } else if (StringUtils.isBlank(searchRecordsRequest.getFieldName()) && StringUtils.isNotBlank(searchRecordsRequest.getFieldValue())){
            return getQuryForAllFieldsSpecificValue(searchRecordsRequest);
        } else if (StringUtils.isNotBlank(searchRecordsRequest.getFieldName())
                && StringUtils.isNotBlank(searchRecordsRequest.getFieldValue())
                && (!searchRecordsRequest.getFieldName().equalsIgnoreCase(RecapConstants.BARCODE) && !searchRecordsRequest.getFieldName().equalsIgnoreCase(RecapConstants.CALL_NUMBER))){
            return getQuryForBibSpecificFieldSpecificValue(searchRecordsRequest);
        }else if (StringUtils.isNotBlank(searchRecordsRequest.getFieldName())
                && StringUtils.isNotBlank(searchRecordsRequest.getFieldValue())
                && (searchRecordsRequest.getFieldName().equalsIgnoreCase(RecapConstants.BARCODE) || searchRecordsRequest.getFieldName().equalsIgnoreCase(RecapConstants.CALL_NUMBER))){
            return getQuryForItemSpecificFieldSpecificValue(searchRecordsRequest);
        }
        return null;
    }
}
