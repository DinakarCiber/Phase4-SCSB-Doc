package org.recap.executors;

import org.recap.admin.SolrAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by pvsubrah on 6/13/16.
 */

public abstract class IndexExecutorService {
    @Autowired
    SolrAdmin solrAdmin;

    @Value("${solr.url}")
    String solrUrl;

    @Value("${bib.rest.url}")
    public String bibResourceURL;

    @Value("${item.rest.url}")
    public String itemResourceURL;

    public void index(Integer numThreads, Integer docsPerThread) {

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        List<String> coreNames = new ArrayList<>();

        setupCoreNames(numThreads, coreNames);


        Integer totalDocCount = getTotalDocCount();

        int quotient = totalDocCount / (numThreads * docsPerThread);
        int remainder = totalDocCount % (numThreads * docsPerThread);

        Integer loopCount = remainder == 0 ? quotient : quotient + 1;

        int from=0;
        int to=docsPerThread-1;

        solrAdmin.createSolrCores(coreNames);

        for (int i = 0; i < loopCount; i++) {

            List<Future> futures = new ArrayList<>();
            for (int j = 0; j < numThreads; j++) {
                Callable callable = getCallable(coreNames.get(j), getResourceURL(), from, to);
                futures.add(executorService.submit(callable));
                from = to+1;
                to = from+docsPerThread-1;
            }

            for (Iterator<Future> iterator = futures.iterator(); iterator.hasNext(); ) {
                Future future = iterator.next();
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        solrAdmin.mergeCores(coreNames);
        stopWatch.stop();
        System.out.println("Time taken to merge cores: " + stopWatch.getTotalTimeSeconds());
        solrAdmin.unLoadCores(coreNames);
        executorService.shutdown();

    }

    private void setupCoreNames(Integer numThreads, List<String> coreNames) {
        for (int i = 0; i < numThreads; i++) {
            coreNames.add("temp" + i);
        }
    }

    public abstract Callable getCallable(String coreName, String resourceURL, int from, int to);

    protected abstract Integer getTotalDocCount();

    protected abstract String getResourceURL();
}
