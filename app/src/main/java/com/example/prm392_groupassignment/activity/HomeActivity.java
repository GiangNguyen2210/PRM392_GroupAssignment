package com.example.prm392_groupassignment.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.prm392_groupassignment.Global.API.AuthHomeService;
import com.example.prm392_groupassignment.Global.Modal.RecipeItem;
import com.example.prm392_groupassignment.Global.Modal.RecipeResponse;
import com.example.prm392_groupassignment.ProfileContent.ProfileActivity;
import com.example.prm392_groupassignment.R;
import com.example.prm392_groupassignment.RecipeDetail.API.ApiClient;
import com.example.prm392_groupassignment.RecipeDetail.RecipeDetailActivity;
import com.example.prm392_groupassignment.adapter.RecipeGridAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private Button prevBtn, nextBtn;
    private TextView pageInfo;
    private ProgressBar progressBar;
    private ChipGroup categoryChips;
    private EditText searchEdit;
    private String selectedCategory;
    private String searchTerm;
    private int currentPage = 1;
    private int totalPages = 1;
    private int recipesPerPage = 10;
    private List<RecipeItem> allRecipes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        viewPager = findViewById(R.id.view_pager);
        prevBtn = findViewById(R.id.prev_btn);
        nextBtn = findViewById(R.id.next_btn);
        pageInfo = findViewById(R.id.page_info);
        progressBar = findViewById(R.id.progress_bar);
        categoryChips = findViewById(R.id.category_chips);
        searchEdit = findViewById(R.id.search_edit);

        // Setup categories
        String[] categories = {"All Recipes", "Breakfast", "Lunch", "Dinner", "Snacks"};
        for (String cat : categories) {
            Chip chip = new Chip(this);
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                selectedCategory = cat.equals("All Recipes") ? null : cat.toLowerCase();
                currentPage = 1; // Reset về trang 1 khi đổi category
                fetchRecipes(currentPage);
            });
            categoryChips.addView(chip);
        }

        // Search (dùng Enter key)
        searchEdit.setOnEditorActionListener((v, actionId, event) -> {
            searchTerm = searchEdit.getText().toString().trim();
            if (TextUtils.isEmpty(searchTerm)) searchTerm = null;
            currentPage = 1; // Reset về trang 1 khi tìm kiếm
            fetchRecipes(currentPage);
            return true;
        });

        // Pagination
        prevBtn.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                fetchRecipes(currentPage);
            }
        });
        nextBtn.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                fetchRecipes(currentPage);
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // Không cần vì mỗi lần fetchRecipes sẽ chỉ có 1 trang
            }
        });

        fetchRecipes(currentPage);
    }

    private void fetchRecipes(int page) {
        progressBar.setVisibility(View.VISIBLE);
        AuthHomeService apiService = ApiClient.getInstance().create(AuthHomeService.class);
        apiService.getRecipes(page, recipesPerPage, selectedCategory, searchTerm).enqueue(new Callback<RecipeResponse>() {
            @Override
            public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    RecipeResponse res = response.body();
                    allRecipes = res.getItems() != null ? res.getItems() : new ArrayList<>();
                    totalPages = (int) Math.ceil((double) res.getTotalCount() / recipesPerPage);
                    currentPage = page; // Cập nhật trang hiện tại
                    if (allRecipes.isEmpty()) {
                        Toast.makeText(HomeActivity.this, "No recipes found", Toast.LENGTH_SHORT).show();
                        viewPager.setAdapter(null); // Xóa adapter nếu không có dữ liệu
                        updatePageInfo();
                        return;
                    }
                    setupViewPager();
                    updatePageInfo();
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load recipes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RecipeResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupViewPager() {
        viewPager.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                RecyclerView rv = new RecyclerView(parent.getContext());
                rv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                rv.setLayoutManager(new GridLayoutManager(HomeActivity.this, 2));
                return new RecyclerView.ViewHolder(rv) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                RecyclerView rv = (RecyclerView) holder.itemView;
                // Hiển thị toàn bộ danh sách công thức của trang hiện tại
                RecipeGridAdapter adapter = new RecipeGridAdapter(allRecipes, recipe -> {
                    Intent intent = new Intent(HomeActivity.this, RecipeDetailActivity.class);
                    intent.putExtra("recipeId", recipe.getRecipeId());
                    startActivity(intent);
                });
                rv.setAdapter(adapter);
            }

            @Override
            public int getItemCount() {
                // Chỉ có 1 trang trong ViewPager2, vì fetchRecipes lấy dữ liệu từng trang
                return 1;
            }
        });

        // Đảm bảo ViewPager2 hiển thị trang đầu tiên
        viewPager.setCurrentItem(0, false);
    }

    private void updatePageInfo() {
        pageInfo.setText("Page " + currentPage + " of " + totalPages);
        prevBtn.setEnabled(currentPage > 1);
        nextBtn.setEnabled(currentPage < totalPages);
    }
}