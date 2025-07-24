package com.example.prm392_groupassignment.Global.API;

import com.example.prm392_groupassignment.Global.Modal.CustomerLoginResponse;
import com.example.prm392_groupassignment.Global.Modal.LoginRequest;
import com.example.prm392_groupassignment.Global.Modal.RecipeResponse;
import com.example.prm392_groupassignment.Global.Modal.SignUpRequestDTO;
import com.example.prm392_groupassignment.Global.Modal.SimplifiedSignUpRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthHomeService {
    // New: Login
    @POST("api/Auth/customer/login")
    Call<CustomerLoginResponse> login(@Body LoginRequest body);

    // New: Signup
    @POST("api/SimplifiedAuth/simplified-signup")
    Call<CustomerLoginResponse> simplifiedSignup(@Body SimplifiedSignUpRequest body);
    // New: Recipes home
    @GET("api/Recipes/home")
    Call<RecipeResponse> getRecipes(
            @Query("page") int page,
            @Query("pageSize") int pageSize,
            @Query("category") String category,  // null nếu không có
            @Query("searchTerm") String searchTerm  // null nếu không có
    );
}