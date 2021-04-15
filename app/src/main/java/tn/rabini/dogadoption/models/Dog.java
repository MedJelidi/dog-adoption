package tn.rabini.dogadoption.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.Comparator;

import tn.rabini.dogadoption.MyLocation;

public class Dog {
    private String id, name, race, age, gender, description, lat, lng, image, owner;
    private boolean ready;

    public Dog() {}

    public Dog(String id, String name, String race, String age, String gender,
               String description, String lat, String lng, String image,
               String owner, boolean ready) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
        this.race = race;
        this.age = age;
        this.gender = gender;
        this.image = image;
        this.owner = owner;
        this.ready = ready;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getDescription() {
        return description;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getImage() {
        return image;
    }

    public boolean isReady() {
        return ready;
    }

    public String getOwner() {
        return owner;
    }

    public static Comparator<Dog> LocationComparator = (d1, d2) -> {
        LatLng loc1 = new LatLng(Double.parseDouble(d1.getLat()), Double.parseDouble(d1.getLng()));
        LatLng loc2 = new LatLng(Double.parseDouble(d2.getLat()), Double.parseDouble(d2.getLng()));
        LatLng myLoc = new LatLng(MyLocation.myLat, MyLocation.myLng);
        double distance1 = SphericalUtil.computeDistanceBetween(myLoc, loc1);
        double distance2 = SphericalUtil.computeDistanceBetween(myLoc, loc2);
        return Double.compare(distance1, distance2);
    };

    @Override
    public String toString() {
        return "Dog{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", race='" + race + '\'' +
                ", age='" + age + '\'' +
                ", gender='" + gender + '\'' +
                ", description='" + description + '\'' +
                ", lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                ", image='" + image + '\'' +
                ", owner='" + owner + '\'' +
                ", ready=" + ready +
                '}';
    }
}
