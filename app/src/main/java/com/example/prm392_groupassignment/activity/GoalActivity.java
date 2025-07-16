package com.example.prm392_groupassignment.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_groupassignment.R;
import com.example.prm392_groupassignment.adapter.GoalGridAdapter;
import com.example.prm392_groupassignment.model.Goal;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class GoalActivity extends AppCompatActivity {

    private GridView gridViewGoals;
    private GoalGridAdapter adapter;
    private List<Goal> goalList;

    public interface GoalApiService {
        @GET("api/Goals")
        Call<List<Goal>> getGoals();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_goal_page);

        gridViewGoals = findViewById(R.id.grid_view_goals);
        Button btnBack = findViewById(R.id.btn_back);
        Button btnContinue = findViewById(R.id.button3);


        goalList = new ArrayList<>();
        adapter = new GoalGridAdapter(this, goalList);
        gridViewGoals.setAdapter(adapter);

        fetchGoalsFromApi();

        gridViewGoals.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Goal selectedGoal = goalList.get(position);
                Toast.makeText(GoalActivity.this, "Bạn đã chọn: " + selectedGoal.getGoalName(), Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(GoalActivity.this, "Tiếp tục...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchGoalsFromApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://appchao.azurewebsites.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GoalApiService service = retrofit.create(GoalApiService.class);
        Call<List<Goal>> call = service.getGoals();

        call.enqueue(new Callback<List<Goal>>() {
            @Override
            public void onResponse(Call<List<Goal>> call, Response<List<Goal>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    goalList.clear();
                    goalList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    Toast.makeText(GoalActivity.this, "Goals loaded!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GoalActivity.this, "Failed to load goals: " + response.message(), Toast.LENGTH_LONG).show();
                    Log.e("API_CALL", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Goal>> call, Throwable t) {
                Toast.makeText(GoalActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("API_CALL", "Error: " + t.getMessage(), t);
            }
        });
    }
}
