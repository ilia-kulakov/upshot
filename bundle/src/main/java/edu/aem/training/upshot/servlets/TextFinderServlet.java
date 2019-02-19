package edu.aem.training.upshot.servlets;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.PageManager;
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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
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

        logger.info("Start searching");

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



        List<LinkBean> links = find(searchText, searchPath, queryEngine);

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
        // Setup the query based on user input
        // Search fulltext in current page and sub-paths
        String statement =   "SELECT * FROM [cq:Page] AS page " +
                "WHERE ([jcr:path] = '" + searchPath + "' OR ISDESCENDANTNODE([" + searchPath + "])) AND " +
                "CONTAINS(page.*, '" + searchText + "')";

        return findByQueryManager(statement, javax.jcr.query.Query.JCR_SQL2);
    }

    private List<LinkBean> findByXpath(String searchText, String searchPath) {
        // Search fulltext in current page and sub-paths
        String statement =
                "/jcr:root" + searchPath + "//element(*, cq:PageContent) " +
                "[jcr:contains(., '" + searchText + "' )]";
        List<LinkBean> links = findByQueryManager(statement, javax.jcr.query.Query.XPATH);

        return links;
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
            NodeIterator nodeIter = result.getNodes();

            logger.info("Did we get the result");

            //create a page manager instance
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);

            while ( nodeIter.hasNext() ) {

                Node node = nodeIter.nextNode();
                LinkBean link = extractLink(node);
                resultList.add(link);
            }

            //close the session
            session.logout();

        } catch(Exception e) {
            this.logger.info("Something went wrong with session .. {}", e);
        }

        return resultList;
    }

    private List<LinkBean> findByPredicates(String searchText, String searchPath) {

        // Search fulltext in current page and sub-paths
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
            map.put("path.self", "true");

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

        return resultList;
    }

    private LinkBean extractLink(Node node) {

        // Node may be Page or PageContent
        LinkBean link = null;
        try {
            logger.info(node.getPath());

            Node pageNode;
            Node contentNode;

            if(node.getPrimaryNodeType().toString().equals("cq:PageContent")) {
                contentNode = node;
                pageNode = contentNode.getParent();
            } else {
                pageNode = node;
                contentNode = pageNode.getNode("jcr:content");
            }

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