package com.example.prm392_groupassignment.activity;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_groupassignment.R;
import com.example.prm392_groupassignment.model.Allergy;
import com.example.prm392_groupassignment.model.HealCondition;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class HealthConditionsActivity extends AppCompatActivity {

    private LinearLayout container;

    public interface HealthConditionsApiService {
        @GET("api/HealthCondition/health-conditions")
        Call<PaginatedHealResponse> getHealthConditionsApiService(
                @Query("type") String type,
                @Query("searchTerm") String searchTerm,
                @Query("page") int page,
                @Query("pageSize") int pageSize
        );
    }

    List<HealCondition> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_conditions);

        container = findViewById(R.id.conditionContainer);

        this.list = new ArrayList<>();

        fetch();

    }

    private void addConditionItem(HealCondition condition) {
        HealthConditionItemView itemView = new HealthConditionItemView(this);
        itemView.bind(condition);

        itemView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this,
                    condition.getHealthConditionName() + (isChecked ? " selected" : " deselected"),
                    Toast.LENGTH_SHORT).show();
        });

        container.addView(itemView);
    }

    private List<HealCondition> getFakeApiResponse() {
        List<HealCondition> list = new ArrayList<>();
        list.add(new HealCondition(1, "Diabetes", "Type 1 or Type 2 diabetes", "chronic"));
        list.add(new HealCondition(2, "High Blood Pressure", "Hypertension", "chronic"));
        list.add(new HealCondition(3, "Heart Disease", "Cardiovascular conditions", "chronic"));
        list.add(new HealCondition(4, "Asthma", "Respiratory condition", "chronic"));
        list.add(new HealCondition(5, "Arthritis", "Joint inflammation", "chronic"));
        list.add(new HealCondition(6, "Allergies", "Food or environmental", "immunological"));
        list.add(new HealCondition(7, "Depression/Anxiety", "Mental health conditions", "mental"));
        list.add(new HealCondition(8, "Sleep Disorders", "Sleep-related conditions", "neurological"));
        return list;
    }

    private void fetch() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://appchao.azurewebsites.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        HealthConditionsApiService service = retrofit.create(HealthConditionsApiService.class);

        Call<PaginatedHealResponse> call = service.getHealthConditionsApiService("", "", 1, 33);

        call.enqueue(new Callback<PaginatedHealResponse>() {
            @Override
            public void onResponse(Call<PaginatedHealResponse> call, Response<PaginatedHealResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    list.clear();
                    list.addAll(response.body().getItems());
                    for (HealCondition item : list) {
                        addConditionItem(item);
                    }
                } else {
                    Log.e("API_ERROR", "Response failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PaginatedHealResponse> call, Throwable t) {
                Log.e("API_ERROR", "Network error: " + t.getMessage(), t);
            }
        });
    }
}


class PaginatedHealResponse {
    private List<HealCondition> items;
    private int page;
    private int pageSize;
    private int totalCount;
    private boolean hasNextPage;
    private boolean hasPreviousPage;

    // Getters
    public List<HealCondition> getItems() {
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

 class HealthConditionItemView extends LinearLayout {

    private TextView tvTitle;
    private TextView tvSubtitle;
    private Switch switchToggle;

    public HealthConditionItemView(Context context) {
        super(context);
        init(context);
    }

    public HealthConditionItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_health_condition, this, true);

        tvTitle = findViewById(R.id.tvConditionTitle);
        tvSubtitle = findViewById(R.id.tvConditionSubtitle);
        switchToggle = findViewById(R.id.switchCondition);
    }

    public void bind(HealCondition condition) {
        tvTitle.setText(condition.getHealthConditionName());
        tvSubtitle.setText(condition.getBriefDescription());
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        switchToggle.setOnCheckedChangeListener(listener);
    }

    public boolean isChecked() {
        return switchToggle.isChecked();
    }

    public void setChecked(boolean checked) {
        switchToggle.setChecked(checked);
    }
}


