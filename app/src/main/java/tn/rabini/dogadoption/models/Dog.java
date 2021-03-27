package tn.rabini.dogadoption.models;

public class Dog {
    private String id, name, description, image, location, owner;
    private boolean ready;

    public Dog() {}

    public Dog(String id, String name, String description, String location, String image, String owner, boolean ready) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.image = image;
        this.location = location;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Dog{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", location='" + location + '\'' +
                ", owner='" + owner + '\'' +
                ", ready=" + ready +
                '}';
    }
}
