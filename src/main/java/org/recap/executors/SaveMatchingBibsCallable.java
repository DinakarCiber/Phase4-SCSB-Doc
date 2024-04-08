package org.recap.executors;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.recap.ScsbConstants;
import org.recap.model.jpa.MatchingBibEntity;
import org.recap.model.jpa.MatchingMatchPointsEntity;
import org.recap.model.search.resolver.BibValueResolver;
import org.recap.model.search.resolver.impl.bib.BibIdValueResolver;
import org.recap.model.search.resolver.impl.bib.ISBNValueResolver;
import org.recap.model.search.resolver.impl.bib.ISSNValueResolver;
import org.recap.model.search.resolver.impl.bib.IdValueResolver;
import org.recap.model.search.resolver.impl.bib.IsDeletedBibValueResolver;
import org.recap.model.search.resolver.impl.bib.LCCNValueResolver;
import org.recap.model.search.resolver.impl.bib.LeaderMaterialTypeValueResolver;
import org.recap.model.search.resolver.impl.bib.OCLCValueResolver;
import org.recap.model.search.resolver.impl.bib.OwningInstitutionBibIdValueResolver;
import org.recap.model.search.resolver.impl.bib.OwningInstitutionValueResolver;
import org.recap.model.search.resolver.impl.bib.VersionValueResolver;
import org.recap.model.search.resolver.impl.bib.TitleMatchValueResolver;
import org.recap.model.search.resolver.impl.bib.TitleSubFieldAValueResolver;
import org.recap.model.solr.BibItem;
import org.recap.repository.jpa.MatchingMatchPointsDetailsRepository;
import org.recap.util.CommonUtil;
import org.recap.util.MatchingAlgorithmUtil;
import org.recap.util.SolrQueryBuilder;
import org.springframework.data.solr.core.SolrTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by angelind on 4/11/16.
 */
@Slf4j
public class SaveMatchingBibsCallable implements Callable {

    private MatchingMatchPointsDetailsRepository matchingMatchPointsDetailsRepository;
    private String matchCriteria;
    private SolrTemplate solrTemplate;
    private ProducerTemplate producer;
    private SolrQueryBuilder solrQueryBuilder;
    private long batchSize;
    private int pageNum;
    private MatchingAlgorithmUtil matchingAlgorithmUtil;

    private List<BibValueResolver> bibValueResolvers;
    private static Set<Integer> bibIdList;

    /**
     * This method instantiates a new save matching bibs callable.
     */
    public SaveMatchingBibsCallable() {
        //Do nothing
    }

    /**
     * This method instantiates a new save matching bibs callable with input arguments.
     *
     * @param matchingMatchPointsDetailsRepository the matching match points details repository
     * @param matchCriteria                        the match criteria
     * @param solrTemplate                         the solr template
     * @param producer                             the producer
     * @param solrQueryBuilder                     the solr query builder
     * @param batchSize                            the batch size
     * @param pageNum                              the page num
     * @param matchingAlgorithmUtil                the matching algorithm util
     */
    public SaveMatchingBibsCallable(MatchingMatchPointsDetailsRepository matchingMatchPointsDetailsRepository, String matchCriteria,
                                    SolrTemplate solrTemplate,
                                    ProducerTemplate producer, SolrQueryBuilder solrQueryBuilder, long batchSize, int pageNum, MatchingAlgorithmUtil matchingAlgorithmUtil) {
        this.matchingMatchPointsDetailsRepository = matchingMatchPointsDetailsRepository;
        this.matchCriteria = matchCriteria;
        this.solrTemplate = solrTemplate;
        this.producer = producer;
        this.solrQueryBuilder = solrQueryBuilder;
        this.batchSize = batchSize;
        this.pageNum = pageNum;
        this.matchingAlgorithmUtil = matchingAlgorithmUtil;
    }

    /**
     * This method is used to get the matching bibs from the solr and saves them in the database using ActiveMQ.
     * @return
     * @throws Exception
     */
    @Override
    public Object call() throws Exception {
        Integer size = 0;
        long from = pageNum * batchSize;
        List<MatchingMatchPointsEntity> matchPointsEntityList = matchingMatchPointsDetailsRepository.getMatchPointEntityByCriteria(matchCriteria, from, batchSize);
        if(CollectionUtils.isNotEmpty(matchPointsEntityList)) {
            List<String> matchingCriteriaValues = new ArrayList<>();
            SolrQuery solrQuery = solrQueryBuilder.solrQueryToFetchBibDetails(matchPointsEntityList, matchingCriteriaValues, matchCriteria);
            QueryResponse queryResponse = solrTemplate.getSolrClient().query(solrQuery, SolrRequest.METHOD.POST);
            SolrDocumentList solrDocumentList = queryResponse.getResults();
            List<MatchingBibEntity> matchingBibEntityList = new ArrayList<>();
            for (Iterator<SolrDocument> iterator = solrDocumentList.iterator(); iterator.hasNext(); ) {
                SolrDocument solrDocument = iterator.next();
                BibItem bibItem = new BibItem();
                populateBibItem(solrDocument, bibItem);
                Integer bibId = bibItem.getBibId();
                if (!isBibIdDuplicate(bibId)) {
                    MatchingBibEntity matchingBibEntity = new MatchingBibEntity();
                    matchingBibEntity.setBibId(bibId);
                    matchingBibEntity.setVersion(bibItem.getVersion());
                    matchingBibEntity.setOwningInstitution(bibItem.getOwningInstitution());
                    matchingBibEntity.setOwningInstBibId(bibItem.getOwningInstitutionBibId());
                    matchingBibEntity.setTitle(bibItem.getTitleMatch());
                    matchingBibEntity.setOclc(CollectionUtils.isNotEmpty(bibItem.getOclcNumber()) ? StringUtils.join(bibItem.getOclcNumber(), ",") : null);
                    matchingBibEntity.setIsbn(getTruncatedIsbnValue(bibItem.getIsbn(), bibId));
                    matchingBibEntity.setIssn(CollectionUtils.isNotEmpty(bibItem.getIssn()) ? StringUtils.join(bibItem.getIssn(), ",") : null);
                    matchingBibEntity.setLccn(bibItem.getLccn());
                    matchingBibEntity.setMaterialType(bibItem.getLeaderMaterialType());
                    matchingBibEntity.setMatching(matchCriteria);
                    matchingBibEntity.setStatus(ScsbConstants.PENDING);
                    if(addBibIdToList(bibId)) {
                        matchingBibEntityList.add(matchingBibEntity);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(matchingBibEntityList)) {
                producer.sendBody("scsbactivemq:queue:saveMatchingBibsQ", matchingBibEntityList);
                size = size + matchingBibEntityList.size();
            }
        }
        return size;
    }

    private String getTruncatedIsbnValue(List<String> isbn, Integer bibId) {
        String isbnValue = null;
        if (CollectionUtils.isNotEmpty(isbn)) {
            isbnValue = StringUtils.join(isbn, ",");
            if (isbnValue.length() > 10000) {
                log.info("Length of the isbn value with size greater than 10000 - Bib Id - {}, size - {}", bibId, isbnValue.length());
                isbnValue = isbnValue.substring(0,10000);
            }
        }
        return isbnValue;
    }

    /**
     * This method populates bibItem for the given solr document.
     *
     * @param solrDocument the solr document
     * @param bibItem      the bib item
     */
    public void populateBibItem(SolrDocument solrDocument, BibItem bibItem) {
        Collection<String> fieldNames = solrDocument.getFieldNames();
        new CommonUtil().getBibItemFromSolrFieldNames(solrDocument, fieldNames, bibItem);
    }

    /**
     * To add bib id in the bib id list .
     *
     * @param bibId the bib id
     * @return the boolean
     */
    public static synchronized boolean addBibIdToList(Integer bibId) {
        return getBibIdList().add(bibId);
    }

    /**
     * Gets bib id list.
     *
     * @return the bib id list
     */
    public static synchronized Set<Integer> getBibIdList() {
        if(bibIdList == null) {
            bibIdList = new HashSet<>();
        }
        return bibIdList;
    }

    /**
     * Checks whether the bib id is duplicate.
     *
     * @param bibId the bib id
     * @return the boolean
     */
    public static synchronized boolean isBibIdDuplicate(Integer bibId) {
        return getBibIdList().contains(bibId);
    }

    /**
     * This method sets bib id list.
     *
     * @param bibIdList the bib id list
     */
    public static synchronized void setBibIdList(Set<Integer> bibIdList) {
        SaveMatchingBibsCallable.bibIdList = bibIdList;
    }

    /**
     * This method gets bib value resolvers which is used to build the values for Bib fields.
     *
     * @return the bib value resolvers
     */
    public List<BibValueResolver> getBibValueResolvers() {
        if (null == bibValueResolvers) {
            bibValueResolvers = new ArrayList<>();
            bibValueResolvers.add(new VersionValueResolver());
            bibValueResolvers.add(new BibIdValueResolver());
            bibValueResolvers.add(new IdValueResolver());
            bibValueResolvers.add(new ISBNValueResolver());
            bibValueResolvers.add(new ISSNValueResolver());
            bibValueResolvers.add(new LCCNValueResolver());
            bibValueResolvers.add(new LeaderMaterialTypeValueResolver());
            bibValueResolvers.add(new OCLCValueResolver());
            bibValueResolvers.add(new OwningInstitutionBibIdValueResolver());
            bibValueResolvers.add(new OwningInstitutionValueResolver());
            bibValueResolvers.add(new TitleSubFieldAValueResolver());
            bibValueResolvers.add(new TitleMatchValueResolver());
            bibValueResolvers.add(new IsDeletedBibValueResolver());
        }
        return bibValueResolvers;
    }
}
