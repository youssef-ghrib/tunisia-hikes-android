package com.esprit.randonnetunisie.entities;

/**
 * Created by youss on 13/11/2016.
 */

public class User {

    private int id;
    private long id_facebook;
    private String email;
    private String password;
    private String name;
    private String photo;
    private String thumbnail;

    public User() {
    }

    public User(int id, long id_facebook, String email, String password, String name, String photo, String thumbnail) {
        this.id = id;
        this.id_facebook = id_facebook;
        this.email = email;
        this.password = password;
        this.name = name;
        this.photo = photo;
        this.thumbnail = thumbnail;
    }

    public User(long id_facebook, String name, String photo, String thumbnail) {
        this.id_facebook = id_facebook;
        this.name = name;
        this.photo = photo;
        this.thumbnail = thumbnail;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", id=" + id +
                ", id_facebook=" + id_facebook +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", photo='" + photo + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                '}';
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getId_facebook() {
        return id_facebook;
    }

    public void setId_facebook(long id_facebook) {
        this.id_facebook = id_facebook;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
