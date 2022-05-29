package org.recap.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.jpa.MatchingMatchPointsEntity;
import org.recap.model.search.SearchRecordsRequest;
import org.springframework.stereotype.Component;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by peris on 9/30/16.
 */
@Component
public class SolrQueryBuilder {

    private String and = " AND ";
    private String or = " OR ";
    private String to = " TO ";
    private String coreParentFilterQuery = "{!parent which=\"ContentType:parent\"}";
    private String coreChildFilterQuery = "{!child of=\"ContentType:parent\"}";
    private String joinQueryOnMatchingIdentifier = "{!join from=MatchingIdentifier to=MatchingIdentifier}";

    /**
     * Gets query string for item criteria for parent.
     *
     * @param searchRecordsRequest the search records request
     * @return the query string for item criteria for parent
     */
    public String getQueryStringForItemCriteriaForParent(SearchRecordsRequest searchRecordsRequest) {
        StringBuilder stringBuilder = new StringBuilder();

        List<String> availability = searchRecordsRequest.getAvailability();
        List<String> collectionGroupDesignations = searchRecordsRequest.getCollectionGroupDesignations();
        List<String> useRestrictions = searchRecordsRequest.getUseRestrictions();
        List<String> imsDepositoryCodes = searchRecordsRequest.getImsDepositoryCodes();

        if (CollectionUtils.isNotEmpty(availability)) {
            stringBuilder.append(buildQueryForFilterGivenChild(ScsbCommonConstants.AVAILABILITY, availability));
        }
        appendToQueryByFieldType(stringBuilder, collectionGroupDesignations, ScsbCommonConstants.COLLECTION_GROUP_DESIGNATION);
        appendToQueryByFieldType(stringBuilder, useRestrictions, ScsbCommonConstants.USE_RESTRICTION);
        appendToQueryByFieldType(stringBuilder, imsDepositoryCodes, ScsbConstants.IMS_LOCATION_CODE);
        stringBuilder
                .append(and)
                .append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(searchRecordsRequest.isDeleted())
                .append(and)
                .append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(searchRecordsRequest.getCatalogingStatus());

        return coreParentFilterQuery + "(" + stringBuilder.toString() + ")";
    }

    private void appendToQueryByFieldType(StringBuilder stringBuilder, List<String> values, String fieldName) {
        if (StringUtils.isNotBlank(stringBuilder.toString()) && CollectionUtils.isNotEmpty(values)) {
            stringBuilder.append(and).append(buildQueryForFilterGivenChild(fieldName, values));
        } else if (CollectionUtils.isNotEmpty(values)) {
            stringBuilder.append(buildQueryForFilterGivenChild(fieldName, values));
        }
    }

    private String buildQueryForFilterGivenChild(String fieldName, List<String> values) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Iterator<String> iterator = values.iterator(); iterator.hasNext(); ) {
            String value = iterator.next();
            stringBuilder.append(fieldName).append(":").append(value);
            if (iterator.hasNext()) {
                stringBuilder.append(or);
            }
        }
        return "(" + stringBuilder.toString() + ")";
    }

    /**
     * Gets query string for parent criteria for child.
     *
     * @param searchRecordsRequest the search records request
     * @return the query string for parent criteria for child
     */
    public String getQueryStringForParentCriteriaForChild(SearchRecordsRequest searchRecordsRequest) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(buildQueryForBibFacetCriteria(searchRecordsRequest));
        stringBuilder
                .append(and).append(coreChildFilterQuery)
                .append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(searchRecordsRequest.isDeleted())
                .append(and).append(coreChildFilterQuery)
                .append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(searchRecordsRequest.getCatalogingStatus());
        return stringBuilder.toString();
    }

    private String buildQueryForBibFacetCriteria(SearchRecordsRequest searchRecordsRequest) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> owningInstitutions = searchRecordsRequest.getOwningInstitutions();
        List<String> materialTypes = searchRecordsRequest.getMaterialTypes();
        List<String> titleMatch = searchRecordsRequest.getTitleMatch();

        if (CollectionUtils.isNotEmpty(owningInstitutions)) {
            stringBuilder.append(buildQueryForParentGivenChild(ScsbCommonConstants.BIB_OWNING_INSTITUTION, owningInstitutions, coreChildFilterQuery));
        }
        if (StringUtils.isNotBlank(stringBuilder.toString()) && CollectionUtils.isNotEmpty(materialTypes)) {
            stringBuilder.append(and).append(buildQueryForParentGivenChild(ScsbCommonConstants.LEADER_MATERIAL_TYPE, materialTypes, coreChildFilterQuery));
        } else if (CollectionUtils.isNotEmpty(materialTypes)) {
            stringBuilder.append(buildQueryForParentGivenChild(ScsbCommonConstants.LEADER_MATERIAL_TYPE, materialTypes, coreChildFilterQuery));
        }
        if (titleMatch.size() == 1) {
            StringBuilder stringBuilderTemp = new StringBuilder();
            stringBuilderTemp.append(coreChildFilterQuery).append(ScsbConstants.MATCHING_IDENTIFIER).append(":").append("*");
            if (ScsbConstants.TITLE_MATCHED.equalsIgnoreCase(titleMatch.get(0))) {
                stringBuilder.append(or).append("(").append(stringBuilderTemp).append(")");
            } else {
                stringBuilder.append(or).append("-").append("(").append(stringBuilderTemp).append(")");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Gets query string for match child return parent.
     *
     * @param searchRecordsRequest the search records request
     * @return the query string for match child return parent
     */
    public String getQueryStringForMatchChildReturnParent(SearchRecordsRequest searchRecordsRequest) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> owningInstitutions = searchRecordsRequest.getOwningInstitutions();
        if (CollectionUtils.isNotEmpty(owningInstitutions)) {
            stringBuilder.append(buildQueryForFilterGivenChild(ScsbCommonConstants.BIB_OWNING_INSTITUTION, owningInstitutions));
        }

        List<String> materialTypes = searchRecordsRequest.getMaterialTypes();
        List<String> titleMatch = searchRecordsRequest.getTitleMatch();
        buildQueryByFieldType(stringBuilder, materialTypes, ScsbCommonConstants.LEADER_MATERIAL_TYPE);
        if (titleMatch.size() == 1) {
            StringBuilder stringBuilderTemp = new StringBuilder();
            stringBuilderTemp.append(ScsbConstants.MATCHING_IDENTIFIER).append(":").append("*");
            if (ScsbConstants.TITLE_MATCHED.equalsIgnoreCase(titleMatch.get(0))) {
                stringBuilder.append(and).append("(").append(stringBuilderTemp).append(")");
            } else {
                stringBuilder.append(and).append("-").append("(").append(stringBuilderTemp).append(")");
            }
        }
        return stringBuilder.toString();
    }

    private String buildQueryByFieldType(StringBuilder stringBuilder, List<String> materialTypes, String leaderMaterialType) {
        if (StringUtils.isNotBlank(stringBuilder.toString()) && CollectionUtils.isNotEmpty(materialTypes)) {
            stringBuilder.append(and).append(buildQueryForMatchChildReturnParent(leaderMaterialType, materialTypes));
        } else if (CollectionUtils.isNotEmpty(materialTypes)) {
            stringBuilder.append(buildQueryForMatchChildReturnParent(leaderMaterialType, materialTypes));
        }
        return stringBuilder.toString();
    }

    /**
     * Gets query string for match parent return child.
     *
     * @param searchRecordsRequest the search records request
     * @return the query string for match parent return child
     */
    public String getQueryStringForMatchParentReturnChild(SearchRecordsRequest searchRecordsRequest) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> availability = searchRecordsRequest.getAvailability();
        if (CollectionUtils.isNotEmpty(availability)) {
            stringBuilder.append(buildQueryForMatchChildReturnParent(ScsbCommonConstants.AVAILABILITY, availability));
        }
        buildQueryByFieldType(stringBuilder, searchRecordsRequest.getCollectionGroupDesignations(), ScsbCommonConstants.COLLECTION_GROUP_DESIGNATION);
        buildQueryByFieldType(stringBuilder, searchRecordsRequest.getUseRestrictions(), ScsbCommonConstants.USE_RESTRICTION);
        return buildQueryByFieldType(stringBuilder, searchRecordsRequest.getImsDepositoryCodes(), ScsbConstants.IMS_LOCATION_CODE);
    }

    /**
     * Gets query string for match parent return child for deleted data dump cgd to private.
     *
     * @return the query string for match parent return child for deleted data dump cgd to private
     */
    public String getQueryStringForMatchParentReturnChildForDeletedDataDumpCGDToPrivate() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(buildQueryForMatchChildReturnParent(ScsbCommonConstants.COLLECTION_GROUP_DESIGNATION, Arrays.asList(ScsbCommonConstants.PRIVATE)));
        return stringBuilder.toString();
    }

    /**
     * This method is used to build query for parent using the given field name,list of values and child query.
     * @param fieldName
     * @param values
     * @param parentQuery
     * @return
     */
    private String buildQueryForParentGivenChild(String fieldName, List<String> values, String parentQuery) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Iterator<String> iterator = values.iterator(); iterator.hasNext(); ) {
            String value = iterator.next();
            stringBuilder.append(parentQuery).append(fieldName).append(":").append(value);
            if (iterator.hasNext()) {
                stringBuilder.append(or);
            }
        }
        return "(" + stringBuilder.toString() + ")";
    }

    /**
     * This method is used to build query for the given fieldName and list of values.
     * @param fieldName
     * @param values
     * @return
     */
    private String buildQueryForMatchChildReturnParent(String fieldName, List<String> values) {
        List<String> modifiedValues = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(values)) {
            for (String value : values) {
                modifiedValues.add("\"" + value.trim() + "\"");
            }
        }
        return fieldName + ":" + "(" + StringUtils.join(modifiedValues, " ") + ")";
    }

    /**
     * IF the getQueryForFieldCriteria() is called with Item field/value combination, the query would still return
     * only Bib Criteria. You will need to call getItemSolrQueryForCriteria()
     *
     * @param searchRecordsRequest the search records request
     * @return the query for field criteria
     * @throws Exception
     */
    public String getQueryForFieldCriteria(SearchRecordsRequest searchRecordsRequest) {
        StringBuilder stringBuilder = new StringBuilder();
        String fieldValue = parseSearchRequest(searchRecordsRequest.getFieldValue().trim());
        String fieldName = searchRecordsRequest.getFieldName();

        if (StringUtils.isNotBlank(fieldName) && StringUtils.isNotBlank(fieldValue)) {
            //The following "if" condition is for exact match (i.e string data type fields in Solr)
            //Author, Title, Publisher, Publication Place, Publication date, Subjet & Notes.
            if(!(fieldName.equalsIgnoreCase(ScsbCommonConstants.BARCODE) || fieldName.equalsIgnoreCase(ScsbCommonConstants.CALL_NUMBER) || fieldName.equalsIgnoreCase(ScsbCommonConstants.CUSTOMER_CODE)
                    || fieldName.equalsIgnoreCase(ScsbCommonConstants.ISBN_CRITERIA) || fieldName.equalsIgnoreCase(ScsbCommonConstants.OCLC_NUMBER) || fieldName.equalsIgnoreCase(ScsbCommonConstants.ISSN_CRITERIA))) {

                if(fieldName.contains(ScsbConstants.DATE) && !fieldName.equalsIgnoreCase(ScsbCommonConstants.PUBLICATION_DATE)){
                    stringBuilder.append(fieldName).append(":").append("[");
                    stringBuilder.append(fieldValue).append("]");
                    return stringBuilder.toString();
                }

                String[] fieldValues = fieldValue.split("\\s+");

                if(fieldName.equalsIgnoreCase(ScsbCommonConstants.TITLE_STARTS_WITH)) {
                    prepareQueryForTitleBrowse(stringBuilder, fieldValue);
                } else {
                    if(fieldValues.length > 1) {
                        List<String> fieldValuesList = Arrays.asList(fieldValues);
                        for (Iterator<String> iterator = fieldValuesList.iterator(); iterator.hasNext(); ) {
                            String value = iterator.next();
                            stringBuilder.append(fieldName).append(":").append("(").append("\"");
                            stringBuilder.append(value).append("\"").append(")");
                            if (iterator.hasNext()) {
                                stringBuilder.append(and);
                            }
                        }
                    } else {
                        stringBuilder.append(fieldName).append(":").append("(");
                        stringBuilder.append("\"").append(fieldValue).append("\"").append(")");
                    }
                }
            } else {
                //Check for item fields.
                if(fieldName.equalsIgnoreCase(ScsbCommonConstants.CALL_NUMBER)){
                    fieldValue = fieldValue.replace(" ", "");
                }
                if (fieldName.equalsIgnoreCase(ScsbCommonConstants.BARCODE)) {
                    String[] fieldValues = fieldValue.split(",");
                    if (ArrayUtils.isNotEmpty(fieldValues)) {
                        stringBuilder.append(buildQueryForMatchChildReturnParent(fieldName, Arrays.asList(fieldValues)));
                    }
                }
                //Check for Bib fields.
                else {
                    stringBuilder.append(fieldName).append(":").append("(");
                    stringBuilder.append("\"").append(fieldValue).append("\"").append(")");
                }
            }
            return stringBuilder.toString();
        }
        return "";
    }

    private void prepareQueryForTitleBrowse(StringBuilder stringBuilder, String fieldValue) {
        String fieldValueTitleBrowse = fieldValue.replaceAll("\\s+", "\\\\ ");
        stringBuilder.append("(");
        stringBuilder.append(ScsbConstants.TITLE_245).append(":").append("(");
        stringBuilder.append(fieldValueTitleBrowse).append("*").append(")");
        stringBuilder.append(or);
        stringBuilder.append(ScsbConstants.TITLE_246).append(":").append("(");
        stringBuilder.append(fieldValueTitleBrowse).append("*").append(")");
        stringBuilder.append(or);
        stringBuilder.append(ScsbConstants.TITLE_130).append(":").append("(");
        stringBuilder.append(fieldValueTitleBrowse).append("*").append(")");
        stringBuilder.append(or);
        stringBuilder.append(ScsbConstants.TITLE_730).append(":").append("(");
        stringBuilder.append(fieldValueTitleBrowse).append("*").append(")");
        stringBuilder.append(or);
        stringBuilder.append(ScsbConstants.TITLE_740).append(":").append("(");
        stringBuilder.append(fieldValueTitleBrowse).append("*").append(")");
        stringBuilder.append(or);
        stringBuilder.append(ScsbConstants.TITLE_830).append(":").append("(");
        stringBuilder.append(fieldValueTitleBrowse).append("*").append(")");
        stringBuilder.append(")");
    }

    /**
     * Gets count query for field criteria.
     *
     * @param searchRecordsRequest the search records request
     * @param parentQuery          the parent query
     * @return the count query for field criteria
     */
    public String getCountQueryForFieldCriteria(SearchRecordsRequest searchRecordsRequest, String parentQuery) {
        StringBuilder stringBuilder = new StringBuilder();
        String fieldValue = parseSearchRequest(searchRecordsRequest.getFieldValue().trim());
        String fieldName = searchRecordsRequest.getFieldName();
        if (StringUtils.isNotBlank(fieldName) && StringUtils.isNotBlank(fieldValue)) {
            if(!(fieldName.equalsIgnoreCase(ScsbCommonConstants.BARCODE) || fieldName.equalsIgnoreCase(ScsbCommonConstants.CALL_NUMBER) || fieldName.equalsIgnoreCase(ScsbCommonConstants.CUSTOMER_CODE)
                    || fieldName.equalsIgnoreCase(ScsbCommonConstants.ISBN_CRITERIA) || fieldName.equalsIgnoreCase(ScsbCommonConstants.OCLC_NUMBER) || fieldName.equalsIgnoreCase(ScsbCommonConstants.ISSN_CRITERIA))) {
                String[] fieldValues = fieldValue.split("\\s+");
                if(fieldName.equalsIgnoreCase(ScsbCommonConstants.TITLE_STARTS_WITH)) {
                    stringBuilder.append(parentQuery);
                    prepareQueryForTitleBrowse(stringBuilder,fieldValue);
                } else {
                    if(fieldValues.length > 1) {
                        List<String> fieldValuesList = Arrays.asList(fieldValues);
                        stringBuilder.append(parentQuery);
                        for (Iterator<String> iterator = fieldValuesList.iterator(); iterator.hasNext(); ) {
                            String value = iterator.next();
                            stringBuilder.append(fieldName).append(":").append(value);
                            if (iterator.hasNext()) {
                                stringBuilder.append(and);
                            }
                        }
                    } else {
                        stringBuilder.append(parentQuery).append(fieldName).append(":").append(fieldValue);
                    }
                }
            } else {
                if(fieldName.equalsIgnoreCase(ScsbCommonConstants.CALL_NUMBER)) {
                    fieldValue = fieldValue.replace(" ", "");
                }
                stringBuilder.append(parentQuery).append(fieldName).append(":").append(fieldValue);
            }
            return stringBuilder.toString();
        }
        return "";
    }

    /**
     * Gets solr query for bib item.
     *
     * @param parentQueryString the parent query string
     * @return the solr query for bib item
     */
    public SolrQuery getSolrQueryForBibItem(String parentQueryString) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(parentQueryString);
        return new SolrQuery(stringBuilder.toString());
    }

    /**
     * Gets query for parent and child criteria.
     *
     * @param searchRecordsRequest the search records request
     * @return the query for parent and child criteria
     */
    public SolrQuery getQueryForParentAndChildCriteria(SearchRecordsRequest searchRecordsRequest) {
        String queryForFieldCriteria = getQueryForFieldCriteria(searchRecordsRequest);
        String queryStringForBibCriteria = getQueryStringForMatchChildReturnParent(searchRecordsRequest);
        String queryStringForItemCriteriaForParent = getQueryStringForItemCriteriaForParent(searchRecordsRequest);
        SolrQuery solrQuery = getFullQueryForBibCriteria(searchRecordsRequest, queryForFieldCriteria, queryStringForBibCriteria, searchRecordsRequest.getCatalogingStatus());
        solrQuery.setFilterQueries(queryStringForItemCriteriaForParent);
        return solrQuery;
    }

    /**
     * Gets query for parent and child criteria for data dump.
     *
     * @param searchRecordsRequest the search records request
     * @return the query for parent and child criteria for data dump
     */
    public SolrQuery getQueryForParentAndChildCriteriaForDataDump(SearchRecordsRequest searchRecordsRequest) {
        String queryForFieldCriteria = getQueryForFieldCriteria(searchRecordsRequest);
        String queryStringForBibCriteria = getQueryStringForMatchChildReturnParent(searchRecordsRequest);
        String queryStringForItemCriteriaForParent = getQueryStringForItemCriteriaForParent(searchRecordsRequest);
        SolrQuery solrQuery = getFullQueryForBibCriteria(searchRecordsRequest, queryForFieldCriteria, queryStringForBibCriteria, ScsbCommonConstants.COMPLETE_STATUS);
        solrQuery.setFilterQueries(queryStringForItemCriteriaForParent);
        return solrQuery;
    }

    private SolrQuery getFullQueryForBibCriteria(SearchRecordsRequest searchRecordsRequest, String queryForFieldCriteria, String queryStringForBibCriteria, String completeStatus) {
        return new SolrQuery(queryStringForBibCriteria
                + and + ScsbCommonConstants.IS_DELETED_BIB + ":" + searchRecordsRequest.isDeleted()
                + and + ScsbConstants.BIB_CATALOGING_STATUS + ":" + completeStatus
                + (StringUtils.isNotBlank(queryForFieldCriteria) ? and + queryForFieldCriteria : ""));
    }

    public SolrQuery getQueryForParentAndChildCriteriaForDeletedDataDump(SearchRecordsRequest searchRecordsRequest) {
        String queryForFieldCriteria = getQueryForFieldCriteria(searchRecordsRequest);
        String queryStringForBibCriteria = getQueryStringForMatchChildReturnParent(searchRecordsRequest);
        return getFullQueryForBibCriteria(searchRecordsRequest, queryForFieldCriteria, queryStringForBibCriteria, ScsbCommonConstants.COMPLETE_STATUS);
    }

    /**
     * Gets query for child and parent criteria.
     *
     * @param searchRecordsRequest the search records request
     * @return the query for child and parent criteria
     */
    public SolrQuery getQueryForChildAndParentCriteria(SearchRecordsRequest searchRecordsRequest) {
        String queryForFieldCriteria = getQueryForFieldCriteria(searchRecordsRequest);
        String queryStringForItemCriteria = getQueryStringForMatchParentReturnChild(searchRecordsRequest);
        String queryStringForParentCriteriaForChild = getQueryStringForParentCriteriaForChild(searchRecordsRequest);
        return new SolrQuery(queryStringForItemCriteria
                + and + ScsbCommonConstants.IS_DELETED_ITEM + ":" + searchRecordsRequest.isDeleted()
                + and + ScsbConstants.ITEM_CATALOGING_STATUS + ":" + searchRecordsRequest.getCatalogingStatus()
                + and + queryForFieldCriteria + queryStringForParentCriteriaForChild);
    }

    /**
     * Gets deleted query for data dump.
     *
     * @param searchRecordsRequest  the search records request
     * @param isCGDChangedToPrivate the is cgd changed to private
     * @return the deleted query for data dump
     */
    public SolrQuery getDeletedQueryForDataDump(SearchRecordsRequest searchRecordsRequest,boolean isCGDChangedToPrivate) {
        String queryForFieldCriteria = getQueryForFieldCriteria(searchRecordsRequest);
        String queryForBibCriteria = buildQueryForBibFacetCriteria(searchRecordsRequest);
        String queryForImsLocation = buildQueryForFilterGivenChild(ScsbConstants.IMS_LOCATION_CODE,searchRecordsRequest.getImsDepositoryCodes());
        String queryStringForItemCriteria;
        SolrQuery solrQuery;
        if (isCGDChangedToPrivate) {
            queryStringForItemCriteria = getQueryStringForMatchParentReturnChildForDeletedDataDumpCGDToPrivate();
            solrQuery = new SolrQuery(queryStringForItemCriteria + and+queryForImsLocation+and+"("+ ("("+ScsbCommonConstants.IS_DELETED_ITEM + ":" + false + and + ScsbConstants.CGD_CHANGE_LOG + ":" + "\"" + ScsbConstants.CGD_CHANGE_LOG_SHARED_TO_PRIVATE + "\"" +")")
                    + or + ("("+ScsbCommonConstants.IS_DELETED_ITEM + ":" + false + and + ScsbConstants.CGD_CHANGE_LOG + ":" + "\"" + ScsbConstants.CGD_CHANGE_LOG_OPEN_TO_PRIVATE +"\"" +")") +")"
                    + (StringUtils.isNotBlank(queryForFieldCriteria) ? and + queryForFieldCriteria : "")
                    + (StringUtils.isNotBlank(queryForBibCriteria) ? and + queryForBibCriteria : ""));//to include items that got changed from shared to private, open to private for deleted export
        } else{
            queryStringForItemCriteria = getQueryStringForMatchParentReturnChild(searchRecordsRequest);
            solrQuery = new SolrQuery(queryStringForItemCriteria + and +queryForImsLocation+and+ ScsbCommonConstants.IS_DELETED_ITEM + ":" + searchRecordsRequest.isDeleted()
                    + (StringUtils.isNotBlank(queryForFieldCriteria) ? and + queryForFieldCriteria : "")
                    + (StringUtils.isNotBlank(queryForBibCriteria) ? and + queryForBibCriteria : ""));
        }
        return solrQuery;
    }

    /**
     * Gets count query for parent and child criteria.
     *
     * @param searchRecordsRequest the search records request
     * @return the count query for parent and child criteria
     */
    public SolrQuery getCountQueryForParentAndChildCriteria(SearchRecordsRequest searchRecordsRequest) {
        String countQueryForFieldCriteria = getCountQueryForFieldCriteria(searchRecordsRequest, coreParentFilterQuery);
        String queryStringForBibCriteria = getQueryStringForMatchChildReturnParent(searchRecordsRequest);
        String queryStringForItemCriteriaForParent = getQueryStringForItemCriteriaForParent(searchRecordsRequest);
        SolrQuery solrQuery = new SolrQuery(queryStringForBibCriteria
                + and + ScsbCommonConstants.IS_DELETED_BIB + ":" + searchRecordsRequest.isDeleted()
                + and + ScsbConstants.BIB_CATALOGING_STATUS + ":" + searchRecordsRequest.getCatalogingStatus());
        solrQuery.setFilterQueries(queryStringForItemCriteriaForParent, countQueryForFieldCriteria);
        return solrQuery;
    }

    /**
     * Gets count query for child and parent criteria.
     *
     * @param searchRecordsRequest the search records request
     * @return the count query for child and parent criteria
     */
    public SolrQuery getCountQueryForChildAndParentCriteria(SearchRecordsRequest searchRecordsRequest) {
        String countQueryForFieldCriteria = getCountQueryForFieldCriteria(searchRecordsRequest, coreChildFilterQuery);
        String queryStringForItemCriteria = getQueryStringForMatchParentReturnChild(searchRecordsRequest);
        String queryStringForParentCriteriaForChild = getQueryStringForParentCriteriaForChild(searchRecordsRequest);
        SolrQuery solrQuery = new SolrQuery(queryStringForItemCriteria
                + and + ScsbCommonConstants.IS_DELETED_ITEM + ":" + searchRecordsRequest.isDeleted()
                + and + ScsbConstants.ITEM_CATALOGING_STATUS + ":" + searchRecordsRequest.getCatalogingStatus()
                + and + queryStringForParentCriteriaForChild);
        solrQuery.setFilterQueries(countQueryForFieldCriteria);
        return solrQuery;
    }

    /**
     * This method escapes the special characters.
     *
     * @param searchText the search text
     * @return string
     */
    public String parseSearchRequest(String searchText) {
        StringBuilder modifiedText = new StringBuilder();
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
            }
            else {
                modifiedText.append(character);
            }
            character = stringCharacterIterator.next();
        }
        return modifiedText.toString();
    }

    /**
     * Solr query to fetch bib details.
     *
     * @param matchingMatchPointsEntities the matching match points entities
     * @param matchCriteriaValues         the match criteria values
     * @param matchingCriteria            the matching criteria
     * @return the solr query
     */
    public SolrQuery solrQueryToFetchBibDetails(List<MatchingMatchPointsEntity> matchingMatchPointsEntities, List<String> matchCriteriaValues, String matchingCriteria) {
        Integer rows = 0;
        for (MatchingMatchPointsEntity matchingMatchPointsEntity : matchingMatchPointsEntities) {
            String criteriaValue = matchingMatchPointsEntity.getCriteriaValue();
            if(criteriaValue.contains("\\")) {
                criteriaValue = criteriaValue.replaceAll("\\\\", "\\\\\\\\");
            }
            matchCriteriaValues.add(criteriaValue);
            rows = rows + matchingMatchPointsEntity.getCriteriaValueCount();
        }
        StringBuilder query = new StringBuilder();
        if (CollectionUtils.isNotEmpty(matchCriteriaValues)) {
            query.append(buildQueryForMatchChildReturnParent(matchingCriteria, matchCriteriaValues));
        }
        SolrQuery solrQuery = new SolrQuery(getAllCGDBibliographicFilterSolrQuery(query));
        solrQuery.setRows(rows);
        return solrQuery;
    }

    /**
     * Solr query for initial matching.
     *
     * @param fieldName           the field name
     * @param matchingPointValues the matching point values
     * @return the string
     */
    public String solrQueryForInitialMatching(String fieldName, List<String> matchingPointValues) {
        StringBuilder query = new StringBuilder();
        query.append(buildQueryForMatchChildReturnParent(fieldName, matchingPointValues));
        return getAllCGDBibliographicFilterSolrQuery(query);  // initial MA
    }

    /**
     * Solr query for ongoing matching.
     *
     * @param fieldName           the field name
     * @param matchingPointValues the matching point values
     * @return the string
     */
    public String solrQueryForOngoingMatching(String fieldName, List<String> matchingPointValues) {
        StringBuilder query = new StringBuilder();
        query.append(buildQueryForMatchChildReturnParent(fieldName, matchingPointValues));
        return getBibliographicFilterSolrQueryForOngoingMA(query);  // Ongoing MA
    }

    /**
     * Solr query for initial matching.
     *
     * @param fieldName          the field name
     * @param matchingPointValue the matching point value
     * @return the string
     */
    public String solrQueryForInitialMatching(String fieldName, String matchingPointValue) {
        StringBuilder query = new StringBuilder();
        if(matchingPointValue.contains("\\")) {
            matchingPointValue = matchingPointValue.replaceAll("\\\\", "\\\\\\\\");
        }
        query.append(fieldName).append(":").append("\"").append(matchingPointValue).append("\"");
        return getBibliographicFilterSolrQuery(query);
    }


    /**
     * Solr query for ongoing matching.
     *
     * @param fieldName          the field name
     * @param matchingPointValue the matching point value
     * @return the string
     */
    public String solrQueryForOngoingMatching(String fieldName, String matchingPointValue) {
        StringBuilder query = new StringBuilder();
        if(matchingPointValue.contains("\\")) {
            matchingPointValue = matchingPointValue.replaceAll("\\\\", "\\\\\\\\");
        }
        query.append(fieldName).append(":").append("\"").append(matchingPointValue).append("\"");
        return getBibliographicFilterSolrQueryForOngoingMA(query);
    }

    private String getBibliographicFilterSolrQueryForOngoingMA(StringBuilder query) {
        query.append(and).append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS)
                .append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE)
                .append(and).append(coreParentFilterQuery).append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        return query.toString();
    }

    private String getBibliographicFilterSolrQuery(StringBuilder query) {
        query.append(and).append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS)
                .append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.COLLECTION_GROUP_DESIGNATION).append(":").append(ScsbCommonConstants.SHARED_CGD)
                .append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE)
                .append(and).append(coreParentFilterQuery).append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        return query.toString();
    }

    private String getAllCGDBibliographicFilterSolrQuery(StringBuilder query) {
        query.append(and).append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS)
                .append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE)
                .append(and).append(coreParentFilterQuery).append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        return query.toString();
    }

    /**
     * This query is used to Fetch created or updated bibs based on the date.
     *
     * @param date the date
     * @return the string
     */
    public String fetchBibsForGroupingProcess(String date, boolean isBasedOnMAQualifier) {
        StringBuilder query = new StringBuilder();
        if(isBasedOnMAQualifier){
            query/*.append("(").append(ScsbConstants.BIB_CREATED_DATE).append(":").append("[").append(date).append("]")
                    .append(or).append(ScsbConstants.BIB_LAST_UPDATED_DATE).append(":").append("[").append(date).append("]").append(")")
                    append(and)*/
                    .append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                    .append(and).append("(")
                    .append(ScsbConstants.MATCHING_QUALIFIER).append(":").append(ScsbCommonConstants.MA_QUALIFIER_1)
                    .append(or).append(ScsbConstants.MATCHING_QUALIFIER).append(":").append(ScsbCommonConstants.MA_QUALIFIER_3)
                    .append(")")
                    .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
            query.append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE)
                    .append(and).append(coreParentFilterQuery).append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        }else {
            query.append("(").append(ScsbConstants.BIB_CREATED_DATE).append(":").append("[").append(date).append("]")
                    .append(or).append(ScsbConstants.BIB_LAST_UPDATED_DATE).append(":").append("[").append(date).append("]").append(")")
                    .append(and).append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                    .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
            query.append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE)
                    .append(and).append(coreParentFilterQuery).append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        }
        return query.toString();
    }

    /**
     * This query is used to Fetch created or updated bibs based on the date.
     *
     * @param date the date
     * @return the string
     */
    public String fetchBibsForCGDProcess(String date, boolean isBasedOnMAQualifier) {
        StringBuilder query = new StringBuilder();
        if(isBasedOnMAQualifier){
            query./*append("(").append(ScsbConstants.BIB_CREATED_DATE).append(":").append("[").append(date).append("]")
                    .append(or).append(ScsbConstants.BIB_LAST_UPDATED_DATE).append(":").append("[").append(date).append("]").append(")")
                    .append(and)*/
            append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                    .append(and).append("(")
                    .append(ScsbConstants.MATCHING_QUALIFIER).append(":").append(ScsbCommonConstants.MA_QUALIFIER_2)
                    .append(or).append(ScsbConstants.MATCHING_QUALIFIER).append(":").append(ScsbCommonConstants.MA_QUALIFIER_3)
                    .append(")")
                    .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
            query.append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.COLLECTION_GROUP_DESIGNATION).append(":").append(ScsbCommonConstants.SHARED_CGD)
                    .append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE)
                    .append(and).append(coreParentFilterQuery).append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        }
        else {
            query.append("(").append(ScsbConstants.BIB_CREATED_DATE).append(":").append("[").append(date).append("]")
                    .append(or).append(ScsbConstants.BIB_LAST_UPDATED_DATE).append(":").append("[").append(date).append("]").append(")")
                    .append(and).append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                    .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
            query.append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.COLLECTION_GROUP_DESIGNATION).append(":").append(ScsbCommonConstants.SHARED_CGD)
                    .append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE)
                    .append(and).append(coreParentFilterQuery).append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        }
        return query.toString();
    }

    public String fetchMatchingQualifiedBibs() {
        StringBuilder query = new StringBuilder();
        query.append(ScsbConstants.MATCHING_QUALIFIER).append(":").append(ScsbConstants.BOOLEAN_TRUE)
                .append(and).append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        query.append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE)
                .append(and).append(coreParentFilterQuery).append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        return query.toString();
    }

    /**
     * This query is used to Fetch created or updated bibs based on the date range.
     *
     * @param fromDate the from date
     * @param toDate the to date
     * @return the string
     */
    public String fetchCreatedOrUpdatedBibsByDateRange(String fromDate, String toDate) {
        StringBuilder query = new StringBuilder();
        query.append("(").append(ScsbConstants.BIB_CREATED_DATE).append(":").append("[").append(fromDate).append(to).append(toDate).append("]")
                .append(or).append(ScsbConstants.BIB_LAST_UPDATED_DATE).append(":").append("[").append(fromDate).append(to).append(toDate).append("]").append(")")
                .append(and).append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        query.append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.COLLECTION_GROUP_DESIGNATION).append(":").append(ScsbCommonConstants.SHARED_CGD)
                .append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE)
                .append(and).append(coreParentFilterQuery).append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        return query.toString();
    }

    /**
     * This query is used to Fetch bibs based on Bib Id Range.
     *
     * @param fromBibId the from Bib Id
     * @param toBibId the to Bib Id
     * @return the string
     */
    public String fetchBibsByBibIdRange(String fromBibId, String toBibId) {
        StringBuilder query = new StringBuilder();
        query.append(ScsbConstants.BIB_ID).append(":").append("[").append(fromBibId).append(to).append(toBibId).append("]")
                .append(and).append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        query.append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.COLLECTION_GROUP_DESIGNATION).append(":").append(ScsbCommonConstants.SHARED_CGD)
                .append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE)
                .append(and).append(coreParentFilterQuery).append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        return query.toString();
    }

    /**
     * This query is used to Fetch bibs based on Bib Ids.
     *
     * @param bibIds the Bib Ids
     * @return the string
     */
    public String fetchBibsByBibIds(String bibIds) {
        StringBuilder query = new StringBuilder();
        String[] fieldValues = bibIds.split(",");
        query.append(buildQueryForMatchChildReturnParent(ScsbConstants.BIB_ID, Arrays.asList(fieldValues)))
                .append(and).append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        query.append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.COLLECTION_GROUP_DESIGNATION).append(":").append(ScsbCommonConstants.SHARED_CGD)
                .append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE)
                .append(and).append(coreParentFilterQuery).append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        return query.toString();
    }

    /**
     * Build solr query for accession reports.
     *
     * @param date                       the date
     * @param owningInstitution          the owning institution
     * @param isDeleted                  the is deleted
     * @param collectionGroupDesignation the collection group designation
     * @return the solr query
     */
    public SolrQuery buildSolrQueryForAccessionReports(String date, String owningInstitution, boolean isDeleted, String collectionGroupDesignation) {
        return getSolrQueryOnDateType(date, owningInstitution, isDeleted, collectionGroupDesignation, ScsbConstants.ITEM_CREATED_DATE);
    }

    private SolrQuery getSolrQueryOnDateType(String date, String owningInstitution, boolean isDeleted, String collectionGroupDesignation, String itemDate) {
        StringBuilder query = new StringBuilder();
        query.append(ScsbCommonConstants.DOCTYPE).append(":").append(ScsbCommonConstants.ITEM).append(and);
        query.append(itemDate).append(":").append("[").append(date).append("]").append(and);
        query.append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(isDeleted).append(and);
        query.append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS).append(and);
        query.append(ScsbCommonConstants.ITEM_OWNING_INSTITUTION).append(":").append(owningInstitution).append(and);
        query.append(ScsbCommonConstants.COLLECTION_GROUP_DESIGNATION).append(":").append(collectionGroupDesignation);
        return new SolrQuery(query.toString());
    }

    /**
     * Build solr query for deaccession reports.
     *
     * @param date                       the date
     * @param owningInstitution          the owning institution
     * @param isDeleted                  the is deleted
     * @param collectionGroupDesignation the collection group designation
     * @return the solr query
     */
    public SolrQuery buildSolrQueryForDeaccessionReports(String date, String owningInstitution, boolean isDeleted, String collectionGroupDesignation) {
        return getSolrQueryOnDateType(date, owningInstitution, isDeleted, collectionGroupDesignation, ScsbConstants.ITEM_LASTUPDATED_DATE);
    }


    /**
     * Build solr query for cgd reports.
     *
     * @param owningInstitution          the owning institution
     * @param collectionGroupDesignation the collection group designation
     * @return the solr query
     */
    public SolrQuery buildSolrQueryForCGDReports(String owningInstitution , String collectionGroupDesignation){
        StringBuilder query = new StringBuilder();
        query.append(ScsbCommonConstants.DOCTYPE).append(":").append(ScsbCommonConstants.ITEM).append(and);
        query.append(ScsbCommonConstants.ITEM_OWNING_INSTITUTION).append(":").append(owningInstitution).append(and);
        query.append(ScsbCommonConstants.COLLECTION_GROUP_DESIGNATION).append(":").append(collectionGroupDesignation).append(and);
        query.append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE).append(and);
        query.append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        return new SolrQuery(query.toString());
    }


    /**
     * Build solr query for deaccesion report information.
     *
     * @param date              the date
     * @param owningInstitution the owning institution
     * @param isDeleted         the is deleted
     * @return the solr query
     */
    public SolrQuery buildSolrQueryForDeaccesionReportInformation(String date, String owningInstitution, boolean isDeleted) {
        StringBuilder query = new StringBuilder();
        query.append(ScsbCommonConstants.DOCTYPE).append(":").append(ScsbCommonConstants.ITEM).append(and);
        query.append(ScsbConstants.ITEM_LASTUPDATED_DATE).append(":").append("[").append(date).append("]").append(and);
        query.append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(isDeleted).append(and);
        query.append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS).append(and);
        query.append(ScsbCommonConstants.ITEM_OWNING_INSTITUTION).append(":").append(owningInstitution);
        return new SolrQuery(query.toString());
    }


    public SolrQuery buildSolrQueryForIncompleteReports(String owningInstitution){
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(ScsbConstants.ITEM_STATUS_INCOMPLETE+or+"("+ ScsbConstants.ITEM_STATUS_COMPLETE+and+coreChildFilterQuery+ ScsbConstants.BIB_STATUS_INCOMPLETE+")");
        solrQuery.addFilterQuery(ScsbCommonConstants.ITEM_OWNING_INSTITUTION+":"+owningInstitution);
        solrQuery.addFilterQuery(ScsbConstants.IS_DELETED_ITEM_FALSE);
        solrQuery.setFields(ScsbCommonConstants.ITEM_ID,ScsbCommonConstants.BARCODE,ScsbCommonConstants.CUSTOMER_CODE, ScsbConstants.ITEM_CREATED_DATE, ScsbConstants.ITEM_CATALOGING_STATUS, ScsbConstants.ITEM_BIB_ID,ScsbCommonConstants.ITEM_OWNING_INSTITUTION);
        return solrQuery;
    }

    public SolrQuery buildSolrQueryToGetBibDetails(List<Integer> bibIdList,int rows){
        String bibIds = StringUtils.join(bibIdList, ",");
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(ScsbConstants.BIB_DOC_TYPE);
        solrQuery.setRows(rows);
        solrQuery.addFilterQuery(ScsbConstants.SOLR_BIB_ID+StringEscapeUtils.escapeJava(bibIds).replace(",","\" \""));
        solrQuery.setFields(ScsbCommonConstants.BIB_ID, ScsbConstants.TITLE_DISPLAY,ScsbCommonConstants.AUTHOR_SEARCH, ScsbConstants.AUTHOR_DISPLAY);
        return solrQuery;
    }
    public SolrQuery buildQueryTitleMatchCount( String date, String owningInst, String cgd, String matchingIdentifier) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(getBibFilterQueryForTitleMatchReport(owningInst, matchingIdentifier));
        solrQuery.setFilterQueries(getCoreParentFilterQueryForTitleMatchReport(date, Collections.singletonList(cgd)));
        return solrQuery;
    }

    public SolrQuery buildQueryTitleMatchedReport(String date, String owningInst, List<String> cgds, String matchingIdentifier, String match) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(getBibFilterQueryForTitleMatchReport(owningInst, matchingIdentifier));
        solrQuery.setFilterQueries(getCoreParentFilterQueryForTitleMatchReport(date, cgds));
        return solrQuery;
    }

    public SolrQuery solrQueryToFetchMatchedRecords() {
        SolrQuery solrQuery = new SolrQuery(ScsbConstants.BIB_DOC_TYPE + and + ScsbConstants.MATCHING_IDENTIFIER + ":" + "*");
        solrQuery.setRows(10000);
        solrQuery.setFields(ScsbConstants.BIB_ID);
        return solrQuery;
    }

    private String getCoreParentFilterQueryForTitleMatchReport(String date, List<String> cgds) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(coreParentFilterQuery).append(ScsbConstants.ITEM_CREATED_DATE).append(":[").append(date).append("]").append(
                and).append(getQueryForCGDs(cgds)).append(
                and).append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS).append(
                and).append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE);
        return queryBuilder.toString();
    }

    StringBuilder buildQueryForTitleMatchReportPreviewAndExport(StringBuilder matchingIdentifierAppendResult) {
        StringBuilder filterQueryForBib = new StringBuilder();
        filterQueryForBib.append(ScsbCommonConstants.DOCTYPE).append(":").append(ScsbCommonConstants.BIB).append(
                and).append(ScsbConstants.MATCHING_IDENTIFIER).append(":(").append(matchingIdentifierAppendResult).append(")");
        return filterQueryForBib;
    }

    private String getBibFilterQueryForTitleMatchReport(String owningInst, String matchingIdentifier) {
        StringBuilder filterQueryForBib = new StringBuilder();
        filterQueryForBib.append(ScsbCommonConstants.DOCTYPE).append(":").append(ScsbCommonConstants.BIB).append(
                and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS).append(
                and).append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE).append(
                and).append(ScsbCommonConstants.BIB_OWNING_INSTITUTION).append(":").append(owningInst).append(
                and).append(matchingIdentifier).append(ScsbConstants.MATCHING_IDENTIFIER).append(":").append("*");
        return filterQueryForBib.toString();
    }

    private StringBuilder getQueryForCGDs(List<String> cgds) {
        StringBuilder cgdAppend = new StringBuilder();
        if (!cgds.isEmpty()) {
            cgdAppend.append("(");
            String cgdText = ScsbCommonConstants.COLLECTION_GROUP_DESIGNATION + ":";
            for (String cgd : cgds) {
                if (cgds.get(cgds.size() - 1).equalsIgnoreCase(cgd))
                    cgdAppend.append(cgdText).append(cgd);
                else
                    cgdAppend.append(cgdText).append(cgd).append(or);
            }
            cgdAppend.append(")");
        }
        return cgdAppend;
    }

    /**
     * This query is used to Fetch created or updated bibs for Grouping or CGD Update Process with/without MA Qualifier.
     *
     * @return the string
     */
    public String getQueryForOngoingMatchingForGroupingOrCgdUpdateProcess(boolean includeMaQualifier, boolean isCgdProcess) {
        StringBuilder query = new StringBuilder();
        if (includeMaQualifier) {
            List<Integer> maQualifiers = isCgdProcess ? Arrays.asList(ScsbCommonConstants.MA_QUALIFIER_2, ScsbCommonConstants.MA_QUALIFIER_3) : Arrays.asList(ScsbCommonConstants.MA_QUALIFIER_1, ScsbCommonConstants.MA_QUALIFIER_3);
            query.append(prepareQueryForMaQualifier(maQualifiers)).append(and);
        }
        query.append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        query.append(and).append(isCgdProcess ? getCoreParentFilterQueryForCgdProcess() : getCoreParentFilterQueryForGroupingProcess());
        return query.toString();
    }

    /**
     * This query is used to Fetch created or updated bibs based on the date and MA Qualifier for Grouping or CGD Update Process.
     *
     * @param date the date
     * @return the string
     */
    public String getQueryForOngoingMatchingBasedOnDateForGroupingOrCgdUpdateProcess(String date, boolean includeMaQualifier, boolean isCgdProcess) {
        StringBuilder query = new StringBuilder();
        if (includeMaQualifier) {
            List<Integer> maQualifiers = isCgdProcess ? Arrays.asList(ScsbCommonConstants.MA_QUALIFIER_2, ScsbCommonConstants.MA_QUALIFIER_3) : Arrays.asList(ScsbCommonConstants.MA_QUALIFIER_1, ScsbCommonConstants.MA_QUALIFIER_3);
            query.append(prepareQueryForMaQualifier(maQualifiers)).append(and);
        }
        query.append("(").append(ScsbConstants.BIB_CREATED_DATE).append(":").append("[").append(date).append("]")
                .append(or).append(ScsbConstants.BIB_LAST_UPDATED_DATE).append(":").append("[").append(date).append("]").append(")")
                .append(and).append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        query.append(and).append(isCgdProcess ? getCoreParentFilterQueryForCgdProcess() : getCoreParentFilterQueryForGroupingProcess());
        return query.toString();
    }

    /**
     * This query is used to Fetch created or updated bibs based on the date range and MA Qualifier for Grouping or CGD Update Process.
     *
     * @param fromDate the from date
     * @param toDate the to date
     * @return the string
     */
    public String getQueryForOngoingMatchingBasedOnDateRangeForGroupingOrCgdUpdateProcess(String fromDate, String toDate, boolean includeMaQualifier, boolean isCgdProcess) {
        StringBuilder query = new StringBuilder();
        if (includeMaQualifier) {
            List<Integer> maQualifiers = isCgdProcess ? Arrays.asList(ScsbCommonConstants.MA_QUALIFIER_2, ScsbCommonConstants.MA_QUALIFIER_3) : Arrays.asList(ScsbCommonConstants.MA_QUALIFIER_1, ScsbCommonConstants.MA_QUALIFIER_3);
            query.append(prepareQueryForMaQualifier(maQualifiers)).append(and);
        }
        query.append("(").append(ScsbConstants.BIB_CREATED_DATE).append(":").append("[").append(fromDate).append(to).append(toDate).append("]")
                .append(or).append(ScsbConstants.BIB_LAST_UPDATED_DATE).append(":").append("[").append(fromDate).append(to).append(toDate).append("]").append(")")
                .append(and).append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        query.append(and).append(isCgdProcess ? getCoreParentFilterQueryForCgdProcess() : getCoreParentFilterQueryForGroupingProcess());
        return query.toString();
    }

    /**
     * This query is used to Fetch bibs based on Bib Ids and MA Qualifier for Grouping or CGD Update Process.
     *
     * @param bibIds the Bib Ids
     * @return the string
     */
    public String getQueryForOngoingMatchingBasedOnBibIdsForGroupingOrCgdUpdateProcess(String bibIds, boolean includeMaQualifier, boolean isCgdProcess) {
        StringBuilder query = new StringBuilder();
        String[] fieldValues = bibIds.split(",");
        if (includeMaQualifier) {
            List<Integer> maQualifiers = isCgdProcess ? Arrays.asList(ScsbCommonConstants.MA_QUALIFIER_2, ScsbCommonConstants.MA_QUALIFIER_3) : Arrays.asList(ScsbCommonConstants.MA_QUALIFIER_1, ScsbCommonConstants.MA_QUALIFIER_3);
            query.append(prepareQueryForMaQualifier(maQualifiers)).append(and);
        }
        query.append(buildQueryForMatchChildReturnParent(ScsbConstants.BIB_ID, Arrays.asList(fieldValues)))
                .append(and).append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        query.append(and).append(isCgdProcess ? getCoreParentFilterQueryForCgdProcess() : getCoreParentFilterQueryForGroupingProcess());
        return query.toString();
    }

    /**
     * This query is used to Fetch bibs based on Bib Id Range and MA Qualifier for Grouping or CGD Update Process.
     *
     * @param fromBibId the from Bib Id
     * @param toBibId the to Bib Id
     * @return the string
     */
    public String getQueryForOngoingMatchingBasedOnBibIdRangeForGroupingOrCgdUpdateProcess(String fromBibId, String toBibId, boolean includeMaQualifier, boolean isCgdProcess) {
        StringBuilder query = new StringBuilder();
        if (includeMaQualifier) {
            List<Integer> maQualifiers = isCgdProcess ? Arrays.asList(ScsbCommonConstants.MA_QUALIFIER_2, ScsbCommonConstants.MA_QUALIFIER_3) : Arrays.asList(ScsbCommonConstants.MA_QUALIFIER_1, ScsbCommonConstants.MA_QUALIFIER_3);
            query.append(prepareQueryForMaQualifier(maQualifiers)).append(and);
        }
        query.append(ScsbConstants.BIB_ID).append(":").append("[").append(fromBibId).append(to).append(toBibId).append("]")
                .append(and).append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        query.append(and).append(isCgdProcess ? getCoreParentFilterQueryForCgdProcess() : getCoreParentFilterQueryForGroupingProcess());
        return query.toString();
    }

    public String getQueryForOngoingMatchingBasedOnInstitutionAndCGD(String institution, String cgd, boolean includeMaQualifier, boolean isCgdProcess) {
        StringBuilder query = new StringBuilder();
        if (includeMaQualifier) {
            List<Integer> maQualifiers = isCgdProcess ? Arrays.asList(ScsbCommonConstants.MA_QUALIFIER_2, ScsbCommonConstants.MA_QUALIFIER_3) : Arrays.asList(ScsbCommonConstants.MA_QUALIFIER_1, ScsbCommonConstants.MA_QUALIFIER_3);
            query.append(prepareQueryForMaQualifier(maQualifiers)).append(and);
        }
        query.append(ScsbCommonConstants.BIB_OWNING_INSTITUTION).append(":").append(institution)
                .append(and).append(ScsbCommonConstants.IS_DELETED_BIB).append(":").append(ScsbConstants.FALSE)
                .append(and).append(ScsbConstants.BIB_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        if(isCgdProcess){
                query.append(and).append(getCoreParentFilterQueryForCgdProcess());
        } else {
            query.append(and).append(getCoreParentFilterQueryForGroupingProcessCGD(cgd));
        }
        return query.toString();
    }

    private String getCoreParentFilterQueryForCgdProcess() {
        StringBuilder query = new StringBuilder();
        query.append(coreParentFilterQuery).append(ScsbCommonConstants.COLLECTION_GROUP_DESIGNATION).append(":").append(ScsbCommonConstants.SHARED_CGD)
                .append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE)
                .append(and).append(coreParentFilterQuery).append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        return query.toString();
    }

    private String getCoreParentFilterQueryForGroupingProcess() {
        StringBuilder query = new StringBuilder();
        query.append(coreParentFilterQuery).append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE)
                .append(and).append(coreParentFilterQuery).append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        return query.toString();
    }

    private String getCoreParentFilterQueryForGroupingProcessCGD(String cgd) {
        StringBuilder query = new StringBuilder();
        query.append(coreParentFilterQuery).append(ScsbCommonConstants.COLLECTION_GROUP_DESIGNATION).append(":").append(cgd)
                .append(and).append(coreParentFilterQuery).append(ScsbCommonConstants.IS_DELETED_ITEM).append(":").append(ScsbConstants.FALSE)
                .append(and).append(coreParentFilterQuery).append(ScsbConstants.ITEM_CATALOGING_STATUS).append(":").append(ScsbCommonConstants.COMPLETE_STATUS);
        return query.toString();
    }

    private String prepareQueryForMaQualifier(List<Integer> maQualifiers) {
        StringBuilder query = new StringBuilder();
        if (!maQualifiers.isEmpty()) {
            query.append("(");
            for (Iterator<Integer> iterator = maQualifiers.iterator(); iterator.hasNext(); ) {
                Integer maQualifier = iterator.next();
                query.append(ScsbConstants.MATCHING_QUALIFIER).append(":").append(maQualifier);
                if (iterator.hasNext()) {
                    query.append(or);
                }
            }
            query.append(")");
        }
        return query.toString();
    }

}
