package org.recap.controller;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.util.StringUtils;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.reports.TitleMatchedReport;
import org.recap.model.solr.SolrIndexRequest;
import org.recap.model.submitCollection.SubmitCollectionReport;
import org.recap.report.ReportGenerator;
import org.recap.util.AWSUtil;
import org.recap.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by angelind on 11/11/16.
 */
@Slf4j
@RestController
@RequestMapping("/reportGeneration")
public class GenerateReportController {

    @Autowired
    private AWSUtil awsUtil;



    @Autowired
    private ReportGenerator reportGenerator;

    @Autowired
    private DateUtil dateUtil;

    @Value("${" + PropertyKeyConstants.SCSB_BUCKET_NAME + "}")
    private String s3BucketName;

    /**
     * This method is used to generate reports appropriately depending on the report type selected in UI.
     *
     * @param solrIndexRequest the solr index request
     * @param result           the result
     * @param model            the model
     * @return the string
     */
    @PostMapping(value = "/generateReports")
    public String generateReports(@Valid @ModelAttribute("solrIndexRequest") SolrIndexRequest solrIndexRequest,
                                  BindingResult result,
                                  Model model) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Date createdDate = solrIndexRequest.getCreatedDate();
        if (createdDate == null) {
            createdDate = new Date();
        }
        Date toDate = solrIndexRequest.getToDate();
        if (toDate == null) {
            toDate = new Date();
        }
        String reportType = solrIndexRequest.getReportType();
        String generatedReportFileName;
        String owningInstitutionCode = solrIndexRequest.getOwningInstitutionCode();
        String status;
        String fileName;
        if (reportType.equalsIgnoreCase(ScsbCommonConstants.DEACCESSION_SUMMARY_REPORT)) {
            fileName = ScsbCommonConstants.DEACCESSION_REPORT;
        } else if (reportType.equalsIgnoreCase(ScsbCommonConstants.ACCESSION_SUMMARY_REPORT) || reportType.equalsIgnoreCase(ScsbConstants.ONGOING_ACCESSION_REPORT)) {
            fileName = ScsbCommonConstants.ACCESSION_REPORT;
        } else if (reportType.equalsIgnoreCase(ScsbCommonConstants.SUBMIT_COLLECTION_REJECTION_REPORT)
                || reportType.equalsIgnoreCase(ScsbCommonConstants.SUBMIT_COLLECTION_EXCEPTION_REPORT)
                || reportType.equalsIgnoreCase(ScsbCommonConstants.SUBMIT_COLLECTION_SUCCESS_REPORT)
                || reportType.equalsIgnoreCase(ScsbCommonConstants.SUBMIT_COLLECTION_FAILURE_REPORT)
                || reportType.equalsIgnoreCase(ScsbConstants.SUBMIT_COLLECTION_SUMMARY_REPORT)) {
            fileName = ScsbCommonConstants.SUBMIT_COLLECTION_REPORT;
        } else {
            fileName = ScsbCommonConstants.SOLR_INDEX_FAILURE_REPORT;
        }
        generatedReportFileName = reportGenerator.generateReport(fileName, owningInstitutionCode, reportType, solrIndexRequest.getTransmissionType(), dateUtil.getFromDate(createdDate), dateUtil.getToDate(toDate));
        if (StringUtils.isEmpty(generatedReportFileName)) {
            status = "Report wasn't generated! Please contact help desk!";
        } else {
            status = "The Generated Report File Name : " + generatedReportFileName;
        }
        stopWatch.stop();
        log.info("Total time taken to generate File : {}" , stopWatch.getTotalTimeSeconds());
        return status;
    }

    /**
     *
     * @param submitCollectionReprot
     * @return
     * @throws ParseException
     */
    @PostMapping("/submitCollectionReport")
    public ResponseEntity<SubmitCollectionReport> submitCollectionReports(@RequestBody SubmitCollectionReport submitCollectionReprot) throws ParseException {
        return (!submitCollectionReprot.isExportEnabled()) ?
                new ResponseEntity<>(reportGenerator.submitCollectionExceptionReportGenerator(submitCollectionReprot), HttpStatus.OK) :
                new ResponseEntity<>(reportGenerator.submitCollectionExceptionReportExport(submitCollectionReprot), HttpStatus.OK);
    }

    /**
     *
     * @param submitCollectionReprot
     * @return
     * @throws ParseException
     */
    @PostMapping("/accessionException")
    public ResponseEntity<SubmitCollectionReport> accessionException(@RequestBody SubmitCollectionReport submitCollectionReprot) throws ParseException {
        return (!submitCollectionReprot.isExportEnabled()) ?
                new ResponseEntity<>(reportGenerator.accessionExceptionReportGenerator(submitCollectionReprot), HttpStatus.OK) :
                new ResponseEntity<>(reportGenerator.accessionExceptionReportExport(submitCollectionReprot), HttpStatus.OK);
    }

    /**
     *
     * @param titleMatchedReport
     * @return List of TitleMatchCount
     * @throws ParseException
     */
    @PostMapping("/titleMatchCount")
    public ResponseEntity<TitleMatchedReport> titleMatchCount(@RequestBody TitleMatchedReport titleMatchedReport) throws ParseException {
        return  new ResponseEntity<>(reportGenerator.getItemMatchCount(titleMatchedReport),HttpStatus.OK);
    }

    /**
     *
     * @param titleMatchedReport
     * @return List of TitleMatchReport
     * @throws ParseException
     */
    @PostMapping("/titleMatchReport")
    public ResponseEntity<TitleMatchedReport> titleMatchReportPreviw(@RequestBody TitleMatchedReport titleMatchedReport) throws ParseException {
        return  new ResponseEntity<>(reportGenerator.getItemMatchReportPreview(titleMatchedReport),HttpStatus.OK);
    }

    /**
     *
     * @param titleMatchedReport
     * @return List of TitleMatchReport
     * @throws ParseException
     */
    @PostMapping("/titleMatchReportExport")
    public ResponseEntity<TitleMatchedReport> titleMatchReportExport(@RequestBody TitleMatchedReport titleMatchedReport) throws ParseException {
        return  new ResponseEntity<>(reportGenerator.getItemMatchReportExport(titleMatchedReport),HttpStatus.OK);
    }
    /**
     *
     * @param titleMatchedReport
     * @return List of TitleMatchReport
     * @throws ParseException
     */
    @PostMapping("/title-match-report-export-s3")
    public ResponseEntity<TitleMatchedReport> titleMatchReportExportS3(@RequestBody TitleMatchedReport titleMatchedReport) throws ParseException {
        reportGenerator.getItemMatchReportExportS3(titleMatchedReport);
        return  new ResponseEntity<>(titleMatchedReport,HttpStatus.OK);
    }

    @GetMapping("/s3-to-local-data")
    public ResponseEntity<String> awsFileDownload() throws ParseException {
        try {
            awsUtil.copyFromAWSToLocal();
        } catch (Exception e) {
            log.info("exception occurred awsFileDownload : {}",e.fillInStackTrace());
        }
        return  new ResponseEntity<>("OKAY",HttpStatus.OK);
    }
}
