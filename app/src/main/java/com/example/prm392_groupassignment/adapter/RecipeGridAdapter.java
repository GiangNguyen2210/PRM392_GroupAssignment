package com.example.prm392_groupassignment.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_groupassignment.Global.Modal.RecipeItem;
import com.example.prm392_groupassignment.R;

import java.util.List;

public class RecipeGridAdapter extends RecyclerView.Adapter<RecipeGridAdapter.ViewHolder> {
    private List<RecipeItem> recipes;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(RecipeItem recipe);
    }

    public RecipeGridAdapter(List<RecipeItem> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeItem recipe = recipes.get(position);
        holder.title.setText(recipe.getRecipeName());
        holder.time.setText(recipe.getTimeEstimation() + " mins");
        holder.difficulty.setText("Difficulty: " + recipe.getDifficultyEstimation() + "/5");
        holder.meal.setText(recipe.getMealName().toUpperCase());

        // Tải hình ảnh bằng Glide
        Glide.with(holder.itemView.getContext())
                .load(recipe.getImageUrl())
                .placeholder(R.drawable.placeholder_image) // Hình ảnh placeholder nếu cần
                .error(R.drawable.error_image) // Hình ảnh lỗi nếu tải thất bại
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, time, difficulty, meal;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.recipe_title);
            time = itemView.findViewById(R.id.recipe_time);
            difficulty = itemView.findViewById(R.id.recipe_difficulty);
            meal = itemView.findViewById(R.id.recipe_meal);
            image = itemView.findViewById(R.id.recipe_image);
        }
    }
}