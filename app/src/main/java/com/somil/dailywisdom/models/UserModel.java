package com.somil.dailywisdom.models;

public class UserModel {
    private String uid;
    private String name;
    private String email;

    public UserModel() {
    }

    public UserModel(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}