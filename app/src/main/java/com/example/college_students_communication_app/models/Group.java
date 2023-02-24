package com.example.college_students_communication_app.models;

import android.net.Uri;

public class Group {

    public String groupName, description, admin, latestMessage, senderName, groupId;
    public String profileImage;
    public long timeStamp;

    public Group(){

    }

    public Group(String groupName, String description, String profileImage, String admin, String latestMessage, String senderName, long timeStamp, String groupId) {
        this.groupName = groupName;
        this.groupId = groupId;
        this.description = description;
        this.profileImage = profileImage;
        this.admin = admin;
        this.latestMessage = latestMessage;
        this.senderName = senderName;
        this.timeStamp = timeStamp;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
