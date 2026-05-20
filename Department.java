package com.attendance.controller;

public class Department {
    private final String name;
    private final String head;
    private final String location;

    public Department(String name, String head, String location) {
        this.name = name;
        this.head = head;
        this.location = location;
    }

    public String getName() { return name; }
    public String getHead() { return head; }
    public String getLocation() { return location; }
}