package com.attendance.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StudentSummary {

    private final StringProperty id;
    private final StringProperty name;
    public StudentSummary(String id, String name) {
        this.id   = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
    }
    public StudentSummary(int id, String name) {
        this(String.valueOf(id), name);
    }

    public String getId()   { return id.get(); }
    public void setId(String v) { this.id.set(v); }
    public StringProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public void setName(String v) { this.name.set(v); }
    public StringProperty nameProperty() { return name; }

    @Override
    public String toString() {
        return "StudentSummary{id=" + id.get() + ", name=" + name.get() + '}';
    }
}
