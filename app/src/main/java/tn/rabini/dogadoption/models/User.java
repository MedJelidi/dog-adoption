package tn.rabini.dogadoption.models;

import java.util.ArrayList;

public class User {
    private String username, email, phone, picture;
    private ArrayList<Dog> dogs;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public ArrayList<Dog> getDogs() {
        return dogs;
    }

    public void setDogs(ArrayList<Dog> dogs) {
        this.dogs = dogs;
    }

    public ArrayList<String> getLikedDogs() {
        return likedDogs;
    }

    public void setLikedDogs(ArrayList<String> likedDogs) {
        this.likedDogs = likedDogs;
    }

    public void addDog(Dog e) {
        this.dogs.add(e);
    }

    public boolean removeDog(String id) {
        for (Dog e: this.dogs) {
            if (e.getId().equals(id)) {
                this.dogs.remove(e);
                return true;
            }
        }
        return false;
    }

    public void likeDog(String  e) {
        this.likedDogs.add(e);
    }

    public boolean unlikeDog(String id) {
        for (String e: this.likedDogs) {
            if (e.equals(id)) {
                this.likedDogs.remove(e);
                return true;
            }
        }
        return false;
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
