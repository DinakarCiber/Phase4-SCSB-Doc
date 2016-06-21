package org.recap.executors;

import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.BibliographicHoldingsEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.model.solr.Bib;
import org.recap.model.solr.Item;
import org.recap.repository.jpa.BibliographicDetailsRepository;
import org.recap.repository.solr.temp.BibCrudRepositoryMultiCoreSupport;
import org.recap.repository.solr.temp.ItemCrudRepositoryMultiCoreSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.CollectionUtils;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by pvsubrah on 6/13/16.
 */


public class BibIndexCallable implements Callable {
    private final int pageNum;
    private final int docsPerPage;
    private String coreName;
    private String solrURL;
    private Integer owningInstitutionId;
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    private BibCrudRepositoryMultiCoreSupport bibCrudRepositoryMultiCoreSupport;

    public BibIndexCallable(String solrURL, String coreName, int pageNum, int docsPerPage, BibliographicDetailsRepository bibliographicDetailsRepository, Integer owningInstitutionId) {
        this.coreName = coreName;
        this.solrURL = solrURL;
        this.pageNum = pageNum;
        this.docsPerPage = docsPerPage;
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
        this.owningInstitutionId = owningInstitutionId;
    }

    @Override
    public Object call() throws Exception {

        Page<BibliographicEntity> bibliographicEntities = owningInstitutionId == null ?
                bibliographicDetailsRepository.findAll(new PageRequest(pageNum, docsPerPage)) :
                bibliographicDetailsRepository.findByOwningInstitutionId(new PageRequest(pageNum, docsPerPage), owningInstitutionId);

        List<Bib> bibsToIndex = new ArrayList<>();

        Iterator<BibliographicEntity> iterator = bibliographicEntities.iterator();


        ExecutorService executorService = Executors.newFixedThreadPool(50);
        List<Future> futures = new ArrayList<>();
        while(iterator.hasNext()){
            BibliographicEntity bibliographicEntity = iterator.next();

            List<BibliographicHoldingsEntity> bibliographicHoldingsEntities = bibliographicEntity.getBibliographicHoldingsEntities();

            List<HoldingsEntity> holdingsEntities = new ArrayList<>();

            List<ItemEntity> itemEntities = new ArrayList<>();

            for (Iterator<BibliographicHoldingsEntity> bibliographicHoldingsEntityIterator = bibliographicHoldingsEntities.iterator(); bibliographicHoldingsEntityIterator.hasNext(); ) {
                BibliographicHoldingsEntity bibliographicHoldingsEntity = bibliographicHoldingsEntityIterator.next();
                HoldingsEntity holdingsEntity = bibliographicHoldingsEntity.getHoldingsEntity();
                holdingsEntities.add(holdingsEntity);
                for (Iterator<ItemEntity> itemEntityIterator = holdingsEntity.getItemEntities().iterator(); itemEntityIterator.hasNext(); ) {
                    ItemEntity itemEntity = itemEntityIterator.next();
                    itemEntities.add(itemEntity);
                }
            }
            Future submit = executorService.submit(new BibRecordSetupCallable(bibliographicEntity, holdingsEntities, itemEntities));
            futures.add(submit);
        }

        for (Iterator<Future> futureIterator = futures.iterator(); futureIterator.hasNext(); ) {
            Future future = futureIterator.next();

            Bib bib = (Bib) future.get();
            bibsToIndex.add(bib);
        }

        executorService.shutdown();

        bibCrudRepositoryMultiCoreSupport = new BibCrudRepositoryMultiCoreSupport(coreName, solrURL);
        if (!CollectionUtils.isEmpty(bibsToIndex)) {
            bibCrudRepositoryMultiCoreSupport.save(bibsToIndex);
        }
        return bibliographicEntities.getSize();
    }
}
