package com.cooldevs.ridealong;

public class User {
    private String email;
    private String password;
    private String fullname;
    private String phone;
    // [OBSOLETE]
    public User() {

    }

    public User(String email, String password){
        this.email = email;
        this.password = password;
    }

    public User(String email, String password, String fullname, String phone)
    {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.phone = phone;
    }

    public String getEmail(){
        return email;
    }

    public String getPassword(){
        return password;
    }

    public String getFullname(){
        return fullname;
    }

    public String getPhone(){
        return phone;
    }

}
