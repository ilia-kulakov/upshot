package edu.aem.training.upshot.services;

import edu.aem.training.upshot.beans.LinkBean;
import org.apache.sling.api.SlingHttpServletRequest;

import java.util.List;

/**
 * A simple service interface
 */
public interface TextFinderService {

    List<LinkBean> findBySql2(SlingHttpServletRequest request, String searchText, String searchPath);

    List<LinkBean> findByXpath(SlingHttpServletRequest request, String searchText, String searchPath);

    List<LinkBean> findByPredicates(SlingHttpServletRequest request, String searchText, String searchPath);
}