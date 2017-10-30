package com.esprit.randonnetunisie.entities;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;

/**
 * Created by youss on 14/11/2016.
 */

public class Randonnee implements Serializable {

    private int id;
    private String title;
    private String location;
    private Double longitude;
    private Double latitude;
    private Date date;
    @JsonProperty("start_time")
    private Time startTime;
    @JsonProperty("end_time")
    private Time endTime;
    private String description;
    private String type;
    private String photo;
    private String thumbnail;
    private int availability;
    private float cost;
    private boolean validation;
    private String status;
    @JsonProperty("user_id")
    private int userId;

    public Randonnee() {
    }

    public Randonnee(int id, String title, String location, Double longitude, Double latitude, Date date, Time startTime, Time endTime, String description, String type, String photo, String thumbnail, int availability, float cost, boolean validation, String status, int userId) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.longitude = longitude;
        this.latitude = latitude;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.type = type;
        this.photo = photo;
        this.thumbnail = thumbnail;
        this.availability = availability;
        this.cost = cost;
        this.validation = validation;
        this.status = status;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Randonnee{" +
                "availability=" + availability +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", date=" + date +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", photo='" + photo + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", cost=" + cost +
                ", validation=" + validation +
                ", status='" + status + '\'' +
                ", userId=" + userId +
                '}';
    }

    public int getAvailability() {
        return availability;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isValidation() {
        return validation;
    }

    public void setValidation(boolean validation) {
        this.validation = validation;
    }
}
