package com.cooldevs.ridealong.Model;

import java.util.HashMap;

public class User {

    private String uid;
    private String email;
    private String phoneNumber;
    private String image;
    private HashMap < String, User > acceptList; // list of user friends

    public User() {  }

    public User(String uid, String email) {
        this.uid = uid;
        this.email = email;
        this.image = image;
        this.phoneNumber = phoneNumber;

        acceptList = new HashMap < > ();
    }

    // getters/setters
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phoneNumber; }
    public void setPhone(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public HashMap < String, User > getAcceptList() { return acceptList; }

    public void setAcceptList(HashMap < String, User > acceptList) { this.acceptList = acceptList;  }
}