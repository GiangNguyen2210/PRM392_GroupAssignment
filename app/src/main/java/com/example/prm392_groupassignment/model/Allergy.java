package com.example.prm392_groupassignment.model;

import com.google.gson.annotations.SerializedName;

public class Allergy {
    @SerializedName("ingredientId")
    private int ingredientId;

    @SerializedName("ingredientName")
    private String ingredientName;

    @SerializedName("defaultUnit")
    private String defaultUnit;

    public Allergy(int ingredientId, String ingredientName, String defaultUnit) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.defaultUnit = defaultUnit;
    }

    public Allergy(){}

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }

    public void setDefaultUnit(String defaultUnit) {
        this.defaultUnit = defaultUnit;
    }

    @Override
    public String toString() {
        return "Allergy{" +
                "ingredientId=" + ingredientId +
                ", ingredientName='" + ingredientName + '\'' +
                ", defaultUnit='" + defaultUnit + '\'' +
                '}';
    }
}
