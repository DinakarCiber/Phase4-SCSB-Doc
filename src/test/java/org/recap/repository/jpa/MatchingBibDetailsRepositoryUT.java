package org.recap.repository.jpa;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.RecapConstants;
import org.recap.model.jpa.MatchingBibEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by angelind on 1/11/16.
 */
public class MatchingBibDetailsRepositoryUT extends BaseTestCase{

    @Autowired
    MatchingBibDetailsRepository matchingBibDetailsRepository;

    @Test
    public void saveMatchingBibEntity() throws Exception {
        MatchingBibEntity savedMatchingBibEntity = saveMatchingBibEntity(RecapConstants.MATCH_POINT_FIELD_OCLC);
        assertNotNull(savedMatchingBibEntity.getId());
        MatchingBibEntity bibEntity = matchingBibDetailsRepository.findOne(savedMatchingBibEntity.getId());
        assertNotNull(bibEntity);
    }

    private MatchingBibEntity saveMatchingBibEntity(String matchingCriteria) {
        MatchingBibEntity matchingBibEntity = new MatchingBibEntity();
        matchingBibEntity.setBibId(1);
        matchingBibEntity.setOwningInstitution("NYPL");
        matchingBibEntity.setOwningInstBibId("N1029");
        matchingBibEntity.setTitle("Middleware for ReCAP");
        matchingBibEntity.setOclc("129393");
        matchingBibEntity.setIsbn("93930");
        matchingBibEntity.setIssn("12283");
        matchingBibEntity.setLccn("039329");
        matchingBibEntity.setMaterialType("monograph");
        matchingBibEntity.setMatching(matchingCriteria);
        matchingBibEntity.setRoot("31");
        return matchingBibDetailsRepository.save(matchingBibEntity);
    }

    @Test
    public void getMultipleMatchPointBibEntity() throws Exception {
        saveMatchingBibEntity(RecapConstants.MATCH_POINT_FIELD_OCLC);
        saveMatchingBibEntity(RecapConstants.MATCH_POINT_FIELD_ISBN);
        List<MatchingBibEntity> multipleMatchPointBibEntity = matchingBibDetailsRepository.getBibEntityBasedOnBibIds(Arrays.asList(1));
        assertNotNull(multipleMatchPointBibEntity);
        assertTrue(multipleMatchPointBibEntity.size() == 2);
    }

    @Test
    public void getMultipleMatchedBibIdsBasedOnLimit() throws Exception {
        saveMatchingBibEntity(RecapConstants.MATCH_POINT_FIELD_OCLC);
        saveMatchingBibEntity(RecapConstants.MATCH_POINT_FIELD_ISBN);
        List<Integer> multipleMatchedBibIdsBasedOnLimit = matchingBibDetailsRepository.getMultipleMatchedBibIdsBasedOnLimit(0, 1);
        assertNotNull(multipleMatchedBibIdsBasedOnLimit);
        assertTrue(multipleMatchedBibIdsBasedOnLimit.size() == 1);
    }

    @Test
    public void getMultipleMatchUniqueBibCount() throws Exception {
        saveMatchingBibEntity(RecapConstants.MATCH_POINT_FIELD_OCLC);
        saveMatchingBibEntity(RecapConstants.MATCH_POINT_FIELD_ISBN);
        long multipleMatchUniqueBibCount = matchingBibDetailsRepository.getMultipleMatchUniqueBibCount();
        assertNotNull(multipleMatchUniqueBibCount);
        assertTrue(multipleMatchUniqueBibCount > 0);
    }

    @Test
    public void getSingleMatchBibCountBasedOnMatching() throws Exception {
        saveMatchingBibEntity(RecapConstants.MATCH_POINT_FIELD_OCLC);
        long multipleMatchUniqueBibCount = matchingBibDetailsRepository.getSingleMatchBibCountBasedOnMatching(RecapConstants.MATCH_POINT_FIELD_OCLC);
        assertNotNull(multipleMatchUniqueBibCount);
        assertTrue(multipleMatchUniqueBibCount > 0);
    }

    @Test
    public void getMultiMatchBibEntitiesForMatchCriterias() throws Exception {
        saveMatchingBibEntity(RecapConstants.OCLC_CRITERIA);
        saveMatchingBibEntity(RecapConstants.ISBN_CRITERIA);
        Page<MatchingBibEntity> multiMatchBibEntitiesForMatchCriterias = matchingBibDetailsRepository.getMultiMatchBibEntitiesForOCLCAndISBN(new PageRequest(0, 10), RecapConstants.OCLC_CRITERIA, RecapConstants.ISBN_CRITERIA);
        assertNotNull(multiMatchBibEntitiesForMatchCriterias);
        assertTrue(multiMatchBibEntitiesForMatchCriterias.getTotalElements() > 0);
    }

}