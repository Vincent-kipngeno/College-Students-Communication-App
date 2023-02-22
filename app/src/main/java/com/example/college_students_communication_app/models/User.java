package com.example.college_students_communication_app.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String uid, email, username, phone;
    public boolean isChecked;
    public ArrayList<String> groups;

    public User(){

    }

    public User(String uid, String email, String username, String phone) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.isChecked = false;
    }

    public User(String uid, String email, String username, String phone, ArrayList<String> groups) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.isChecked = false;
        this.groups = groups;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public ArrayList<String> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<String> groups) {
        this.groups = groups;
    }
}
