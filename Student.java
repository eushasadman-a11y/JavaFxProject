package com.attendance.model;

import javafx.beans.property.*;

public class Student {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final StringProperty photoPath = new SimpleStringProperty(this, "photoPath");

    public Student() {}

    public Student(int id, String name, String photoPath) {
        this.id.set(id);
        this.name.set(name);
        this.photoPath.set(photoPath);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public String getPhotoPath() { return photoPath.get(); }
    public void setPhotoPath(String p) { this.photoPath.set(p); }
    public StringProperty photoPathProperty() { return photoPath; }
}