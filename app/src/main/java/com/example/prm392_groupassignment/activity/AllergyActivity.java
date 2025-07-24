package com.example.prm392_groupassignment.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_groupassignment.R;
import com.example.prm392_groupassignment.model.Allergy;
import com.example.prm392_groupassignment.model.Goal;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class AllergyActivity extends AppCompatActivity {

    private GridLayout allergenGrid;
    private FlexboxLayout selectedContainer;

    private List<Allergy> allAllergens;

    private List<String> selectedAllergens;

    public interface AllergiesApiService {
        @GET("api/Ingredients/common/allergens")
        Call<List<Allergy>> getAllergens();

        @GET("api/Ingredients")
        Call<PaginatedAllergyResponse> searchIngredients(
                @Query("searchTerm") String searchTerm,
                @Query("page") int page,
                @Query("pageSize") int pageSize
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allergy);

        allAllergens = new ArrayList<>();

        selectedAllergens = new ArrayList<>();

        SearchView searchView = findViewById(R.id.searchView);

// Ensure SearchView is expanded and focused
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.setQueryHint("Search allergens...");
        searchView.clearFocus(); // Prevent keyboard auto-popup on load

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) {
                    searchAllergens(query.trim());
                    // Hide keyboard after submit
                    searchView.clearFocus();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    fetchCommonAllergens();
                }
                return false;
            }
        });

        fetchCommonAllergens();

        allergenGrid = findViewById(R.id.allergenGrid);
        selectedContainer = findViewById(R.id.selectedAllergensContainer);
    }

    private void refreshAllergenGrid() {
        allergenGrid.removeAllViews();

        for (Allergy allergen : allAllergens) {
            if (selectedAllergens.contains(allergen.getIngredientName())) continue;

            Button btn = new Button(this);
            btn.setText(allergen.getIngredientName());
            btn.setTextColor(Color.BLACK);
            btn.setAllCaps(false);
            btn.setTextSize(16);
            btn.setBackgroundResource(R.drawable.allergen_button_bg);
            btn.setPadding(24, 16, 24, 16);

            btn.setOnClickListener(v -> {
                selectedAllergens.add(allergen.getIngredientName());
                updateSelectedAllergensUI();
                refreshAllergenGrid();
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(8, 8, 8, 8);
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            btn.setLayoutParams(params);

            allergenGrid.addView(btn);
        }
    }

    private void updateSelectedAllergensUI() {
        selectedContainer.removeAllViews();

        if (selectedAllergens.isEmpty()) {
            selectedContainer.setVisibility(View.GONE);
            return;
        }

        selectedContainer.setVisibility(View.VISIBLE);

        for (String allergen : selectedAllergens) {
            LinearLayout chipLayout = new LinearLayout(this);
            chipLayout.setOrientation(LinearLayout.HORIZONTAL);
            chipLayout.setBackgroundResource(R.drawable.chip_bg);
            chipLayout.setPadding(24, 12, 24, 12);
            chipLayout.setGravity(Gravity.CENTER_VERTICAL);

            TextView text = new TextView(this);
            text.setText(allergen);
            text.setTextColor(Color.BLACK);
            text.setTextSize(14);

            TextView close = new TextView(this);
            close.setText(" ✕");
            close.setTextColor(Color.DKGRAY);
            close.setTextSize(14);
            close.setPadding(16, 0, 0, 0);
            close.setOnClickListener(v -> {
                selectedAllergens.remove(allergen);
                updateSelectedAllergensUI();
                refreshAllergenGrid();
            });

            chipLayout.addView(text);
            chipLayout.addView(close);

            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(8, 8, 8, 8);
            chipLayout.setLayoutParams(lp);

            selectedContainer.addView(chipLayout);
        }
    }

    private void fetchCommonAllergens() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://appchao.azurewebsites.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AllergiesApiService service = retrofit.create(AllergiesApiService.class);
        Call<List<Allergy>> call = service.getAllergens();

        call.enqueue(new Callback<List<Allergy>>() {
            @Override
            public void onResponse(Call<List<Allergy>> call, Response<List<Allergy>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allAllergens.clear();
                    allAllergens.addAll(response.body());
                    Toast.makeText(AllergyActivity.this, "AllergyActivity loaded!", Toast.LENGTH_SHORT).show();
                    refreshAllergenGrid();
                } else {
                    Toast.makeText(AllergyActivity.this, "Failed to load AllergyActivity: " + response.message(), Toast.LENGTH_LONG).show();
                    Log.e("API_CALL", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Allergy>> call, Throwable t) {
                Toast.makeText(AllergyActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("API_CALL", "Error: " + t.getMessage(), t);
            }
        });
    }

    private void searchAllergens(String searchTerm) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://appchao.azurewebsites.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AllergiesApiService service = retrofit.create(AllergiesApiService.class);

        Call<PaginatedAllergyResponse> call = service.searchIngredients(searchTerm, 1, 20);

        call.enqueue(new Callback<PaginatedAllergyResponse>() {
            @Override
            public void onResponse(Call<PaginatedAllergyResponse> call, Response<PaginatedAllergyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allAllergens.clear();
                    allAllergens.addAll(response.body().getItems());
                    refreshAllergenGrid();
                } else {
                    Log.e("API_ERROR", "Response failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PaginatedAllergyResponse> call, Throwable t) {
                Log.e("API_ERROR", "Network error: " + t.getMessage(), t);
            }
        });
    }

}

    class PaginatedAllergyResponse {
    private List<Allergy> items;
    private int page;
    private int pageSize;
    private int totalCount;
    private boolean hasNextPage;
    private boolean hasPreviousPage;

    // Getters
    public List<Allergy> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }
}
