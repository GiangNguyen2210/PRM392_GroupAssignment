package com.example.prm392_groupassignment.Global.Modal;

import com.google.gson.annotations.SerializedName;

public class CustomerLoginResponse {
    @SerializedName("Token")
    private String Token;

    @SerializedName("UPId")
    private int UPId;

    @SerializedName("Role")
    private String Role;

    // Getters
    public String getToken() { return Token; }
    public int getUPId() { return UPId; }
    public String getRole() { return Role; }
}