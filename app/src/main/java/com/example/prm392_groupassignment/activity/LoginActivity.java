package com.example.prm392_groupassignment.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_groupassignment.Global.API.AuthHomeService;
import com.example.prm392_groupassignment.Global.Modal.CustomerLoginResponse;
import com.example.prm392_groupassignment.Global.Modal.ErrorMessageResponse;
import com.example.prm392_groupassignment.Global.Modal.LoginRequest;
import com.example.prm392_groupassignment.Global.Modal.SimplifiedSignUpRequest;
import com.example.prm392_groupassignment.R;
import com.example.prm392_groupassignment.RecipeDetail.API.ApiClient;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private boolean isLogin = true;
    private LinearLayout loginForm, signUpForm;
    private EditText emailEdit, passwordEdit, confirmPasswordEdit;
    private CheckBox rememberMe;
    private Button loginBtn, signUpBtn;
    private TextView toggleLogin, toggleSignUp;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        String mode = getIntent().getStringExtra("mode");
        if ("signup".equalsIgnoreCase(mode)) {
            isLogin = false;
        } else {
            isLogin = true;
        }
        
        // Init views
        loginForm = findViewById(R.id.login_form);
        signUpForm = findViewById(R.id.signup_form);
        emailEdit = findViewById(R.id.email_edit); // For login form
        passwordEdit = findViewById(R.id.password_edit); // For login form
        confirmPasswordEdit = findViewById(R.id.confirm_password_edit); // For signup form
        rememberMe = findViewById(R.id.remember_me);
        loginBtn = findViewById(R.id.login_btn);
        signUpBtn = findViewById(R.id.signup_btn);
        toggleLogin = findViewById(R.id.toggle_login);
        toggleSignUp = findViewById(R.id.toggle_signup);

        updateForm();

        // Toggle
        toggleLogin.setOnClickListener(v -> {
            isLogin = true;
            updateForm();
        });
        toggleSignUp.setOnClickListener(v -> {
            isLogin = false;
            updateForm();
        });

        // Login button
        loginBtn.setOnClickListener(v -> performLogin());

        // Sign up button
        signUpBtn.setOnClickListener(v -> performSignUp());
    }

    private void updateForm() {
        if (isLogin) {
            loginForm.setVisibility(View.VISIBLE);
            signUpForm.setVisibility(View.GONE);
            toggleLogin.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            toggleSignUp.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            loginForm.setVisibility(View.GONE);
            signUpForm.setVisibility(View.VISIBLE);
            toggleLogin.setTextColor(getResources().getColor(android.R.color.darker_gray));
            toggleSignUp.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        }
    }

    private void performLogin() {
        String email = emailEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        Log.d(TAG, "Login attempt - Email: " + email + ", Password: " + password);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Log.d(TAG, "Login failed - Empty fields detected");
            Toast.makeText(this, "Please fill in both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthHomeService apiService = ApiClient.getInstance().create(AuthHomeService.class);
        LoginRequest request = new LoginRequest(email, password);
        apiService.login(request).enqueue(new Callback<CustomerLoginResponse>() {
            @Override
            public void onResponse(Call<CustomerLoginResponse> call, Response<CustomerLoginResponse> response) {
                Log.d(TAG, "Login response - isSuccessful: " + response.isSuccessful() + ", Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    CustomerLoginResponse res = response.body();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("UPId", res.getUPId());
                    if (rememberMe.isChecked()) {
                        editor.putString("jwt_token", res.getToken());
                    }
                    editor.apply();
                    Log.d(TAG, "Login successful - Role: " + res.getRole());
                    Toast.makeText(LoginActivity.this, "Welcome, " + res.getRole(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        ResponseBody errorBody = response.errorBody();
                        if (errorBody != null) {
                            String errorContent = errorBody.string();
                            Log.d(TAG, "Login error - Response: " + errorContent);
                            Gson gson = new Gson();
                            ErrorMessageResponse error = gson.fromJson(errorContent, ErrorMessageResponse.class);
                            Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "Login error - No error body");
                            Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Login error - IOException: " + e.getMessage());
                        Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<CustomerLoginResponse> call, Throwable t) {
                Log.e(TAG, "Login failure - Error: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSignUp() {
        // Lấy giá trị từ form đăng ký
        EditText emailEdit1 = findViewById(R.id.email_edit1);
        EditText passwordEdit1 = findViewById(R.id.password_edit1);
        String email = emailEdit1.getText().toString().trim();
        String password = passwordEdit1.getText().toString().trim();
        String confirm = confirmPasswordEdit.getText().toString().trim();

        Log.d(TAG, "Sign up attempt - Email: " + email + ", Password: " + password + ", Confirm: " + confirm);

        // Kiểm tra tất cả các trường
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
            Log.d(TAG, "Sign up failed - Empty fields detected: Email=" + email + ", Password=" + password + ", Confirm=" + confirm);
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Log.d(TAG, "Sign up failed - Passwords do not match");
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        SimplifiedSignUpRequest request = new SimplifiedSignUpRequest(email, password);
        AuthHomeService apiService = ApiClient.getInstance().create(AuthHomeService.class);
        apiService.simplifiedSignup(request).enqueue(new Callback<CustomerLoginResponse>() {
            @Override
            public void onResponse(Call<CustomerLoginResponse> call, Response<CustomerLoginResponse> response) {
                Log.d(TAG, "Sign up response - isSuccessful: " + response.isSuccessful() + ", Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    CustomerLoginResponse res = response.body();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("jwt_token", res.getToken());
                    editor.putInt("UPId", res.getUPId());
                    editor.apply();
                    Log.d(TAG, "Sign up successful - Role: " + res.getRole());
                    Toast.makeText(LoginActivity.this, "Sign up successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        ResponseBody errorBody = response.errorBody();
                        if (errorBody != null) {
                            String errorContent = errorBody.string();
                            Log.d(TAG, "Sign up error - Response: " + errorContent);
                            Gson gson = new Gson();
                            ErrorMessageResponse error = gson.fromJson(errorContent, ErrorMessageResponse.class);
                            Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "Sign up error - No error body");
                            Toast.makeText(LoginActivity.this, "Sign up failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Sign up error - IOException: " + e.getMessage());
                        Toast.makeText(LoginActivity.this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<CustomerLoginResponse> call, Throwable t) {
                Log.e(TAG, "Sign up failure - Error: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}