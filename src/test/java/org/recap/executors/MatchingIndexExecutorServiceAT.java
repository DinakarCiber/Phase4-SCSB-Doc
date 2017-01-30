package org.recap.executors;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.RecapConstants;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertTrue;

/**
 * Created by angelind on 30/1/17.
 */
public class MatchingIndexExecutorServiceAT extends BaseTestCase{

    @Autowired
    MatchingBibItemIndexExecutorService matchingBibItemIndexExecutorService;

    @Test
    public void indexingForMatchingAlgorithmTest() {
        Integer count = matchingBibItemIndexExecutorService.indexingForMatchingAlgorithm(RecapConstants.MATCHING_OPERATION_TYPE);
        assertTrue(count > 0);
    }

}