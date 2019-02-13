package edu.aem.training.upshot.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Model(
        adaptables = SlingHttpServletRequest.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class GridModel {

    Logger logger = LoggerFactory.getLogger(GridModel.class);

    @Self
    SlingHttpServletRequest request;

    @Inject
    @ValueMapValue
    @Default(values="2")
    private String rows;

    @Inject
    @ValueMapValue
    @Default(values="2")
    private String cols;

    @PostConstruct
    // PostConstructs are called after all the injection has occurred, but before the Model object is returned for use.
    private void init() {
        logger.info("Inject rows:" + rows);
        logger.info("Inject cols:" + cols);
        checkNodes();
    }

    public Long[] getRows() {
        return sequence(1L, Long.parseLong(rows));
    }

    public Long[] getCols() {
        return sequence(1L, Long.parseLong(cols));
    }

    private Long[] sequence(Long min, Long max) {

        List<Long> seq = new ArrayList<Long>();

        for(Long i = min; i <= max; i++ ) {
            seq.add(i);
        }

        return seq.toArray(new Long[]{});
    }

    private void checkNodes() {
        // Remove redundant nodes
        logger.info("--> checkNodes -->");

        Resource res = request.getResource();
        Node gridNode = res.adaptTo(Node.class);

        if(gridNode == null) {
            logger.info("Node is null");
            return;
        }

        try {
            logger.info("Node: " + gridNode);
            logger.info("Name: " + gridNode.getName());
            logger.info("Path: " + gridNode.getPath());
            logger.info("Children: ");

            NodeIterator iter = gridNode.getNodes();
            while(iter.hasNext()) {
                Node node = iter.nextNode();
                String name = node.getName();
                logger.info("Name: " + name);

                // Extract indexes of grid cell
                // par_r1c1
                Pattern pattern = Pattern.compile("^par_r(\\d+)c(\\d+)$");
                Matcher matcher = pattern.matcher(name);
                if (matcher.find()) {
                    String r = matcher.group(1);
                    String c = matcher.group(2);
                    logger.info("r: " + r);
                    logger.info("c: " + c);

                    Long curRow = Long.parseLong(r);    // index begin from 1,..
                    Long curCol = Long.parseLong(c);    // index begin from 1,..
                    Long oldRows = Long.parseLong(rows);
                    Long oldCols = Long.parseLong(cols);

                    if(curRow > oldRows || curCol > oldCols) {

                        node.remove();
                        node.getSession().save();
                        logger.info("Remove this node");
                    }
                }
            }

        } catch(RepositoryException e) {

            logger.info("ERROR: " + e.getMessage());
        }


    }
}
