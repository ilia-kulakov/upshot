package edu.aem.training.upshot.servlets;

import edu.aem.training.upshot.beans.LinkBean;
import edu.aem.training.upshot.services.TextFinderService;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jcr.Session;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

@SlingServlet(
        paths = "/bin/upshot/servlets/TextFinderServlet"
        )
public class TextFinderServlet extends SlingSafeMethodsServlet {

    Logger logger = LoggerFactory.getLogger(TextFinderServlet.class);

    @Reference
    TextFinderService textFinderService;

    private Session session;
    private ResourceResolver resourceResolver;

    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        logger.info("Start searching");

        resourceResolver = request.getResourceResolver();
        session = resourceResolver.adaptTo(Session.class);

        // Get the submitted form data
        String id = UUID.randomUUID().toString();
        //request.setCharacterEncoding("UTF-8"); // Instead this configure Apache Sling Request Parameter Handling

        String searchPath = request.getParameter("searchPath");
        String searchText = request.getParameter("searchText");
        String queryEngine = request.getParameter("queryEngine");

        logger.info("Char Encoding: " + request.getCharacterEncoding());

        logger.info("searchPath: " + searchPath);
        logger.info("searchText: " + searchText);
        logger.info("queryEngine: " + queryEngine);



        List<LinkBean> links = find(request, searchText, searchPath, queryEngine);

        try {
            // Encode the submitted form data to JSON
            JSONArray jsonLinks = new JSONArray();
            for (LinkBean link : links)
            {
                jsonLinks.put(link.toJSONObject());
            }

            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("links", jsonLinks);

            // Get the JSON formatted data
            String jsonData = json.toString();
            // Return the json formatted data
            response.getWriter().write(jsonData);
        } catch (JSONException e) {
            logger.info("ERROR: ", e.getMessage());
        }
    }

    private List<LinkBean> find(SlingHttpServletRequest request, String searchText, String searchPath, String queryEngine) {

        if("sql2".equals(queryEngine)) {

            return textFinderService.findBySql2(request, searchText, searchPath);

        } else if("xpath".equals(queryEngine)) {

            return textFinderService.findByXpath(request, searchText, searchPath);

        } else {

            return textFinderService.findByPredicates(request, searchText, searchPath);
        }
    }
}