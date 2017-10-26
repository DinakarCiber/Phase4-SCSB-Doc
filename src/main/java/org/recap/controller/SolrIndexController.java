package org.recap.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.recap.RecapConstants;
import org.recap.admin.SolrAdmin;
import org.recap.executors.BibItemIndexExecutorService;
import org.recap.model.solr.SolrIndexRequest;
import org.recap.repository.solr.main.BibSolrCrudRepository;
import org.recap.repository.solr.main.HoldingsSolrCrudRepository;
import org.recap.repository.solr.main.ItemCrudRepository;
import org.recap.service.accession.SolrIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.InstanceFilter;
import org.springframework.util.StopWatch;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Sheik on 6/18/2016.
 */
@Controller
public class SolrIndexController {

    private static final Logger logger = LoggerFactory.getLogger(SolrIndexController.class);

    @Autowired
    private BibItemIndexExecutorService bibItemIndexExecutorService;

    @Autowired
    private BibSolrCrudRepository bibSolrCrudRepository;

    @Autowired
    private ItemCrudRepository itemCrudRepository;

    @Autowired
    private HoldingsSolrCrudRepository holdingsSolrCrudRepository;

    @Autowired
    private SolrAdmin solrAdmin;

    @Value("${commit.indexes.interval}")
    private Integer commitIndexesInterval;

    @Autowired
    private SolrIndexService solrIndexService;

    @Value("${solr.parent.core}")
    private String solrCore;

    /**
     * To initialize solr indexing ui page.
     *
     * @param model the model
     * @return the string
     */
    @RequestMapping("/")
    public String solrIndexer(Model model){
        model.addAttribute("solrIndexRequest",new SolrIndexRequest());
        model.addAttribute("matchingAlgoDate", "");
        return "solrIndexer";
    }

    /**
     * This method is used to perform full index and incremental indexing through ui.
     *
     * @param solrIndexRequest the solr index request
     * @param result           the result
     * @param model            the model
     * @return the string
     * @throws Exception the exception
     */
    @ResponseBody
    @RequestMapping(value = "/solrIndexer/fullIndex", method = RequestMethod.POST)
    public String fullIndex(@Valid @ModelAttribute("solrIndexRequest") SolrIndexRequest solrIndexRequest,
                            BindingResult result,
                            Model model) throws Exception {
        String docType = solrIndexRequest.getDocType();
        Integer numberOfThread = solrIndexRequest.getNumberOfThreads();
        Integer numberOfDoc = solrIndexRequest.getNumberOfDocs();
        String owningInstitutionCode = solrIndexRequest.getOwningInstitutionCode();
        if (solrIndexRequest.getCommitInterval() == null) {
            solrIndexRequest.setCommitInterval(commitIndexesInterval);
        }
        Integer commitInterval = solrIndexRequest.getCommitInterval();

        logger.info("Document Type : {} Number of Threads : {} Number of Docs : {} Commit Interval : {} From Date : {}",docType,numberOfThread,numberOfDoc,commitInterval,solrIndexRequest.getDateFrom());

        if (solrIndexRequest.isDoClean()) {
            if(StringUtils.isNotBlank(owningInstitutionCode)) {
                bibSolrCrudRepository.deleteByOwningInstitution(owningInstitutionCode);
                holdingsSolrCrudRepository.deleteByOwningInstitution(owningInstitutionCode);
                itemCrudRepository.deleteByOwningInstitution(owningInstitutionCode);
            } else {
                bibSolrCrudRepository.deleteAll();
                holdingsSolrCrudRepository.deleteAll();
                itemCrudRepository.deleteAll();
            }
            try {
                solrAdmin.unloadTempCores();
            } catch (IOException | SolrServerException e) {
                logger.error(RecapConstants.LOG_ERROR,e);
            }
        }

        Integer totalProcessedRecords = bibItemIndexExecutorService.index(solrIndexRequest);
        String status = "Total number of records processed : " + totalProcessedRecords;

        return report(status);
    }

    /**
     * Partial index string.
     *
     * @param solrIndexRequest the solr index request
     * @param result           the result
     * @param model            the model
     * @return the string
     * @throws Exception the exception
     */
    @ResponseBody
    @RequestMapping(value = "/solrIndexer/partialIndex", method = RequestMethod.POST)
    public String partialIndex(@Valid @ModelAttribute("solrIndexRequest") SolrIndexRequest solrIndexRequest,
                            BindingResult result,
                            Model model) throws Exception {
        Integer numberOfThread = solrIndexRequest.getNumberOfThreads();
        Integer numberOfDoc = solrIndexRequest.getNumberOfDocs();
        if (solrIndexRequest.getCommitInterval() == null) {
            solrIndexRequest.setCommitInterval(commitIndexesInterval);
        }
        Integer commitInterval = solrIndexRequest.getCommitInterval();

        logger.info("Number of Threads : {} Number of Docs : {} Commit Interval : {} From Date : {}",numberOfThread,numberOfDoc,commitInterval,solrIndexRequest.getDateFrom());

        Integer totalProcessedRecords = bibItemIndexExecutorService.partialIndex(solrIndexRequest);
        String status = "Total number of records processed : " + totalProcessedRecords;

        return report(status);
    }

    /**
     * This method is used to get the status of the report.
     *
     * @param status the status
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/solrIndexer/report", method = RequestMethod.GET)
    public String report(String status) {
        return StringUtils.isBlank(status) ? "Index process initiated!" : status;
    }

    /**
     * This method is used to perform indexing by using bibliographic id.
     *
     * @param bibliographicIdList the bibliographic id list
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/solrIndexer/indexByBibliographicId", method = RequestMethod.POST)
    public String indexByBibliographicId(@RequestBody List<Integer> bibliographicIdList) {
        String response = null;
        try {
            for (Integer bibliographicId : bibliographicIdList) {
                getSolrIndexService().indexByBibliographicId(bibliographicId);
            }
            response = RecapConstants.SUCCESS;
        } catch (Exception e) {
            response = RecapConstants.FAILURE;
            logger.error(RecapConstants.LOG_ERROR,e);
        }
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/solrIndexer/indexByOwningInstBibliographicIdList", method = RequestMethod.POST)
    public String indexByOwningInstBibliographicIdList(@RequestParam Map<String,Object> requestParameters) {
        String ownInstbibliographicIdListString = (String)requestParameters.get(RecapConstants.OWN_INST_BIBID_LIST);
        ownInstbibliographicIdListString = ownInstbibliographicIdListString.replace("[","");
        ownInstbibliographicIdListString = ownInstbibliographicIdListString.replace("]","");
        ownInstbibliographicIdListString = ownInstbibliographicIdListString.replace("\"","");
        logger.info("ownInstbibliographicIdListString--->{}",ownInstbibliographicIdListString);
        String[] ownInstbibliographicIdArray = ownInstbibliographicIdListString.split(",");
        List<String> ownInstbibliographicIdList = Arrays.asList(ownInstbibliographicIdArray);
        Integer owningInstId = Integer.valueOf((String) requestParameters.get(RecapConstants.OWN_INSTITUTION_ID));
        String response = null;
        try {
            getSolrIndexService().indexByOwnInstBibId(ownInstbibliographicIdList,owningInstId);
            response = RecapConstants.SUCCESS;
        } catch (Exception e) {
            response = RecapConstants.FAILURE;
            logger.error(RecapConstants.LOG_ERROR,e);
        }
        return response;
    }

    /**
     * This method is used to delete records by bib,holding and item id.
     *
     * @param idMapToRemoveIndexList the id list of map to remove index
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/solrIndexer/deleteByBibHoldingItemId", method = RequestMethod.POST)
    public String deleteByBibHoldingItemId(@RequestBody List<Map<String,String>> idMapToRemoveIndexList) {
        String response = null;
        logger.info("idMapToRemoveIndexList size--->{}",idMapToRemoveIndexList.size());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (Map<String,String> idMapToRemoveIndex : idMapToRemoveIndexList) {
            StopWatch stopWatchDeleteDummyRec = new StopWatch();
            stopWatchDeleteDummyRec.start();
            String bibliographicId = idMapToRemoveIndex.get("BibId");
            String holdingId = idMapToRemoveIndex.get("HoldingId");
            String itemId = idMapToRemoveIndex.get("ItemId");
            logger.info("deleting dummy record from solr bib id - {}, holding id - {}, item id {}",bibliographicId,holdingId,itemId);
            try {
                getSolrIndexService().deleteByDocId(RecapConstants.BIB_ID,bibliographicId);
                getSolrIndexService().deleteByDocId(RecapConstants.HOLDING_ID,holdingId);
                getSolrIndexService().deleteByDocId(RecapConstants.ITEM_ID,itemId);
                response = RecapConstants.SUCCESS;
            } catch (Exception e) {
                response = RecapConstants.FAILURE;
                logger.error(RecapConstants.LOG_ERROR,e);
            }
            stopWatchDeleteDummyRec.stop();
            logger.info("Time taken to delete  bib id - {}, holding id - {}, item id {}--> is {} milli sec",bibliographicId,holdingId,itemId,stopWatchDeleteDummyRec.getTotalTimeMillis());
        }
        stopWatch.stop();
        logger.info("Total time to delete dummy record from solr--->{} milli sec",stopWatch.getTotalTimeMillis());
        return response;
    }

    /**
     * This method gets solr index service.
     *
     * @return the solr index service
     */
    public SolrIndexService getSolrIndexService() {
        return solrIndexService;
    }

    /**
     * This method sets solr index service.
     *
     * @param solrIndexService the solr index service
     */
    public void setSolrIndexService(SolrIndexService solrIndexService) {
        this.solrIndexService = solrIndexService;
    }
}
