package org.recap.model.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by angelind on 31/10/16.
 */
@Entity
@Table(name = "MATCHING_BIB_T", catalog = "")
@Data
@EqualsAndHashCode(callSuper = false)
public class MatchingBibEntity extends AbstractEntity<Integer> {
    @Column(name = "ROOT")
    private String version;

    @Column(name = "BIB_ID")
    private Integer bibId;

    @Column(name = "OWNING_INSTITUTION")
    private String owningInstitution;

    @Column(name = "OWNING_INST_BIB_ID")
    private String owningInstBibId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "OCLC")
    private String oclc;

    @Column(name = "ISBN")
    private String isbn;

    @Column(name = "ISSN")
    private String issn;

    @Column(name = "LCCN")
    private String lccn;

    @Column(name = "MATERIAL_TYPE")
    private String materialType;

    @Column(name = "MATCHING")
    private String matching;

    @Column(name = "STATUS")
    private String status;
}
