package org.recap.model.solr;

import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.util.Date;
import java.util.List;

/**
 * Created by angelind on 15/6/16.
 */
@Data
@SolrDocument(collection = "recap")
public class Item {

    @Id
    @Field
    private String id;

    @Field("ItemId")
    private Integer itemId;

    @Field("OwningInstitutionItemId")
    private String owningInstitutionItemId;

    @Field("Barcode")
    private String barcode;

    @Field("Availability_search")
    private String availability;

    @Field("CollectionGroupDesignation")
    private String collectionGroupDesignation;

    @Field("DocType")
    private String docType;

    @Field("CustomerCode")
    private String customerCode;

    @Field("UseRestriction_search")
    private String useRestriction;

    @Field("VolumePartYear")
    private String volumePartYear;

    @Field("CallNumber_search")
    private String callNumberSearch;

    @Field("CallNumber_display")
    private String callNumberDisplay;

    @Field("ItemOwningInstitution")
    private String owningInstitution;

    @Field("ItemBibId")
    private List<Integer> itemBibIdList;

    @Field("HoldingsId")
    private List<Integer> holdingsIdList;

    @Field("Availability_display")
    private String availabilityDisplay;

    @Field("UseRestriction_display")
    private String useRestrictionDisplay;

    @Field("CopyNumber")
    private String copyNumber;

    @Field("ItemCreatedBy")
    private String itemCreatedBy;

    @Field("ItemCreatedDate")
    private Date itemCreatedDate;

    @Field("ItemLastUpdatedBy")
    private String itemLastUpdatedBy;

    @Field("ItemLastUpdatedDate")
    private Date itemLastUpdatedDate;

    @Field("IsDeletedItem")
    private boolean isDeletedItem = false;

    @Field("Title_sort")
    private String titleSort;

    @Field("ItemCatalogingStatus")
    private String itemCatalogingStatus;

    @Field("CGDChangeLog")
    private String cgdChangeLog;

    @Field("ImsLocation")
    private String imsLocation;

    private String version;
}
