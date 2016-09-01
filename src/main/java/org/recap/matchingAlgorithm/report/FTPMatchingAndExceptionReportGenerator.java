package org.recap.matchingAlgorithm.report;

import org.apache.camel.ProducerTemplate;
import org.recap.RecapConstants;
import org.recap.matchingAlgorithm.service.MatchingAlgorithmHelperService;
import org.recap.model.csv.MatchingReportReCAPCSVRecord;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.jpa.ReportDetailRepository;
import org.recap.util.ReCAPCSVMatchingRecordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by angelind on 23/8/16.
 */

@Component
public class FTPMatchingAndExceptionReportGenerator implements ReportGeneratorInterface {

    Logger logger = LoggerFactory.getLogger(FTPMatchingAndExceptionReportGenerator.class);

    @Autowired
    ReportDetailRepository reportDetailRepository;

    @Autowired
    ProducerTemplate producer;

    @Autowired
    MatchingAlgorithmHelperService matchingAlgorithmHelperService;

    @Override
    public boolean isInterested(String reportType) {
        return (reportType.equalsIgnoreCase(RecapConstants.MATCHING_TYPE) || reportType.equalsIgnoreCase(RecapConstants.EXCEPTION_TYPE)) ? true : false;
    }

    @Override
    public boolean isTransmitted(String transmissionType) {
        return transmissionType.equalsIgnoreCase(RecapConstants.FTP) ? true : false;
    }

    @Override
    public String generateReport(String fileName, String reportType, Date from, Date to) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<MatchingReportReCAPCSVRecord> matchingReportReCAPCSVRecords = new ArrayList<>();

        List<ReportEntity> reportEntityList = reportDetailRepository.findByFileAndInstitutionAndTypeAndDateRange(fileName, RecapConstants.ALL_INST, reportType, from, to);

        stopWatch.stop();
        logger.info("Total Time taken to fetch Report Entities From DB : " + stopWatch.getTotalTimeSeconds());
        logger.info("Total Num of Report Entities Fetched From DB : " + reportEntityList.size());

        stopWatch = new StopWatch();
        stopWatch.start();

        ReCAPCSVMatchingRecordGenerator reCAPCSVMatchingRecordGenerator = new ReCAPCSVMatchingRecordGenerator();
        for(ReportEntity reportEntity : reportEntityList) {
            MatchingReportReCAPCSVRecord matchingReportReCAPCSVRecord = reCAPCSVMatchingRecordGenerator.prepareMatchingReportReCAPCSVRecord(reportEntity, new MatchingReportReCAPCSVRecord());
            matchingReportReCAPCSVRecords.add(matchingReportReCAPCSVRecord);
        }

        stopWatch.stop();
        logger.info("Total time taken to prepare CSVRecords : " + stopWatch.getTotalTimeSeconds());
        logger.info("Total Num of CSVRecords Prepared : " + matchingReportReCAPCSVRecords.size());

        if(!CollectionUtils.isEmpty(matchingReportReCAPCSVRecords)) {
            matchingReportReCAPCSVRecords.sort(Comparator.comparing(MatchingReportReCAPCSVRecord::getTitle));

            producer.sendBodyAndHeader(RecapConstants.FTP_MATCHING_ALGO_REPORT_Q, matchingReportReCAPCSVRecords, RecapConstants.REPORT_FILE_NAME, fileName);

            DateFormat df = new SimpleDateFormat(RecapConstants.DATE_FORMAT_FOR_FILE_NAME);
            String generatedFileName = fileName + "-" + df.format(new Date()) + ".csv";
            return generatedFileName;
        }

        return null;
    }
}
