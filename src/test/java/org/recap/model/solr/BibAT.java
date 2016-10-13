package org.recap.model.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.RecapConstants;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.repository.solr.impl.BibSolrDocumentRepositoryImpl;
import org.recap.repository.solr.main.BibSolrDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BibAT extends BaseTestCase {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    SolrTemplate solrTemplate;

    @Autowired
    BibSolrDocumentRepository bibSolrDocumentRepository;

    @Autowired
    BibSolrDocumentRepositoryImpl bibSolrDocumentRepositoryImpl;


    @Test
    public void indexBib() throws Exception {

        List<String> issnList = new ArrayList<>();
        List<String>isbnList = new ArrayList<>();
        List<String> oclcNumberList = new ArrayList<>();
        List<Integer> holdingsIdList = new ArrayList<>();
        List<Integer> itemIdList = new ArrayList<>();
        issnList.add("0394469756");
        isbnList.add("0394469755");
        oclcNumberList.add("00133182");
        oclcNumberList.add("00440790");
        holdingsIdList.add(201);
        holdingsIdList.add(202);
        itemIdList.add(301);
        itemIdList.add(302);

        Bib bib = new Bib();
        bib.setBibId(101);
        bib.setDocType("Bib");
        bib.setTitle("Middleware for ReCAP");
        bib.setBarcode("1");
        bib.setTitle("Test Bib 1");
        bib.setAuthorDisplay("Hoepli, Nancy L");
        bib.setAuthorSearch(Arrays.asList("Hoepli, Nancy L", "Ibn Jubayr"));
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
        Bib indexedBib = this.bibSolrCrudRepository.save(bib);
        assertNotNull(indexedBib);

        assertTrue(indexedBib.getIssn().get(0).equals("0394469756"));
        assertTrue(indexedBib.getIsbn().get(0).equals("0394469755"));
        assertTrue(indexedBib.getOclcNumber().get(0).equals("00133182"));
        assertTrue(indexedBib.getHoldingsIdList().equals(holdingsIdList));
        assertTrue(indexedBib.getBibItemIdList().equals(itemIdList));
        assertEquals(indexedBib.getBibId(),new Integer(101));
        assertEquals(indexedBib.getDocType(),"Bib");
        assertEquals(indexedBib.getTitle(),"Test Bib 1");
        assertEquals(indexedBib.getBarcode(),"1");
        assertEquals(indexedBib.getTitle(),"Test Bib 1");
        assertEquals(indexedBib.getAuthorDisplay(),"Hoepli, Nancy L");
        assertEquals(indexedBib.getAuthorSearch().get(0),"Hoepli, Nancy L");
        assertEquals(indexedBib.getAuthorSearch().get(1),"Ibn Jubayr");
        assertEquals(indexedBib.getPublisher(),"McClelland & Stewart, limited");
        assertEquals(indexedBib.getImprint(),"Toronto, McClelland & Stewart, limited [c1926]");
        assertEquals(indexedBib.getPublicationDate(),"1960");
        assertEquals(indexedBib.getMaterialType(),"Material Type 1");
        assertEquals(indexedBib.getNotes(),"Bibliographical footnotes 1");
        assertEquals(indexedBib.getOwningInstitution(),"PUL");
        assertEquals(indexedBib.getSubject(),"Arab countries Politics and government.");
        assertEquals(indexedBib.getPublicationPlace(),"Paris");
        assertEquals(indexedBib.getLccn(),"71448228");
    }

    @Test
    public void testTitleStartsWith()throws Exception{

        List<String> issnList = new ArrayList<>();
        List<String>isbnList = new ArrayList<>();
        List<String> oclcNumberList = new ArrayList<>();
        List<Integer> holdingsIdList = new ArrayList<>();
        List<Integer> itemIdList = new ArrayList<>();
        issnList.add("0394469756");
        isbnList.add("0394469755");
        oclcNumberList.add("00133182");
        oclcNumberList.add("00440790");
        holdingsIdList.add(201);
        holdingsIdList.add(202);
        itemIdList.add(301);
        itemIdList.add(302);

        Bib bib = new Bib();
        bib.setBibId(101);
        bib.setDocType("Bib");
        bib.setTitle("Test Bib 1");
        bib.setBarcode("1");
        String[] titleTokened = bib.getTitle().split(" ");
        bib.setTitleStartsWith("^"+titleTokened[0]);
        bib.setAuthorDisplay("Nancy L");
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

        Bib bib1 = new Bib();
        bib1.setBibId(102);
        bib1.setDocType("Bib");
        bib1.setTitle("Bib Test 1");
        bib1.setBarcode("1");
        String[] titleTokened1 = bib1.getTitle().split(" ");
        bib1.setTitleStartsWith("^"+titleTokened1[0]);
        bib1.setAuthorDisplay("Hoepli");
        bib1.setPublisher("Tata, limited");
        bib1.setImprint("Tata, limited [c1926]");
        bib1.setIssn(issnList);
        bib1.setIsbn(isbnList);
        bib1.setOclcNumber(oclcNumberList);
        bib1.setPublicationDate("1960");
        bib1.setMaterialType("Material Type 1");
        bib1.setNotes("Bibliographical footnotes 1");
        bib1.setOwningInstitution("PUL");
        bib1.setSubject("Politics.");
        bib1.setPublicationPlace("London");
        bib1.setLccn("43435");
        bib1.setHoldingsIdList(holdingsIdList);
        bib1.setBibItemIdList(itemIdList);
        List<Bib> bibs = new ArrayList<>();
        bibs.add(bib);
        bibs.add(bib1);
        Iterable<Bib> indexedBibs = bibSolrCrudRepository.save(bibs);
        solrTemplate.softCommit();
        int count = 0;
        for(Bib indexedBib: indexedBibs){
            if(count==0){
                assertEquals("Test Bib 1",indexedBib.getTitle());
                assertEquals("Nancy L",indexedBib.getAuthorDisplay());
            }else{
                assertEquals("Bib Test 1",indexedBib.getTitle());
                assertEquals("Hoepli",indexedBib.getAuthorDisplay());
            }
            count++;
        }
        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();

        searchRecordsRequest.setFieldName("TitleStartsWith");
        String searchFieldValue = "Test";
        searchRecordsRequest.setFieldValue(searchFieldValue.split(" ")[0]);

        Map searchRecordMap = bibSolrDocumentRepository.search(searchRecordsRequest);
        List<BibItem> bibItems = (List<BibItem>) searchRecordMap.get(RecapConstants.SEARCH_SUCCESS_RESPONSE);
        assertNotNull(bibItems.get(0));
    }

    public File getUnicodeContentFile() throws URISyntaxException {
        URL resource = getClass().getResource("BibContent.xml");
        return new File(resource.toURI());
    }

    @Test
    public void testBoundWithSolrDocs() throws Exception {
        Random random = new Random();
        List<Bib> bibs = new ArrayList<>();
        List<Item> items = new ArrayList<>();
        List<String> issnList = new ArrayList<>();
        List<String>isbnList = new ArrayList<>();
        List<String> oclcNumberList = new ArrayList<>();
        issnList.add("0394469756");
        isbnList.add("0394469755");
        oclcNumberList.add("00133182");
        oclcNumberList.add("00440790");

        Integer bibId1 = random.nextInt();
        Integer itemId1 = random.nextInt();

        Bib bib1 = new Bib();
        bib1.setBibId(bibId1);
        bib1.setDocType("Bib");
        bib1.setTitle("Test Bib Doc 1");
        String[] titleTokened1 = bib1.getTitle().split(" ");
        bib1.setTitleStartsWith("^"+titleTokened1[0]);
        bib1.setAuthorDisplay("Nancy L");
        bib1.setPublisher("McClelland & Stewart, limited");
        bib1.setImprint("Toronto, McClelland & Stewart, limited [c1926]");
        bib1.setIssn(issnList);
        bib1.setIsbn(isbnList);
        bib1.setOclcNumber(oclcNumberList);
        bib1.setPublicationDate("1960");
        bib1.setMaterialType("Material Type 1");
        bib1.setNotes("Bibliographical footnotes 1");
        bib1.setOwningInstitution("CUL");
        bib1.setSubject("Arab countries Politics and government.");
        bib1.setPublicationPlace("Paris");
        bib1.setLccn("71448228");
        bib1.setHoldingsIdList(Arrays.asList(201));
        bib1.setBibItemIdList(Arrays.asList(itemId1));

        Integer bibId2 = random.nextInt();

        Bib bib2 = new Bib();
        bib2.setBibId(bibId2);
        bib2.setDocType("Bib");
        bib2.setTitle("Test Bib Doc 2");
        String[] titleTokened2 = bib2.getTitle().split(" ");
        bib2.setTitleStartsWith("^"+titleTokened2[0]);
        bib2.setAuthorDisplay("Nancy L");
        bib2.setPublisher("McClelland & Stewart, limited");
        bib2.setImprint("Toronto, McClelland & Stewart, limited [c1926]");
        bib2.setIssn(issnList);
        bib2.setIsbn(isbnList);
        bib2.setOclcNumber(oclcNumberList);
        bib2.setPublicationDate("1960");
        bib2.setMaterialType("Material Type 2");
        bib2.setNotes("Bibliographical footnotes 2");
        bib2.setOwningInstitution("CUL");
        bib2.setSubject("Arab countries Politics and government.");
        bib2.setPublicationPlace("Paris");
        bib2.setLccn("71448229");
        bib2.setHoldingsIdList(Arrays.asList(202));
        bib2.setBibItemIdList(Arrays.asList(itemId1));

        Item item1 = new Item();
        item1.setItemId(itemId1);
        item1.setDocType("Item");
        item1.setBarcode("1201");
        item1.setAvailability("Available");
        item1.setCollectionGroupDesignation("Shared");
        item1.setCallNumberSearch("H3");
        item1.setCustomerCode("Test Cust Code");
        item1.setUseRestriction("In Library Use");
        item1.setVolumePartYear("V.1 1982");
        item1.setItemBibIdList(Arrays.asList(bibId1, bibId2));
        item1.setHoldingsIdList(Arrays.asList(201, 202));

        Item item2 = new Item();
        item2.setItemId(itemId1);
        item2.setDocType("Item");
        item2.setBarcode("1201");
        item2.setAvailability("Available");
        item2.setCollectionGroupDesignation("Shared");
        item2.setCallNumberSearch("H3");
        item2.setCustomerCode("Test Cust Code");
        item2.setUseRestriction("In Library Use");
        item2.setVolumePartYear("V.1 1982");
        item2.setItemBibIdList(Arrays.asList(bibId1, bibId2));
        item2.setHoldingsIdList(Arrays.asList(201, 202));


        bibs.add(bib1);
        bibs.add(bib2);
        items.add(item1);
        items.add(item2);

        bibSolrCrudRepository.save(bib1);
        itemCrudRepository.save(item1);
        bibSolrCrudRepository.save(bib2);
        itemCrudRepository.save(item2);
        solrTemplate.commit();
        Thread.sleep(2000);

        Long countByItemId = itemCrudRepository.countByItemId(itemId1);
        assertNotNull(countByItemId);
        assertEquals("1", String.valueOf(countByItemId));
    }

    @Test
    public void blindSearch() throws Exception {
        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.getOwningInstitutions().addAll(Arrays.asList("NYPL", "CUL", "PUL"));
        searchRecordsRequest.getCollectionGroupDesignations().addAll(Arrays.asList("Shared", "Private", "Open"));
        searchRecordsRequest.getAvailability().addAll(Arrays.asList("Available", "Not Available"));
        searchRecordsRequest.getUseRestrictions().addAll(Arrays.asList("No Restrictions", "In Library Use", "Supervised Use"));
        searchRecordsRequest.getMaterialTypes().addAll(Arrays.asList("Monograph", "Serial", "Other"));

        bibSolrDocumentRepositoryImpl.search(searchRecordsRequest);

    }

    @Test
    public void searchNested() throws Exception {
        SolrQuery solrQuery = new SolrQuery("Title_search: Kuṟaḷ neṟiyum Citta");
        solrQuery.setParam("fl", "*,[child parentFilter=DocType:Bib childFilter=DocType:Holdings]");
        QueryResponse queryResponse = solrTemplate.getSolrClient().query(solrQuery);
        assertNotNull(queryResponse);
        SolrDocumentList results = queryResponse.getResults();
        assertNotNull(results);
        SolrDocument solrDocument = results.get(0);
        List<SolrDocument> childDocuments = solrDocument.getChildDocuments();
        SolrDocument childDoc = childDocuments.get(0);
        assertNotNull(childDoc);
    }

    @Test
    public void allFieldsNoValue() throws Exception {
        String itemQueryCriteria = "CollectionGroupDesignation:(\"Shared\"+\"Private\"+\"Open\")+AND+Availability:(\"Available\"+\"Not+Available\")+AND+UseRestriction:(\"No+Restrictions\"+\"In+Library+Use\"+\"Supervised+Use\")";
        SolrQuery solrQuery = new SolrQuery("{!parent which=\"DocType:Bib\"}*:*+AND+"+itemQueryCriteria);
        QueryResponse queryResponse = solrTemplate.getSolrClient().query(solrQuery);
        assertNotNull(queryResponse);
        SolrDocumentList results = queryResponse.getResults();
        assertNotNull(results);
        SolrDocument solrDocument = results.get(0);
        List<SolrDocument> childDocuments = solrDocument.getChildDocuments();
        SolrDocument childDoc = childDocuments.get(0);
        assertNotNull(childDoc);
    }

    @Test
    public void titleSearchWithValue() throws Exception {
        SolrQuery solrQuery = new SolrQuery("BibOwningInstitution(\"NYPL\" \"PUL\" \"CUL\") AND LeaderMaterialType:Monograph AND Title_search:Un signo AND ({!parent which=\"ContentType:parent\"}UseRestriction_search:NoRestrictions OR {!parent which=\"ContentType:parent\"}UseRestriction_search:SupervisedUse)");
        QueryResponse queryResponse = solrTemplate.getSolrClient().query(solrQuery);
        assertNotNull(queryResponse);
        SolrDocumentList results = queryResponse.getResults();
        assertNotNull(results);
    }

    @Test
    public void allFieldsWithValue() throws Exception {
        SolrQuery solrQuery = new SolrQuery("BibOwningInstitution(\"NYPL\" \"PUL\" \"CUL\") AND LeaderMaterialType:Monograph AND Un signo AND ({!parent which=\"ContentType:parent\"}UseRestriction_search:NoRestrictions OR {!parent which=\"ContentType:parent\"}UseRestriction_search:SupervisedUse)");
        QueryResponse queryResponse = solrTemplate.getSolrClient().query(solrQuery);
        assertNotNull(queryResponse);
        SolrDocumentList results = queryResponse.getResults();
        assertNotNull(results);
    }

    @Test
    public void fetchChildBasedOnParentCriteria() throws Exception {
        SolrQuery solrQuery = new SolrQuery("UseRestriction_search:SupervisedUse AND ({!child of=\"ContentType:parent\"}BibOwningInstitution:NYPL OR {!child of=\"ContentType:parent\"}BibOwningInstitution:PUL OR {!child of=\"ContentType:parent\"}BibOwningInstitution:CUL) AND {!child of=\"ContentType:parent\"}LeaderMaterialType:Monograph");
        QueryResponse queryResponse = solrTemplate.getSolrClient().query(solrQuery);
        assertNotNull(queryResponse);
        SolrDocumentList results = queryResponse.getResults();
        assertNotNull(results);
    }


    @Test
    public void childrenForParents() throws Exception {
        String query = "_root_:(\"3.b10719868x\" \"3.b127185884\") Availability:(\"Available\"+\"Not+Available\") CollectionGroupDesignation:(\"Shared\"+\"Private\"+\"Open\") UseRestriction:(\"No+Restrictions\"+\"In+Library+Use\"+\"Supervised+Use\")Availability:(\"Available\"+\"Not+Available\") CollectionGroupDesignation:(\"Shared\"+\"Private\"+\"Open\") UseRestriction:(\"No+Restrictions\"+\"In+Library+Use\"+\"Supervised+Use\")Availability:(\"Available\"+\"Not+Available\") CollectionGroupDesignation:(\"Shared\"+\"Private\"+\"Open\") UseRestriction:(\"No+Restrictions\"+\"In+Library+Use\"+\"Supervised+Use\")";
        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.setStart(0);
        solrQuery.setRows(2);
        QueryResponse queryResponse = solrTemplate.getSolrClient().query(solrQuery);
        assertNotNull(queryResponse);
    }
}
