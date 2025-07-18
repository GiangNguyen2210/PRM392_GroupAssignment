package com.example.prm392_groupassignment.RecipeDetail.Modal;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Recipe {
    @SerializedName("recipeId")
    private int recipeId;

    @SerializedName("recipeName")
    private String recipeName;

    @SerializedName("meals")
    private String meals;

    @SerializedName("difficultyEstimation")
    private int difficultyEstimation;

    @SerializedName("timeEstimation")
    private int timeEstimation;

    @SerializedName("nation")
    private String nation;

    @SerializedName("instructionVideoLink")
    private String instructionVideoLink;

    @SerializedName("ingredients")
    private List<Ingredient> ingredients;

    @SerializedName("recipeSteps")
    private String recipeSteps;

    // Getters
    public int getRecipeId() { return recipeId; }
    public String getRecipeName() { return recipeName; }
    public String getMeals() { return meals; }
    public int getDifficultyEstimation() { return difficultyEstimation; }
    public int getTimeEstimation() { return timeEstimation; }
    public String getNation() { return nation; }
    public String getInstructionVideoLink() { return instructionVideoLink; }
    public List<Ingredient> getIngredients() { return ingredients; }
    public String getRecipeSteps() { return recipeSteps; }


}