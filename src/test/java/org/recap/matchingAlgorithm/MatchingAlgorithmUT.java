package org.recap.matchingAlgorithm;

import org.apache.activemq.broker.jmx.DestinationViewMBean;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.RecapConstants;
import org.recap.camel.activemq.JmxHelper;
import org.recap.executors.SaveMatchingBibsCallable;
import org.recap.model.jpa.MatchingBibEntity;
import org.recap.model.jpa.MatchingMatchPointsEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.jpa.MatchingBibDetailsRepository;
import org.recap.repository.jpa.MatchingMatchPointsDetailsRepository;
import org.recap.repository.jpa.ReportDetailRepository;
import org.recap.repository.solr.impl.BibSolrDocumentRepositoryImpl;
import org.recap.util.MatchingAlgorithmUtil;
import org.recap.util.SolrQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by angelind on 27/10/16.
 */
public class MatchingAlgorithmUT extends BaseTestCase {

    Logger logger = LoggerFactory.getLogger(MatchingAlgorithmUT.class);

    @Autowired
    BibSolrDocumentRepositoryImpl bibSolrDocumentRepository;

    @Autowired
    ProducerTemplate producer;

    @Autowired
    MatchingMatchPointsDetailsRepository matchingMatchPointsDetailsRepository;

    @Autowired
    SolrQueryBuilder solrQueryBuilder;

    @Autowired
    SolrTemplate solrTemplate;

    @Autowired
    MatchingBibDetailsRepository matchingBibDetailsRepository;

    @Autowired
    ReportDetailRepository reportDetailRepository;

    @Autowired
    MatchingAlgorithmUtil matchingAlgorithmUtil;

    @Autowired
    JmxHelper jmxHelper;

    String and = " AND ";
    String coreParentFilterQuery = "{!parent which=\"ContentType:parent\"}";
    private ExecutorService executorService;

    @Test
    public void populateTempMatchingPointsEntity() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<MatchingMatchPointsEntity> matchingMatchPointsEntities;
        long count = 0;

        matchingMatchPointsEntities = getMatchingMatchPointsEntity(RecapConstants.MATCH_POINT_FIELD_OCLC);
        count = count + matchingMatchPointsEntities.size();
        saveMatchingMatchPointEntities(matchingMatchPointsEntities);

        matchingMatchPointsEntities = getMatchingMatchPointsEntity(RecapConstants.MATCH_POINT_FIELD_ISBN);
        count = count + matchingMatchPointsEntities.size();
        saveMatchingMatchPointEntities(matchingMatchPointsEntities);

        matchingMatchPointsEntities = getMatchingMatchPointsEntity(RecapConstants.MATCH_POINT_FIELD_ISSN);
        count = count + matchingMatchPointsEntities.size();
        saveMatchingMatchPointEntities(matchingMatchPointsEntities);

        matchingMatchPointsEntities = getMatchingMatchPointsEntity(RecapConstants.MATCH_POINT_FIELD_LCCN);
        count = count + matchingMatchPointsEntities.size();
        saveMatchingMatchPointEntities(matchingMatchPointsEntities);

        logger.info("Total count : " + count);

        DestinationViewMBean saveMatchingMatchPointsQ = jmxHelper.getBeanForQueueName("saveMatchingMatchPointsQ");

        while (saveMatchingMatchPointsQ.getQueueSize() != 0) {

        }

        stopWatch.stop();
        logger.info("Total Time taken : " + stopWatch.getTotalTimeSeconds());

        Thread.sleep(10000);
        long savedCount = matchingMatchPointsDetailsRepository.count();
        assertEquals(count, savedCount);
    }

    @Test
    public void populateTempMatchingBibsEntity() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Integer count = 0;
        count = count + fetchAndSaveMatchingBibs(RecapConstants.MATCH_POINT_FIELD_OCLC);
        stopWatch.stop();
        logger.info("Time taken to complete OCLC : " + stopWatch.getTotalTimeSeconds());
        stopWatch.start();
        count = count + fetchAndSaveMatchingBibs(RecapConstants.MATCH_POINT_FIELD_ISBN);
        stopWatch.stop();
        logger.info("Time taken to complete ISBN : " + stopWatch.getTotalTimeSeconds());
        stopWatch.start();
        count = count + fetchAndSaveMatchingBibs(RecapConstants.MATCH_POINT_FIELD_ISSN);
        stopWatch.stop();
        logger.info("Time taken to complete ISSN : " + stopWatch.getTotalTimeSeconds());
        stopWatch.start();
        count = count + fetchAndSaveMatchingBibs(RecapConstants.MATCH_POINT_FIELD_LCCN);
        stopWatch.stop();
        logger.info("Time taken to complete LCCN : " + stopWatch.getTotalTimeSeconds());
        stopWatch.start();
        logger.info("Total count : " + count);
        DestinationViewMBean saveMatchingBibsQ = jmxHelper.getBeanForQueueName("saveMatchingBibsQ");
        while (saveMatchingBibsQ.getQueueSize() != 0) {

        }
        stopWatch.stop();
        logger.info("Total Time taken : " + stopWatch.getTotalTimeSeconds());
        long savedBibsCount = matchingBibDetailsRepository.count();
        assertTrue(savedBibsCount > 0);
    }

    @Test
    public void populateReportEntityForMultiMatchPoints() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        long batchSize = 1000;
        long count = 0;
        long reportsCountBefore = reportDetailRepository.count();
        long multipleMatchUniqueBibCount = matchingBibDetailsRepository.getMultipleMatchUniqueBibCount();
        logger.info("Total Unique Bib Count : " + multipleMatchUniqueBibCount);
        int totalPagesCount = (int) Math.ceil(multipleMatchUniqueBibCount / batchSize);
        for (int pageNum = 0; pageNum < totalPagesCount + 1; pageNum++) {
            long from = pageNum * batchSize;
            List<Integer> multipleMatchedBibIdsBasedOnLimit = matchingBibDetailsRepository.getMultipleMatchedBibIdsBasedOnLimit(from, batchSize);
            List<MatchingBibEntity> multipleMatchPointBibEntityList = matchingBibDetailsRepository.getBibEntityBasedOnBibIds(multipleMatchedBibIdsBasedOnLimit);
            if (CollectionUtils.isNotEmpty(multipleMatchPointBibEntityList)) {
                List<ReportEntity> reportEntityList = matchingAlgorithmUtil.populateReportEntities(multipleMatchPointBibEntityList, RecapConstants.MATCHING_TYPE);
                producer.sendBody("scsbactivemq:queue:saveMatchingReportsQ", reportEntityList);
                count = count + reportEntityList.size();
            }
        }
        logger.info("Total count : " + count);
        DestinationViewMBean saveMatchingReportsQ = jmxHelper.getBeanForQueueName("saveMatchingReportsQ");
        while (saveMatchingReportsQ.getQueueSize() != 0) {

        }
        stopWatch.stop();
        logger.info("Total Time taken : " + stopWatch.getTotalTimeSeconds());

        Thread.sleep(10000);

        long savedReportsCount = 0;
        long reportsCountAfter = reportDetailRepository.count();
        savedReportsCount = reportsCountAfter - reportsCountBefore;
        assertEquals(savedReportsCount, count);
    }

    @Test
    public void populateReportEntityForSingleMatchPoint() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        long reportsCountBefore = reportDetailRepository.count();
        long batchSize = 10000;
        long count = 0;
        count += verifyTitleAndPopulateReports(batchSize, RecapConstants.MATCH_POINT_FIELD_OCLC);
        count += verifyTitleAndPopulateReports(batchSize, RecapConstants.MATCH_POINT_FIELD_ISBN);
        count += verifyTitleAndPopulateReports(batchSize, RecapConstants.MATCH_POINT_FIELD_ISSN);
        count += verifyTitleAndPopulateReports(batchSize, RecapConstants.MATCH_POINT_FIELD_LCCN);

        logger.info("Total count : " + count);
        DestinationViewMBean saveMatchingReportsQ = jmxHelper.getBeanForQueueName("saveMatchingReportsQ");
        while (saveMatchingReportsQ.getQueueSize() != 0) {

        }
        stopWatch.stop();
        logger.info("Total Time taken : " + stopWatch.getTotalTimeSeconds());

        Thread.sleep(10000);

        long reportsCountAfter = reportDetailRepository.count();
        long savedReportsCount = reportsCountAfter - reportsCountBefore;
        assertEquals(savedReportsCount, count);
    }

    private long saveReportEntity(List<ReportEntity> reportEntityList) throws IOException, SolrServerException {
        if(CollectionUtils.isNotEmpty(reportEntityList)) {
            producer.sendBody("scsbactivemq:queue:saveMatchingReportsQ", reportEntityList);
        }
        return reportEntityList.size();
    }

    public Integer verifyTitleAndPopulateReports(long batchSize, String matchingCriteria) throws IOException, SolrServerException {
        Integer count = 0;
        long singleMatchUniqueBibCount = matchingBibDetailsRepository.getSingleMatchBibCountBasedOnMatching(matchingCriteria);
        logger.info("Total Unique Bib Count for "+ matchingCriteria + " : " + singleMatchUniqueBibCount);
        int totalPagesCount = (int) Math.ceil(singleMatchUniqueBibCount / batchSize);
        Map<String, List<MatchingBibEntity>> matchingMap = new HashMap<>();
        for(int pageNum = 0; pageNum < totalPagesCount + 1; pageNum++) {
            long from = pageNum * batchSize;
            List<MatchingBibEntity> singleMatchBibEntities = matchingBibDetailsRepository.getSingleMatchBibEntities(matchingCriteria, from, batchSize);
            if(CollectionUtils.isNotEmpty(singleMatchBibEntities)) {
                for(MatchingBibEntity matchingBibEntity : singleMatchBibEntities) {
                    if(matchingCriteria.equalsIgnoreCase(RecapConstants.MATCH_POINT_FIELD_OCLC)) {
                        matchingAlgorithmUtil.getMatchingEntitiesMap(matchingMap, matchingBibEntity, matchingBibEntity.getOclc());
                    } else if(matchingCriteria.equalsIgnoreCase(RecapConstants.MATCH_POINT_FIELD_ISBN)) {
                        matchingAlgorithmUtil.getMatchingEntitiesMap(matchingMap, matchingBibEntity, matchingBibEntity.getIsbn());
                    } else if(matchingCriteria.equalsIgnoreCase(RecapConstants.MATCH_POINT_FIELD_ISSN)) {
                        matchingAlgorithmUtil.getMatchingEntitiesMap(matchingMap, matchingBibEntity, matchingBibEntity.getIssn());
                    } else {
                        matchingAlgorithmUtil.getMatchingEntitiesMap(matchingMap, matchingBibEntity, matchingBibEntity.getLccn());
                    }
                }
            }
        }

        List<MatchingBibEntity> unMatchedBibEntities = new ArrayList<>();
        List<MatchingBibEntity> matchingBibEntities = new ArrayList<>();
        for (Iterator<String> iterator = matchingMap.keySet().iterator(); iterator.hasNext(); ) {
            String matchingValue = iterator.next();
            List<MatchingBibEntity> matchingBibEntityList = matchingMap.get(matchingValue);
            Set<MatchingBibEntity> unMatchingBibsAfterTitleVerification = matchingAlgorithmUtil.getUnMatchingBibsAfterTitleVerification(matchingBibEntityList);
            matchingBibEntityList.removeAll(unMatchingBibsAfterTitleVerification);
            if(CollectionUtils.isNotEmpty(matchingBibEntityList) && matchingBibEntityList.size() > 1) {
                matchingBibEntities.addAll(matchingBibEntityList);
            }
            if(CollectionUtils.isNotEmpty(unMatchingBibsAfterTitleVerification)) {
                unMatchedBibEntities.addAll(unMatchingBibsAfterTitleVerification);
            }
            if(matchingBibEntities.size() >= 200 || unMatchedBibEntities.size() >= 100) {
                count += saveReportEntitiesForSingleMatch(unMatchedBibEntities, matchingBibEntities);
                matchingBibEntities = new ArrayList<>();
                unMatchedBibEntities = new ArrayList<>();
            }
        }
        count += saveReportEntitiesForSingleMatch(unMatchedBibEntities, matchingBibEntities);
        return count;
    }

    private Integer saveReportEntitiesForSingleMatch(List<MatchingBibEntity> unMatchedBibEntities, List<MatchingBibEntity> matchingBibEntities) throws IOException, SolrServerException {
        List<ReportEntity> reportEntityList = new ArrayList<>();
        reportEntityList.addAll(matchingAlgorithmUtil.populateReportEntities(matchingBibEntities, RecapConstants.MATCHING_TYPE));
        reportEntityList.addAll(matchingAlgorithmUtil.populateReportEntities(unMatchedBibEntities, RecapConstants.EXCEPTION_TYPE));
        saveReportEntity(reportEntityList);
        return reportEntityList.size();
    }

    public List<MatchingMatchPointsEntity> getMatchingMatchPointsEntity(String fieldName) throws Exception {
        List<MatchingMatchPointsEntity> matchingMatchPointsEntities = new ArrayList<>();
        String query = RecapConstants.DOCTYPE + ":" + RecapConstants.BIB + and + RecapConstants.IS_DELETED_BIB + ":false" + and + coreParentFilterQuery + RecapConstants.COLLECTION_GROUP_DESIGNATION
                + ":" + RecapConstants.SHARED_CGD + and + coreParentFilterQuery + RecapConstants.IS_DELETED_ITEM + ":false";
        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.setFacet(true);
        solrQuery.addFacetField(fieldName);
        solrQuery.setFacetLimit(-1);
        solrQuery.setFacetMinCount(2);
        solrQuery.setRows(0);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        QueryResponse queryResponse = solrTemplate.getSolrClient().query(solrQuery);
        stopWatch.stop();
        logger.info("Total Time Taken to get "+ fieldName +" duplicates from solr : " + stopWatch.getTotalTimeSeconds());
        List<FacetField> facetFields = queryResponse.getFacetFields();
        for (FacetField facetField : facetFields) {
            List<FacetField.Count> values = facetField.getValues();
            for (Iterator<FacetField.Count> iterator = values.iterator(); iterator.hasNext(); ) {
                FacetField.Count next = iterator.next();
                String name = next.getName();
                if(StringUtils.isNotBlank(name)) {
                    MatchingMatchPointsEntity matchingMatchPointsEntity = new MatchingMatchPointsEntity();
                    matchingMatchPointsEntity.setMatchCriteria(fieldName);
                    matchingMatchPointsEntity.setCriteriaValue(name);
                    matchingMatchPointsEntity.setCriteriaValueCount((int) next.getCount());
                    matchingMatchPointsEntities.add(matchingMatchPointsEntity);
                }
            }
        }
        return matchingMatchPointsEntities;
    }

    public Integer fetchAndSaveMatchingBibs(String matchCriteria) throws SolrServerException, IOException {
        long batchSize = 300;
        Integer size = 0;
        long countBasedOnCriteria = matchingMatchPointsDetailsRepository.countBasedOnCriteria(matchCriteria);
        SaveMatchingBibsCallable saveMatchingBibsCallable = new SaveMatchingBibsCallable();
        saveMatchingBibsCallable.setBibIdList(new ArrayList<>());
        int totalPagesCount = (int) Math.ceil(countBasedOnCriteria / batchSize);
        ExecutorService executorService = getExecutorService(50);
        List<Callable<Integer>> callables = new ArrayList<>();
        for (int pageNum = 0; pageNum < totalPagesCount + 1; pageNum++) {
            Callable callable = new SaveMatchingBibsCallable(matchingMatchPointsDetailsRepository, matchCriteria, solrTemplate,
                    producer, solrQueryBuilder, batchSize, pageNum, matchingAlgorithmUtil);
            callables.add(callable);
        }

        size = executeCallables(size, executorService, callables);
        return size;
    }

    private Integer executeCallables(Integer size, ExecutorService executorService, List<Callable<Integer>> callables) {
        List<Future<Integer>> futures = null;
        try {
            futures = executorService.invokeAll(callables);
            futures
                    .stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Iterator<Future<Integer>> iterator = futures.iterator(); iterator.hasNext(); ) {
            Future future = iterator.next();
            try {
                size += (Integer) future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return size;
    }

    public void saveMatchingMatchPointEntities(List<MatchingMatchPointsEntity> matchingMatchPointsEntities) {
        int batchSize = 1000;
        int size = 0;
        if (CollectionUtils.isNotEmpty(matchingMatchPointsEntities)) {
            for (int i = 0; i < matchingMatchPointsEntities.size(); i += batchSize) {
                List<MatchingMatchPointsEntity> matchingMatchPointsEntityList = new ArrayList<>();
                matchingMatchPointsEntityList.addAll(matchingMatchPointsEntities.subList(i, Math.min(i + batchSize, matchingMatchPointsEntities.size())));
                producer.sendBody("scsbactivemq:queue:saveMatchingMatchPointsQ", matchingMatchPointsEntityList);
                size = size + matchingMatchPointsEntityList.size();
            }
        }
        assertEquals(size, matchingMatchPointsEntities.size());
    }

    private ExecutorService getExecutorService(Integer numThreads) {
        if (null == executorService || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(numThreads);
        }
        return executorService;
    }

}
