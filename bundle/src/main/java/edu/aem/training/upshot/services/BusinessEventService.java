package edu.aem.training.upshot.services;

import edu.aem.training.upshot.beans.BusinessEventBean;
import org.apache.sling.api.SlingHttpServletRequest;

import java.util.List;

/**
 * A simple service interface
 */
public interface BusinessEventService {

    long getTotalEvents();

    List<BusinessEventBean> getAllEvents(SlingHttpServletRequest request, String sortField, String sortOrder, int pageSize, int pageNo);

    BusinessEventBean getEvent(SlingHttpServletRequest request, String id);
}