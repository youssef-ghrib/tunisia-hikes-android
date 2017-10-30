package com.esprit.randonnetunisie.entities;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by youss on 23/11/2016.
 */

public class Message {

    private int id;
    private String text;
    @JsonProperty("randonnee_id")
    private int randonneeId;
    @JsonProperty("user_id")
    private int userId;

    public Message() {
    }

    public Message(int id, String text, int randonneeId, int userId) {
        this.id = id;
        this.text = text;
        this.randonneeId = randonneeId;
        this.userId = userId;
    }

    public Message(String text, int randonneeId, int userId) {
        this.text = text;
        this.randonneeId = randonneeId;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", randonneeId=" + randonneeId +
                ", userId=" + userId +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getRandonneeId() {
        return randonneeId;
    }

    public void setRandonneeId(int randonneeId) {
        this.randonneeId = randonneeId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
