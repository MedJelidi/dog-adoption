package tn.rabini.dogadoption.models;

import java.util.HashMap;

public class User {
    private String username, email, phone, picture;
    private HashMap<String, String> dogs;
    private HashMap<String, String> likedDogs;

    public User() {}

    public User(String username, String email, String phone, String picture) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.picture = picture;
        this.dogs = new HashMap<>();
        this.likedDogs = new HashMap<>();
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPicture() {
        return picture;
    }

    public HashMap<String, String> getDogs() {
        return dogs;
    }

    public HashMap<String, String> getLikedDogs() {
        return likedDogs;
    }

    public void setLikedDogs(HashMap<String, String> likedDogs) {
        this.likedDogs = likedDogs;
    }


    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", picture='" + picture + '\'' +
                ", dogs=" + dogs +
                ", likedDogs=" + likedDogs +
                '}';
    }
}
