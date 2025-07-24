package com.example.prm392_groupassignment.Global.Modal;

import com.google.gson.annotations.SerializedName;

public class CustomerLoginResponse {
    @SerializedName("token")
    private String Token;

    @SerializedName("upId")
    private int UPId;

    @SerializedName("role")
    private String Role;

    // Getters
    public String getToken() { return Token; }
    public int getUPId() { return UPId; }
    public String getRole() { return Role; }
}