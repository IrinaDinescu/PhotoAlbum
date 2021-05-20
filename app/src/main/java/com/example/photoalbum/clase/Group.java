package com.example.photoalbum.clase;

public class Group {

    private String name;
    private String id;
    private String admin;

    public Group(){

    }

    public Group(String name, String id, String admin) {
        this.name = name;
        this.id = id;
        this.admin = admin;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }
}
