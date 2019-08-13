package com.example.chillspot.models;

public class Posts
{
    public String uid, date, time, desc, postImage, profileImage, userName;

    public Posts()
    {

    }

    public Posts(String uid, String date, String time, String desc, String postImage, String profileImage, String userName) {
        this.uid = uid;
        this.date = date;
        this.time = time;
        this.desc = desc;
        this.postImage = postImage;
        this.profileImage = profileImage;
        this.userName = userName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
