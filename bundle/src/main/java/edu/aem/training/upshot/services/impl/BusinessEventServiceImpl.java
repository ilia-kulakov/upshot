package edu.aem.training.upshot.services.impl;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import edu.aem.training.upshot.beans.BusinessEventBean;
import edu.aem.training.upshot.beans.LinkBean;
import edu.aem.training.upshot.services.BusinessEventService;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;

/**
 * One implementation of the {@link BusinessEventService}. Note that
 * the repository is injected, not retrieved.
 */
@Service(BusinessEventService.class)
@Component(immediate=true, metatype = true)
public class BusinessEventServiceImpl implements BusinessEventService {

    Logger logger = LoggerFactory.getLogger(BusinessEventService.class);

    public static final String PN_DEFAULT_SORT_FIELD = "aem.training.upshot.business_event.default_sort_field";
    public static final String PN_DEFAULT_SORT_ORDER = "aem.training.upshot.business_event.default_sort_order";
    public static final String PN_DEFAULT_PAGE_SIZE = "aem.training.upshot.business_event.default_page_size";
    public static final String PN_DEFAULT_EVENT_CONTROL_PANEL_URL = "aem.training.upshot.business_event.default_event_control_panel_url";

    public static final String DEFAULT_SORT_FIELD = "Title";
    public static final String DEFAULT_SORT_ORDER = "asc";
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String DEFAULT_EVENT_CONTROL_PANEL_URL ="/etc/training-site/event-control-panel";

    @Property(name= PN_DEFAULT_SORT_FIELD, label = "Sort field", description = "Default sort field",  value = DEFAULT_SORT_FIELD,
            options = {
                    @PropertyOption(value = "Title", name = "title"),
                    @PropertyOption(value = "Description", name = "description"),
                    @PropertyOption(value = "Date", name = "date"),
                    @PropertyOption(value = "Place", name = "place"),
                    @PropertyOption(value = "Topic", name = "topic")
            })
    private static String defaultSortField;

    @Property(name= PN_DEFAULT_SORT_ORDER, label = "Sort order", description = "Default sort order",  value = DEFAULT_SORT_ORDER,
            options = {
                    @PropertyOption(value = "Ascending", name = "asc"),
                    @PropertyOption(value = "Descending", name = "desc")
            })
    private static String defaultSortOrder;

    @Property(name= PN_DEFAULT_PAGE_SIZE, label = "Page size", description = "Default page size",  value = "" + DEFAULT_PAGE_SIZE)
    private static int defaultPageSize;

    @Property(name= PN_DEFAULT_EVENT_CONTROL_PANEL_URL, label = "Event control panel", description = "Default event control panel URL",  value = DEFAULT_EVENT_CONTROL_PANEL_URL)
    private static String defaultEventControlPanelUrl;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private QueryBuilder queryBuilder;

    @Activate
    @Modified
    protected void onChangeProperties(ComponentContext context) {
        Dictionary properties = context.getProperties();
        defaultSortField = PropertiesUtil.toString(
                properties.get(PN_DEFAULT_SORT_FIELD), DEFAULT_SORT_FIELD );
        defaultSortOrder = PropertiesUtil.toString(
                properties.get(PN_DEFAULT_SORT_ORDER), DEFAULT_SORT_ORDER );
        defaultPageSize = PropertiesUtil.toInteger(
                properties.get(PN_DEFAULT_PAGE_SIZE), DEFAULT_PAGE_SIZE);
        defaultEventControlPanelUrl = PropertiesUtil.toString(
                properties.get(PN_DEFAULT_EVENT_CONTROL_PANEL_URL), DEFAULT_EVENT_CONTROL_PANEL_URL );

        if(defaultPageSize <= 0) {
            defaultPageSize = DEFAULT_PAGE_SIZE;
        }
    }

    public List<BusinessEventBean> getAllEvents(String sortField, String sortOrder, int pageSize, int pageNo) {
        // Search fulltext in current page and sub-paths
        logger.info("*** Find By Query Builder ***");

        if(sortField == null) {
            sortField = defaultSortField;
        } else {

            try {
                BusinessEventBean.class.getField(sortField);
            } catch (NoSuchFieldException e) {
                sortField = defaultSortField;
            }
        }

        if(sortOrder == null) {
            sortOrder = defaultSortOrder;
        } else {
            if (!("asc".equals(sortOrder) || "desc".equals(sortOrder))) {
                sortOrder = defaultSortOrder;
            }
        }

        if(pageNo < 0) {
            pageNo = 0;
        }

        List<BusinessEventBean> events = new ArrayList<BusinessEventBean>();

        try
        {
            logger.info("Getting Ready to create SESSION!!");

            //Invoke the adaptTo method to create a Session
            ResourceResolver resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
            Session session = resourceResolver.adaptTo(Session.class);

            // create query description as hash map
            Map<String, String> map = new HashMap<String, String>();
            map.put("path", defaultEventControlPanelUrl);
            map.put("property", "sling:resourceType");
            map.put("property.value", "upshot/components/businessevent");
            // sorting
            map.put("orderby", "@" + sortField);
            map.put("orderby.sort", sortOrder);
            // can be done in map or with Query methods
            map.put("p.offset", "" + (pageNo * pageSize));
            map.put("p.limit", "" + pageSize);

            Query query = queryBuilder.createQuery(PredicateGroup.create(map), session);
            SearchResult result = query.getResult();

            logger.info("Did we get the result");
            for (Hit hit : result.getHits()) {
                Node node = hit.getNode();
                BusinessEventBean event = extractEvent(node);
                events.add(event);
            }
            //close the session
            session.logout();

        } catch(Exception e) {
            this.logger.info("Something went wrong with session .. {}", e);
        }

        return events;
    }

    public BusinessEventBean getEvent(String title) {

        // Search fulltext in current page and sub-paths
        logger.info("*** Find By Query Builder ***");

        BusinessEventBean event = null;

        try
        {
            logger.info("Getting Ready to create SESSION!!");

            //Invoke the adaptTo method to create a Session
            ResourceResolver resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
            Session session = resourceResolver.adaptTo(Session.class);

            // create query description as hash map
            Map<String, String> map = new HashMap<String, String>();
            map.put("path", defaultEventControlPanelUrl);
            map.put("1_property", "sling:resourceType");
            map.put("1_property.value", "upshot/components/businessevent");
            map.put("2_property", "title");
            map.put("2_property.value", title);
            map.put("p.limit", "1");

            logger.info("Created map:");
            for(String key : map.keySet()) {
                String value = map.get(key);
                logger.info(key + "=" + value);
            }

            Query query = queryBuilder.createQuery(PredicateGroup.create(map), session);
            SearchResult result = query.getResult();

            logger.info("Did we get the result");
            if(result.getHits().size() > 0) {
                Node node = result.getHits().get(0).getNode();
                event = extractEvent(node);
            }
            //close the session
            session.logout();

        } catch(Exception e) {
            this.logger.info("Something went wrong with session .. {}", e);
        }

        return event;
    }

    private BusinessEventBean extractEvent(Node node) {

        try {
            BusinessEventBean event = new BusinessEventBean();
            event.setTitle(node.getProperty("title").getString());
            event.setDescription(node.getProperty("description").getString());
            event.setDate(node.getProperty("date").getDate().getTime());
            event.setPlace(node.getProperty("place").getString());
            event.setTopic(node.getProperty("topic").getString());
            return event;
        } catch(RepositoryException e) {
            logger.info("ERROR:" + e.getMessage());
        }

        return null;
    }
}
