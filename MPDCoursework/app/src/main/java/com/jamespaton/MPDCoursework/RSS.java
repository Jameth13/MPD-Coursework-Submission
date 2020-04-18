//James Paton S1111175

package com.jamespaton.MPDCoursework;


import java.io.Serializable;
import java.util.Calendar;


//Implements serializable for transfer between activities.
public class RSS implements Serializable {

    //Note: Encapsulation keywords are not missing, apparently this is known as 'package-private'.
    String title;
    String description;
    private String link;
    private String geo;
    private String author;
    private String comments;
    String pubDate;
    Calendar startDate;
    Calendar endDate;
    int lengthInDays;
    long timeAgo;
    String timeAgoString;
    String geoPoints;
    float lat;
    float lng;
    boolean clickable;

    RSS() {
        title = "";
        description = "";
        link = "";
        geo = "";
        author = "";
        comments = "";
        pubDate = "";
        startDate = null;
        endDate = null;
        lengthInDays = -1;
        timeAgo = -1;
        timeAgoString = "";
        geoPoints = "";
        lat = 0.0f;
        lng = 0.0f;
        clickable = true;
    }

    RSS(String title) {
        this.title = title;
        description = "";
        link = "";
        geo = "";
        author = "";
        comments = "";
        pubDate = "";
        startDate = null;
        endDate = null;
        lengthInDays = -1;
        timeAgo = -1;
        timeAgoString = "";
        geoPoints = "";
        lat = 0.0f;
        lng = 0.0f;
        clickable = true;
    }

    RSS(RSS rss) {
        title = rss.title;
        description = rss.description;
        link = rss.link;
        geo = rss.geo;
        author = rss.author;
        comments = rss.comments;
        pubDate = rss.pubDate;
        startDate = rss.startDate;
        endDate = rss.endDate;
        lengthInDays = rss.lengthInDays;
        timeAgo = rss.timeAgo;
        timeAgoString = rss.timeAgoString;
        geoPoints = rss.geoPoints;
        lat = rss.lat;
        lng = rss.lng;
        clickable = rss.clickable;
    }

    @Override
    public String toString() {
        return "RSS [title=" + title + ", " +
                "description=" + description + "]";
    }
}
