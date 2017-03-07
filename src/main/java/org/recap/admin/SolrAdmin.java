package org.recap.admin;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.params.CoreAdminParams;
import org.recap.RecapConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by pvsubrah on 6/12/16.
 */
@Component
public class SolrAdmin {

    private static final Logger logger = LoggerFactory.getLogger(SolrAdmin.class);

    @Value("${solr.configsets.dir}")
    String configSetsDir;

    @Value("${solr.solr.home}")
    String solrHome;

    @Value("${solr.parent.core}")
    String solrParentCore;

    @Autowired
    private SolrClient solrAdminClient;

    @Autowired
    private SolrClient solrClient;

    private CoreAdminRequest coreAdminRequest;

    private CoreAdminRequest.Create coreAdminCreateRequest;

    private CoreAdminRequest.Unload coreAdminUnloadRequest;


    public CoreAdminResponse createSolrCores(List<String> coreNames) {
        CoreAdminRequest.Create coreAdminRequest = getCoreAdminCreateRequest();
        CoreAdminResponse coreAdminResponse = null;

        for (Iterator<String> iterator = coreNames.iterator(); iterator.hasNext(); ) {
            String coreName = iterator.next();
            String dataDir = solrHome + coreName + File.separator + "data";

            coreAdminRequest.setCoreName(coreName);
            coreAdminRequest.setConfigSet("recap_config");
            coreAdminRequest.setInstanceDir(solrHome + File.separator + coreName);
            coreAdminRequest.setDataDir(dataDir);

            try {
                if (!isCoreExist(coreName)) {
                    coreAdminResponse = coreAdminRequest.process(solrAdminClient);
                    if (coreAdminResponse.getStatus() == 0) {
                        logger.info("Created Solr core with name: {}",coreName);
                    } else {
                        logger.error("Error in creating Solr core with name: {}",coreName);
                    }
                } else {
                    logger.info("Solr core with name {} already exists.",coreName);
                }
            } catch (SolrServerException | IOException e) {
                logger.error(RecapConstants.LOG_ERROR,e);
            }
        }

        return coreAdminResponse;
    }

    public void mergeCores(List<String> coreNames) {
        List<String> tempCores = new ArrayList();
        List<String> tempCoreNames = new ArrayList();

        for (Iterator<String> iterator = coreNames.iterator(); iterator.hasNext(); ) {
            String coreName = iterator.next();
            tempCores.add(solrHome + File.separator + coreName + File.separator + "data" + File.separator + "index");
            tempCoreNames.add(coreName);
        }

        String[] indexDirs = tempCores.toArray(new String[tempCores.size()]);
        String[] tempCoreNamesObjectArray = tempCoreNames.toArray(new String[tempCores.size()]);
        try {
            getCoreAdminRequest().mergeIndexes(solrParentCore, indexDirs, tempCoreNamesObjectArray, solrAdminClient);
            solrClient.commit();
        } catch (SolrServerException | IOException e) {
            logger.error(RecapConstants.LOG_ERROR,e);
        }
    }

    public void unLoadCores(List<String> coreNames){
        for (Iterator<String> iterator = coreNames.iterator(); iterator.hasNext(); ) {
            String coreName = iterator.next();
            try {
                CoreAdminResponse adminResponse = getCoreAdminRequest().unloadCore(coreName, true, true, solrAdminClient);
            } catch (SolrServerException | IOException e) {
                logger.error(RecapConstants.LOG_ERROR,e);
            }

        }
    }


    public void unloadTempCores() throws IOException, SolrServerException {
        CoreAdminRequest coreAdminRequest = getCoreAdminRequest();

        coreAdminRequest.setAction(CoreAdminParams.CoreAdminAction.STATUS);
        CoreAdminResponse cores = coreAdminRequest.process(solrAdminClient);

        List<String> coreList = new ArrayList<>();
        for (int i = 0; i < cores.getCoreStatus().size(); i++) {
            String name = cores.getCoreStatus().getName(i);
            if (name.contains("temp")) {
                coreList.add(name);
            }
        }

        unLoadCores(coreList);
    }

    public CoreAdminRequest.Create getCoreAdminCreateRequest() {
        coreAdminCreateRequest = new CoreAdminRequest.Create();
        return coreAdminCreateRequest;
    }

    public CoreAdminRequest.Unload getCoreAdminUnloadRequest() {
        coreAdminUnloadRequest = new CoreAdminRequest.Unload(true);
        return coreAdminUnloadRequest;
    }

    public CoreAdminRequest getCoreAdminRequest() {
        if (null == coreAdminRequest) {
            coreAdminRequest = new CoreAdminRequest();
        }
        return coreAdminRequest;
    }

    public boolean isCoreExist(String coreName) throws IOException, SolrServerException {
        CoreAdminRequest coreAdminRequest = getCoreAdminRequest();
        coreAdminRequest.setAction(CoreAdminParams.CoreAdminAction.STATUS);
        CoreAdminResponse cores = coreAdminRequest.process(solrAdminClient);
        for (int i = 0; i < cores.getCoreStatus().size(); i++) {
            String name = cores.getCoreStatus().getName(i);
            if (name.equals(coreName)) {
                return true;
            }
        }
        return false;
    }

    public Integer getCoresStatus() {
        CoreAdminRequest coreAdminRequest = getCoreAdminCreateRequest();
        coreAdminRequest.setAction(CoreAdminParams.CoreAdminAction.STATUS);
        try {
            CoreAdminResponse coresStatusResponse = coreAdminRequest.process(solrAdminClient);
            return coresStatusResponse.getStatus();
        } catch (SolrServerException | IOException e) {
            logger.error(RecapConstants.LOG_ERROR,e);
        }
        return null;
    }
}
