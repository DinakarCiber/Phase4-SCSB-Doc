package org.recap;

/**
 * Created by SheikS on 6/20/2016.
 */
public class RecapConstants {
    public static final String PATH_SEPARATOR = "/";
    public static final String PROCESSSED_RECORDS = "processedRecords";

    public static final String ALL = "*";
    public static final String DOCTYPE = "DocType";
    public static final String BIB = "Bib";
    public static final String ITEM = "Item";
    public static final String HOLDINGS = "Holdings";
    public static final String HOLDINGS_ID = "HoldingsId";
    public static final String HOLDING_ID = "HoldingId";
    public static final String ITEM_ID = "ItemId";
    public static final String SEARCH = "search";
    public static final String BIB_OWNING_INSTITUTION = "BibOwningInstitution";
    public static final String HOLDINGS_OWNING_INSTITUTION = "HoldingsOwningInstitution";
    public static final String ITEM_OWNING_INSTITUTION = "ItemOwningInstitution";
    public static final String OWNING_INSTITUTION = "OwningInstitution";
    public static final String COLLECTION_GROUP_DESIGNATION = "CollectionGroupDesignation";
    public static final String AVAILABILITY = "Availability_search";
    public static final String TITLE_SEARCH = "Title_search";
    public static final String AUTHOR_SEARCH = "Author_search";
    public static final String PUBLISHER = "Publisher";
    public static final String TITLE_STARTS_WITH= "TitleStartsWith";
    public static final String TITLE_SORT= "Title_sort";
    public static final String BARCODE = "Barcode";
    public static final String CALL_NUMBER = "CallNumber_search";
    public static final String NOTES = "Notes";
    public static final String LEADER_MATERIAL_TYPE = "LeaderMaterialType";
    public static final String MONOGRAPH = "Monograph";
    public static final String SERIAL = "Serial";
    public static final String OTHER = "Other";
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String ALL_DIACRITICS = "all_diacritics";
    public static final String ALL_FIELDS = "_text_";

    public static final String USE_RESTRICTION = "UseRestriction_search";
    public static final String NO_RESTRICTIONS = "No Restrictions";
    public static final String IN_LIBRARY_USE = "In Library Use";
    public static final String SUPERVISED_USE = "Supervised Use";

    public static final String INCREMENTAL_DATE_FORMAT = "dd-MM-yyyy hh:mm";

    //Matching Algorithm Constants
    public static final String CSV_MATCHING_ALGO_REPORT_Q = "scsbactivemq:queue:csvMatchingAlgoReportQ";
    public static final String CSV_SUMMARY_ALGO_REPORT_Q = "scsbactivemq:queue:csvSummaryAlgoReportQ";
    public static final String CSV_SOLR_EXCEPTION_REPORT_Q = "scsbactivemq:queue:csvSolrExceptionReportQ";
    public static final String FTP_MATCHING_ALGO_REPORT_Q = "scsbactivemq:queue:ftpMatchingAlgoReportQ";
    public static final String FTP_SUMMARY_ALGO_REPORT_Q = "scsbactivemq:queue:ftpSummaryAlgoReportQ";
    public static final String FTP_SOLR_EXCEPTION_REPORT_Q = "scsbactivemq:queue:ftpSolrExceptionReportQ";
    public static final String REPORT_Q= "scsbactivemq:queue:reportQ";

    public static final String CSV_MATCHING_ALGO_REPORT_ROUTE_ID = "csvMatchingAlgoReportRoute";
    public static final String CSV_SUMMARY_ALGO_REPORT_ROUTE_ID = "csvSummaryAlgoReportRoute";
    public static final String CSV_SOLR_EXCEPTION_REPORT_ROUTE_ID = "csvSolrExceptionReportRoute";
    public static final String FTP_MATCHING_ALGO_REPORT_ROUTE_ID = "ftpMatchingAlgoReportRoute";
    public static final String FTP_SUMMARY_ALGO_REPORT_ROUTE_ID = "ftpSummaryAlgoReportRoute";
    public static final String FTP_SOLR_EXCEPTION_REPORT_ROUTE_ID = "ftpSolrExceptionReportRoute";
    public static final String REPORT_ROUTE_ID = "reportQRoute";

    public static final String MATCHING_ALGO_FULL_FILE_NAME = "Matching_Algo_Phase1";
    public static final String MATCHING_ALGO_OCLC_FILE_NAME = "Matching_Algo_OCLC";
    public static final String MATCHING_ALGO_ISBN_FILE_NAME = "Matching_Algo_ISBN";
    public static final String MATCHING_ALGO_ISSN_FILE_NAME = "Matching_Algo_ISSN";
    public static final String MATCHING_ALGO_LCCN_FILE_NAME = "Matching_Algo_LCCN";

    public static final String EXCEPTION_REPORT_FILE_NAME = "Exception_Report";
    public static final String EXCEPTION_REPORT_OCLC_FILE_NAME = "Exception_Report_OCLC";
    public static final String EXCEPTION_REPORT_ISBN_FILE_NAME = "Exception_Report_ISBN";
    public static final String EXCEPTION_REPORT_ISSN_FILE_NAME = "Exception_Report_ISSN";
    public static final String EXCEPTION_REPORT_LCCN_FILE_NAME = "Exception_Report_LCCN";

    public static final String SUMMARY_REPORT_FILE_NAME = "Summary_Report_Phase1";
    public static final String SUMMARY_REPORT_OCLC_FILE_NAME = "Summary_Report_OCLC";
    public static final String SUMMARY_REPORT_ISBN_FILE_NAME = "Summary_Report_ISBN";
    public static final String SUMMARY_REPORT_ISSN_FILE_NAME = "Summary_Report_ISSN";
    public static final String SUMMARY_REPORT_LCCN_FILE_NAME = "Summary_Report_LCCN";

    public static final String REPORT_FILE_NAME = "fileName";
    public static final String DATE_FORMAT_FOR_FILE_NAME = "ddMMMyyyy";

    public static final String MATCHING_BIB_ID = "BibId";
    public static final String MATCHING_TITLE = "Title";
    public static final String MATCHING_BARCODE = "Barcode";
    public static final String MATCHING_VOLUME_PART_YEAR = "VolumePartYear";
    public static final String MATCHING_INSTITUTION_ID = "InstitutionId";
    public static final String MATCHING_OCLC = "Oclc";
    public static final String MATCHING_ISBN = "Isbn";
    public static final String MATCHING_ISSN = "Issn";
    public static final String MATCHING_LCCN = "Lccn";
    public static final String MATCHING_USE_RESTRICTIONS = "UseRestrictions";
    public static final String MATCHING_SUMMARY_HOLDINGS = "SummaryHoldings";

    public static final String SUMMARY_NUM_BIBS_IN_TABLE = "CountOfBibsInTable";
    public static final String SUMMARY_NUM_ITEMS_IN_TABLE = "CountOfItemsInTable";
    public static final String SUMMARY_MATCHING_KEY_FIELD = "MatchingKeyField";
    public static final String SUMMARY_MATCHING_BIB_COUNT = "CountOfBibMatches";
    public static final String SUMMARY_NUM_ITEMS_AFFECTED = "CountOfItemAffected";

    public static final String MATCHING_LOCAL_BIB_ID = "LocalBibId";

    public static final String MATCH_POINT_FIELD_OCLC = "OCLCNumber";
    public static final String MATCH_POINT_FIELD_ISBN = "ISBN";
    public static final String MATCH_POINT_FIELD_ISSN = "ISSN";

    public static final String MATCH_POINT_FIELD_LCCN = "LCCN";
    public static final String ALL_INST = "ALL";

    public static final String OCLC_TAG = "035";
    public static final String ISBN_TAG = "020";
    public static final String ISSN_TAG = "022";
    public static final String LCCN_TAG = "010";

    //Report Types
    public static final String MATCHING_TYPE = "Matching";
    public static final String EXCEPTION_TYPE = "Exception";
    public static final String SUMMARY_TYPE = "Summary";

    //Transmission Types
    public static final String FILE_SYSTEM = "FileSystem";
    public static final String FTP = "FTP";


    public static final String SHARED_CGD = "Shared";
    public static final String OCLC_CRITERIA = "OCLC";

    public static final String ISBN_CRITERIA = "ISBN";
    public static final String ISSN_CRITERIA = "ISSN";
    public static final String LCCN_CRITERIA = "LCCN";

    public static final String OCLC_NUMBER = "OCLCNumber";

    public static final String BIB_COUNT = "bibCount";
    public static final String ITEM_COUNT = "itemCount";
    public static final String BIB_ITEM_COUNT = "bibItemCount";

    public static final String MATCHING_EXCEPTION_OCCURED = "MatchingExceptionOccurred";
    public static final String EXCEPTION_MSG = "ExceptionMessage";

    public static final String MATCHING_REPORT_ENTITY_MAP = "matchingReportEntityMap";
    public static final String EXCEPTION_REPORT_ENTITY_MAP = "exceptionReportEntityMap";

    //Error Message
    public static final String RECORD_NOT_AVAILABLE = "Database may be empty or Bib table does not contains this record";
    public static final String SERVER_ERROR_MSG = "Server is down for Maintenance Please Try again Later.";
    public static final String EMPTY_FACET_ERROR_MSG = "At least one Bib Facet Box and one Item Facet Box needs to be checked to get results.";

    //Search Response Types
    public static final String SEARCH_SUCCESS_RESPONSE = "SuccessResponse";
    public static final String SEARCH_ERROR_RESPONSE = "ErrorResponse";


    public static final String SOLR_CORE = "solrCore";
    public static final String SOLR_QUEUE = "scsbactivemq:queue:solrQ";

    public static final String SOLR_INDEX_FAILURE_REPORT = "Solr_Index_Failure_Report";
    public static final String SOLR_INDEX_EXCEPTION = "SolrIndexException";
    public static final String OWNING_INST_BIB_ID = "OwningInstitutionBibId";
    public static final String BIB_ID = "BibId";
    public static final String NA = "NA";

    //Collection
    public static final String UPDATE_CGD = "Update CGD";
    public static final String DEACCESSION = "Deaccession";
    public static final String TEMPLATE = "template";
    public static final String COLLECTION = "collection";
    public static final String GUEST = "guest";
    public static final String ITEM_BARCODES = "itemBarcodes";
    public static final String API_KEY = "api_key";
    public static final String RECAP = "recap";
    public static final String AVAILABLE = "Available";
    public static final String NOT_AVAILABLE = "Not Available";
    public static final String PERMANENT_WITHDRAWL_DIRECT = "Permanent Withdrawl Direct (PWD)";
    public static final String PERMANENT_WITHDRAWL_INDIRECT = "Permanent Withdrawl Indirect (PWI)";

    public static final String DEACCESSION_URL = "sharedCollection/deAccession";

    public static final String SUCCESS = "Success";
    public static final String FAILURE = "Failure";
    public static final String NO_RESULTS_FOUND = "No results found.";
    public static final String BARCODES_NOT_FOUND = "Barcode(s) not found";
    public static final String CGD_UPDATE_SUCCESSFUL = "The CGD has been successfully updated.";
    public static final String CGD_UPDATE_FAILED = "Updating CGD failed";
    public static final String DEACCESSION_SUCCESSFUL = "The item has been successfully deaccessioned.";
    public static final String DEACCESSION_FAILED = "Deaccessioning the item failed";

    public static final String SEARCH_RESULT_ERROR_NO_RECORDS_FOUND="No search results found. Please refine search conditions.";
    public static final String SEARCH_RESULT_ERROR_INVALID_CHARACTERS="No search results found. Search conditions, has invalid characters (double quotes [\"],open parenthesis [(], backslash [\\], curly braces[{}] and caret [^). Please refine search conditions.";
}
