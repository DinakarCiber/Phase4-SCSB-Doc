package org.recap.camel.route;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.recap.RecapConstants;
import org.recap.model.csv.SolrExceptionReportReCAPCSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by angelind on 30/9/16.
 */

@Component
public class FTPSolrExceptionRecordRouteBuilder {

    @Autowired
    public FTPSolrExceptionRecordRouteBuilder(CamelContext context,
                                         @Value("${ftp.userName}") String ftpUserName, @Value("${ftp.solr.remote.server}") String ftpRemoteServer,
                                         @Value("${ftp.knownHost}") String ftpKnownHost, @Value("${ftp.privateKey}") String ftpPrivateKey) {

        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(RecapConstants.FTP_SOLR_EXCEPTION_REPORT_Q)
                            .routeId(RecapConstants.FTP_SOLR_EXCEPTION_REPORT_ROUTE_ID)
                            .marshal().bindy(BindyType.Csv, SolrExceptionReportReCAPCSVRecord.class)
                            .to("sftp://" + ftpUserName + "@" + ftpRemoteServer + "?privateKeyFile=" + ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost + "&fileName=${in.header.fileName}-${date:now:ddMMMyyyy}.csv")
                            .onCompletion().log("File has been uploaded to ftp successfully.");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
