package com.example.prm392_groupassignment.Global.Modal;

import com.google.gson.annotations.SerializedName;

public class MealScheduledDTO {
    @SerializedName("breakFastTime")
    private String breakFastTime;

    @SerializedName("lunchTime")
    private String lunchTime;

    @SerializedName("dinnerTime")
    private String dinnerTime;

    // Constructor empty (Flutter default)
    public MealScheduledDTO() {}
}
