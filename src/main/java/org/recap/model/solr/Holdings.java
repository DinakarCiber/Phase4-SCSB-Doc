package org.recap.model.solr;

import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.util.Date;

/**
 * Created by rajeshbabuk on 13/9/16.
 */
@Data
@SolrDocument(collection = "recap")
public class Holdings {

    @Id
    @Field
    private String id;

    @Field("HoldingId")
    private Integer holdingsId;

    @Field("DocType")
    private String docType;

    @Field("SummaryHoldings")
    private String summaryHoldings;

    @Field("HoldingsOwningInstitution")
    private String owningInstitution;

    @Field("OwningInstitutionHoldingsId")
    private String owningInstitutionHoldingsId;

    @Field("HoldingsCreatedBy")
    private String holdingsCreatedBy;

    @Field("HoldingsCreatedDate")
    private Date holdingsCreatedDate;

    @Field("HoldingsLastUpdatedBy")
    private String holdingsLastUpdatedBy;

    @Field("HoldingsLastUpdatedDate")
    private Date holdingsLastUpdatedDate;

    @Field("IsDeletedHoldings")
    private boolean isDeletedHoldings = false;

    private String version;
}
