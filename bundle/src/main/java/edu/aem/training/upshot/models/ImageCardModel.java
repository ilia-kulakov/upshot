package edu.aem.training.upshot.models;

import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;
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

import com.day.cq.wcm.foundation.Image;
import com.day.cq.wcm.foundation.Placeholder;

@Model(
        adaptables = SlingHttpServletRequest.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ImageCardModel {

    Logger logger = LoggerFactory.getLogger(GridModel.class);

    @Self
    SlingHttpServletRequest request;

    @Inject
    @ValueMapValue
    private String file;

    @Inject
    @ValueMapValue
    private String fileName;

    @Inject
    @ValueMapValue
    private String fileReference;

    @PostConstruct
    // PostConstructs are called after all the injection has occurred, but before the Model object is returned for use.
    private void init() {

    }

    public String getImgSrc() {
        Resource resource = request.getResource();

        Image image = new Image(resource);
        image.setSelector(".img"); // use image script
        image.setIsInUITouchMode(Placeholder.isAuthoringUIModeTouch(request));

        return image.getSrc();
    }
}
