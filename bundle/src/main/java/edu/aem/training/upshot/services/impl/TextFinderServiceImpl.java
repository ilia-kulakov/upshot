package edu.aem.training.upshot.services.impl;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import edu.aem.training.upshot.beans.LinkBean;
import edu.aem.training.upshot.services.TextFinderService;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One implementation of the {@link TextFinderService}. Note that
 * the repository is injected, not retrieved.
 */
@Service(TextFinderService.class)
@Component(immediate=true, metatype = true)
public class TextFinderServiceImpl implements TextFinderService {

    Logger logger = LoggerFactory.getLogger(TextFinderService.class);

    private static final String PROPERTY_NAV_TITLE = "navTitle";
    private static final String PROPERTY_PAGE_TITLE = "pageTitle";

    @Reference
    private QueryBuilder queryBuilder;

    public List<LinkBean> findBySql2(SlingHttpServletRequest request, String searchText, String searchPath) {
        // Setup the query based on user input
        // Search fulltext in current page and sub-paths
        String statement =   "SELECT * FROM [cq:Page] AS page " +
                "WHERE ([jcr:path] = '" + searchPath + "' OR ISDESCENDANTNODE([" + searchPath + "])) AND " +
                "CONTAINS(page.*, '" + searchText + "')";

        return findByQueryManager(request, statement, javax.jcr.query.Query.JCR_SQL2);
    }

    public List<LinkBean> findByXpath(SlingHttpServletRequest request, String searchText, String searchPath) {
        // Search fulltext in current page and sub-paths
        String statement =
                "/jcr:root" + searchPath + "//element(*, cq:PageContent) " +
                        "[jcr:contains(., '" + searchText + "' )]";
        List<LinkBean> links = findByQueryManager(request, statement, javax.jcr.query.Query.XPATH);

        return links;
    }

    public List<LinkBean> findByPredicates(SlingHttpServletRequest request, String searchText, String searchPath) {
        // Search fulltext in current page and sub-paths
        logger.info("*** Find By Query Builder ***");

        List<LinkBean> resultList = new ArrayList();

        try
        {
            logger.info("Getting Ready to create SESSION!!");

            // Get current user session
            Session session = request.getResourceResolver().adaptTo(Session.class);

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

        } catch(Exception e)
        {
            this.logger.info("Something went wrong with session .. {}", e);
        }

        return resultList;
    }

    private List<LinkBean> findByQueryManager(SlingHttpServletRequest request, String statement, String engine) {

        logger.info("*** Find By " + engine + " with Query Manager ***");

        List<LinkBean> resultList = new ArrayList();

        try {
            logger.info("Getting Ready to get SESSION!!");

            // Get current user session
            Session session = request.getResourceResolver().adaptTo(Session.class);
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

            while ( nodeIter.hasNext() ) {

                Node node = nodeIter.nextNode();
                LinkBean link = extractLink(node);
                resultList.add(link);
            }

        } catch(Exception e) {
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
            if (contentNode.hasProperty(PROPERTY_NAV_TITLE)) {
                title = contentNode.getProperty(PROPERTY_NAV_TITLE).getString();
            } else if (contentNode.hasProperty(PROPERTY_PAGE_TITLE)) {
                title = contentNode.getProperty(PROPERTY_PAGE_TITLE).getString();
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
