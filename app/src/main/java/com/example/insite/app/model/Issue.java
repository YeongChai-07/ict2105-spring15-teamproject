package com.example.insite.app.model;

/**
 * Created by Lim Xing Yi on 4/1/2015.
 */
public class Issue {
    private String title;
    private String thumbnailUrl;
    private String location;
    private String urgency_level;
    private String date;

    public Issue() {
    }

    public Issue(String name, String thumbnailUrl,String location,
                 String urgency_level, String date) {
        this.title = name;
        this.thumbnailUrl = thumbnailUrl;
        this.location = location;
        this.urgency_level = urgency_level;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUrgency_level() {
        return urgency_level;
    }

    public void setUrgency_level(String urgency_level) {
        this.urgency_level = urgency_level;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

