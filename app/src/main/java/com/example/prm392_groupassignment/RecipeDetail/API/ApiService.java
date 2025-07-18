package com.example.prm392_groupassignment.RecipeDetail.API;

import com.example.prm392_groupassignment.RecipeDetail.Modal.Recipe;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("api/Recipes/{id}")
    Call<Recipe> getRecipeById(@Path("id") int id);
}