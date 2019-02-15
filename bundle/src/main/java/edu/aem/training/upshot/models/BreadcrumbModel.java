package edu.aem.training.upshot.models;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;
import com.day.cq.wcm.foundation.Image;
import com.day.cq.wcm.foundation.Placeholder;
import edu.aem.training.upshot.beans.LinkBean;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Node;
import java.util.ArrayList;
import java.util.List;

@Model(
        adaptables = SlingHttpServletRequest.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class BreadcrumbModel {

    Logger logger = LoggerFactory.getLogger(GridModel.class);

    @Self
    SlingHttpServletRequest request;

    @PostConstruct
    // PostConstructs are called after all the injection has occurred, but before the Model object is returned for use.
    private void init() {

    }

    public LinkBean[] getLinks() {

        List<LinkBean> links = new ArrayList<LinkBean>();
        Resource resource = request.getResource();

        final Designer designer = request.getResourceResolver().adaptTo(Designer.class);
        Style currentStyle = designer.getStyle(resource);

        PageManager pMgr = resource.getResourceResolver().adaptTo(PageManager.class);
        Page currentPage = pMgr.getContainingPage(resource);

        // get starting point of trail
        long level = currentStyle.get("absParent", 2L);
        long endLevel = currentStyle.get("relParent", 1L);
        int currentLevel = currentPage.getDepth();

        while (level < currentLevel - endLevel) {

            Page trail = currentPage.getAbsoluteParent((int) level);
            if (trail == null) {
                break;
            }

            if(trail.isHideInNav()) {
                level++;
                continue;
            }

            String title = trail.getNavigationTitle();
            if (title == null || title.equals("")) {
                title = trail.getNavigationTitle();
            }
            if (title == null || title.equals("")) {
                title = trail.getTitle();
            }
            if (title == null || title.equals("")) {
                title = trail.getName();
            }

            links.add( new LinkBean(title, trail.getPath() + ".html") );
            level++;
        }

        return links.toArray(new LinkBean[] {});
    }
}
