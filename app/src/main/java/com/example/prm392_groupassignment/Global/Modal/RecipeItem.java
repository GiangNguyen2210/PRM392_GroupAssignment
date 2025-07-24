package com.example.prm392_groupassignment.Global.Modal;

import com.google.gson.annotations.SerializedName;

public class RecipeItem {
    @SerializedName("recipeId")
    private int recipeId;

    @SerializedName("recipeName")
    private String recipeName;

    @SerializedName("timeEstimation")
    private int timeEstimation;

    @SerializedName("difficultyEstimation")
    private int difficultyEstimation;

    @SerializedName("mealName")
    private String mealName;

    @SerializedName("imageUrl")
    private String imageUrl;

    // Getters
    public int getRecipeId() { return recipeId; }
    public String getRecipeName() { return recipeName; }
    public int getTimeEstimation() { return timeEstimation; }
    public int getDifficultyEstimation() { return difficultyEstimation; }
    public String getMealName() { return mealName; }
    public String getImageUrl() { return imageUrl; }
}