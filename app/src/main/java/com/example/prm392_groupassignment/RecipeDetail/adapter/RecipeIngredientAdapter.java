package com.example.prm392_groupassignment.RecipeDetail.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm392_groupassignment.R;
import com.example.prm392_groupassignment.RecipeDetail.Modal.Ingredient;
import java.util.List;

public class RecipeIngredientAdapter extends RecyclerView.Adapter<RecipeIngredientAdapter.ViewHolder> {

    private List<Ingredient> ingredients;

    public RecipeIngredientAdapter(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.tvIngredient.setText(String.format("• %s: %s %s",
                ingredient.getIngredient() != null ? ingredient.getIngredient() : "Unknown",
                ingredient.getAmount() != null ? ingredient.getAmount() : "0",
                ingredient.getDefaultUnit() != null ? ingredient.getDefaultUnit() : "unit"));
    }

    @Override
    public int getItemCount() {
        return ingredients != null ? ingredients.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIngredient;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIngredient = itemView.findViewById(R.id.tvIngredient);
        }
    }
}