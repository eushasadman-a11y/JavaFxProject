package com.attendance.model;

public class Profile {
    private final String studentId;
    private final String fullName;
    private final String email;
    private final String department;
    private final String role;
    private final String phone;
    private String photoPath;

    public Profile(String studentId, String fullName, String email, String department, String role, String phone) {
        this.studentId  = studentId;
        this.fullName   = fullName;
        this.email      = email;
        this.department = department;
        this.role       = role;
        this.phone      = phone;
        this.photoPath  = null;
    }

    public Profile(String studentId, String fullName, String email, String department, String role, String phone, String photoPath) {
        this(studentId, fullName, email, department, role, phone);
        this.photoPath = photoPath;
    }

    public String getStudentId()  { return studentId; }
    public String getFullName()   { return fullName; }
    public String getEmail()      { return email; }
    public String getDepartment() { return department; }
    public String getDept()       { return department; }  
    public String getRole()       { return role; }
    public String getPhone()      { return phone; }
    public String getPhotoPath()  { return photoPath; }
}
