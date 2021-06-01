package com.example.photoalbum.clase;

public class Model {

    boolean isSelected = false;
    User user;

    public Model(User user){
        this.user = user;
    }

    public User getUser(){
        return this.user;
    }

    public void setSelected(boolean selected){
        isSelected = selected;
    }

    public boolean isSelected(){
        return isSelected;
    }


}
