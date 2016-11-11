package org.recap.controller;

import org.codehaus.plexus.util.StringUtils;
import org.recap.RecapConstants;
import org.recap.model.solr.SolrIndexRequest;
import org.recap.report.ReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by angelind on 11/11/16.
 */
@Controller
public class GenerateReportController {

    Logger logger = LoggerFactory.getLogger(GenerateReportController.class);

    @Autowired
    ReportGenerator reportGenerator;

    @ResponseBody
    @RequestMapping(value = "/reportGeneration/generateReports", method = RequestMethod.POST)
    public String generateReports(@Valid @ModelAttribute("solrIndexRequest") SolrIndexRequest solrIndexRequest,
                                  BindingResult result,
                                  Model model) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Date createdDate = solrIndexRequest.getCreatedDate();
        if(createdDate == null) {
            createdDate = new Date();
        }
        String reportType = solrIndexRequest.getReportType();
        String generatedReportFileName = null;
        String owningInstitutionCode = solrIndexRequest.getOwningInstitutionCode();
        String status = "";
        String fileName = reportType.equalsIgnoreCase(RecapConstants.DEACCESSION_SUMMARY_REPORT) ? RecapConstants.DEACCESSION_REPORT : RecapConstants.SOLR_INDEX_FAILURE_REPORT;

        generatedReportFileName = reportGenerator.generateReport(fileName, owningInstitutionCode, reportType, solrIndexRequest.getTransmissionType(), getFromDate(createdDate), getToDate(createdDate));
        if(StringUtils.isEmpty(generatedReportFileName)) {
            status = "Report wasn't generated! Please contact help desk!";
        } else {
            status = "The Generated Report File Name : " + generatedReportFileName;
        }
        stopWatch.stop();
        logger.info("Total time taken to generate File : " + stopWatch.getTotalTimeSeconds());
        return status;
    }

    public Date getFromDate(Date createdDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(createdDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return  cal.getTime();
    }

    public Date getToDate(Date createdDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(createdDate);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

}
