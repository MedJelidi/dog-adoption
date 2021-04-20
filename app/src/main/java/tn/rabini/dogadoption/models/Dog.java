package tn.rabini.dogadoption.models;

public class Dog {
    private String id, name, race, age, gender, description, lat, lng, image, owner;
    private boolean ready;
    private double distance;
    private long publishedDate;

    public Dog() {
    }

    public Dog(String id, String name, String race, String age, String gender,
               String description, String lat, String lng, String image,
               String owner, boolean ready, long publishedDate) {
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
        this.publishedDate = publishedDate;
        this.distance = 0.0;
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

    public String getLng() {
        return lng;
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(long publishedDate) {
        this.publishedDate = publishedDate;
    }

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
                ", distance=" + distance +
                ", publishedDate=" + publishedDate +
                '}';
    }
}
