package com.example.prm392_groupassignment.Global.Modal;

public class LoginRequest {
    private String email;
    private String password;
    private String deviceToken;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
        this.deviceToken = "string";
    }
}