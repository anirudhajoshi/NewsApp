package com.example.android.appbar;

import android.net.Uri;

/**
 * Created by Anirudha.Joshi on 7/11/2016.
 */

// This class will use the default constructor
public class NewsItem {

    private String sectionName;
    private String webTitle;
    private String urlString;
    private Uri urlLink;
    private String thumbnail;

    // Get the news item section name
    public String getSectionName() {
        return sectionName;
    }

    // Set the news item section name
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    // Get the news item title
    public String getWebTitle() {
        return webTitle;
    }

    // Set the news item title
    public void setWebTitle(String webTitle) {
        this.webTitle = webTitle;
    }

    // Get the URI
    public String getUrl() {
        return urlString;
    }

    // Set the URI
    public void setUrl(String url) {

        if (!url.equals("")) {
            this.urlString = url;
            this.urlLink = Uri.parse(url);
        } else {
            this.urlString = "No URL";
        }
    }

    public String getThumbnailURL() {
        return thumbnail;
    }

    public void setThumnailURL(String newsItemURL) {
        this.thumbnail = newsItemURL;
    }
}
