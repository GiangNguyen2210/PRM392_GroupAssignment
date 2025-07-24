package com.example.prm392_groupassignment.Global.Modal;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecipeResponse {
    @SerializedName("items")
    private List<RecipeItem> items;

    @SerializedName("page")
    private int page;

    @SerializedName("pageSize")
    private int pageSize;

    @SerializedName("totalCount")
    private int totalCount;

    @SerializedName("hasNextPage")
    private boolean hasNextPage;

    @SerializedName("hasPreviousPage")
    private boolean hasPreviousPage;

    // Getters
    public List<RecipeItem> getItems() { return items; }
    public int getTotalCount() { return totalCount; }
    // Other getters if needed
}