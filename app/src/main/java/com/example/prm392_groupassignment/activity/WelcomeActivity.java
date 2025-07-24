package com.example.prm392_groupassignment.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_groupassignment.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page); // replace with actual layout name

        TextView loginText = findViewById(R.id.textView3);
        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            intent.putExtra("mode", "login");
            startActivity(intent);
        });

        Button startButton = findViewById(R.id.button);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, GoalActivity.class);
            startActivity(intent);
            finish(); // optional: prevents back navigation to welcome page
        });
    }
}
