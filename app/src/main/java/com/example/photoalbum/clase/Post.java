package com.example.photoalbum.clase;

public class Post {

    private String pictureName;
    private String imageUrl;
    private String publisherId;
    private String postId;
    private String uId;
    private String date;


    public Post() {
    }

    public Post(String pictureName, String imageUrl, String publisherId, String postId, String uId, String date) {
        this.imageUrl = imageUrl;
        this.publisherId = publisherId;
        this.postId = postId;
        this.uId = uId;
        this.date = date;
        this.pictureName = pictureName;
    }

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
