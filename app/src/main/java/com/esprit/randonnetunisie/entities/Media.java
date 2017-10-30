package com.esprit.randonnetunisie.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.esprit.randonnetunisie.enums.MediaType;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by youss on 30/11/2016.
 */

public class Media implements Parcelable {

    private int id;
    private String url;
    private String thumbnail;
    private MediaType type;
    @JsonProperty("post_id")
    private int postId;

    public Media() {
    }

    private Media(Parcel in) {
        id = in.readInt();
        url = in.readString();
        thumbnail = in.readString();
        type = (MediaType) in.readValue(MediaType.class.getClassLoader());
        postId = in.readInt();
    }

    public Media(int id, String url, String thumbnail, MediaType type, int postId) {
        this.id = id;
        this.url = url;
        this.thumbnail = thumbnail;
        this.type = type;
        this.postId = postId;
    }

    @Override
    public String toString() {
        return "Media{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", type=" + type +
                ", postId=" + postId +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public MediaType getType() {
        return type;
    }

    public void setType(MediaType type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeInt(id);
        out.writeString(url);
        out.writeString(thumbnail);
        out.writeValue(type);
        out.writeInt(postId);
    }

    public static final Parcelable.Creator<Media> CREATOR = new Parcelable.Creator<Media>() {
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        public Media[] newArray(int size) {
            return new Media[size];
        }
    };
}
