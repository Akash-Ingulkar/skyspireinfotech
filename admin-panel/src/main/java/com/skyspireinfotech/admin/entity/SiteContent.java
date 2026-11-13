package com.skyspireinfotech.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * One row per editable page section (Home, About). Kept generic so either
 * section can reuse the same fields without needing a table each.
 */
@Entity
@Table(name = "site_content")
public class SiteContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sectionKey;

    private String heading;

    private String subheading;

    @Lob
    private String bodyText;

    private String imageUrl;

    /** HOME only: what the hero card shows — "IMAGE", "VIDEO", or null for the website's built-in photo. */
    private String mediaType;

    /** HOME only: uploaded hero video (served under /uploads/). */
    private String videoUrl;

    /** ABOUT only: headline numbers shown on the Home and About pages (free text, e.g. "120+"). */
    private String statProjects;
    private String statClients;
    private String statYears;

    /** ABOUT only: the three "why us" points shown on the Home and About pages. */
    private String feature1Title;
    @Column(length = 600)
    private String feature1Text;
    private String feature2Title;
    @Column(length = 600)
    private String feature2Text;
    private String feature3Title;
    @Column(length = 600)
    private String feature3Text;

    public String getStatProjects() { return statProjects; }
    public void setStatProjects(String statProjects) { this.statProjects = statProjects; }
    public String getStatClients() { return statClients; }
    public void setStatClients(String statClients) { this.statClients = statClients; }
    public String getStatYears() { return statYears; }
    public void setStatYears(String statYears) { this.statYears = statYears; }
    public String getFeature1Title() { return feature1Title; }
    public void setFeature1Title(String feature1Title) { this.feature1Title = feature1Title; }
    public String getFeature1Text() { return feature1Text; }
    public void setFeature1Text(String feature1Text) { this.feature1Text = feature1Text; }
    public String getFeature2Title() { return feature2Title; }
    public void setFeature2Title(String feature2Title) { this.feature2Title = feature2Title; }
    public String getFeature2Text() { return feature2Text; }
    public void setFeature2Text(String feature2Text) { this.feature2Text = feature2Text; }
    public String getFeature3Title() { return feature3Title; }
    public void setFeature3Title(String feature3Title) { this.feature3Title = feature3Title; }
    public String getFeature3Text() { return feature3Text; }
    public void setFeature3Text(String feature3Text) { this.feature3Text = feature3Text; }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSectionKey() {
        return sectionKey;
    }

    public void setSectionKey(String sectionKey) {
        this.sectionKey = sectionKey;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getSubheading() {
        return subheading;
    }

    public void setSubheading(String subheading) {
        this.subheading = subheading;
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
