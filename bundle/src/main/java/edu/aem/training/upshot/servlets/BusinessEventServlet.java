package edu.aem.training.upshot.servlets;

import edu.aem.training.upshot.beans.BusinessEventBean;
import edu.aem.training.upshot.services.BusinessEventService;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

@SlingServlet(
        paths = "/bin/upshot/servlets/BusinessEventServlet"
        )
public class BusinessEventServlet extends SlingSafeMethodsServlet {

    Logger logger = LoggerFactory.getLogger(BusinessEventServlet.class);

    private static final String PARAM_ID = "id";
    private static final String PARAM_SORT_FIELD = "sortField";
    private static final String PARAM_SORT_ORDER = "sortOrder";
    private static final String PARAM_TABULATOR_SORT_FIELD = "sorters[0][field]";
    private static final String PARAM_TABULATOR_SORT_ORDER = "sorters[0][dir]";
    private static final String PARAM_PAGE_SIZE = "pageSize";
    private static final String PARAM_PAGE_NO = "pageNo";

    private static final String JSON_PARAM_EVENTS = "events";
    private static final String JSON_PARAM_MAX_PAGES = "maxPages";
    private static final String JSON_PARAM_TOTAL_EVENTS = "totalEvents";

    @Reference
    BusinessEventService eventService;

    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        JSONObject json = (request.getParameter(PARAM_ID) != null && request.getParameter(PARAM_ID).length() > 0) ?
                    getEvent(request) : getAllEvents(request);



        // Get the JSON formatted data
        String jsonData = json.toString();
        // Return the json formatted data
        response.getWriter().write(jsonData);
    }

    private JSONObject getEvent(SlingHttpServletRequest request) throws ServletException, IOException {

        String id = request.getParameter(PARAM_ID);

        BusinessEventBean event = eventService.getEvent(request, id);

        JSONObject json = null;

        // Encode the submitted form data to JSON
        if(event != null) {
            json = event.toJSONObject();
        } else {
            json = new JSONObject();
        }

        return json;
    }

    private JSONObject getAllEvents(SlingHttpServletRequest request) throws ServletException, IOException {

        String sortField = (request.getParameter(PARAM_SORT_FIELD) != null) ?
                request.getParameter(PARAM_SORT_FIELD) : request.getParameter(PARAM_TABULATOR_SORT_FIELD);
        String sortOrder = (request.getParameter(PARAM_SORT_ORDER) != null) ?
                request.getParameter(PARAM_SORT_ORDER) : request.getParameter(PARAM_TABULATOR_SORT_ORDER);
        String pageSizeStr = request.getParameter(PARAM_PAGE_SIZE);
        String pageNoStr = request.getParameter(PARAM_PAGE_NO);

        int pageSize, pageNo;

        try {
            pageSize = Integer.parseInt(pageSizeStr);
        } catch (NumberFormatException e) {
            pageSize = 0;
        }

        try {
            pageNo = Integer.parseInt((pageNoStr));
        } catch (NumberFormatException e) {
            pageNo = 0;
        }


        List<BusinessEventBean> events = eventService.getAllEvents(request, sortField, sortOrder, pageSize, pageNo);
        long totalEvents = eventService.getTotalEvents();

        JSONObject json = new JSONObject();
        try {
            // Encode the submitted form data to JSON
            JSONArray jsonEvents = new JSONArray();
            for (BusinessEventBean event : events)
            {
                jsonEvents.put(event.toJSONObject());
            }

            json.put(JSON_PARAM_EVENTS, jsonEvents);
            if(pageSize > 0) {
                json.put(JSON_PARAM_MAX_PAGES, 1 + Math.floor(totalEvents / pageSize));
            }
            json.put(JSON_PARAM_TOTAL_EVENTS, totalEvents);
        } catch (JSONException e) {
            logger.info("ERROR: ", e.getMessage());
        }

        return json;
    }

}