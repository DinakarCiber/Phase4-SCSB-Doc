package org.recap.camel.route;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.recap.RecapConstants;
import org.recap.model.csv.SummaryReportReCAPCSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by angelind on 31/8/16.
 */
@Component
public class CSVSummaryRecordRouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CSVSummaryRecordRouteBuilder.class);

    /**
     * This route builder is used to generate matching algorithm summary report to the file system.
     *
     * @param context                  the context
     * @param matchingReportsDirectory the matching reports directory
     */
    @Autowired
    public CSVSummaryRecordRouteBuilder(CamelContext context, @Value("${matching.report.directory}") String matchingReportsDirectory) {
        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(RecapConstants.CSV_SUMMARY_ALGO_REPORT_Q)
                            .routeId(RecapConstants.CSV_SUMMARY_ALGO_REPORT_ROUTE_ID)
                            .marshal().bindy(BindyType.Csv, SummaryReportReCAPCSVRecord.class)
                            .to("file:" + matchingReportsDirectory + File.separator + "?fileName=${in.header.fileName}-${date:now:ddMMMyyyy}.csv")
                            .onCompletion().log("File has been created successfully.");
                }
            });
        } catch (Exception e) {
            logger.error(RecapConstants.LOG_ERROR,e);
        }
    }
}
