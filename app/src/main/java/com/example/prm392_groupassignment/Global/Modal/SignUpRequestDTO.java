package com.example.prm392_groupassignment.Global.Modal;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SignUpRequestDTO {
    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("role")
    private String role = "User";

    @SerializedName("weight")
    private Integer weight;

    @SerializedName("goalWeight")
    private Integer goalWeight;

    @SerializedName("height")
    private Double height;

    @SerializedName("gender")
    private String gender;

    @SerializedName("age")
    private Integer age;

    @SerializedName("goalId")
    private Integer goalId;

    @SerializedName("mealScheduledDTO")
    private MealScheduledDTO mealScheduledDTO;

    @SerializedName("listAllergies")
    private List<Integer> listAllergies = new ArrayList<>();

    @SerializedName("listHConditions")
    private List<Integer> listHConditions = new ArrayList<>();

    @SerializedName("deviceId")
    private String deviceId;

    // Constructor (default mealScheduledDTO)
    public SignUpRequestDTO(MealScheduledDTO mealScheduledDTO) {
        this.mealScheduledDTO = mealScheduledDTO;
    }

    // Setters (chỉ cần cho fields cần thiết, dựa trên Flutter)
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    // Thêm setters khác nếu cần từ args
}