package edu.aem.training.upshot.services;

import edu.aem.training.upshot.beans.BusinessEventBean;

import java.util.List;

/**
 * A simple service interface
 */
public interface BusinessEventService {

    List<BusinessEventBean> getAllEvents(String sortField, String sortOrder, int pageSize, int pageNo);

    BusinessEventBean getEvent(String title);
}