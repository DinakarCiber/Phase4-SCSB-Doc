package org.recap.executors;

import com.google.common.collect.Lists;
import org.apache.camel.ProducerTemplate;
import org.recap.admin.SolrAdmin;
import org.recap.repository.jpa.InstitutionDetailsRepository;
import org.recap.repository.solr.main.BibSolrCrudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by angelind on 30/1/17.
 */
public abstract class MatchingIndexExecutorService {

    Logger logger = LoggerFactory.getLogger(MatchingIndexExecutorService.class);

    @Autowired
    SolrAdmin solrAdmin;

    @Autowired
    ProducerTemplate producerTemplate;

    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    BibSolrCrudRepository bibSolrCrudRepository;

    @Value("${solr.server.protocol}")
    String solrServerProtocol;

    @Value("${solr.parent.core}")
    String solrCore;

    @Value("${solr.url}")
    String solrUrl;

    @Value("${solr.router.uri.type}")
    String solrRouterURI;

    public Integer indexingForMatchingAlgorithm(String operationType) {
        StopWatch stopWatch1 = new StopWatch();
        stopWatch1.start();
        Integer numThreads = 5;
        Integer docsPerThread = 1000;
        Integer commitIndexesInterval = 10000;
        String coreName = solrCore;
        Integer totalBibsProcessed = 0;

        try {
            ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
            Integer totalDocCount = getTotalDocCount(operationType);
            if(totalDocCount > 0) {
                int quotient = totalDocCount / (docsPerThread);
                int remainder = totalDocCount % (docsPerThread);
                Integer loopCount = remainder == 0 ? quotient : quotient + 1;
                logger.info("Loop Count Value : " + loopCount);
                logger.info("Commit Indexes Interval : " + commitIndexesInterval);
                Integer callableCountByCommitInterval = commitIndexesInterval / (docsPerThread);
                if (callableCountByCommitInterval == 0) {
                    callableCountByCommitInterval = 1;
                }
                logger.info("Number of callables to execute to commit indexes : " + callableCountByCommitInterval);

                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                List<Callable<Integer>> callables = new ArrayList<>();
                for (int pageNum = 0; pageNum < loopCount; pageNum++) {
                    Callable callable = getCallable(coreName, pageNum, docsPerThread, operationType);
                    callables.add(callable);
                }

                int futureCount = 0;
                List<List<Callable<Integer>>> partitions = Lists.partition(new ArrayList<Callable<Integer>>(callables), callableCountByCommitInterval);
                for (List<Callable<Integer>> partitionCallables : partitions) {
                    int numOfBibsProcessed = 0;
                    for (Iterator<Callable<Integer>> iterator = partitionCallables.iterator(); iterator.hasNext(); ) {
                        Callable<Integer> callable = iterator.next();
                        Future<Integer> future = executorService.submit(callable);
                        Integer entitiesCount = (Integer) future.get();
                        numOfBibsProcessed += entitiesCount;
                        totalBibsProcessed += entitiesCount;
                        logger.info("Num of bibs fetched by thread : " + entitiesCount);
                        futureCount++;
                    }

                    logger.info("Num of Bibs Processed and indexed to core " + coreName + " on commit interval : " + numOfBibsProcessed);
                    logger.info("Total Num of Bibs Processed and indexed to core " + coreName + " : " + totalBibsProcessed);
                }
                logger.info("Total futures executed: " + futureCount);
                stopWatch.stop();
                logger.info("Time taken to fetch " + totalBibsProcessed + " Bib Records and index to recap core : " + stopWatch.getTotalTimeSeconds() + " seconds");
                executorService.shutdown();
            } else {
                logger.info("No records found to index for the criteria");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopWatch1.stop();
        logger.info("Total time taken:" + stopWatch1.getTotalTimeSeconds() + " secs");
        return totalBibsProcessed;
    }

    public abstract Callable getCallable(String coreName, int pageNum, int docsPerpage, String operationType);

    protected abstract Integer getTotalDocCount(String operationType);
}
