package edu.aem.training.upshot.beans;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkBean {

    Logger logger = LoggerFactory.getLogger(LinkBean.class);

    private String title = "";

    private String url = "";

    public LinkBean() {
    }

    public LinkBean(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JSONObject toJSONObject() {

        JSONObject obj = null;

        try {
            obj = new JSONObject();
            obj.put("title", title);
            obj.put("url", url);
        } catch(JSONException e) {
            logger.info("ERROR: ", e.getMessage());
        }

        return obj;
    }
}
