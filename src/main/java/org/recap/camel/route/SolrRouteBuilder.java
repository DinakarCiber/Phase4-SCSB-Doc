package org.recap.camel.route;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.recap.RecapConstants;
import org.recap.camel.processor.SolrPayloadProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by rajeshbabuk on 30/8/16.
 */
@Component
public class SolrRouteBuilder {

    Logger logger = LoggerFactory.getLogger(SolrRouteBuilder.class);

    @Autowired
    public SolrRouteBuilder(CamelContext camelContext, ProducerTemplate producerTemplate,
                            @Value("${solr.url}") String solrUri,
                            @Value("${solr.router.uri.type}") String solrRouterURI,
                            @Value("${solr.parent.core}") String solrCore) {

        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    SolrPayloadProcessor solrPayloadProcessor = new SolrPayloadProcessor(solrUri, solrCore, solrRouterURI, producerTemplate, camelContext);
                    from(RecapConstants.SOLR_QUEUE).process(solrPayloadProcessor);
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
