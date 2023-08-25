package org.recap.model.solr;

import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by rajeshbabuk on 8/7/16.
 */
@Data
@SolrDocument(collection = "recap")
public class BibItem {

    @Id
    @Field("id")
    private String id;

    @Field("BibId")
    private Integer bibId;

    @Field("DocType")
    private String docType;

    @Field("Barcode")
    private String barcode;

    @Field("Title_search")
    private String title;

    @Field("Title_display")
    private String titleDisplay;

    @Field("Title_subfield_a")
    private String titleSubFieldA;

    @Field("Title_match")
    private String titleMatch;

    @Field("Author_display")
    private String authorDisplay;

    @Field("Author_search")
    private List<String> authorSearch;

    @Field("BibOwningInstitution")
    private String owningInstitution;

    @Field("Publisher")
    private String publisher;

    @Field("PublicationPlace")
    private String publicationPlace;

    @Field("PublicationDate")
    private String publicationDate;

    @Field("Subject")
    private String subject;

    @Field("ISBN")
    private List<String> isbn;

    @Field("ISSN")
    private List<String> issn;

    @Field("OCLCNumber")
    private List<String> oclcNumber;

    @Field("MaterialType")
    private String materialType;

    @Field("Notes")
    private String notes;

    @Field("LCCN")
    private String lccn;

    @Field("Imprint")
    private String imprint;

    @Field("OwningInstHoldingsId")
    private List<Integer> owningInstHoldingsIdList;

    @Field("OwningInstitutionBibId")
    private String owningInstitutionBibId;

    @Field("LeaderMaterialType")
    private String leaderMaterialType;

    @Field("Title_sort")
    private String titleSort;

    @Field("BibCreatedBy")
    private String bibCreatedBy;

    @Field("BibCreatedDate")
    private Date bibCreatedDate;

    @Field("BibLastUpdatedBy")
    private String bibLastUpdatedBy;

    @Field("BibLastUpdatedDate")
    private Date bibLastUpdatedDate;

    @Field("IsDeletedBib")
    private boolean isDeletedBib = false;

    @Field("MatchingIdentifier")
    private String matchingIdentifier;

    @Field("MatchScore")
    private  Integer matchScore;

    @Field("AnamolyFlag")
    private  Boolean anamolyFlag;

    @Field("MAQualifier")
    private  Integer maQualifier;

    private String version;

    private List<Item> items = new ArrayList<>();

    private List<Holdings> holdingsList = new ArrayList<>();

    /**
     * Gets items.
     *
     * @return the items
     */
    public List<Item> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    /**
     * Sets items.
     *
     * @param items the items
     */
    public void setItems(List<Item> items) {
        this.items = items;
    }

    /**
     * Gets holdings list.
     *
     * @return the holdings list
     */
    public List<Holdings> getHoldingsList() {
        if(holdingsList == null) {
            holdingsList = new ArrayList<>();
        }
        return holdingsList;
    }

    /**
     * Sets holdings list.
     *
     * @param holdingsList the holdings list
     */
    public void setHoldingsList(List<Holdings> holdingsList) {
        this.holdingsList = holdingsList;
    }

    /**
     * Add items to the item list.
     *
     * @param item the item
     */
    public void addItem(Item item) {
        getItems().add(item);
    }

    /**
     * Add holdings.
     *
     * @param holdings the holdings
     */
    public void addHoldings(Holdings holdings) {
        getHoldingsList().add(holdings);
    }
}
