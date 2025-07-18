package com.example.prm392_groupassignment.RecipeDetail.Modal;

public class Ingredient {
    private String ingredient;
    private String amount;
    private String defaultUnit;

    // Constructor
    public Ingredient(String ingredient, String amount, String defaultUnit) {
        this.ingredient = ingredient;
        this.amount = amount;
        this.defaultUnit = defaultUnit;
    }

    // Getters
    public String getIngredient() { return ingredient; }
    public String getAmount() { return amount; }
    public String getDefaultUnit() { return defaultUnit; }
}