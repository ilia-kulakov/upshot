package edu.aem.training.upshot.servlets;

import edu.aem.training.upshot.beans.BusinessEventBean;
import edu.aem.training.upshot.services.BusinessEventService;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
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

        String json = (request.getParameter("title") != null && request.getParameter("title").length() > 0) ?
                    getEventJson(request) : getAllEventsJson(request);

        // Return the json formatted data
        response.getWriter().write(json);
    }

    private String getEventJson(SlingHttpServletRequest request) throws ServletException, IOException {

        String title = request.getParameter("title");

        BusinessEventBean event = eventService.getEvent(title);

        String json = "";

        // Encode the submitted form data to JSON
        if(event != null) {
            json = event.toJSONObject().toString();
        }

        return json;
    }

    private String getAllEventsJson(SlingHttpServletRequest request) throws ServletException, IOException {

        String sortField = request.getParameter("sortField");
        String sortOrder = request.getParameter("sortOrder");
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

        // Encode the submitted form data to JSON
        JSONArray jsonEvents = new JSONArray();
        for (BusinessEventBean event : events)
        {
            jsonEvents.put(event.toJSONObject());
        }

        return jsonEvents.toString();
    }

}