package org.recap.util;

import org.apache.commons.io.FileUtils;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemChangeLogEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.model.reports.ReportsRequest;
import org.recap.model.reports.ReportsResponse;
import org.recap.model.search.DeaccessionItemResultsRow;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Created by rajeshbabuk on 13/1/17.
 */
public class ReportsServiceUtilUT extends BaseTestCase {

    @Autowired
    ReportsServiceUtil reportsServiceUtil;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    public void populateAccessionDeaccessionItemCounts() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String requestedFromDate = simpleDateFormat.format(new Date());
        String requestedToDate = simpleDateFormat.format(new Date());

        ReportsRequest reportsRequest = new ReportsRequest();
        reportsRequest.setAccessionDeaccessionFromDate(requestedFromDate);
        reportsRequest.setAccessionDeaccessionToDate(requestedToDate);
        reportsRequest.setOwningInstitutions(Arrays.asList("CUL", "PUL", "NYPL"));
        reportsRequest.setCollectionGroupDesignations(Arrays.asList("Private", "Open", "Shared"));

        BibliographicEntity bibliographicEntity = saveBibHoldingItemEntity(1, 2, false);
        indexBibHoldingItem(bibliographicEntity);

        BibliographicEntity bibliographicEntity1 = saveBibHoldingItemEntity(2, 2, false);
        indexBibHoldingItem(bibliographicEntity1);

        BibliographicEntity bibliographicEntity2 = saveBibHoldingItemEntity(3, 2, false);
        indexBibHoldingItem(bibliographicEntity2);

        BibliographicEntity bibliographicEntity3 = saveBibHoldingItemEntity(1, 1, false);
        indexBibHoldingItem(bibliographicEntity3);

        BibliographicEntity bibliographicEntity4 = saveBibHoldingItemEntity(2, 1, false);
        indexBibHoldingItem(bibliographicEntity4);

        BibliographicEntity bibliographicEntity5 = saveBibHoldingItemEntity(3, 1, false);
        indexBibHoldingItem(bibliographicEntity5);

        BibliographicEntity bibliographicEntity6 = saveBibHoldingItemEntity(1, 3, false);
        indexBibHoldingItem(bibliographicEntity6);

        BibliographicEntity bibliographicEntity7 = saveBibHoldingItemEntity(2, 3, false);
        indexBibHoldingItem(bibliographicEntity7);

        BibliographicEntity bibliographicEntity8 = saveBibHoldingItemEntity(3, 3, false);
        indexBibHoldingItem(bibliographicEntity8);

        BibliographicEntity bibliographicEntity9 = saveBibHoldingItemEntity(1, 2, true);
        indexBibHoldingItem(bibliographicEntity9);

        BibliographicEntity bibliographicEntity10 = saveBibHoldingItemEntity(2, 2, true);
        indexBibHoldingItem(bibliographicEntity10);

        BibliographicEntity bibliographicEntity11 = saveBibHoldingItemEntity(3, 2, true);
        indexBibHoldingItem(bibliographicEntity11);

        BibliographicEntity bibliographicEntity12 = saveBibHoldingItemEntity(1, 1, true);
        indexBibHoldingItem(bibliographicEntity12);

        BibliographicEntity bibliographicEntity13 = saveBibHoldingItemEntity(2, 1, true);
        indexBibHoldingItem(bibliographicEntity13);

        BibliographicEntity bibliographicEntity14 = saveBibHoldingItemEntity(3, 1, true);
        indexBibHoldingItem(bibliographicEntity14);

        BibliographicEntity bibliographicEntity15 = saveBibHoldingItemEntity(1, 3, true);
        indexBibHoldingItem(bibliographicEntity15);

        BibliographicEntity bibliographicEntity16 = saveBibHoldingItemEntity(2, 3, true);
        indexBibHoldingItem(bibliographicEntity16);

        BibliographicEntity bibliographicEntity17 = saveBibHoldingItemEntity(3, 3, true);
        indexBibHoldingItem(bibliographicEntity17);

        ReportsResponse reportsResponse = reportsServiceUtil.populateAccessionDeaccessionItemCounts(reportsRequest);
        assertNotNull(reportsResponse);
        assertEquals(1, reportsResponse.getAccessionOpenPulCount());
        assertEquals(1, reportsResponse.getAccessionOpenCulCount());
        assertEquals(1, reportsResponse.getAccessionOpenNyplCount());
        assertEquals(1, reportsResponse.getAccessionSharedPulCount());
        assertEquals(1, reportsResponse.getAccessionSharedCulCount());
        assertEquals(1, reportsResponse.getAccessionSharedNyplCount());
        assertEquals(1, reportsResponse.getAccessionPrivatePulCount());
        assertEquals(1, reportsResponse.getAccessionPrivateCulCount());
        assertEquals(1, reportsResponse.getAccessionPrivateNyplCount());
        assertEquals(1, reportsResponse.getDeaccessionOpenPulCount());
        assertEquals(1, reportsResponse.getDeaccessionOpenCulCount());
        assertEquals(1, reportsResponse.getDeaccessionOpenNyplCount());
        assertEquals(1, reportsResponse.getDeaccessionSharedPulCount());
        assertEquals(1, reportsResponse.getDeaccessionSharedCulCount());
        assertEquals(1, reportsResponse.getDeaccessionSharedNyplCount());
        assertEquals(1, reportsResponse.getDeaccessionPrivatePulCount());
        assertEquals(1, reportsResponse.getDeaccessionPrivateCulCount());
        assertEquals(1, reportsResponse.getDeaccessionPrivateNyplCount());
    }

    @Test
    public void populateCGDItemCounts() throws Exception {
        ReportsRequest reportsRequest = new ReportsRequest();
        reportsRequest.setOwningInstitutions(Arrays.asList("CUL", "PUL", "NYPL"));
        reportsRequest.setCollectionGroupDesignations(Arrays.asList("Private", "Open", "Shared"));

        BibliographicEntity bibliographicEntity = saveBibHoldingItemEntity(1, 2, false);
        indexBibHoldingItem(bibliographicEntity);

        BibliographicEntity bibliographicEntity1 = saveBibHoldingItemEntity(2, 2, false);
        indexBibHoldingItem(bibliographicEntity1);

        BibliographicEntity bibliographicEntity2 = saveBibHoldingItemEntity(3, 2, false);
        indexBibHoldingItem(bibliographicEntity2);

        BibliographicEntity bibliographicEntity3 = saveBibHoldingItemEntity(1, 1, false);
        indexBibHoldingItem(bibliographicEntity3);

        BibliographicEntity bibliographicEntity4 = saveBibHoldingItemEntity(2, 1, false);
        indexBibHoldingItem(bibliographicEntity4);

        BibliographicEntity bibliographicEntity5 = saveBibHoldingItemEntity(3, 1, false);
        indexBibHoldingItem(bibliographicEntity5);

        BibliographicEntity bibliographicEntity6 = saveBibHoldingItemEntity(1, 3, false);
        indexBibHoldingItem(bibliographicEntity6);

        BibliographicEntity bibliographicEntity7 = saveBibHoldingItemEntity(2, 3, false);
        indexBibHoldingItem(bibliographicEntity7);

        BibliographicEntity bibliographicEntity8 = saveBibHoldingItemEntity(3, 3, false);
        indexBibHoldingItem(bibliographicEntity8);

        ReportsResponse reportsResponse = reportsServiceUtil.populateCgdItemCounts(reportsRequest);
        assertNotNull(reportsResponse);
        assertEquals(1, reportsResponse.getOpenPulCgdCount());
        assertEquals(1, reportsResponse.getOpenCulCgdCount());
        assertEquals(1, reportsResponse.getOpenNyplCgdCount());
        assertEquals(1, reportsResponse.getSharedPulCgdCount());
        assertEquals(1, reportsResponse.getSharedCulCgdCount());
        assertEquals(1, reportsResponse.getSharedNyplCgdCount());
        assertEquals(1, reportsResponse.getPrivatePulCgdCount());
        assertEquals(1, reportsResponse.getPrivateCulCgdCount());
        assertEquals(1, reportsResponse.getPrivateNyplCgdCount());
    }

    @Test
    public void populateDeaccessionResults() throws Exception {
        ReportsRequest reportsRequest = new ReportsRequest();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        reportsRequest.setAccessionDeaccessionFromDate(simpleDateFormat.format(new Date()));
        reportsRequest.setAccessionDeaccessionToDate(simpleDateFormat.format(new Date()));
        reportsRequest.setDeaccessionOwningInstitution("PUL");

        BibliographicEntity bibliographicEntity = saveBibHoldingItemEntity(1, 2, true);
        indexBibHoldingItem(bibliographicEntity);

        ReportsResponse reportsResponse = reportsServiceUtil.populateDeaccessionResults(reportsRequest);
        assertNotNull(reportsResponse);
        assertNotNull(reportsResponse.getDeaccessionItemResultsRows());
        assertTrue(reportsResponse.getDeaccessionItemResultsRows().size() > 0);
        List<DeaccessionItemResultsRow> deaccessionItemResultsRows = reportsResponse.getDeaccessionItemResultsRows();
        assertNotNull(deaccessionItemResultsRows);
        assertTrue(deaccessionItemResultsRows.size() > 0);
    }

    private BibliographicEntity saveBibHoldingItemEntity(Integer owningInstitutionId, Integer collectionGroupId, boolean isDeleted) throws Exception {
        Random random = new Random();

        File bibContentFile = getBibContentFile();
        File holdingsContentFile = getHoldingsContentFile();
        String sourceBibContent = FileUtils.readFileToString(bibContentFile, "UTF-8");
        String sourceHoldingsContent = FileUtils.readFileToString(holdingsContentFile, "UTF-8");

        String owningInstitutionBibId = String.valueOf(random.nextInt());
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setBibliographicId(new Random().nextInt());
        bibliographicEntity.setContent(sourceBibContent.getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setCreatedBy("ut");
        bibliographicEntity.setLastUpdatedBy("ut");
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setOwningInstitutionBibId(owningInstitutionBibId);
        bibliographicEntity.setOwningInstitutionId(owningInstitutionId);

        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent(sourceHoldingsContent.getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("ut");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setLastUpdatedBy("ut");
        holdingsEntity.setOwningInstitutionId(owningInstitutionId);
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(new Random().nextInt()));

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setItemId(new Random().nextInt());
        itemEntity.setBarcode("b3");
        itemEntity.setCustomerCode("c1");
        itemEntity.setCallNumber("cn1");
        itemEntity.setCallNumberType("ct1");
        itemEntity.setItemAvailabilityStatusId(1);
        itemEntity.setCopyNumber(1);
        itemEntity.setOwningInstitutionId(owningInstitutionId);
        itemEntity.setCollectionGroupId(collectionGroupId);
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("ut");
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setLastUpdatedBy("ut");
        itemEntity.setUseRestrictions("no");
        itemEntity.setVolumePartYear("v3");
        itemEntity.setOwningInstitutionItemId(String.valueOf(new Random().nextInt()));
        itemEntity.setDeleted(isDeleted);

        itemEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        holdingsEntity.setItemEntities(Arrays.asList(itemEntity));

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);
        return savedBibliographicEntity;
    }

    private void indexBibHoldingItem(BibliographicEntity savedBibliographicEntity) {
        BibJSONUtil bibJSONUtil = new BibJSONUtil();
        SolrInputDocument solrInputDocument = bibJSONUtil.generateBibAndItemsForIndex(savedBibliographicEntity, solrTemplate, bibliographicDetailsRepository, holdingDetailRepository);
        solrTemplate.saveDocument(solrInputDocument);
        solrTemplate.commit();
    }

    public File getBibContentFile() throws URISyntaxException {
        URL resource = getClass().getResource("BibContent.xml");
        return new File(resource.toURI());
    }

    public File getHoldingsContentFile() throws URISyntaxException {
        URL resource = getClass().getResource("HoldingsContent.xml");
        return new File(resource.toURI());
    }

}
