package org.recap.executors;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.admin.SolrAdmin;
import org.recap.model.jpa.CollectionGroupEntity;
import org.recap.model.jpa.InstitutionEntity;
import org.recap.model.solr.Bib;
import org.recap.model.solr.SolrIndexRequest;
import org.recap.repository.jpa.CollectionGroupDetailsRepository;
import org.recap.repository.jpa.InstitutionDetailsRepository;
import org.recap.repository.solr.main.BibSolrCrudRepository;
import org.recap.repository.solr.temp.BibCrudRepositoryMultiCoreSupport;
import org.recap.util.CommonUtil;
import org.recap.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.convert.MappingSolrConverter;
import org.springframework.data.solr.core.mapping.SimpleSolrMappingContext;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.util.StopWatch;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by pvsubrah on 6/13/16.
 */
@Slf4j
public abstract class IndexExecutorService {



    /**
     * The Solr admin.
     */
    @Autowired
    SolrAdmin solrAdmin;

    /**
     * The Producer template.
     */
    @Autowired
    ProducerTemplate producerTemplate;

    /**
     * The Institution details repository.
     */
    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    /**
     * The Bib solr crud repository.
     */
    @Autowired
    BibSolrCrudRepository bibSolrCrudRepository;

    @Autowired
    CommonUtil  commonUtil;

    /**
     * The Solr server protocol.
     */
    @Value("${" + PropertyKeyConstants.SOLR_SERVER_PROTOCOL + "}")
    String solrServerProtocol;

    /**
     * The Solr core.
     */
    @Value("${" + PropertyKeyConstants.SOLR_PARENT_CORE + "}")
    String solrCore;

    /**
     * The Solr url.
     */
    @Value("${" + PropertyKeyConstants.SOLR_URL + "}")
    String solrUrl;

    /**
     * The Solr router uri.
     */
    @Value("${" + PropertyKeyConstants.SOLR_ROUTER_URI_TYPE + "}")
    String solrRouterURI;

    /**
     * The Date util.
     */
    @Autowired
    DateUtil dateUtil;

    /**
     * This method initiates the solr indexing based on the selected owning institution.
     *
     * @param solrIndexRequest the solr index request
     * @return integer
     */
    public Integer indexByOwningInstitutionId(SolrIndexRequest solrIndexRequest) {
        StopWatch stopWatch1 = new StopWatch();
        stopWatch1.start();

        Integer numThreads = solrIndexRequest.getNumberOfThreads();
        Integer docsPerThread = solrIndexRequest.getNumberOfDocs();
        Integer commitIndexesInterval = solrIndexRequest.getCommitInterval();
        String owningInstitutionCode = solrIndexRequest.getOwningInstitutionCode();
        String fromDate = solrIndexRequest.getDateFrom();
        Integer owningInstitutionId = null;
        Integer cgdId = null;
        Date from = null;
        String coreName = solrCore;
        Integer totalBibsProcessed = 0;
        boolean isIncremental = StringUtils.isNotBlank(fromDate) ? Boolean.TRUE : Boolean.FALSE;

        try {
            ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
            if (StringUtils.isNotBlank(owningInstitutionCode)) {
                InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionCode(owningInstitutionCode);
                if (null != institutionEntity) {
                    owningInstitutionId = institutionEntity.getId();
                }
            }
            if (isIncremental) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat(ScsbCommonConstants.INCREMENTAL_DATE_FORMAT);
                from = dateFormatter.parse(fromDate);
            }
            Integer totalDocCount = getTotalDocCount(owningInstitutionId, from);
            log.info("Total Document Count From DB : {}",totalDocCount);

            if (totalDocCount > 0) {
                int quotient = totalDocCount / (docsPerThread);
                int remainder = totalDocCount % (docsPerThread);
                Integer loopCount = remainder == 0 ? quotient : quotient + 1;
                log.info("Loop Count Value : {} ",loopCount);
                log.info("Commit Indexes Interval : {}",commitIndexesInterval);

                Integer callableCountByCommitInterval = commitIndexesInterval / (docsPerThread);
                if (callableCountByCommitInterval == 0) {
                    callableCountByCommitInterval = 1;
                }
                log.info("Number of callables to execute to commit indexes : {}",callableCountByCommitInterval);

                List<String> coreNames = new ArrayList<>();
                if (!isIncremental) {
                    setupCoreNames(numThreads, coreNames);
                    solrAdmin.createSolrCores(coreNames);
                }

                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                int coreNum = 0;
                String tempCoreName = null;
                List<Callable<Integer>> callables = new ArrayList<>();
                for (int pageNum = 0; pageNum < loopCount; pageNum++) {
                    if (!isIncremental) {
                        tempCoreName = coreNames.get(coreNum);
                        coreNum = coreNum < numThreads - 1 ? coreNum + 1 : 0;
                    }
                    Callable callable = getCallable(tempCoreName, pageNum, docsPerThread, owningInstitutionId, from, null, null,cgdId);
                    callables.add(callable);
                }

                int futureCount = 0;
                List<List<Callable<Integer>>> partitions = Lists.partition(new ArrayList<>(callables), callableCountByCommitInterval);
                for (List<Callable<Integer>> partitionCallables : partitions) {
                    List<Future<Integer>> futures = executorService.invokeAll(partitionCallables);
                    log.info("No of Futures Added : {}",futures.size());
                    List<Future<Integer>> collectedFutures = futures.stream().map(future -> {
                        try {
                            future.get();
                            return future;
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }).collect(Collectors.toCollection(ArrayList::new));
                    log.info("No of Futures Collected : {}",collectedFutures.size());

                    int numOfBibsProcessed = 0;
                    for (Future<Integer> future : collectedFutures) {
                        try {
                            Integer entitiesCount = future.get();
                            numOfBibsProcessed += entitiesCount;
                            totalBibsProcessed += entitiesCount;
                            log.info("Num of bibs fetched by thread : {}", entitiesCount);
                            futureCount++;
                        } catch (InterruptedException | ExecutionException e) {
                            Thread.currentThread().interrupt();
                            log.error(ScsbCommonConstants.LOG_ERROR, e);
                        }
                    }
                    if (!isIncremental) {
                        solrAdmin.mergeCores(coreNames);
                        log.info("Solr core status : {}" , solrAdmin.getCoresStatus());
                        while (solrAdmin.getCoresStatus() != 0) {
                            log.info("Solr core status : {}" , solrAdmin.getCoresStatus());
                        }
                        deleteTempIndexes(coreNames, solrServerProtocol + solrUrl);
                    }
                    log.info("Num of Bibs Processed and indexed to core {} on commit interval : {} ",coreName,numOfBibsProcessed);
                    log.info("Total Num of Bibs Processed and indexed to core {} : {}",coreName,totalBibsProcessed);
                    Long solrBibCount = bibSolrCrudRepository.countByDocType(ScsbCommonConstants.BIB);
                    log.info("Total number of Bibs in Solr in recap core : {}",solrBibCount);
                }
                log.info("Total futures executed: {}",futureCount);
                stopWatch.stop();
                log.info("Time taken to fetch {} Bib Records and index to recap core : {} seconds",totalBibsProcessed,stopWatch.getTotalTimeSeconds());
                if (!isIncremental) {
                    solrAdmin.unLoadCores(coreNames);
                }
                executorService.shutdown();
            } else {
                log.info("No records found to index for the criteria");
            }
        } catch (Exception e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        stopWatch1.stop();
        log.info("Total time taken:{} secs",stopWatch1.getTotalTimeSeconds());
        return totalBibsProcessed;
    }

    /**
     * This method initiates the solr indexing partially based on the bibIdList or bibIdRange or dateRange.
     *
     * @param solrIndexRequest the solr index request
     * @return integer
     */
    public Integer partialIndex(SolrIndexRequest solrIndexRequest) {
        StopWatch stopWatch1 = new StopWatch();
        stopWatch1.start();

        Integer numThreads = solrIndexRequest.getNumberOfThreads();
        Integer docsPerThread = solrIndexRequest.getNumberOfDocs();
        Integer commitIndexesInterval = solrIndexRequest.getCommitInterval();
        String partialIndexType = solrIndexRequest.getPartialIndexType();
        String owningInstitutionCode = solrIndexRequest.getOwningInstitutionCode();
        String cgdCode = solrIndexRequest.getCgd();
        Integer owningInstitutionId = null;
        Integer cgdId = null;
        Map<String, Object> partialIndexMap;
        String coreName = solrCore;
        Integer totalBibsProcessed = 0;
        owningInstitutionId = commonUtil.findOwningInstitutionCode(owningInstitutionCode);
        cgdId = commonUtil.findcgdId(cgdCode);
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
            partialIndexMap = populatePartialIndexMap(solrIndexRequest, partialIndexType);

            Integer totalDocCount = getTotalDocCountForPartialIndex(partialIndexType, partialIndexMap);
            log.info("Total Document Count From DB : {}",totalDocCount);

            if (totalDocCount > 0) {
                int quotient = totalDocCount / (docsPerThread);
                int remainder = totalDocCount % (docsPerThread);
                Integer loopCount = remainder == 0 ? quotient : quotient + 1;
                log.info("Loop Count Value : {} ",loopCount);
                log.info("Commit Indexes Interval : {}",commitIndexesInterval);

                Integer callableCountByCommitInterval = commitIndexesInterval / (docsPerThread);
                if (callableCountByCommitInterval == 0) {
                    callableCountByCommitInterval = 1;
                }
                log.info("Number of callables to execute to commit indexes : {}",callableCountByCommitInterval);

                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                List<Callable<Integer>> callables = new ArrayList<>();
                for (int pageNum = 0; pageNum < loopCount; pageNum++) {
                    Callable callable = getCallable(coreName, pageNum, docsPerThread, owningInstitutionId, null, partialIndexType, partialIndexMap, cgdId);
                    callables.add(callable);
                }

                int futureCount = 0;
                List<List<Callable<Integer>>> partitions = Lists.partition(new ArrayList<>(callables), callableCountByCommitInterval);
                for (List<Callable<Integer>> partitionCallables : partitions) {
                    List<Future<Integer>> futures = executorService.invokeAll(partitionCallables);
                    log.info("No of Futures Added : {}",futures.size());
                    List<Future<Integer>> collectedFutures = futures.stream().map(future -> {
                        try {
                            future.get();
                            return future;
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }).collect(Collectors.toCollection(ArrayList::new));
                    log.info("No of Futures Collected : {}",collectedFutures.size());

                    int numOfBibsProcessed = 0;
                    for (Future<Integer> future : collectedFutures) {
                        try {
                            Integer entitiesCount = future.get();
                            numOfBibsProcessed += entitiesCount;
                            totalBibsProcessed += entitiesCount;
                            futureCount++;
                            log.info("Num of bibs fetched by thread : {}", entitiesCount);
                        } catch (InterruptedException | ExecutionException e) {
                            Thread.currentThread().interrupt();
                            log.error(ScsbCommonConstants.LOG_ERROR, e);
                        }
                    }
                    log.info("Num of Bibs Processed and indexed to core {} on commit interval : {} ",coreName,numOfBibsProcessed);
                    log.info("Total Num of Bibs Processed and indexed to core {} : {}",coreName,totalBibsProcessed);
                    Long solrBibCount = bibSolrCrudRepository.countByDocType(ScsbCommonConstants.BIB);
                    log.info("Total number of Bibs in Solr in recap core : {}",solrBibCount);
                }
                log.info("Total futures executed: {}",futureCount);
                stopWatch.stop();
                log.info("Time taken to fetch {} Bib Records and index to recap core : {} seconds",totalBibsProcessed,stopWatch.getTotalTimeSeconds());
                executorService.shutdown();
            } else {
                log.info("No records found to index for the criteria");
            }
        } catch (Exception e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        stopWatch1.stop();
        log.info("Total time taken:{} secs",stopWatch1.getTotalTimeSeconds());
        return totalBibsProcessed;
    }

    /**
     * This method populates the values for the partial index based on bibIdList or bibIdRange or dateRange.
     * @param solrIndexRequest
     * @param partialIndexType
     */
    private Map<String, Object> populatePartialIndexMap(SolrIndexRequest solrIndexRequest, String partialIndexType) throws ParseException {
        Map<String, Object> partialIndexMap = null;
        if(StringUtils.isNotBlank(partialIndexType)) {
            partialIndexMap = new HashMap<>();
            if(partialIndexType.equalsIgnoreCase(ScsbConstants.BIB_ID_LIST)) {
                String bibIds = solrIndexRequest.getBibIds();
                if(StringUtils.isNotBlank(bibIds)) {
                    String[] bibIdString = bibIds.split(",");
                    List<Integer> bibIdList = new ArrayList<>();
                    for(String bibId : bibIdString) {
                        bibIdList.add(Integer.valueOf(bibId));
                    }
                    partialIndexMap.put(ScsbConstants.BIB_ID_LIST, bibIdList);
                }
            } else if(partialIndexType.equalsIgnoreCase(ScsbConstants.BIB_ID_RANGE)) {
                if(StringUtils.isNotBlank(solrIndexRequest.getFromBibId()) && StringUtils.isNotBlank(solrIndexRequest.getToBibId())) {
                    partialIndexMap.put(ScsbConstants.BIB_ID_RANGE_FROM, solrIndexRequest.getFromBibId());
                    partialIndexMap.put(ScsbConstants.BIB_ID_RANGE_TO, solrIndexRequest.getToBibId());
                }
            } else if(partialIndexType.equalsIgnoreCase(ScsbConstants.DATE_RANGE)) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat(ScsbCommonConstants.INCREMENTAL_DATE_FORMAT);
                Date fromDate;
                Date toDate;
                if(StringUtils.isNotBlank(solrIndexRequest.getDateFrom())) {
                    fromDate = dateFormatter.parse(solrIndexRequest.getDateFrom());
                } else {
                    fromDate = dateUtil.getFromDate(new Date());
                }
                if(StringUtils.isNotBlank(solrIndexRequest.getDateTo())) {
                    toDate = dateFormatter.parse(solrIndexRequest.getDateTo());
                } else {
                    toDate = dateUtil.getToDate(new Date());
                }
                partialIndexMap.put(ScsbConstants.DATE_RANGE_FROM, fromDate);
                partialIndexMap.put(ScsbConstants.DATE_RANGE_TO, toDate);
            } else if(partialIndexType.equalsIgnoreCase(ScsbConstants.CGD_TYPE)){
                partialIndexMap.put(ScsbConstants.OWNING_INST,commonUtil.findOwningInstitutionCode(solrIndexRequest.getOwningInstitutionCode()));
                partialIndexMap.put(ScsbConstants.CGD,commonUtil.findcgdId(solrIndexRequest.getCgd()));
            }
        }
        return partialIndexMap;
    }

    /**
     * This method initiates solr indexing.
     *
     * @param solrIndexRequest the solr index request
     * @return integer
     */
    public Integer index(SolrIndexRequest solrIndexRequest) {
        return indexByOwningInstitutionId(solrIndexRequest);
    }

    /**
     * This method deletes the indexed data from the temporary cores after it is merged to main core.
     * @param coreNames
     * @param solrUrl
     */
    private static void deleteTempIndexes(List<String> coreNames, String solrUrl) {
        SolrTemplate solrTemplate = new SolrTemplate(new HttpSolrClient.Builder(solrUrl + File.separator).build());
        solrTemplate.setSolrConverter(new MappingSolrConverter(new SimpleSolrMappingContext()) {});
        for (Iterator<String> iterator = coreNames.iterator(); iterator.hasNext(); ) {
            String coreName = iterator.next();
            solrTemplate.delete(coreName, new SimpleQuery("*:*"));
            solrTemplate.commit(coreName);
            //BibCrudRepositoryMultiCoreSupport bibCrudRepositoryMultiCoreSupport = getBibCrudRepositoryMultiCoreSupport(solrUrl, coreName);
            //bibCrudRepositoryMultiCoreSupport.deleteAll();
        }
    }

    /**
     * To get the bib solr crud repository object based on the given core name for operations on that core.
     *
     * @param solrUrl  the solr url
     * @param coreName the core name
     * @return bib crud repository multi core support
     */
    protected BibCrudRepositoryMultiCoreSupport getBibCrudRepositoryMultiCoreSupport(String solrUrl, String coreName) {
        SolrTemplate solrTemplate = new SolrTemplate(new HttpSolrClient.Builder(solrUrl + File.separator + coreName).build());
        solrTemplate.setSolrConverter(new MappingSolrConverter(new SimpleSolrMappingContext()) {});
        return new BibCrudRepositoryMultiCoreSupport(solrTemplate, Bib.class);
    }

    /**
     * To create names for temporary cores.
     * @param numThreads
     * @param coreNames
     */
    private static void setupCoreNames(Integer numThreads, List<String> coreNames) {
        for (int i = 0; i < numThreads; i++) {
            coreNames.add("temp" + i);
        }
    }

    /**
     * Sets solr admin.
     *
     * @param solrAdmin the solr admin
     */
    public void setSolrAdmin(SolrAdmin solrAdmin) {
        this.solrAdmin = solrAdmin;
    }

    /**
     * This method gets the appropiate callable to be processed by the thread to generate solr input documents and index to solr.
     *
     * @param coreName            the core name
     * @param pageNum             the page num
     * @param docsPerpage         the docs perpage
     * @param owningInstitutionId the owning institution id
     * @param fromDate            the from date
     * @param partialIndexType    the partial index type
     * @param partialIndexMap     the partial index map
     * @return the callable
     */
    public abstract Callable getCallable(String coreName, int pageNum, int docsPerpage, Integer owningInstitutionId, Date fromDate, String partialIndexType, Map<String, Object> partialIndexMap,Integer cgdId);


    /**
     * This method gets the total doc count.
     *
     * @param owningInstitutionId the owning institution id
     * @param fromDate            the from date
     * @return the total doc count
     */
    protected abstract Integer getTotalDocCount(Integer owningInstitutionId, Date fromDate);

    /**
     * Gets total doc count for partial index.
     *
     * @param partialIndexType the partial index type
     * @param partialIndexMap  the partial index map
     * @return the total doc count for partial index
     */
    protected abstract Integer getTotalDocCountForPartialIndex(String partialIndexType, Map<String, Object> partialIndexMap);
}