package com.example.prm392_groupassignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.prm392_groupassignment.RecipeDetail.RecipeDetailActivity;
import com.example.prm392_groupassignment.activity.AllergyActivity;
import com.example.prm392_groupassignment.activity.HealthConditionsActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Use a simple layout (e.g., a button or list)

        // Navigate to RecipeDetailActivity
        Intent intent = new Intent(this, HealthConditionsActivity.class);
        startActivity(intent);
        finish(); // Optional: Close MainActivity if it’s just a launcher
    }
}