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

    @Reference
    BusinessEventService eventService;

    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        JSONObject json = (request.getParameter("id") != null && request.getParameter("id").length() > 0) ?
                    getEvent(request) : getAllEvents(request);

        // Get the JSON formatted data
        String jsonData = json.toString();
        // Return the json formatted data
        response.getWriter().write(jsonData);
    }

    private JSONObject getEvent(SlingHttpServletRequest request) throws ServletException, IOException {

        String id = request.getParameter("id");

        BusinessEventBean event = eventService.getEvent(id);

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

        String sortField = (request.getParameter("sortField") != null) ?
                request.getParameter("sortField") : request.getParameter("sorters[0][field]");
        String sortOrder = (request.getParameter("sortOrder") != null) ?
                request.getParameter("sortOrder") : request.getParameter("sorters[0][dir]");
        String pageSizeStr = request.getParameter("pageSize");
        String pageNoStr = request.getParameter("pageNo");

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


        List<BusinessEventBean> events = eventService.getAllEvents(sortField, sortOrder, pageSize, pageNo);
        long totalEvents = eventService.getTotalEvents();

        JSONObject json = new JSONObject();
        try {
            // Encode the submitted form data to JSON
            JSONArray jsonEvents = new JSONArray();
            for (BusinessEventBean event : events)
            {
                jsonEvents.put(event.toJSONObject());
            }

            json.put("events", jsonEvents);
            if(pageSize > 0) {
                json.put("maxPages", 1 + Math.floor(totalEvents / pageSize));
            }
            json.put("totalEvents", totalEvents);
        } catch (JSONException e) {
            logger.info("ERROR: ", e.getMessage());
        }

        return json;
    }

}