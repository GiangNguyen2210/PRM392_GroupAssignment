package com.example.prm392_groupassignment.Global.Modal;

import com.google.gson.annotations.SerializedName;

public class ErrorMessageResponse {
    @SerializedName("Message")
    private String Message;

    @SerializedName("Error")
    private String Error;

    // Getter
    public String getMessage() { return Message != null ? Message : Error; }
}