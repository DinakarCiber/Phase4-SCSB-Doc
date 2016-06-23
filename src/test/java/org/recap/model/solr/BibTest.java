package org.recap.model.solr;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.recap.BaseTestCase;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;

public class BibTest extends BaseTestCase {

    @Before
    public void setUp() throws Exception {
        assertNotNull(bibCrudRepository);
        bibCrudRepository.deleteAll();
    }

    @Test
    public void indexBib() throws Exception {

        List<String> issnList = new ArrayList<>();
        List<String>isbnList = new ArrayList<>();
        List<String> oclcNumberList = new ArrayList<>();
        List<String> holdingsIdList = new ArrayList<>();
        List<String> itemIdList = new ArrayList<>();
        issnList.add("0394469756");
        isbnList.add("0394469755");
        oclcNumberList.add("00133182");
        oclcNumberList.add("00440790");
        holdingsIdList.add("201");
        holdingsIdList.add("202");
        itemIdList.add("301");
        itemIdList.add("302");

        Bib bib = new Bib();
        bib.setBibId("101");
        bib.setDocType("Bibliographic");
        bib.setTitle("Middleware for ReCAP");
        bib.setBarcode("1");
        bib.setTitle("Test Bib 1");
        bib.setAuthor("Hoepli, Nancy L");
        bib.setPublisher("McClelland & Stewart, limited");
        bib.setImprint("Toronto, McClelland & Stewart, limited [c1926]");
        bib.setIssn(issnList);
        bib.setIsbn(isbnList);
        bib.setOclcNumber(oclcNumberList);
        bib.setPublicationDate("1960");
        bib.setMaterialType("Material Type 1");
        bib.setNotes("Bibliographical footnotes 1");
        bib.setOwningInstitution("PUL");
        bib.setSubject("Arab countries Politics and government.");
        bib.setPublicationPlace("Paris");
        bib.setLccn("71448228");
        bib.setHoldingsIdList(holdingsIdList);
        bib.setBibItemIdList(itemIdList);
        Bib indexedBib = bibCrudRepository.save(bib);
        assertNotNull(indexedBib);

        Bib searchBib = bibCrudRepository.findByBarcode(indexedBib.getBarcode());
        assertNotNull(searchBib);
        System.out.println("id -->"+searchBib.getId());
        assertTrue(indexedBib.getIssn().get(0).equals("0394469756"));
        assertTrue(indexedBib.getIsbn().get(0).equals("0394469755"));
        assertTrue(indexedBib.getOclcNumber().get(0).equals("00133182"));
        Assert.assertTrue(indexedBib.getHoldingsIdList().equals(holdingsIdList));
        Assert.assertTrue(indexedBib.getBibItemIdList().equals(itemIdList));
        Assert.assertEquals(indexedBib.getBibId(),"101");
        Assert.assertEquals(indexedBib.getDocType(),"Bibliographic");
        Assert.assertEquals(indexedBib.getTitle(),"Test Bib 1");
        Assert.assertEquals(indexedBib.getBarcode(),"1");
        Assert.assertEquals(indexedBib.getTitle(),"Test Bib 1");
        Assert.assertEquals(indexedBib.getAuthor(),"Hoepli, Nancy L");
        Assert.assertEquals(indexedBib.getPublisher(),"McClelland & Stewart, limited");
        Assert.assertEquals(indexedBib.getImprint(),"Toronto, McClelland & Stewart, limited [c1926]");
        Assert.assertEquals(indexedBib.getPublicationDate(),"1960");
        Assert.assertEquals(indexedBib.getMaterialType(),"Material Type 1");
        Assert.assertEquals(indexedBib.getNotes(),"Bibliographical footnotes 1");
        Assert.assertEquals(indexedBib.getOwningInstitution(),"PUL");
        Assert.assertEquals(indexedBib.getSubject(),"Arab countries Politics and government.");
        Assert.assertEquals(indexedBib.getPublicationPlace(),"Paris");
        Assert.assertEquals(indexedBib.getLccn(),"71448228");
    }
}
