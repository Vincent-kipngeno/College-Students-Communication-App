package com.example.college_students_communication_app.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    public String uid, email, username, phone;
    @Exclude
    public boolean isChecked;

    public User(){

    }

    public User(String uid, String email, String username, String phone) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.isChecked = false;
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
}
