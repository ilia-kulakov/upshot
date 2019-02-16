package edu.aem.training.upshot.services.impl;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import edu.aem.training.upshot.services.TextFinderService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    Logger logger = LoggerFactory.getLogger(TextFinderServiceImpl.class);

    private Session session;

    //Inject a Sling ResourceResolverFactory
    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private QueryBuilder queryBuilder;

    public String[] find(String[] findWhatList, String[] findWhereList, String queryApi) {

        if("Query Manager".equals(queryApi)) {
            return findByQueryManager(findWhatList, findWhereList);
        } else {
            return findByQueryBuilder(findWhatList, findWhereList);
        }
    }

    private String[] findByQueryManager(String[] findWhatList, String[] findWhereList) {

        logger.info("*** Find By Query Manager ***");

        List<String> resultList = new ArrayList();

        try {
            logger.info("Getting Ready to create SESSION!!");

            //Invoke the adaptTo method to create a Session
            ResourceResolver resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
            session = resourceResolver.adaptTo(Session.class);

            //Set the query
            // Obtain the query manager for the session ...
            QueryManager queryManager = session.getWorkspace().getQueryManager();

            // Setup paths condition
            String includePaths = "";
            String excludePaths = "";
            if(findWhereList != null && findWhereList.length > 0) {
                for(String path : findWhereList) {

                    if(includePaths.length() > 0) {
                        includePaths += " OR ";
                        excludePaths += " AND ";
                    }

                    includePaths += "ISDESCENDANTNODE(el, [" + path +"])";
                    excludePaths += "el.[jcr:path] NOT LIKE '" + path + "/%/subassets/%'";
                }

                if(includePaths.length() > 0) {
                    includePaths = "(" + includePaths + ") AND " ;
                    excludePaths += " AND ";
                }
            }

            // Setup searching text
            String containsText = "";
            if(findWhatList != null && findWhatList.length > 0) {
                for(String findWhat : findWhatList) {

                    if(containsText.length() > 0) {
                        containsText += " OR ";
                    }

                    containsText += "CONTAINS(el.*, '" + findWhat + "')";
                }

                if(containsText.length() > 0) {
                    containsText = " AND (" + containsText + ")" ;
                }
            }

            //Setup the query based on user input
            String sqlStatement = "SELECT el.* FROM [dam:Asset] AS el WHERE " +
                    includePaths + excludePaths + "NAME() LIKE '%.pdf'" + containsText;

            logger.info("Prepared SQL statement:");
            logger.info(sqlStatement);

            javax.jcr.query.Query query = queryManager.createQuery(sqlStatement,"JCR-SQL2");

            //Execute the query and get the results ...
            javax.jcr.query.QueryResult result = query.execute();

            //Iterate over the nodes in the results ...
            javax.jcr.NodeIterator nodeIter = result.getNodes();

            logger.info("Did we get the result");

            while ( nodeIter.hasNext() ) {

                javax.jcr.Node node = nodeIter.nextNode();
                resultList.add(node.getPath());

                logger.info(node.getPath());
            }

            //close the session
            session.logout();

        } catch(Exception e)
        {
            this.logger.info("Something went wrong with session .. {}", e);
        }


        return resultList.toArray(new String[] {});
    }

    private String[] findByQueryBuilder(String[] findWhatList, String[] findWhereList) {

        logger.info("*** Find By Query Builder ***");

        List<String> resultList = new ArrayList();

        try
        {
            logger.info("Getting Ready to create SESSION!!");

            //Invoke the adaptTo method to create a Session
            ResourceResolver resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
            session = resourceResolver.adaptTo(Session.class);

            // create query description as hash map
            Map<String, String> map = new HashMap<String, String>();
            map.put("nodename", "*.pdf");
            // exclude subassets (for example Book.pdf -> Page1.pdf)
            map.put("property", "../../jcr:primaryType");
            map.put("property.operation", "unequals");
            map.put("property.value", "dam:Asset");

            map.put("p.limit", "-1");

            int groupNum = 1;

            if(findWhatList != null && findWhatList.length > 0) {
                String group = groupNum + "_group";
                for(int i = 0; i < findWhatList.length; i++) {
                    int n = i + 1;
                    String fulltext = n + "_fulltext";
                    map.put(group + "." + fulltext, findWhatList[i]);
                }

                map.put(group + ".p.or", "true");
                groupNum++;
            }

            if(findWhereList != null && findWhereList.length > 0) {
                String group = groupNum + "_group";
                for(int i = 0; i < findWhereList.length; i++) {
                    int n = i + 1;
                    String path = n + "_path";
                    map.put(group + "." + path, findWhereList[i]);
                }

                map.put(group + ".p.or", "true");
            }

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
                String path = hit.getPath();
                logger.info(path);

                //Create a result element
                resultList.add(path);
            }

            //close the session
            session.logout();

        } catch(Exception e)
        {
            this.logger.info("Something went wrong with session .. {}", e);
        }

        return resultList.toArray(new String[]{});

    }
}
