package org.recap;

import java.util.Arrays;
import java.util.List;

/**
 * Created by SheikS on 6/20/2016.
 */
public final class ScsbConstants {
    public static final String MONOGRAPHIC_SET = "MonographicSet";
    public static final String FALSE = "false";
    public static final String CGD_CHANGE_LOG = "CGDChangeLog";
    public static final String CGD_CHANGE_LOG_SHARED_TO_PRIVATE = "SharedToPrivate";
    public static final String CGD_CHANGE_LOG_OPEN_TO_PRIVATE = "OpenToPrivate";
    public static final String TITLE_SUBFIELD_A = "Title_subfield_a";
    public static final String TITLE_MATCH_SOLR = "Title_match";
    public static final String USE_RESTRICTION_DISPLAY = "UseRestriction_display";
    public static final String SUCCESS = "success";
    public static final String TITLE_MATCHED = "Matched";
    //Camel Queues Constants
    public static final String FS_SUBMIT_COLLECTION_EXCEPTION_REPORT_Q = "scsbactivemq:queue:fsSubmitCollectionExceptionReportQ";
    public static final String FTP_SUBMIT_COLLECTION_EXCEPTION_REPORT_Q = "scsbactivemq:queue:ftpSubmitCollectionExceptionReportQ";
    public static final String FTP_SUBMIT_COLLECTION_SUMMARY_REPORT_Q = "scsbactivemq:queue:ftpSubmitCollectionSummaryReportQ";
    public static final String FS_SUBMIT_COLLECTION_SUMMARY_REPORT_Q = "scsbactivemq:queue:fsSubmitCollectionSummaryReportQ";
    public static final String FTP_SUBMIT_COLLECTION_SUCCESS_REPORT_Q = "scsbactivemq:queue:ftpSubmitCollectionSuccessReportQ";
    public static final String FS_SUBMIT_COLLECTION_SUCCESS_REPORT_Q = "scsbactivemq:queue:fsSubmitCollectionSuccessReportQ";
    public static final String FTP_SUBMIT_COLLECTION_FAILURE_REPORT_Q = "scsbactivemq:queue:ftpSubmitCollectionFailureReportQ";
    public static final String FS_SUBMIT_COLLECTION_FAILURE_REPORT_Q = "scsbactivemq:queue:fsSubmitCollectionFailureReportQ";
    public static final String FTP_ONGOING_ACCESSON_REPORT_Q = "scsbactivemq:queue:ftpOngoingAccessionReportQ";
    public static final String FS_ONGOING_ACCESSION_REPORT_Q = "scsbactivemq:queue:fsOngoingAccessionReportQ";
    public static final String FTP_SUBMIT_COLLECTION_REPORT_Q = "scsbactivemq:queue:ftpSubmitCollectionReportQ";
    public static final String FTP_DE_ACCESSION_SUMMARY_REPORT_Q = "scsbactivemq:queue:ftpDeAccessionSummaryReportQ";
    public static final String FS_ACCESSION_SUMMARY_REPORT_Q = "scsbactivemq:queue:fsAccessionSummaryReportQ";
    public static final String EMAIL_Q = "scsbactivemq:queue:solrClientEmailQ";
    public static final String FTP_SERIAL_MVM_REPORT_Q = "scsbactivemq:queue:ftpSerialMvmReportsQ";
    public static final String FTP_MATCHING_SUMMARY_REPORT_Q = "scsbactivemq:queue:ftpMatchingSummaryReportQ";
    public static final String S3_ONGOING_MATCHING_CGD_REPORT_Q = "scsbactivemq:queue:s3OngoingMatchingCGDReportQ";
    public static final String MATCHING_ALGORITHM_GROUPING_INDEX = "scsbactivemq:queue:matchingAlgorithmGroupIndex";

    //Camel Route Id Constants
    public static final String FS_SUBMIT_COLLECTION_REJECTION_REPORT_ID = "fsSubmitCollectionRejectionReport";
    public static final String FTP_SUBMIT_COLLECTION_REJECTION_REPORT_ID = "ftpSubmitCollectionRejectionReport";
    public static final String FS_SUBMIT_COLLECTION_EXCEPTION_REPORT_ID = "fsSubmitCollectionExceptionReport";
    public static final String FTP_SUBMIT_COLLECTION_EXCEPTION_REPORT_ID = "ftpSubmitCollectionExceptionReport";
    public static final String FTP_SUBMIT_COLLECTION_SUMMARY_REPORT_ID = "ftpSubmitCollectionSummaryReport";
    public static final String FS_SUBMIT_COLLECTION_SUMMARY_REPORT_ID = "fsSubmitCollectionSummaryReport";
    public static final String FTP_SUBMIT_COLLECTION_SUCCESS_REPORT_ID = "ftpSubmitCollectionSuccessReport";
    public static final String FS_SUBMIT_COLLECTION_SUCCESS_REPORT_ID = "fsSubmitCollectionSuccessReport";
    public static final String FTP_SUBMIT_COLLECTION_FAILURE_REPORT_ID = "ftpSubmitCollectionFailureReport";
    public static final String FS_SUBMIT_COLLECTION_FAILURE_REPORT_ID = "fsSubmitCollectionFailureReport";
    public static final String FTP_SUBMIT_COLLECTION_REPORT_ID = "ftpSubmitCollectionReportRoute";
    public static final String EMAIL_ROUTE_ID = "solrClientEmailQ";
    public static final String FTP_TITLE_EXCEPTION_REPORT_ROUTE_ID = "ftpTitleExceptionReportsRoute";
    public static final String FTP_SERIAL_MVM_REPORT_ROUTE_ID = "ftpSerialMvmReportsRoute";
    public static final String FTP_MATCHING_SUMMARY_REPORT_ROUTE_ID = "ftpMatchingSummaryReportRoute";
    public static final String S3_ONGOING_MATCHING_CGD_REPORT_ROUTE_ID = "s3OngoingMatchingCGDReportRoute";

    public static final String SUBMIT_COLLECTION_SUMMARY_Q_SUFFIX = "SubmitCollectionSummaryReportQ";
    public static final String SUMMARY_REPORT_FILE_NAME = "MatchingCGDSummaryReport";
    public static final String MATCHING_SUMMARY_MONOGRAPH = "MatchingMonographCGDSummary";
    public static final String MATCHING_SUMMARY_SERIAL = "MatchingSerialCGDSummary";
    public static final String MATCHING_SUMMARY_MVM = "MatchingMVMCGDSummary";
    public static final String TITLE_EXCEPTION_REPORT = "TitleExceptionReport";
    public static final String MATCHING_SUMMARY_REPORT = "MatchingSummaryReport";
    public static final String CGD_ROUND_TRIP_REPORT = "CGD_RoundTripReport";
    public static final String MATCHING_SERIAL_MVM_REPORT = "MatchingSerialMvmReport";
    public static final String UNDER_SCORE = "_";
    public static final String CSV_EXTENSION = ".csv";
    public static final String MATCHING_BIB_IDS = "matchingBibIds";
    public static final String MATCHING_REPORTS_SEND_EMAIL = "sendEmailForMatchingReports";
    public static final String MATCHING_CGD_REPORTS_SEND_EMAIL = "sendEmailForMatchingCGDReports";
    public static final String ACCESSION_REPORTS_SEND_EMAIL = "sendEmailForAccessionReports";

    public static final String DATE_FORMAT_FOR_FILE_NAME = "yyyyMMdd_HHmmss";
    public static final String DATE_FORMAT_FOR_REPORT_FILE_NAME = "ddMMMyyyyHHmmss";
    public static final String EST_TIMEZONE = "America/New_York";

    public static final String VOLUME_PART_YEAR = "VolumePartYear";

    public static final String SUMMARY_HOLDINGS = "SummaryHoldings";
    public static final String MATERIAL_TYPE = "MaterialType";
    public static final String INITIAL_MATCHING_OPERATION_TYPE = "InitialMatchingAlgorithm";
    public static final String ONGOING_MATCHING_OPERATION_TYPE = "OngoingMatchingAlgorithm";

    public static final String MATCHING_PENDING_BIBS = "PendingBibMatches";

    //Report Types For Matching Algorithm
    public static final String SINGLE_MATCH = "SingleMatch";
    public static final String MULTI_MATCH = "MultiMatch";

    public static final String MATERIAL_TYPE_EXCEPTION = "MaterialTypeException";
    public static final String TITLE_EXCEPTION_TYPE = "TitleException";
    public static final String CRITERIA_VALUES = "CriteriaValues";
    public static final String MATCH_POINT = "MatchPoint";

    public static final String IMS_LOCATION_CODE = "ImsLocation";
    public  static  final String FAILURE_UPDATE_CGD = "Failure: User is not allowed to update other instituion Item";

    //Error Message
    public static final String SERVER_ERROR_MSG = "Server is down for maintenance. Please try again later.";
    public static final String EMPTY_FACET_ERROR_MSG = "Check facets. At least one Bib Facet and one Item Facet must be checked to get results.";

    public static final String UNKNOWN_INSTITUTION = "UN";
    public static final String SCSB = "SCSB";

    public static final String OWNING_INSTITUTION_ITEM_ID = "OwningInstitutionItemId";
    public static final String BIB_CREATED_DATE = "BibCreatedDate";
    public static final String BIB_LAST_UPDATED_DATE = "BibLastUpdatedDate";
    public static final String MATCHING_IDENTIFIER = "MatchingIdentifier";
    public static final String MATCHING_QUALIFIER = "MAQualifier";
    public static final String DATE = "Date";
    public static final String COLUMN_CREATED_DATE = "CREATED_DATE";
    public static final String ITEM_BARCDE_DOESNOT_EXIST = "Item Barcode doesn't exist in SCSB database.";
    public static final String BIB_ITEM_DOESNOT_EXIST = "Bib Id doesn't exist in SCSB database.";

    public static final String FS_DE_ACCESSION_SUMMARY_REPORT_ID = "fsDeAccessionSummaryReportQ";
    public static final String FTP_DE_ACCESSION_SUMMARY_REPORT_ID = "ftpDeAccessionSummaryReportQ";

    public static final String FS_ONGOING_ACCESSION_REPORT_ID = "fsOngoingAccessionReportQ";
    public static final String FTP_ONGOING_ACCESSION_REPORT_ID = "ftpOngingAccessionReportQ";
    public static final String TRANSFER_REPORT = "Transfer_Report";
    public static final String FAILURE_BIB_REASON = "ReasonForFailureBib";
    public static final String FAILURE_ITEM_REASON = "ReasonForFailureItem";

    public static final String SUBMIT_COLLECTION_SUMMARY_REPORT = "SubmitCollectionSummary";
    public static final String REJECTION = "Rejection";
    public static final String EXCEPTION_TEXT = "Exception";
    public static final String DUMMY_CALL_NUMBER_TYPE = "dummycallnumbertype";
    public static final String POPULATE_DATA_FOR_DATA_DUMP_JOB = "populateDataForDataDumpJob";

    //solr
    public static final String BIB_ID_LIST = "BibIdList";
    public static final String BIB_ID_RANGE = "BibIdRange";
    public static final String DATE_RANGE = "DateRange";
    public static final String CGD_TYPE = "CGD";
    public static final String FROM_DATE = "FromDate";
    public static final String BIB_ID_RANGE_FROM = "BibIdRangeFrom";
    public static final String BIB_ID_RANGE_TO = "BibIdRangeTo";
    public static final String DATE_RANGE_FROM = "DateRangeFrom";
    public static final String DATE_RANGE_TO = "DateRangeTo";

    //Reports
    public static final String TRANSMISSION_TYPE = "TransmissionType";
    public static final String REPORT_TYPE = "ReportType";
    public static final String JOB_PARAM_DATA_FILE_NAME = "FileName";
    public static final String DATE_FORMAT_FOR_REPORTS = "yyyyMMdd_HHmmss";

    public static final String  BIBITEM_LASTUPDATED_DATE = "BibItemLastUpdatedDate";
    public static final String  ITEM_LASTUPDATED_DATE = "ItemLastUpdatedDate";
    public static final String  ITEM_CREATED_DATE = "ItemCreatedDate";
    public static final String  BIB_LASTUPDATED_DATE = "BibLastUpdatedDate";

    public static final String OWNING_INST = "owningInstitution";
    public static final String TO = "To";
    public static final String ITEM_CATALOGING_STATUS = "ItemCatalogingStatus";
    public static final String BIB_CATALOGING_STATUS = "BibCatalogingStatus";
    public static final String ONGOING_ACCESSION_REPORT = "Ongoing_Accession_Report";
    public static final String ERROR = "error->";
    public static final String EXCEPTION = "exception->";
    public static final String DUMMY_BIB_CONTENT_XML = "dummybibcontent.xml";
    public static final String DUMMY_HOLDING_CONTENT_XML = "dummyholdingcontent.xml";
    public static final String ITEM_ALREADY_ACCESSIONED = "Item already accessioned - Existing item details : ";

    public static final String STATUS_DONE="Status  : Done";
    public static final String TOTAL_TIME_TAKEN="**** Total Time Taken ";
    public static final String STATUS_FAILED="Status : Failed";
    public static final String PUL_MATCHING_COUNT="pulMatchingCount";
    public static final String CUL_MATCHING_COUNT="culMatchingCount";
    public static final String NYPL_MATCHING_COUNT="nyplMatchingCount";
    public static final String TOTAL_BIB_ID_PARTITION_LIST="Total Bib Id partition List : {}";
    public static final String NON_MONOGRAPH_RECORD_NUMS="NonMonographRecordNums";
    public static final String EXCEPTION_RECORD_NUMS="ExceptionRecordNums";
    //Permissions
    public static final String ROLE_SUPER_ADMIN = "Super Admin";
    public static final String ROLE_RECAP = "ReCAP";
    public static final Boolean BOOLEAN_TRUE = true;
    public static final Boolean BOOLEAN_FALSE = false;

    //Logger
    public static final String SCSB_PERSISTENCE_SERVICE_IS_UNAVAILABLE = "Scsb Persistence Service is Unavailable.";

    public static final String NUMBER_PATTERN="[^0-9]";

    public static final String LCCN_PATTERN="[\\^\\#]";

    public static final String FILE = "file:";
    public static final String DELETE_FILE_OPTION = "?delete=true";
    public static final String FILE_NAME = "fileName";
    public static final String ITEM_LAST_UPDATED_DATE = "ItemLastUpdatedDate";

    //deaccession report
    public static final String BIB_DOC_TYPE = "DocType:Bib";
    public static final String SOLR_BIB_ID = "BibId:";
    public static final String TITLE_DISPLAY = "Title_display";
    public static final String TITLE_245 = "Title_245";
    public static final String TITLE_246 = "Title_246";
    public static final String TITLE_130 = "Title_130";
    public static final String TITLE_730 = "Title_730";
    public static final String TITLE_740 = "Title_740";
    public static final String TITLE_830 = "Title_830";

    public static final String EMAIL_FOR = "emailFor";
    public static final String UPDATECGD = "updateCgd";
    public static final String BATCHJOB = "batchJob";
    public static final String PURGE_EXCEPTION_REQUESTS = "PurgeExceptionRequests";
    public static final String GENERATE_ACCESSION_REPORT_JOB = "GenerateAccessionReport";
    public static final String PENDING = "pending";
    public static final String MATCHING_REPORTS = "matchingReports";
    public static final String ACCESSION_REPORTS = "accessionReports";
    public static final String MATCHING_ALGORITHM_REPORTS = "MatchingAlgorithm Reports";
    public static final String ACCESSION_BATCH_COMPLETE = "Accession Batch Complete";
    public static final String INSTITUTION_NAME = "institutionName";

    public static final String MIXED_STATUS = "MixedStatus";

    public static final String DOC_TYPE_ITEM = "DocType:Item";
    public static final String ITEM_STATUS_INCOMPLETE = "ItemCatalogingStatus:Incomplete";
    public static final String IS_DELETED_ITEM_FALSE = "IsDeletedItem:false";
    public static final String BIB_ID = "BibId";
    public static final String ITEM_BIB_ID = "ItemBibId";
    public static final String AUTHOR_DISPLAY = "Author_display";

    public static final String SUBMIT_COLLECTION = "SubmitCollectionReport";
    public static final String OWN_INST_BIBID_LIST = "owningInstBibIdList";

    public static final String BULK_ACCESSION_SUMMARY = "BULK_ACCESSION_SUMMARY";
    public static final String ACCESSION_SUMMARY = "ACCESSION_SUMMARY";
    public static final String ACCESSION_JOB_FAILURE = "Exception occurred in SCSB Ongoing Accession Job";
    public static final String MATCHING_COUNTER_SHARED = "SharedCount";
    public static final String MATCHING_COUNTER_OPEN = "OpenCount";
    public static final String MATCHING_COUNTER_UPDATED_SHARED = "AfterSharedCount";
    public static final String MATCHING_COUNTER_UPDATED_OPEN = "AfterOpenCount";
    public static final String SHARED = "Shared";
    public static final String OPEN = "Open";
    public static final String COMMITTED="Committed";
    public static final String LOGGER_MSG = " : {0}";
    public static final List<String> MATCHING_MATCH_POINTS = Arrays.asList(ScsbCommonConstants.MATCH_POINT_FIELD_OCLC,ScsbCommonConstants.MATCH_POINT_FIELD_ISBN,ScsbCommonConstants.MATCH_POINT_FIELD_LCCN,ScsbCommonConstants.MATCH_POINT_FIELD_ISSN,ScsbCommonConstants.MATCH_POINT_FIELD_TITLE);
    public static final String LOG_EXECUTION_TIME = "Time taken to execute {} : {}";
    public static final String INSTITUTION = "Institution";
    public static final String CGD_ROUND_TRIP_EXCEPTION_MESSAGE = "The reported item is not found";
    public static final String EXCEPTION_TITLE_MATCH_REPORT = "Exception Occured while pulling records for TitleMatchReports:: {}";
    public static final String EXCEPTION_TITLE_MATCH_COUNT = "Exception Occured while pulling records for TitleMatchCount:: {} ";
    public static final String EXCEPTION_TITLE_MATCH_EXPORT = "Exception Occured while pulling records for TitleMatchExportReports:: {}";
    public static final String SINGLE_MATCH_TITLE_EXCEPTION = "SingleMatch-TitleException";
    public static final String MATCH_SCORE = "MatchScore";
    public static final String CGD = "cgd";

    public static class ServicePath {
        private ServicePath() {}
        public static final String REPLACE_REQUEST = "requestItem/replaceRequest";
    }

    public static final String INCOMPLETE_RESPONSE = "incompleteResponse";

    public static class Transfer {
        private Transfer() {}
        public static final String TRANSFER_REQUEST = "transfer";
        public static final String INSTITUTION_EMPTY = "Institution is empty";
        public static final String UNKNOWN_INSTITUTION = "Unknow institution";
        public static final String SOURCE_DESTINATION_ITEM_IDS_NOT_MATCHING = "Source and Destination item ids are not matching";
        public static final String SOURCE_DESTINATION_HOLDINGS_IDS_NOT_MATCHING = "Source and Destination holdings ids are not matching";
        public static final String SOURCE_EMPTY = "Source is empty";
        public static final String DESTINATION_EMPTY = "Destination is empty";
        public static final String SOURCE_OWN_INST_BIB_ID_EMPTY = "Source owning institution bib id is empty";
        public static final String SOURCE_OWN_INST_ITEM_ID_EMPTY = "Source owning institution item id is empty";
        public static final String DEST_OWN_INST_BIB_ID_EMPTY = "Destination owning institution bib id is empty";
        public static final String SOURCE_OWN_INST_HOLDINGS_ID_EMPTY = "Source owning institution holdings id is empty";
        public static final String DEST_OWN_INST_HOLDINGS_ID_EMPTY = "Destination owning institution holdings id is empty";
        public static final String DEST_OWN_INST_ITEM_ID_EMPTY = "Destination owning institution item id is empty";
        public static final String SOURCE_BIB_NOT_EXIST = "Source bib does not exist";
        public static final String SOURCE_HOLDING_NOT_UNDER_SOURCE_BIB = "Source holdings is not under source bib";
        public static final String SOURCE_ITEM_NOT_UNDER_SOURCE_HOLDING = "Source item is not under source holding";
        public static final String DEST_HOLDINGS_ATTACHED_WITH_DIFF_BIB = "Destination holdings is linked with another bib and not under destination bib";
        public static final String DEST_HOLDING_DEACCESSIONED="Destination holding is a deaccessioned holding";
        public static final String SOURCE_ITEM_DEACCESSIONED="Source item is a deaccessioned item";
        public static final String DEST_BIB_DEACCESSIONED="Destination Bib is a deaccessioned bib";
        public static final String SOURCE_HOLDING_DEACCESSIONED="Source Holding is a deaccessioned holding";

        public static final String SUCCESSFULLY_RELINKED = "Successfully relinked";
        public static final String RELINKED_FAILED = "Relinked Failed";
        public static final String COMPLETED = "Success";
        public static final String FAILED = "Failed";
        public static final String PARTIALLY_SUCCESS = "Partially Success";
        public static final String REQUEST = "Request";
        public static final String RESPONSE = "Response";
        public static final String INSTITUTION = "Institution";
        public static final String TRANSFER_TYPE = "TransferType";
        public static class TransferTypes {
            public static final String HOLDINGS_TRANSFER = "Holdings Transfer";
            public static final String ITEM_TRANSFER = "Item Transfer";
        }
        public static final String ROOT = "Root";
    }

    public static final String ITEM_STATUS_COMPLETE="ItemCatalogingStatus:Complete";
    public static final String BIB_STATUS_INCOMPLETE="BibCatalogingStatus:Incomplete";
    public static final String DISTINCT_VALUES_FALSE="{!distinctValues=false}Barcode";
    public static final String GROUP = "group";
    public static final String GROUP_FIELD = "group.field";
    public static final String INCREMENTAL_DUMP_TO_NOW = "TO NOW";

    public static final String MATCHING_ALGORITHM_JOB_INITIATE_ROUTE_ID = "scsbactivemq:queue:matchingAlgorithmInitiateRoute";
    public static final String MATCHING_BATCH_JOB_DATE_FORMAT = "EEE MMM dd HH:mm:ss z yyyy";
    public static final String SCSB_CAMEL_S3_TO_ENDPOINT = "aws-s3://{{scsbBucketName}}?autocloseBody=false&region={{awsRegion}}&accessKey=RAW({{awsAccessKey}})&secretKey=RAW({{awsAccessSecretKey}})";
    public static final String  SUBMIT_COLLECTION_REPORTS_BASE_PATH= "reports/collection/submitCollection/";
    public static final String  SUBMIT_COLLECTION_MANUAL_REPORTS_BASE_PATH= "reports/collection/submitCollection/supportTeamGeneratedReports/";
    public static final String PROTECTED = "protection";
    public static final String NOT_PROTECTED = "no_protection";

    public static final String TOTAL_RECORDS = "Total Records : ";
    public static final String TOTAL_PAGES = "Total Pages : ";
    public static final String UPDATE_ITEMS_Q = "updateItemsQ";
    public static final String SUBJECT = "subject";
    public static final String SUBJECT_MESSAGE_FOR_MATCHING_ALGORITHM = "The Reports for Matching Algorithm is available at the s3 location ";
    public static final String SMTPS_PREFIX = "smtps://";
    public static final String SMTPS_USERNAME = "?username=";
    public static final String SMTPS_PASSWORD = "&password=";
    public static final String EMAIL_HEADER_MESSAGE = "${header.emailPayLoad.message}";
    public static final String EMAIL_HEADER_SUBJECT = "${header.emailPayLoad.subject}";
    public static final String EMAIL_HEADER_TO = "${header.emailPayLoad.to}";
    public static final String EMAIL_HEADER_CC = "${header.emailPayLoad.cc}";
    public static final String MATCHING_REPORT_FILE_NAME_CAMEL_HEADER = "${in.header.fileName}-${date:now:ddMMMyyyyHHmmss}.csv";
    public static final String MATCHING_ALGORITHM_UPDATE_CGD_MESSAGE = "updateCGDBasedOnMaterialTypes, Match type is Single Match, TitleException Found";

    public static final String ONGOING_MATCHING_DATE_FORMAT = "yyyy/MM/dd";
    public static final String ONGOING_MATCHING_DATE_TIME_FORMAT = "dd-MM-yyyy hh:mm";
    public static final String CURRENT_PAGE = "Current Page";
    public static final String ROWS_UPDATED = "Rows Updated";
    public static final String RECORDS_UPDATED = "Records Updated";
    public static final String TITLE_MATCH_REPORT_PATH = "reports/title-match-reports/";
    public static final String TITLE_MATCH = "_Title_Match_";
    public static final String TITLE_MATCH_REPORT_MESSAGE =  "This report is being exported to S3 location at ";
    public static final String TITLE_MATCH_REPORT_MESSAGE_APPEND = ". Please check after 1 hr.";
    public static final String OCLCISBN = "OCLCNumber-ISBN";
    public static final String OCLCISSN = "OCLCNumber-ISSN";
    public static final String OCLCLCCN =  "OCLCNumber-LCCN";
    public static final String ISBNISSN = "ISBN-ISSN";
    public static final String ISBNLCCN = "ISBN-LCCN";
    public static final String ISSNLCCN = "ISSN-LCCN";
    public static final String OCLCTITLE = "OCLCNumber-TITLE";
    public static final String ISBNTITLE = "ISBN-TITLE";
    public static final String ISSNTITLE = "ISSN-TITLE";
    public static final String LCCNTITLE = "LCCN-TITLE";
    public static final String TITLE = "TITLE";
    public static final String IN_PROGRESS = "IN PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String TITLE_REPORT_MESSAGE_IN_PROGRESS = "Title Match export is going on for other request. Please try to export after sometime.";

    public static final String ONGOING_MA_BOTH_GROUPING_CGD_PROCESS = "bothGroupingAndCGDProcess";
    public static final String ONGOING_MA_UPDATE_CGD_PROCESS = "updateCGDProcess";
    public static final String ONGOING_MA_ONLY_GROUPING = "onlyGrouping";

    public static final String OCOLC = "OCoLC";

    private ScsbConstants(){}
}
