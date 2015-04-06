package com.example.insite.app.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lim Xing Yi on 4/1/2015.
 */
public class Issue implements Parcelable {
    private int issue_id;
    private String title;
    private String thumbnail_url;
    private String location;
    private String urgency_level;
    private String date;

    private String time;
    private String description;
    private String reporter;
    private String email;
    private String contact;
    private String status;
    private String status_comment;

    public Issue() {
    }

    public Issue(int issue_id, String name, String thumbnail_url, String location,
                 String urgency_level, String time,
                 String date, String description, String reporter,
                 String email, String contact, String status, String status_comment) {
        this.issue_id = issue_id;
        this.title = name;
        this.thumbnail_url = thumbnail_url;
        this.location = location;
        this.urgency_level = urgency_level;
        this.date = date;

        this.time = time;
        this.description = description;
        this.reporter = reporter;
        this.email = email;
        this.contact = contact;
        this.status = status;
        this.status_comment = status_comment;
    }

    public int getIssue_id() {
        return issue_id;
    }

    public void setIssue_id(int issue_id) {
        this.issue_id = issue_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public void setThumbnail_url(String thumbnail_url) {
        this.thumbnail_url = thumbnail_url;
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


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus_comment() {
        return status_comment;
    }

    public void setStatus_comment(String status_comment) {
        this.status_comment = status_comment;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(this.issue_id);
        dest.writeString(this.title);
        dest.writeString(this.thumbnail_url);
        dest.writeString(this.location);
        dest.writeString(this.urgency_level);
        dest.writeString(this.date);
        dest.writeString(this.time);
        dest.writeString(this.description);
        dest.writeString(this.reporter);
        dest.writeString(this.email);
        dest.writeString(this.contact);
        dest.writeString(this.status);
        dest.writeString(this.status_comment);
    }

    public static final Parcelable.Creator<Issue> CREATOR
            = new Parcelable.Creator<Issue>() {
        public Issue createFromParcel(Parcel in) {
            return new Issue(in);
        }

        public Issue[] newArray(int size) {
            return new Issue[size];
        }
    };

    private Issue(Parcel in) {
        this.issue_id = in.readInt();
        this.title = in.readString();
        this.thumbnail_url = in.readString();
        this.location = in.readString();
        this.urgency_level = in.readString();
        this.date = in.readString();

        this.time = in.readString();
        this.description = in.readString();
        this.reporter = in.readString();
        this.email = in.readString();
        this.contact = in.readString();
        this.status = in.readString();
        this.status_comment = in.readString();
    }
}

