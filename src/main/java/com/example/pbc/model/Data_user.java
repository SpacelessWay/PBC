package com.example.pbc.model;

public class Data_user {
    private String login;
    private String password;
    private String uuid;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    public Data_user(String login, String password,String uuid, String firstName,String lastName,String email,String phone){
        this.login=login;
        this.password=password;
        this.uuid=uuid;
        this.firstName=firstName;
        this.lastName=lastName;
        this.email=email;
        this.phone=phone;
    }
    public String getLogin(){
        return login;
    }
    public String getPassword(){
        return password;
    }
    public String getUuid(){
        return uuid;
    }
    public String getFirstName(){
        return firstName;
    }
    public String getLastName(){
        return lastName;
    }
    public String getEmail(){
        return email;
    }
    public String getPhone(){
        return phone;
    }
}
