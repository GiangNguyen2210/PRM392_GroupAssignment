package com.example.prm392_groupassignment.RecipeDetail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm392_groupassignment.R;
import com.example.prm392_groupassignment.RecipeDetail.Modal.Ingredient;
import com.example.prm392_groupassignment.RecipeDetail.Modal.Recipe;
import com.example.prm392_groupassignment.RecipeDetail.adapter.RecipeIngredientAdapter;
import com.example.prm392_groupassignment.RecipeDetail.API.ApiClient;
import com.example.prm392_groupassignment.RecipeDetail.API.ApiService;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.Toast;

public class RecipeDetailActivity extends AppCompatActivity {

    private static final String TAG = "RecipeDetailActivity";
    private ProgressBar progressBar;
    private TextView tvRecipeTitle, tvMealType, tvTime;
    private ImageView ivVideoThumbnail, ivFlag;
    private LinearLayout llDifficultyStars, llRecipeSteps, llSuggestions;
    private WebView webViewVideo;
    private Recipe currentRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Initialize UI elements
        progressBar = findViewById(R.id.progressBar);
        tvRecipeTitle = findViewById(R.id.tvRecipeTitle);
        tvMealType = findViewById(R.id.tvMealType);
        tvTime = findViewById(R.id.tvTime);
        ivVideoThumbnail = findViewById(R.id.ivVideoThumbnail);
        ivFlag = findViewById(R.id.ivFlag);
        llDifficultyStars = findViewById(R.id.llDifficultyStars);
        llRecipeSteps = findViewById(R.id.llRecipeSteps);
        llSuggestions = findViewById(R.id.llSuggestions);
        webViewVideo = findViewById(R.id.webViewVideo);

        // Configure WebView
        WebSettings webSettings = webViewVideo.getSettings();
        webSettings.setJavaScriptEnabled(true); // Required for YouTube
        webViewVideo.setWebChromeClient(new WebChromeClient());
        webViewVideo.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE); // Hide progress when video loads
            }
        });

        // Set up RecyclerView for ingredients
        RecyclerView rvIngredients = findViewById(R.id.rvIngredients);
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvIngredients.setHasFixedSize(true);

        // Fetch recipe data
        int recipeId = 2; // Hardcoded for testing
        fetchRecipe(recipeId);

        // Set click listener for video thumbnail
        ivVideoThumbnail.setOnClickListener(this::playVideo);
    }

    private void fetchRecipe(int recipeId) {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Fetching recipe with ID: " + recipeId);
        ApiService apiService = ApiClient.getInstance().create(ApiService.class);
        Log.d(TAG, "ApiService created, base URL: " + ApiClient.getInstance().baseUrl().toString());
        Call<Recipe> call = apiService.getRecipeById(recipeId);

        call.enqueue(new Callback<Recipe>() {
            @Override
            public void onResponse(Call<Recipe> call, Response<Recipe> response) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Response Code: " + response.code());
                Log.d(TAG, "Raw Response: " + (response.body() != null ? response.body().toString() : "Null body"));
                if (response.isSuccessful() && response.body() != null) {
                    currentRecipe = response.body();
                    Log.d(TAG, "Fetched Recipe: " + currentRecipe.getRecipeName());
                    runOnUiThread(() -> displayRecipe(currentRecipe));
                } else {
                    String errorBody = response.errorBody() != null ? response.errorBody().toString() : "No error body";
                    Log.e(TAG, "API Error: Code " + response.code() + ", Error: " + errorBody);
                    runOnUiThread(() -> {
                        Toast.makeText(RecipeDetailActivity.this, "Failed to load recipe. Code: " + response.code() + " Error: " + errorBody, Toast.LENGTH_LONG).show();
                        displayFallbackMessage();
                    });
                }
            }

            @Override
            public void onFailure(Call<Recipe> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "API Failure: " + t.getMessage(), t);
                runOnUiThread(() -> {
                    Toast.makeText(RecipeDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    displayFallbackMessage();
                });
            }
        });
    }

    private void displayRecipe(Recipe recipe) {
        try {
            tvRecipeTitle.setText(recipe.getRecipeName() != null ? recipe.getRecipeName() : "No Name");
            tvMealType.setText(recipe.getMeals() != null ? recipe.getMeals() : "No Meal");

            int timeInMinutes = recipe.getTimeEstimation();
            int hours = timeInMinutes / 60;
            int minutes = timeInMinutes % 60;
            String timeText = hours > 0 ? String.format("%dh %dm", hours, minutes) : String.format("%dm", minutes);
            tvTime.setText(timeText);

            // Enhanced difficulty stars with RatingBar
            llDifficultyStars.removeAllViews();
            int difficulty = Math.min(recipe.getDifficultyEstimation(), 5); // Define difficulty here
            RatingBar ratingBar = new RatingBar(this, null, android.R.attr.ratingBarStyleSmall);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 8); // Adjust as needed
            ratingBar.setLayoutParams(params);
            ratingBar.setNumStars(5);
            ratingBar.setStepSize(1.0f);
            ratingBar.setRating((float) difficulty);
            ratingBar.setIsIndicator(true); // Prevent user interaction
            llDifficultyStars.addView(ratingBar);

            // Set up RecyclerView with ingredients
            RecyclerView rvIngredients = findViewById(R.id.rvIngredients);
            List<Ingredient> ingredients = recipe.getIngredients();
            if (ingredients != null) {
                RecipeIngredientAdapter adapter = new RecipeIngredientAdapter(ingredients);
                rvIngredients.setAdapter(adapter);
            } else {
                TextView tvNoIngredients = new TextView(this);
                tvNoIngredients.setText("No ingredients available");
                tvNoIngredients.setTextSize(14);
                rvIngredients.setAdapter(null);
                ((ViewGroup) rvIngredients.getParent()).addView(tvNoIngredients);
            }

            ivFlag.setImageResource(getFlagResource(recipe.getNation()));

            // Display recipe steps
            llRecipeSteps.removeAllViews();
            String recipeSteps = recipe.getRecipeSteps();
            if (recipeSteps != null && !recipeSteps.trim().isEmpty()) {
                String[] steps = recipeSteps.split("\n");
                for (String step : steps) {
                    if (!step.trim().isEmpty()) {
                        String[] parts = step.split("\\|");
                        if (parts.length >= 3) {
                            String stepNumber = parts[1].trim();
                            String description = parts[2].trim();
                            TextView tvStep = new TextView(this);
                            tvStep.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            tvStep.setText(String.format("%s. %s", stepNumber, description));
                            tvStep.setTextSize(14);
                            tvStep.setPadding(0, 4, 0, 4);
                            llRecipeSteps.addView(tvStep);
                        }
                    }
                }
            } else {
                TextView tvNoSteps = new TextView(this);
                tvNoSteps.setText("No steps available");
                tvNoSteps.setTextSize(14);
                llRecipeSteps.addView(tvNoSteps);
            }

            // Placeholder for suggestions (to be implemented later)
            llSuggestions.removeAllViews();

        } catch (Exception e) {
            Log.e(TAG, "Error in displayRecipe: " + e.getMessage(), e);
            runOnUiThread(() -> Toast.makeText(this, "UI Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private void displayFallbackMessage() {
        tvRecipeTitle.setText("Failed to Load Recipe");
        tvMealType.setText("N/A");
        tvTime.setText("N/A");
        llDifficultyStars.removeAllViews();
        RecyclerView rvIngredients = findViewById(R.id.rvIngredients);
        rvIngredients.setAdapter(null);
        TextView tvError = new TextView(this);
        tvError.setText("Could not fetch recipe details. Check internet connection or API.");
        tvError.setTextSize(14);
        ((ViewGroup) rvIngredients.getParent()).addView(tvError);
        llRecipeSteps.removeAllViews();
        llSuggestions.removeAllViews();
    }

    private int getFlagResource(String nation) {
        switch (nation != null ? nation.toLowerCase() : "") {
            case "viet nam":
                return R.drawable.ic_flag_vietnam;
//            case "united states":
//                return R.drawable.ic_flag_usa;
//            case "italy":
//                return R.drawable.ic_flag_italy;
            default:
                return R.drawable.ic_flag_placeholder;
        }
    }

    public void playVideo(View view) {
        if (currentRecipe != null && currentRecipe.getInstructionVideoLink() != null) {
            String videoUrl = currentRecipe.getInstructionVideoLink();
            // Extract YouTube video ID (e.g., A_o2qfaTgKs from https://youtu.be/A_o2qfaTgKs?si=...)
            String videoId = videoUrl.contains("youtu.be/") ? videoUrl.split("youtu.be/")[1].split("\\?")[0] : videoUrl.split("v=")[1].split("\\?")[0];
            String embedUrl = "https://www.youtube.com/embed/" + videoId;

            webViewVideo.setVisibility(View.VISIBLE);
            webViewVideo.loadData("<html><body><iframe width=\"100%\" height=\"200\" src=\"" + embedUrl + "\" frameborder=\"0\" allowfullscreen></iframe></body></html>", "text/html", "utf-8");
            ivVideoThumbnail.setVisibility(View.GONE); // Hide thumbnail while video plays
        } else {
            Toast.makeText(this, "Video link not available", Toast.LENGTH_SHORT).show();
        }
    }
}