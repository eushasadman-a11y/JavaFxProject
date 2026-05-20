package com.attendance.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RecentAttendance {
    
    private final StringProperty name;
    private final StringProperty time;

    public RecentAttendance(String name, String time) {
        this.name = new SimpleStringProperty(name);
        this.time = new SimpleStringProperty(time);
    }

    public String getName() { 
        return name.get(); 
    }
    
    public StringProperty nameProperty() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name.set(name); 
    }

    public String getTime() { 
        return time.get(); 
    }
    
    public StringProperty timeProperty() { 
        return time; 
    }
    
    public void setTime(String time) { 
        this.time.set(time); 
    }
}