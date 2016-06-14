package org.recap;

import org.junit.Before;
import org.junit.Test;
import org.recap.model.Bib;
import org.recap.repository.main.BibCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;

/**
 * Created by pvsubrah on 6/11/16.
 */
public class BibIndexerTest extends BaseTestCase {

    @Autowired
    BibCrudRepository bibCrudRepository;


    @Before
    public void setUp() throws Exception {
        assertNotNull(bibCrudRepository);
        bibCrudRepository.deleteAll();
    }

    @Test
    public void indexBib() throws Exception {
        Bib bib = new Bib();
        bib.setId(1L);
        Bib indexedBib = bibCrudRepository.save(bib);

        assertNotNull(indexedBib);

        Iterable<Bib> bibs = bibCrudRepository.findAll();
        assertNotNull(bibs);

    }
}
