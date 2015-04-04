package com.example.insite.app.model;

/**
 * Created by User on 4/4/2015.
 */
public class IssueContent {

    private String m_IssueName;
    private String m_IssueLocation;
    private String m_Urgency;
    private String m_ImagePath;
    private String m_Description;

    public static final String ISSUE_FIELD = "issue_name";
    public static final String DESC_FIELD = "description";
    public static final String LOCATION_FIELD = "location_name";
    public static final String LATITUDE_FIELD = "latitude";
    public static final String LONGITUDE_FIELD = "longitude";
    public static final String IMAGEPATH_FIELD = "image_path";
    public static final String DATE_REPORTED_FIELD = "date_reported";
    public static final String TIME_REPORTED_FIELD = "time_reported";
    public static final String URGENCY_FIELD = "urgency_level";
    public static final String REPORTER_FIELD = "reporter";
    public static final String EMAIL_FIELD = "email";
    public static final String MOBILE_FIELD = "mobile";

    //Getters
    public String getIssueName()
    { return m_IssueName; }

    public String getIssueLocation()
    { return m_IssueLocation; }

    public String getUrgency()
    { return m_Urgency; }

    public String getImagePath()
    { return m_ImagePath; }

    public String getDescription()
    { return m_Description; }

    //Setters
    public void setIssueName(String in_IssueName)
    { m_IssueName = in_IssueName; }

    public void setIssueLocation(String in_Location)
    { m_IssueLocation = in_Location; }

    public void setUrgency(String in_Urgency)
    { m_Urgency = in_Urgency; }

    public void setIssuePath(String in_ImagePath)
    { m_ImagePath = in_ImagePath; }

    public void setDescription(String in_Description)
    { m_Description = in_Description; }




}
