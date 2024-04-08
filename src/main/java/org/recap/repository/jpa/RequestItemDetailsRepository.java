package org.recap.repository.jpa;

import org.recap.model.jpa.RequestItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by rajeshbabuk on 26/10/16.
 */
public interface RequestItemDetailsRepository extends BaseRepository<RequestItemEntity> {

    /**
     * Finds a list of request item entities based on the given item id.
     *
     * @param itemId the item id
     * @return the list
     */
    List<RequestItemEntity> findByItemId(@Param("itemId") Integer itemId);

    /**
     * Finds a list of request item entities based on the given list of item id in.
     *
     * @param itemIds the item ids
     * @return the list
     */
    List<RequestItemEntity> findByItemIdIn(@Param("itemIds") List<Integer> itemIds);

    /**
     * Finds RequestItemEntity based on the given stop code.
     *
     * @param pageable the pageable
     * @param stopCode the stop code
     * @return the page
     */
    Page<RequestItemEntity> findByStopCode(Pageable pageable, @Param("stopCode") String stopCode);

    /**
     * Finds RequestItemEntity based on the given item barcode.
     *
     * @param pageable    the pageable
     * @param itemBarcode the item barcode
     * @return the page
     */
    @Query(value = "select request from RequestItemEntity request where request.itemId = (select itemId from ItemEntity item where item.barcode = :itemBarcode)")
    Page<RequestItemEntity> findByItemBarcode(Pageable pageable, @Param("itemBarcode") String itemBarcode);

    /**
     * Finds RequestItemEntity based on the given item barcode and delivery location.
     *
     * @param pageable         the pageable
     * @param itemBarcode      the item barcode
     * @param deliveryLocation the delivery location
     * @return the page
     */
    @Query(value = "select request from RequestItemEntity request where request.itemId = (select itemId from ItemEntity item where item.barcode = :itemBarcode) and request.stopCode = :deliveryLocation")
    Page<RequestItemEntity> findByItemBarcodeAndDeliveryLocation(Pageable pageable, @Param("itemBarcode") String itemBarcode, @Param("deliveryLocation") String deliveryLocation);

    /**
     * Deletes item based on the given list of item ids.
     *
     * @param itemIds the item ids
     * @return the int
     */
    @Transactional
    int deleteByItemIdIn(@Param("itemIds") List<Integer> itemIds);


    /**
     * Gets counts for InterLibrary request based on the given parameters.
     *
     * @param fromDate          the from date
     * @param toDate            the to date
     * @param itemOwningInstId  the item owning inst id
     * @param requestingInstIds the requesting inst ids
     * @return the il request counts
     */
    @Query(value = "SELECT COUNT(*) FROM REQUEST_ITEM_T , REQUEST_TYPE_T,ITEM_T WHERE REQUEST_ITEM_T.REQUEST_TYPE_ID=REQUEST_TYPE_T.REQUEST_TYPE_ID " +
            "AND REQUEST_ITEM_T.ITEM_ID=ITEM_T.ITEM_ID " +
            "AND REQUEST_ITEM_T.REQUEST_TYPE_ID IN (SELECT REQUEST_TYPE_ID FROM REQUEST_TYPE_T WHERE REQUEST_TYPE_CODE IN ('RETRIEVAL', 'RECALL', 'EDD')) " +
            "AND REQUEST_ITEM_T.CREATED_DATE >= :fromDate AND REQUEST_ITEM_T.CREATED_DATE<= :toDate " +
            "AND ITEM_T.OWNING_INST_ID = :itemOwningInstId " +
            "AND REQUEST_ITEM_T.REQUESTING_INST_ID IN (:requestingInstIds)", nativeQuery = true)
    long getIlRequestCounts(@Param("fromDate") Date fromDate,
                            @Param("toDate") Date toDate,
                            @Param("itemOwningInstId") int itemOwningInstId,
                            @Param("requestingInstIds") List<Integer> requestingInstIds);

    /**
     * Gets count for BorrowDirect's- hold ,recall ,retrieval request.
     *
     * @param fromDate         the from date
     * @param toDate           the to date
     * @param itemOwningInstId the item owning inst id
     * @param requestTypeCode  the request type code
     * @return the bd hold recall retrieval request counts
     */
    @Query(value = "SELECT COUNT(*) FROM REQUEST_ITEM_T ,REQUEST_TYPE_T, ITEM_T WHERE REQUEST_ITEM_T.REQUEST_TYPE_ID=REQUEST_TYPE_T.REQUEST_TYPE_ID " +
            "AND REQUEST_ITEM_T.ITEM_ID=ITEM_T.ITEM_ID " +
            "AND REQUEST_ITEM_T.CREATED_DATE >= :fromDate AND REQUEST_ITEM_T.CREATED_DATE <= :toDate " +
            "AND REQUEST_ITEM_T.REQUEST_TYPE_ID = (SELECT REQUEST_TYPE_ID FROM REQUEST_TYPE_T WHERE REQUEST_TYPE_CODE = :requestTypeCode) " +
            "AND ITEM_T.OWNING_INST_ID = :itemOwningInstId", nativeQuery = true)
    long getBDHoldRecallRetrievalRequestCounts(@Param("fromDate") Date fromDate,
                                               @Param("toDate") Date toDate,
                                               @Param("itemOwningInstId") int itemOwningInstId,
                                               @Param("requestTypeCode") String requestTypeCode);

    /**
     * Gets counts of physical and edd items.
     *
     * @param fromDate           the from date
     * @param toDate             the to date
     * @param itemOwningInstId   the item owning inst id
     * @param collectionGroupIds the collection group ids
     * @param requestTypeCodes   the request type codes
     * @return the physical and edd counts
     */
    @Query(value = "SELECT COUNT(*) FROM REQUEST_ITEM_T , REQUEST_TYPE_T , ITEM_T WHERE REQUEST_ITEM_T.REQUEST_TYPE_ID=REQUEST_TYPE_T.REQUEST_TYPE_ID " +
            "AND REQUEST_ITEM_T.ITEM_ID=ITEM_T.ITEM_ID " +
            "AND REQUEST_ITEM_T.CREATED_DATE >= :fromDate AND REQUEST_ITEM_T.CREATED_DATE <= :toDate " +
            "AND REQUEST_ITEM_T.REQUEST_TYPE_ID IN (SELECT REQUEST_TYPE_ID FROM REQUEST_TYPE_T WHERE REQUEST_TYPE_CODE IN (:requestTypeCodes)) " +
            "AND ITEM_T.COLLECTION_GROUP_ID IN (:collectionGroupIds) " +
            "AND ITEM_T.OWNING_INST_ID = :itemOwningInstId ", nativeQuery = true)
    long getPhysicalAndEDDCounts(@Param("fromDate") Date fromDate,
                                 @Param("toDate") Date toDate,
                                 @Param("itemOwningInstId") int itemOwningInstId,
                                 @Param("collectionGroupIds") List<Integer> collectionGroupIds,
                                 @Param("requestTypeCodes") List<String> requestTypeCodes);

}
