package org.recap.camel.route;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.recap.RecapConstants;
import org.recap.camel.processor.ReportProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by angelind on 28/9/16.
 */
@Component
public class ReportsRouteBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ReportsRouteBuilder.class);

    @Autowired
    public ReportsRouteBuilder(CamelContext camelContext, ReportProcessor reportProcessor) {
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(RecapConstants.REPORT_Q)
                            .routeId(RecapConstants.REPORT_ROUTE_ID)
                            .process(reportProcessor);
                }
            });
        } catch (Exception e) {
            logger.error(RecapConstants.LOG_ERROR,e);
        }
    }
}
