package org.recap.executors;

import org.apache.camel.ProducerTemplate;
import org.apache.solr.common.SolrInputDocument;
import org.recap.RecapConstants;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.repository.jpa.BibliographicDetailsRepository;
import org.recap.repository.jpa.HoldingsDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by chenchulakshmig on 21/6/16.
 */
public class BibItemIndexCallable implements Callable {

    Logger logger = LoggerFactory.getLogger(BibItemIndexCallable.class);

    private final int pageNum;
    private final int docsPerPage;
    private String coreName;
    private String solrURL;
    private Integer owningInstitutionId;
    private BibliographicDetailsRepository bibliographicDetailsRepository;
    private HoldingsDetailsRepository holdingsDetailsRepository;
    private ProducerTemplate producerTemplate;
    private SolrTemplate solrTemplate;

    public BibItemIndexCallable(String solrURL, String coreName, int pageNum, int docsPerPage, BibliographicDetailsRepository bibliographicDetailsRepository, HoldingsDetailsRepository holdingsDetailsRepository, Integer owningInstitutionId, ProducerTemplate producerTemplate, SolrTemplate solrTemplate) {
        this.coreName = coreName;
        this.solrURL = solrURL;
        this.pageNum = pageNum;
        this.docsPerPage = docsPerPage;
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
        this.holdingsDetailsRepository = holdingsDetailsRepository;
        this.owningInstitutionId = owningInstitutionId;
        this.producerTemplate = producerTemplate;
        this.solrTemplate = solrTemplate;
    }

    @Override
    public Object call() throws Exception {

        Page<BibliographicEntity> bibliographicEntities = owningInstitutionId == null ?
                bibliographicDetailsRepository.findAllByIsDeletedFalse(new PageRequest(pageNum, docsPerPage)) :
                bibliographicDetailsRepository.findByOwningInstitutionIdAndIsDeletedFalse(new PageRequest(pageNum, docsPerPage), owningInstitutionId);

        logger.info("Num Bibs Fetched : " + bibliographicEntities.getNumberOfElements());
        Iterator<BibliographicEntity> iterator = bibliographicEntities.iterator();


        ExecutorService executorService = Executors.newFixedThreadPool(50);
        List<Future> futures = new ArrayList<>();
        while (iterator.hasNext()) {
            BibliographicEntity bibliographicEntity = iterator.next();
            Future submit = executorService.submit(new BibItemRecordSetupCallable(bibliographicEntity, solrTemplate, bibliographicDetailsRepository, holdingsDetailsRepository, producerTemplate));
            futures.add(submit);
        }

        logger.info("Num futures to prepare Bib and Associated data : " + futures.size());

        List<SolrInputDocument> solrInputDocumentsToIndex = new ArrayList<>();
        for (Iterator<Future> futureIterator = futures.iterator(); futureIterator.hasNext(); ) {
            try {
                Future future = futureIterator.next();
                SolrInputDocument solrInputDocument = (SolrInputDocument) future.get();
                if(solrInputDocument != null) solrInputDocumentsToIndex.add(solrInputDocument);
            } catch (Exception e) {
                logger.error("Exception : " + e.getMessage());
            }
        }

        executorService.shutdown();

        if (!CollectionUtils.isEmpty(solrInputDocumentsToIndex)) {
            producerTemplate.sendBodyAndHeader(RecapConstants.SOLR_QUEUE, solrInputDocumentsToIndex, RecapConstants.SOLR_CORE, coreName);
        }
        return solrInputDocumentsToIndex.size();
    }
}
