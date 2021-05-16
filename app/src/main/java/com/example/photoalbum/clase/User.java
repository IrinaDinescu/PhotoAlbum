package com.example.photoalbum.clase;

public class User {

    private String name;
    private String uid;
    private String profileImageUri;
    private String email;

    public User(){
    }

    public User(String name, String uid, String profileImageUri, String email) {
        this.name = name;
        this.uid = uid;
        this.profileImageUri = profileImageUri;
        this.email = email;
    }

    public User(String name, String uid){
        this.name = name;
        this.uid = uid;
        this.profileImageUri = "";
        this.email = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfileImageUri() {
        return profileImageUri;
    }

    public void setProfileImageUri(String profileImageUri) {
        this.profileImageUri = profileImageUri;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
