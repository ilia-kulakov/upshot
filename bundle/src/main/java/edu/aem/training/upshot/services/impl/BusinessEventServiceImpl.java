package edu.aem.training.upshot.services.impl;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import edu.aem.training.upshot.beans.BusinessEventBean;
import edu.aem.training.upshot.services.BusinessEventService;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.sling.jcr.api.SlingRepository;

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

    private static final String PROPERTY_TITLE = "title";
    private static final String PROPERTY_DESCRIPTION = "description";
    private static final String PROPERTY_DATE = "date";
    private static final String PROPERTY_PLACE = "place";
    private static final String PROPERTY_TOPIC = "topic";

    private static final String PN_DEFAULT_SORT_FIELD = "aem.training.upshot.business_event.default_sort_field";
    private static final String PN_DEFAULT_SORT_ORDER = "aem.training.upshot.business_event.default_sort_order";
    private static final String PN_DEFAULT_PAGE_SIZE = "aem.training.upshot.business_event.default_page_size";
    private static final String PN_DEFAULT_EVENT_CONTROL_PANEL_URL = "aem.training.upshot.business_event.default_event_control_panel_url";

    private static final String DEFAULT_SORT_FIELD = "title";
    private static final String DEFAULT_SORT_ORDER = "asc";
    private static final int    DEFAULT_PAGE_SIZE = 10;
    private static final String DEFAULT_EVENT_CONTROL_PANEL_URL ="/etc/training-site/event-control-panel";

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
    private QueryBuilder queryBuilder;

    private long totalEvents;

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


    public List<BusinessEventBean> getAllEvents(SlingHttpServletRequest request, String sortField, String sortOrder, int pageSize, int pageNo) {
        // Search fulltext in current page and sub-paths
        logger.info("*** Find By Query Builder ***");

        if(sortField == null) {
            sortField = defaultSortField;
        } else {

            try {
                BusinessEventBean.class.getDeclaredField(sortField);
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

        if(pageNo <= 0) {
            pageNo = 1;
        }


        List<BusinessEventBean> events = new ArrayList<BusinessEventBean>();

        try
        {
            logger.info("Getting Ready to get user SESSION!!");

            //Invoke the adaptTo method to get user Session
            ResourceResolver resourceResolver = request.getResourceResolver();
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
            map.put("p.offset", "" + ( (pageNo - 1) * pageSize));
            map.put("p.limit", "" + pageSize);

            Query query = queryBuilder.createQuery(PredicateGroup.create(map), session);
            SearchResult result = query.getResult();

            logger.info("Did we get the result");
            for (Hit hit : result.getHits()) {
                Node node = hit.getNode();
                BusinessEventBean event = extractEvent(node);
                events.add(event);
            }

            totalEvents = result.getTotalMatches();

        } catch(Exception e) {
            this.logger.info("Something went wrong with session .. {}", e);
        }

        return events;
    }

    public BusinessEventBean getEvent(SlingHttpServletRequest request, String id) {

        // Search fulltext in current page and sub-paths
        logger.info("*** Find By Query Builder ***");

        BusinessEventBean event = null;

        try
        {
            logger.info("Getting Ready to get user SESSION!!");

            //Invoke the adaptTo method to get user Session
            ResourceResolver resourceResolver = request.getResourceResolver();
            Session session = resourceResolver.adaptTo(Session.class);

            // create query description as hash map
            Map<String, String> map = new HashMap<String, String>();
            map.put("path", defaultEventControlPanelUrl);
            map.put("nodename", id);

            Query query = queryBuilder.createQuery(PredicateGroup.create(map), session);
            SearchResult result = query.getResult();

            logger.info("Did we get the result");
            if(result.getHits().size() > 0) {
                Node node = result.getHits().get(0).getNode();
                event = extractEvent(node);
            }

        } catch(Exception e) {
            this.logger.info("Something went wrong with session .. {}", e);
        }

        return event;
    }

    private BusinessEventBean extractEvent(Node node) {

        try {
            BusinessEventBean event = new BusinessEventBean();
            event.setId(node.getName());
            if(node.hasProperty(PROPERTY_TITLE))
                event.setTitle(node.getProperty(PROPERTY_TITLE).getString());
            if(node.hasProperty(PROPERTY_DESCRIPTION))
                event.setDescription(node.getProperty(PROPERTY_DESCRIPTION).getString());
            if(node.hasProperty(PROPERTY_DATE))
                event.setDate(node.getProperty(PROPERTY_DATE).getDate().getTime());
            if(node.hasProperty(PROPERTY_PLACE))
                event.setPlace(node.getProperty(PROPERTY_PLACE).getString());
            if(node.hasProperty(PROPERTY_TOPIC))
                event.setTopic(node.getProperty(PROPERTY_TOPIC).getString());
            return event;
        } catch(RepositoryException e) {
            logger.info("ERROR:" + e.getMessage());
        }

        return null;
    }


    public long getTotalEvents() {
        return totalEvents;
    }
}
