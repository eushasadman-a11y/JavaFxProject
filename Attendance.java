package com.attendance.model;

import javafx.beans.property.*;

public class Attendance {
    private final StringProperty studentName = new SimpleStringProperty();
    private final StringProperty department = new SimpleStringProperty();
    private final StringProperty date = new SimpleStringProperty();
    private final StringProperty timeLog = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    public Attendance(String name, String dept, String date, String time, String status) {
        this.studentName.set(name);
        this.department.set(dept);
        this.date.set(date);
        this.timeLog.set(time);
        this.status.set(status);
    }

    public StringProperty studentNameProperty() { return studentName; }
    public StringProperty departmentProperty() { return department; }
    public StringProperty dateProperty() { return date; }
    public StringProperty timeLogProperty() { return timeLog; }
    public StringProperty statusProperty() { return status; }

    public String getStudentName() { return studentName.get(); }
    public String getDepartment() { return department.get(); }
    public String getDate() { return date.get(); }
    public String getTimeLog() { return timeLog.get(); }
    public String getStatus() { return status.get(); }
}