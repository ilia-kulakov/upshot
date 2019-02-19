package edu.aem.training.upshot.servlets;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.PageManager;
import edu.aem.training.upshot.beans.BusinessEventBean;
import edu.aem.training.upshot.beans.LinkBean;
import edu.aem.training.upshot.services.BusinessEventService;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

@SlingServlet(
        paths = "/bin/upshot/servlets/BusinessEventServlet"
        )
public class BusinessEventServlet extends SlingSafeMethodsServlet {

    Logger logger = LoggerFactory.getLogger(BusinessEventServlet.class);

    @Reference
    BusinessEventService eventService;

    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        JSONObject json = (request.getParameter("title") != null && request.getParameter("title").length() > 0) ?
                    getEvent(request) : getAllEvents(request);

        try {
            String id = UUID.randomUUID().toString();
            json.put("id", id);
            // Get the JSON formatted data
            String jsonData = json.toString();
            // Return the json formatted data
            response.getWriter().write(jsonData);

        } catch(JSONException e) {
            logger.info("ERROR: ", e.getMessage());
        }
    }

    private JSONObject getEvent(SlingHttpServletRequest request) throws ServletException, IOException {

        String title = request.getParameter("title");

        BusinessEventBean event = eventService.getEvent(title);

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

        JSONObject json = new JSONObject();
        try {
            // Encode the submitted form data to JSON
            JSONArray jsonEvents = new JSONArray();
            for (BusinessEventBean event : events)
            {
                jsonEvents.put(event.toJSONObject());
            }

            json.put("events", jsonEvents);
        } catch (JSONException e) {
            logger.info("ERROR: ", e.getMessage());
        }

        return json;
    }

}