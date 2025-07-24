package com.example.prm392_groupassignment.Global.Modal;

public class LoginRequest {
    private String email;
    private String password;
    private String deviceToken;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
        this.deviceToken = "1c75d608-0f85-406e-a761-b64db2ebc416";
    }
}