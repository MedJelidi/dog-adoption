package tn.rabini.dogadoption.models;

import java.util.ArrayList;

public class User {
    private String username, email, phone, picture;
    private ArrayList<String> dogs;
    private ArrayList<String> likedDogs;

    public User() {}

    public User(String username, String email, String phone, String picture) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.picture = picture;
        this.dogs = new ArrayList<>();
        this.likedDogs = new ArrayList<>();
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

    public ArrayList<String> getDogs() {
        return dogs;
    }

    public ArrayList<String> getLikedDogs() {
        return likedDogs;
    }

    public void setLikedDogs(ArrayList<String> likedDogs) {
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
