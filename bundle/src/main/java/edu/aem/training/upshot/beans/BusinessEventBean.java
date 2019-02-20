package edu.aem.training.upshot.beans;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;

public class BusinessEventBean {

    Logger logger = LoggerFactory.getLogger(BusinessEventBean.class);

    String id = "";
    String title = "";
    String description = "";
    Date date;
    String place = "";
    String topic = "";

    public BusinessEventBean() {

    }

    public BusinessEventBean(String id, String title, String description, Date date, String place, String topic) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.place = place;
        this.topic = topic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JSONObject toJSONObject() {

        JSONObject obj = null;

        try {
            obj = new JSONObject();
            obj.put("id", id);
            obj.put("title", title);
            obj.put("description", description);
            obj.put("date", String.format("%td/%tm/%tY", date, date, date) );
            obj.put("place", place);
            obj.put("topic", topic);
        } catch(JSONException e) {
            logger.info("ERROR: ", e.getMessage());
        }

        return obj;
    }
}
