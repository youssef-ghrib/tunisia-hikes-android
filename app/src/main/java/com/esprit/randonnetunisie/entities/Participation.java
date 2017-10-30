package com.esprit.randonnetunisie.entities;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by youss on 01/01/2017.
 */

public class Participation implements Serializable {

    @JsonProperty("user_id")
    private int userId;
    @JsonProperty("randonnee_id")
    private int randonneeId;
    private String status;

    public Participation() {
    }

    public Participation(int randonneeId, String status, int userId) {
        this.randonneeId = randonneeId;
        this.status = status;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Participation{" +
                "randonneeId=" + randonneeId +
                ", userId=" + userId +
                ", status='" + status + '\'' +
                '}';
    }

    public int getRandonneeId() {
        return randonneeId;
    }

    public void setRandonneeId(int randonneeId) {
        this.randonneeId = randonneeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
