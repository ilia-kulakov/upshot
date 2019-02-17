package edu.aem.training.upshot.servlets;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import edu.aem.training.upshot.beans.LinkBean;
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

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

@SlingServlet(
        paths = "/bin/upshot/servlets/TextFinderServlet"
        )
public class TextFinderServlet extends SlingSafeMethodsServlet {

    Logger logger = LoggerFactory.getLogger(TextFinderServlet.class);

    @Reference
    private SlingRepository repository;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private QueryBuilder queryBuilder;

    private Session session;

    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        // Get the submitted form data
        String id = UUID.randomUUID().toString();
        String searchPath = request.getParameter("searchPath");
        String searchText = request.getParameter("searchText");
        String queryEngine = request.getParameter("queryEngine");

        List<LinkBean> links = find(searchText, searchPath, queryEngine);

        try {
            // Encode the submitted form data to JSON
            JSONArray jsonLinks = new JSONArray();
            for (LinkBean link : links)
            {
                JSONObject obj = new JSONObject();
                obj.put("title", link.getTitle());
                obj.put("url", link.getUrl());
                jsonLinks.put(obj);
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

    private List<LinkBean> find(String searchText, String searchPath, String queryEngine) {

        if("sql2".equals(queryEngine)) {

            return findBySql2(searchText, searchPath);

        } else if("xpath".equals(queryEngine)) {

            return findByXpath(searchText, searchPath);

        } else {

            return findByPredicates(searchText, searchPath);
        }
    }

    private List<LinkBean> findBySql2(String searchText, String searchPath) {

        //Setup the query based on user input
        String statement =   "SELECT * FROM [cq:Page] AS page " +
                "WHERE ISDESCENDANTNODE([" + searchPath + "]) AND " +
                "CONTAINS(page.*, '" + searchText + "')";

        return findByQueryManager(statement, javax.jcr.query.Query.JCR_SQL2);
    }

    private List<LinkBean> findByXpath(String searchText, String searchPath) {

        //Setup the query based on user input
        String statement =
                "/jcr:root" + searchPath + "//element(*, cq:Page) " +
                "[jcr:contains(., '" + searchText + "' )]";

        return findByQueryManager(statement, javax.jcr.query.Query.XPATH);
    }

    private List<LinkBean> findByQueryManager(String statement, String engine) {

        logger.info("*** Find By " + engine + " with Query Manager ***");

        List<LinkBean> resultList = new ArrayList();

        try {
            logger.info("Getting Ready to create SESSION!!");

            //Invoke the adaptTo method to create a Session
            ResourceResolver resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
            session = resourceResolver.adaptTo(Session.class);

            //Set the query
            // Obtain the query manager for the session ...
            QueryManager queryManager = session.getWorkspace().getQueryManager();


            logger.info("Prepared SQL statement:");
            logger.info(statement);

            javax.jcr.query.Query query = queryManager.createQuery(statement, engine);

            //Execute the query and get the results ...
            javax.jcr.query.QueryResult result = query.execute();

            //Iterate over the nodes in the results ...
            javax.jcr.NodeIterator nodeIter = result.getNodes();

            logger.info("Did we get the result");

            while ( nodeIter.hasNext() ) {

                javax.jcr.Node pageNode = nodeIter.nextNode();
                LinkBean link = extractLink(pageNode);
                resultList.add(link);
            }

            //close the session
            session.logout();

        } catch(Exception e) {
            this.logger.info("Something went wrong with session .. {}", e);
        }

        return resultList;//.toArray(new LinkBean[] {});
    }

    private List<LinkBean> findByPredicates(String searchText, String searchPath) {

        logger.info("*** Find By Query Builder ***");

        List<LinkBean> resultList = new ArrayList();

        try
        {
            logger.info("Getting Ready to create SESSION!!");

            //Invoke the adaptTo method to create a Session
            ResourceResolver resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
            session = resourceResolver.adaptTo(Session.class);

            // create query description as hash map
            Map<String, String> map = new HashMap<String, String>();
            map.put("fulltext", searchText);
            map.put("type", "cq:Page");
            map.put("path", searchPath);

            logger.info("Created map:");
            for(String key : map.keySet()) {
                String value = map.get(key);
                logger.info(key + "=" + value);
            }

            Query query = queryBuilder.createQuery(PredicateGroup.create(map), session);
            SearchResult result = query.getResult();

            logger.info("Did we get the result");
            // iterating over the results
            for (Hit hit : result.getHits()) {

                javax.jcr.Node pageNode = hit.getNode();
                LinkBean link = extractLink(pageNode);
                resultList.add(link);
            }

            //close the session
            session.logout();

        } catch(Exception e)
        {
            this.logger.info("Something went wrong with session .. {}", e);
        }

        return resultList;//.toArray(new LinkBean[]{});
    }

    private LinkBean extractLink(javax.jcr.Node pageNode) {

        LinkBean link = null;
        try {
            logger.info(pageNode.getPath());

            javax.jcr.Node contentNode = pageNode.getNode("jcr:content");
            String title;
            if (contentNode.hasProperty("navTitle")) {
                title = contentNode.getProperty("navTitle").getString();
            } else if (contentNode.hasProperty("pageTitle")) {
                title = contentNode.getProperty("pageTitle").getString();
            } else {
                title = contentNode.getProperty("jcr:title").getString();
            }

            link = new LinkBean(title, pageNode.getPath() + ".html");
        } catch (RepositoryException e) {
            logger.info("ERROR: " + e.getMessage());
        }

        return link;
    }

}