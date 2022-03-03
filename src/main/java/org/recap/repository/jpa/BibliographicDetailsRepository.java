package org.recap.repository.jpa;

import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by pvsubrah on 6/10/16.
 */
//@RepositoryRestResource(collectionResourceRel = "bibliographic", path = "bibliographic")
public interface BibliographicDetailsRepository extends BaseRepository<BibliographicEntity> {

    /**
     * Gets the count the number of bibs by using IsDeleted field which has false.
     *
     * @return the long
     */
    Long countByIsDeletedFalse();

    /**
     * Gets the count the number of bibs by using owning institution id.
     *
     * @param owningInstitutionId the owning institution id
     * @return the long
     */
    Long countByOwningInstitutionId(Integer owningInstitutionId);

    /**
     * Gets the count the number of bibs by using the date in the parameter.
     *
     * @param createdDate the created date
     * @return the long
     */
    Long countByLastUpdatedDateAfter(Date createdDate);

    /**
     * Gets the count the number of bibs by using owning institution id and last updated date.
     *
     * @param owningInstitutionId the owning institution id
     * @param createdDate         the created date
     * @return the long
     */
    Long countByOwningInstitutionIdAndLastUpdatedDateAfter(Integer owningInstitutionId, Date createdDate);

    /**
     * Gets pageable Bibliographic Entities by using last updated date from the given parameter.
     *
     * @param pageable    the pageable
     * @param createdDate the created date
     * @return the page
     */
    Page<BibliographicEntity> findByLastUpdatedDateAfter(Pageable pageable, Date createdDate);

    /**
     * Gets pageable Bibliographic Entities by using owning institution id and last updated date.
     *
     * @param pageable      the pageable
     * @param institutionId the institution id
     * @param createdDate   the created date
     * @return the page
     */
    Page<BibliographicEntity> findByOwningInstitutionIdAndLastUpdatedDateAfter(Pageable pageable, Integer institutionId, Date createdDate);

    /**
     * Gets the count of bibs by using owning institution id and is deleted false.
     *
     * @param owningInstitutionId the owning institution id
     * @return the long
     */
    Long countByOwningInstitutionIdAndIsDeletedFalse(Integer owningInstitutionId);

    /**
     * Find all bibliographic entites by using IsDeleted field which is false.
     *
     * @param pageable the pageable
     * @return the page
     */
    Page<BibliographicEntity> findAllByIsDeletedFalse(Pageable pageable);

    /**
     * Find bibliographic entities by using owning institution id and IsDeleted field which has false.
     *
     * @param pageable      the pageable
     * @param institutionId the institution id
     * @return the page
     */
    Page<BibliographicEntity> findByOwningInstitutionIdAndIsDeletedFalse(Pageable pageable, Integer institutionId);

    /**
     * Find bibliographic entites by using owning institution id.
     *
     * @param pageable      the pageable
     * @param institutionId the institution id
     * @return the page
     */
    Page<BibliographicEntity> findByOwningInstitutionId(Pageable pageable, Integer institutionId);

    /**
     * Find the bibliographic entity by using owning institution id,owning institution bib id and is deleted field which has false value.
     *
     * @param owningInstitutionId    the owning institution id
     * @param owningInstitutionBibId the owning institution bib id
     * @return the bibliographic entity
     */
    BibliographicEntity findByOwningInstitutionIdAndOwningInstitutionBibIdAndIsDeletedFalse(Integer owningInstitutionId, String owningInstitutionBibId);


    /**
     * Find the list of bibliographic entities by using a list of bibliographic ids.
     *
     * @param bibliographicIds the bibliographic ids
     * @return the list
     */
    List<BibliographicEntity> findByIdIn(List<Integer> bibliographicIds);

    /**
     * Find the bibliographic entity by using bibliographic id and is deleted field which has false value.
     *
     * @param bibliographicId the bibliographic id
     * @return the bibliographic entity
     */
    BibliographicEntity findByIdAndIsDeletedFalse(@Param("bibliographicId") Integer bibliographicId);

    /**
     * Find the bibliographic entity by using owning institution id and owning institution bib id.
     *
     * @param owningInstitutionId    the owning institution id
     * @param owningInstitutionBibId the owning institution bib id
     * @return the bibliographic entity
     */
    BibliographicEntity findByOwningInstitutionIdAndOwningInstitutionBibId(@Param("owningInstitutionId") Integer owningInstitutionId, @Param("owningInstitutionBibId") String owningInstitutionBibId);

    /**
     * Find a list of bibliographic entities by using owning institution bib id.
     *
     * @param owningInstitutionBibId the owning institution bib id
     * @return the list
     */
    List<BibliographicEntity> findByOwningInstitutionBibId(String owningInstitutionBibId);

    List<BibliographicEntity> findByOwningInstitutionBibIdInAndOwningInstitutionId(List<String> owningInstBibIdList,Integer owningInstitutionBibId);

    /**
     * Count the number of bibs by using owning institution code and is deleted field which has false value.
     *
     * @param institutionCode the institution code
     * @return the long
     */
    @Query(value = "select count(*) from bibliographic_t bib, institution_t inst where bib.OWNING_INST_ID = inst.INSTITUTION_ID AND inst.INSTITUTION_CODE=?1 limit 1", nativeQuery = true)
    Long countByOwningInstitutionCodeAndIsDeletedFalse(String institutionCode);

    /**
     * Gets a list of holding entities by using owning institution id and owning institution bib id.
     *
     * @param owningInstitutionId    the owning institution id
     * @param owningInstitutionBibId the owning institution bib id
     * @return the non deleted holdings entities
     */
    List<HoldingsEntity> getNonDeletedHoldingsEntities(@Param("owningInstitutionId") Integer owningInstitutionId, @Param("owningInstitutionBibId") String owningInstitutionBibId);

    /**
     * Gets a list of item entities by using owning institution id and owning institution bib id.
     *
     * @param owningInstitutionId    the owning institution id
     * @param owningInstitutionBibId the owning institution bib id
     * @return the non deleted item entities
     */
    List<ItemEntity> getNonDeletedItemEntities(@Param("owningInstitutionId") Integer owningInstitutionId, @Param("owningInstitutionBibId") String owningInstitutionBibId);

    /**
     * Gets the count of all non deleted items based on owning institution id and owning institution bib id.
     *
     * @param owningInstitutionId    the owning institution id
     * @param owningInstitutionBibId the owning institution bib id
     * @return the non deleted items count
     */
    @Query(value = "SELECT COUNT(*) FROM ITEM_T, BIBLIOGRAPHIC_ITEM_T, BIBLIOGRAPHIC_T WHERE " +
            "BIBLIOGRAPHIC_T.BIBLIOGRAPHIC_ID = BIBLIOGRAPHIC_ITEM_T.BIBLIOGRAPHIC_ID AND ITEM_T.ITEM_ID = BIBLIOGRAPHIC_ITEM_T.ITEM_ID " +
            "AND ITEM_T.IS_DELETED = 0 AND " +
            "BIBLIOGRAPHIC_T.OWNING_INST_BIB_ID = :owningInstitutionBibId AND BIBLIOGRAPHIC_T.OWNING_INST_ID = :owningInstitutionId", nativeQuery = true)
    Long getNonDeletedItemsCount(@Param("owningInstitutionId") Integer owningInstitutionId, @Param("owningInstitutionBibId") String owningInstitutionBibId);

    /**
     * This query marks the bibs as deleted for the given list of bib ids.
     *
     * @param bibliographicIds the bibliographic ids
     * @param lastUpdatedBy    the last updated by
     * @param lastUpdatedDate  the last updated date
     * @return the int
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE BibliographicEntity bib SET bib.isDeleted = true, bib.lastUpdatedBy = :lastUpdatedBy, bib.lastUpdatedDate = :lastUpdatedDate WHERE bib.id IN :bibliographicIds")
    int markBibsAsDeleted(@Param("bibliographicIds") List<Integer> bibliographicIds, @Param("lastUpdatedBy") String lastUpdatedBy, @Param("lastUpdatedDate") Date lastUpdatedDate);

    /**
     * Gets bibliographic entities for changed items based on the operation type and date.
     *
     * @param pageable      the pageable
     * @param operationType the operation type
     * @param from          the from date
     * @param to            the to date
     * @return the bibliographic entities for changed items
     */
    @Query(value = "SELECT distinct BIB FROM BibliographicEntity as BIB WHERE BIB.id IN " +
            "(SELECT DISTINCT BIB1.id FROM BibliographicEntity as BIB1 INNER JOIN BIB1.itemEntities AS ITEMS " +
            "WHERE ITEMS.id IN (SELECT recordId FROM ItemChangeLogEntity where operationType=?1 and updated_date between ?2 and ?3))")
    Page<BibliographicEntity> getBibliographicEntitiesForChangedItems(Pageable pageable, String operationType, Date from, Date to);

    /**
     * Gets count of bibliographic entities for changed items based on the operation type and date.
     *
     * @param operationType the operation type
     * @param from          the from date
     * @param to            the to date
     * @return the count of bibliographic entities for changed items
     */
    @Query(value = "SELECT count(distinct BIB) FROM BibliographicEntity as BIB WHERE BIB.id IN " +
            "(SELECT DISTINCT BIB1.id FROM BibliographicEntity as BIB1 INNER JOIN BIB1.itemEntities AS ITEMS " +
            "WHERE ITEMS.id IN (SELECT recordId FROM ItemChangeLogEntity where operationType=?1 and updated_date between ?2 and ?3))")
    Long getCountOfBibliographicEntitiesForChangedItems(String operationType, Date from, Date to);

    /**
     * Gets count of bib based on bib ids.
     *
     * @param bibliographicIds the bibliographic ids
     * @return the count of bib based on bib ids
     */
    @Query(value = "SELECT count(distinct BIB) FROM BibliographicEntity as BIB WHERE BIB.owningInstitutionId in (:owningInstitutionIds) and BIB.id in (:bibliographicIds)")
    Long getCountOfBibBasedOnBibIds(@Param("owningInstitutionIds") List<Integer> owningInstitutionIds, @Param("bibliographicIds") List<Integer> bibliographicIds);

    /**
     * Gets count of bib based on bib id range.
     *
     * @param bibliographicIdFrom the bibliographic id from
     * @param bibliographicIdTo   the bibliographic id to
     * @return the count of bib based on bib id range
     */
    @Query(value = "SELECT count(distinct BIB) FROM BibliographicEntity as BIB WHERE BIB.owningInstitutionId in (:owningInstitutionIds) and BIB.id BETWEEN :bibliographicIdFrom and :bibliographicIdTo")
    Long getCountOfBibBasedOnBibIdRange(@Param("owningInstitutionIds") List<Integer> owningInstitutionIds, @Param("bibliographicIdFrom") Integer bibliographicIdFrom, @Param("bibliographicIdTo")Integer bibliographicIdTo);

    /**
     * Gets count of bib based on date range.
     *
     * @param lastUpdatedDateFrom the last updated date from
     * @param lastUpdatedDateTo   the last updated date to
     * @return the count of bib based on date range
     */
    @Query(value = "SELECT count(distinct BIB) FROM BibliographicEntity as BIB WHERE BIB.owningInstitutionId in (:owningInstitutionIds) and BIB.lastUpdatedDate BETWEEN :lastUpdatedDateFrom and :lastUpdatedDateTo")
    Long getCountOfBibBasedOnDateRange(@Param("owningInstitutionIds") List<Integer> owningInstitutionIds, @Param("lastUpdatedDateFrom") Date lastUpdatedDateFrom, @Param("lastUpdatedDateTo") Date lastUpdatedDateTo);


    @Query(value = "SELECT COUNT(DISTINCT a.BIBLIOGRAPHIC_ID) FROM BIBLIOGRAPHIC_T AS a INNER JOIN BIBLIOGRAPHIC_ITEM_T AS b INNER JOIN ITEM_T AS c ON a.BIBLIOGRAPHIC_ID= b.BIBLIOGRAPHIC_ID AND b.ITEM_ID = c.ITEM_ID AND a.OWNING_INST_ID =?1 AND c.OWNING_INST_ID =?1 AND c.COLLECTION_GROUP_ID =?2",nativeQuery = true)
    Long getCountOfBibBasedOnInstitutionAndCGD(Integer owningInstitutionId, Integer cgdId);
    /**
     * Gets bibs based on bib ids.
     *
     * @param pageable         the pageable
     * @param bibliographicIds the bibliographic ids
     * @return the bibs based on bib ids
     */
    @Query(value = "SELECT BIB FROM BibliographicEntity as BIB WHERE BIB.owningInstitutionId in (:owningInstitutionIds) and BIB.id in (:bibliographicIds)")
    Page<BibliographicEntity> getBibsBasedOnBibIds(Pageable pageable, @Param("owningInstitutionIds") List<Integer> owningInstitutionIds, @Param("bibliographicIds") List<Integer> bibliographicIds);

    /**
     * Gets bibs based on bib id range.
     *
     * @param pageable            the pageable
     * @param bibliographicIdFrom the bibliographic id from
     * @param bibliographicIdTo   the bibliographic id to
     * @return the bibs based on bib id range
     */
    @Query(value = "SELECT BIB FROM BibliographicEntity as BIB WHERE BIB.owningInstitutionId in (:owningInstitutionIds) and BIB.id BETWEEN :bibliographicIdFrom and :bibliographicIdTo")
    Page<BibliographicEntity> getBibsBasedOnBibIdRange(Pageable pageable, @Param("owningInstitutionIds") List<Integer> owningInstitutionIds, @Param("bibliographicIdFrom") Integer bibliographicIdFrom, @Param("bibliographicIdTo")Integer bibliographicIdTo);

    /**
     * Gets bibs based on date range.
     *
     * @param pageable            the pageable
     * @param lastUpdatedDateFrom the last updated date from
     * @param lastUpdatedDateTo   the last updated date to
     * @return the bibs based on date range
     */
    @Query(value = "SELECT BIB FROM BibliographicEntity as BIB WHERE BIB.owningInstitutionId in (:owningInstitutionIds) and BIB.lastUpdatedDate BETWEEN :lastUpdatedDateFrom and :lastUpdatedDateTo")
    Page<BibliographicEntity> getBibsBasedOnDateRange(Pageable pageable, @Param("owningInstitutionIds") List<Integer> owningInstitutionIds, @Param("lastUpdatedDateFrom") Date lastUpdatedDateFrom, @Param("lastUpdatedDateTo") Date lastUpdatedDateTo);

    List<BibliographicEntity> findByOwningInstitutionIdInAndIdIn(List<Integer> owningInstitutionIds,List<Integer> bibliographicIds);

    List<BibliographicEntity> findByOwningInstitutionIdInAndMatchingIdentityIn(List<Integer> allInstitutionIdsExceptSupportInstitution, List<String> matchingIdentifiers);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE BibliographicEntity bib SET bib.matchingIdentity = NULL, bib.matchScore = 0, bib.anamolyFlag = false WHERE bib.matchingIdentity IS NOT NULL")
    int removeMatchingIdentifiers();

    List<BibliographicEntity> findByOwningInstitutionIdInAndMatchingIdentity(List<Integer> owningInstitutionIds,String matchingIdentity);

    @Modifying
    @Query(value = "UPDATE `BIBLIOGRAPHIC_T` SET `ANAMOLY_FLAG`='1' WHERE `BIBLIOGRAPHIC_ID` in (:bibliographicIds)",nativeQuery = true)
    @Transactional
    void updateAnamolyFlag(@Param("bibliographicIds")List<Integer> bibIds);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE BibliographicEntity bib SET bib.maQualifier = 0 WHERE bib.id IN :bibliographicIds AND bib.maQualifier IN :maQualifiers")
    int resetMAQualifier(@Param("bibliographicIds") List<Integer> bibliographicIds, @Param("maQualifiers") List<Integer> maQualifiers);

    @Modifying
    @Query(value = "update bibliographic_t AS a inner JOIN (select distinct MATCHING_IDENTITY from bibliographic_t where BIBLIOGRAPHIC_ID In (:bibliographicIds) group by MATCHING_IDENTITY, MATCH_SCORE having count(MATCHING_IDENTITY) = 1) AS b ON a.MATCHING_IDENTITY = b.MATCHING_IDENTITY set ANAMOLY_FLAG = 1", nativeQuery = true)
    @Transactional
    int updateAnamolyFlagFirstForBibIds(@Param("bibliographicIds") List<Integer> bibIds);

    @Modifying
    @Query(value = "update bibliographic_t AS a inner JOIN (select distinct(b1.MATCHING_IDENTITY) from bibliographic_t b1, bibliographic_t b2 where b1.MATCH_SCORE <> b2.MATCH_SCORE and b1.MATCHING_IDENTITY = b2.MATCHING_IDENTITY and b1.BIBLIOGRAPHIC_ID in (:bibliographicIds) group by b1.MATCHING_IDENTITY ) AS b ON a.MATCHING_IDENTITY = b.MATCHING_IDENTITY and a.ANAMOLY_FLAG = 0 set ANAMOLY_FLAG = 1", nativeQuery = true)
    @Transactional
    int updateAnamolyFlagSecondForBibIds(@Param("bibliographicIds") List<Integer> bibIds);

    @Query(value = "select * from bibliographic_t where BIBLIOGRAPHIC_ID in ( select BIBLIOGRAPHIC_ID from bibliographic_item_t where item_id in ( select item_id from item_t where OWNING_INST_ID =?1 and COLLECTION_GROUP_ID =?1 ) ) and OWNING_INST_ID =?2", nativeQuery = true)
    Page<BibliographicEntity> findByOwningInstitutionIdAndCGD(Pageable pageable, Integer owningInstitutionId, Integer cgdId);

}
