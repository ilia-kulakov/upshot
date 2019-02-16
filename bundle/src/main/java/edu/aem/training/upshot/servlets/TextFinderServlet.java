package edu.aem.training.upshot.servlets;

import jdk.nashorn.internal.ir.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.UUID;

@SlingServlet(
        paths = "/bin/upshot/servlets/TextFinderServlet",
        methods = "POST",
        metatype = true)
public class TextFinderServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 4786478563478698653L;

    @Reference
    private SlingRepository repository;

    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        // Get the submitted form data
        String id = UUID.randomUUID().toString();
        String searchText = request.getParameter("searchText");

        // Encode the submitted form data to JSON
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("searchText", searchText);

        // Get the JSON formatted data
        String jsonData = json.toJSONString();

        // Return the json formatted data
        response.getWriter().write(jsonData);
    }
}