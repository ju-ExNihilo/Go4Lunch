package fr.julien.go4lunch.models;

import androidx.annotation.Nullable;

public class User {
    private String uid;
    private String username;
    @Nullable
    private String urlPicture;
    private String eatingPlace;
    private String eatingPlaceId;
    private double longitude;
    private double latitude;
    private int radius;

    public User() { }

    public User(String uid, String username, @Nullable String urlPicture) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.eatingPlace = "none";
        this.eatingPlaceId = "none";
        this.radius = 5000;
        this.longitude = 0;
        this.latitude = 0;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Nullable
    public String getUrlPicture() {
        return urlPicture;
    }

    public void setUrlPicture(@Nullable String urlPicture) {
        this.urlPicture = urlPicture;
    }

    public String getEatingPlace() {
        return eatingPlace;
    }

    public void setEatingPlace(String eatingPlace) {
        this.eatingPlace = eatingPlace;
    }

    public String getEatingPlaceId() {
        return eatingPlaceId;
    }

    public void setEatingPlaceId(String eatingPlaceId) {
        this.eatingPlaceId = eatingPlaceId;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", urlPicture='" + urlPicture + '\'' +
                ", eatingPlace='" + eatingPlace + '\'' +
                ", eatingPlaceId='" + eatingPlaceId + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", radius=" + radius +
                '}';
    }
}
