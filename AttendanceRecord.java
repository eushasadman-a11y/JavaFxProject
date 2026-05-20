package com.attendance.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AttendanceRecord {
    private final StringProperty studentName;
    private final StringProperty department;
    private final StringProperty date;
    private final StringProperty time;
    private final StringProperty status;

    public AttendanceRecord(String studentName, String department, String date, String time, String status) {
        this.studentName = new SimpleStringProperty(studentName);
        this.department = new SimpleStringProperty(department);
        this.date = new SimpleStringProperty(date);
        this.time = new SimpleStringProperty(time);
        this.status = new SimpleStringProperty(status);
    }

    public StringProperty studentNameProperty() { return studentName; }
    public StringProperty departmentProperty() { return department; }
    public StringProperty dateProperty() { return date; }
    public StringProperty timeProperty() { return time; }
    public StringProperty statusProperty() { return status; }

    public String getStudentName() { return studentName.get(); }
    public String getDepartment() { return department.get(); }
    public String getDate() { return date.get(); }
    public String getTime() { return time.get(); }
    public String getStatus() { return status.get(); }
}