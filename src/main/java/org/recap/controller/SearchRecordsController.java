package org.recap.controller;

import org.apache.commons.compress.utils.IOUtils;
import org.recap.RecapConstants;
import org.recap.model.search.SearchItemResultRow;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.model.search.SearchResultRow;
import org.recap.model.solr.BibItem;
import org.recap.model.solr.Holdings;
import org.recap.model.solr.Item;
import org.recap.repository.solr.main.BibSolrDocumentRepository;
import org.recap.repository.solr.main.ItemCrudRepository;
import org.recap.util.CsvUtil;
import org.recap.util.SearchRecordsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;

/**
 * Created by rajeshbabuk on 6/7/16.
 */

@Controller
public class SearchRecordsController {

    Logger logger = LoggerFactory.getLogger(SearchRecordsController.class);

    @Autowired
    BibSolrDocumentRepository bibSolrDocumentRepository;

    @Autowired
    ItemCrudRepository itemCrudRepository;

    @Autowired
    SearchRecordsUtil searchRecordsUtil;

    @Autowired
    private CsvUtil csvUtil;

    @RequestMapping("/search")
    public String searchRecords(Model model) {
        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        model.addAttribute("searchRecordsRequest", searchRecordsRequest);
        return "searchRecords";
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=search")
    public ModelAndView search(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                                  BindingResult result,
                                  Model model) {
        return new ModelAndView("searchRecords", "searchRecordsRequest", searchRecordsUtil.searchRecords(searchRecordsRequest));
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=previous")
    public ModelAndView searchPrevious(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                               BindingResult result,
                               Model model) {
        searchRecordsRequest.setSearchResultRows(null);
        searchRecordsUtil.searchAndBuildResults(searchRecordsRequest);
        return new ModelAndView("searchRecords", "searchRecordsRequest", searchRecordsRequest);
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=next")
    public ModelAndView searchNext(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                                   BindingResult result,
                                   Model model) {
        searchRecordsRequest.setSearchResultRows(null);
        searchRecordsUtil.searchAndBuildResults(searchRecordsRequest);
        return new ModelAndView("searchRecords", "searchRecordsRequest", searchRecordsRequest);
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=first")
    public ModelAndView searchFirst(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                                       BindingResult result,
                                       Model model) {
        searchRecordsRequest.setSearchResultRows(null);
        searchRecordsRequest.resetPageNumber();
        searchRecordsUtil.searchAndBuildResults(searchRecordsRequest);
        return new ModelAndView("searchRecords", "searchRecordsRequest", searchRecordsRequest);
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=last")
    public ModelAndView searchLast(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                                       BindingResult result,
                                       Model model) {
        searchRecordsRequest.setSearchResultRows(null);
        searchRecordsRequest.setPageNumber(searchRecordsRequest.getTotalPageCount() - 1);
        searchRecordsUtil.searchAndBuildResults(searchRecordsRequest);
        return new ModelAndView("searchRecords", "searchRecordsRequest", searchRecordsRequest);
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=clear")
    public ModelAndView clear(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                              BindingResult result,
                              Model model) {

        searchRecordsRequest.setFieldValue("");
        searchRecordsRequest.setOwningInstitutions(new ArrayList<>());
        searchRecordsRequest.setCollectionGroupDesignations(new ArrayList<>());
        searchRecordsRequest.setAvailability(new ArrayList<>());
        searchRecordsRequest.setMaterialTypes(new ArrayList<>());
        searchRecordsRequest.setUseRestrictions(new ArrayList<>());
        searchRecordsRequest.setShowResults(false);
        return new ModelAndView("searchRecords");
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=newSearch")
    public ModelAndView newSearch(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                                  BindingResult result,
                                  Model model) {
        searchRecordsRequest = new SearchRecordsRequest();
        model.addAttribute("searchRecordsRequest", searchRecordsRequest);
        return new ModelAndView("searchRecords");
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=request")
    public ModelAndView requestRecords(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                                  BindingResult result,
                                  Model model) {
        return new ModelAndView("searchRecords");
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=export")
    public byte[] exportRecords(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest, HttpServletResponse response,
                                  BindingResult result,
                                  Model model) throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileNameWithExtension = "ExportRecords_" + dateFormat.format(new Date()) + ".csv";
        File csvFile = csvUtil.writeSearchResultsToCsv(searchRecordsRequest.getSearchResultRows(), fileNameWithExtension);
        byte[] fileContent = IOUtils.toByteArray(new FileInputStream(csvFile));
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileNameWithExtension + "\"");
        response.setContentLength(fileContent.length);
        return fileContent;
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=pageSizeChange")
    public ModelAndView onPageSizeChange(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                                         BindingResult result,
                                         Model model) throws Exception {
        searchRecordsUtil.searchAndBuildResults(searchRecordsRequest);
        return new ModelAndView("searchRecords", "searchRecordsRequest", searchRecordsRequest);
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(1048576);
    }
}
