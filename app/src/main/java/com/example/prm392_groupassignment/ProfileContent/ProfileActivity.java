package com.example.prm392_groupassignment.ProfileContent; // Ensure this matches your package

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.cardview.widget.CardView;
import org.json.JSONArray; // Import JSONArray
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

import com.example.prm392_groupassignment.R;
import com.google.android.flexbox.FlexboxLayout;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream; // Import OutputStream
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.prm392_groupassignment.ProfileContent.AllergiesDialogFragment;

public class ProfileActivity extends AppCompatActivity
        implements AllergiesDialogFragment.OnMainCategorySelectionsUpdatedListener {

    private TextView editDiscardButton;
    private ImageView cameraIcon;
    private TextView fullNameLabel;
    private EditText fullNameEditText;
    private TextView emailTextView;
    private EditText infoAgeEditText;
    private EditText infoGenderEditText;
    private TextView infoEmailTextView;
    private EditText infoEmailEditText;

    private TextView allergiesCountTextView;
    private TextView conditionsCountTextView;
    private FlexboxLayout allergiesFlexboxLayout;
    private FlexboxLayout healthConditionsFlexboxLayout;
    private Button saveButton;

    private CircleImageView profileImageView;

    private boolean isEditMode = false;

    // Original data to revert on discard
    private String originalFullName;
    private String originalAge;
    private String originalGender;
    private String originalEmail;
    private ArrayList<String> originalAllergies;
    private ArrayList<String> originalHealthConditions;

    // Current data being edited (deep copy for discard functionality)
    private String currentFullName;
    private String currentAge;
    private String currentGender;
    private String currentEmail;
    private ArrayList<String> currentAllergies;
    private ArrayList<String> currentHealthConditions;

    // Map to store selected ingredients for each category from the dialogs
    private Map<String, Map<String, ArrayList<String>>> selectedItemsByMainCategory;

    // User ID for API calls (assuming it's constant for the logged-in user)
    private String userId = "3"; // Replace with actual user ID from login/session

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        int upId = prefs.getInt("UPId", 3); // returns -1 if not found

        this.userId = String.valueOf(upId);

        // Initialize UI elements
        editDiscardButton = findViewById(R.id.editDiscardButton);
        cameraIcon = findViewById(R.id.cameraIcon);
        fullNameLabel = findViewById(R.id.fullNameLabel);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailTextView = findViewById(R.id.email);
        infoAgeEditText = findViewById(R.id.infoAgeEditText);
        infoGenderEditText = findViewById(R.id.infoGenderEditText);
        infoEmailTextView = findViewById(R.id.infoEmailTextView);
        infoEmailEditText = findViewById(R.id.infoEmailEditText);

        allergiesCountTextView = findViewById(R.id.allergiesCount);
        conditionsCountTextView = findViewById(R.id.conditionsCount);
        allergiesFlexboxLayout = findViewById(R.id.allergiesFlexboxLayout);
        healthConditionsFlexboxLayout = findViewById(R.id.healthConditionsFlexboxLayout);
        saveButton = findViewById(R.id.saveButton);

        profileImageView = findViewById(R.id.profileImage);

        // Initialize the map for storing selections from dialogs
        selectedItemsByMainCategory = new HashMap<>();
        selectedItemsByMainCategory.put("Allergies", new HashMap<>());
        selectedItemsByMainCategory.put("Health Conditions", new HashMap<>());


        // Find the CardViews for dialogs
        CardView allergiesCard = findViewById(R.id.allergiesCard);
        CardView healthConditionsCard = findViewById(R.id.healthConditionsCard);

        // Set click listeners for the dialogs
        allergiesCard.setOnClickListener(v -> {
            if (isEditMode) {
                AllergiesDialogFragment allergiesDialog = AllergiesDialogFragment.newInstance(
                        "Allergies", selectedItemsByMainCategory.get("Allergies"));
                allergiesDialog.setOnMainCategorySelectionsUpdatedListener(this);
                allergiesDialog.show(getSupportFragmentManager(), "AllergiesDialog");
            } else {
                Toast.makeText(this, "Enter edit mode to modify allergies.", Toast.LENGTH_SHORT).show();
            }
        });

        healthConditionsCard.setOnClickListener(v -> {
            if (isEditMode) {
                AllergiesDialogFragment healthConditionsDialog = AllergiesDialogFragment.newInstance(
                        "Health Conditions", selectedItemsByMainCategory.get("Health Conditions"));
                healthConditionsDialog.setOnMainCategorySelectionsUpdatedListener(this);
                healthConditionsDialog.show(getSupportFragmentManager(), "HealthConditionsDialog");
            } else {
                Toast.makeText(this, "Enter edit mode to modify health conditions.", Toast.LENGTH_SHORT).show();
            }
        });

        // THIS IS THE CRUCIAL PART: Ensure the OnClickListener is correctly set for the editDiscardButton
        editDiscardButton.setOnClickListener(v -> toggleEditMode());

        // Set up Save button listener
        saveButton.setOnClickListener(v -> saveChanges());

        // Initial fetch of user profile data
        fetchUserProfile();
    }

    private void fetchUserProfile() {
        // Use the userId for the GET request
        String apiUrl = "https://appchao.azurewebsites.net/api/UserProfile/userProfile/" + userId;
        makeApiCall(apiUrl, "GET", null); // No request body for GET
    }

    // Generic API call method for both GET and PUT
    private void makeApiCall(String apiUrl, String requestMethod, String requestBody) {
        executorService.execute(() -> {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            OutputStream os = null;
            try {
                URL url = new URL(apiUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod(requestMethod);

                if (requestMethod.equals("PUT") || requestMethod.equals("POST")) {
                    urlConnection.setDoOutput(true); // Allow output for PUT/POST
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json"); // Optional: specify accepted response type

                    if (requestBody != null) {
                        os = urlConnection.getOutputStream();
                        os.write(requestBody.getBytes("UTF-8"));
                        os.flush();
                    }
                }

                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                final String responseMessage;
                if (responseCode >= 200 && responseCode < 300) { // Success range for HTTP
                    StringBuilder buffer = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }
                    responseMessage = buffer.toString();

                    if (requestMethod.equals("GET")) {
                        // Handle GET response (update UI)
                        runOnUiThread(() -> {
                            try {
                                JSONObject userProfile = new JSONObject(responseMessage);

                                String username = userProfile.optString("username", "N/A");
                                emailTextView.setText(username);
                                originalFullName = userProfile.optString("fullName", "N/A");
                                originalAge = userProfile.optString("age", "N/A").equals("null") ? "N/A" : userProfile.optString("age", "N/A");
                                originalGender = userProfile.optString("gender", "N/A");
                                originalEmail = userProfile.optString("email", "N/A");
                                originalAllergies = new ArrayList<>();
                                if (userProfile.has("allergies")) {
                                    JSONArray allergiesArray = userProfile.optJSONArray("allergies");
                                    if (allergiesArray != null) {
                                        for (int i = 0; i < allergiesArray.length(); i++) {
                                            originalAllergies.add(allergiesArray.getString(i));
                                        }
                                    }
                                }

                                originalHealthConditions = new ArrayList<>();
                                if (userProfile.has("healthConditions")) {
                                    JSONArray conditionsArray = userProfile.optJSONArray("healthConditions");
                                    if (conditionsArray != null) {
                                        for (int i = 0; i < conditionsArray.length(); i++) {
                                            JSONObject hcObj = conditionsArray.getJSONObject(i);
                                            String conditionName = hcObj.optString("condition", "N/A");
                                            originalHealthConditions.add(conditionName);
                                        }
                                    }
                                }

                                currentFullName = originalFullName;
                                currentAge = originalAge;
                                currentGender = originalGender;
                                currentEmail = originalEmail;
                                currentAllergies = new ArrayList<>(originalAllergies);
                                currentHealthConditions = new ArrayList<>(originalHealthConditions);

                                updateUI(false); // Display in view mode initially

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(ProfileActivity.this, "Error parsing API response.", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (requestMethod.equals("PUT") || requestMethod.equals("POST")) {
                        // Handle PUT/POST response (e.g., success message, then switch mode)
                        runOnUiThread(() -> {
                            Toast.makeText(ProfileActivity.this, "Changes saved!", Toast.LENGTH_SHORT).show();
                            // After successful save, update original data and switch to view mode
                            originalFullName = currentFullName;
                            originalAge = currentAge;
                            originalGender = currentGender;
                            originalEmail = currentEmail;
                            originalAllergies = new ArrayList<>(currentAllergies);
                            originalHealthConditions = new ArrayList<>(currentHealthConditions);

                            // Clear the temporary map used for dialog selections after saving
                            selectedItemsByMainCategory.get("Allergies").clear();
                            selectedItemsByMainCategory.get("Health Conditions").clear();

                            toggleEditMode(); // Switch back to view mode
                        });
                    }

                } else {
                    // Read error stream if response code indicates an error
                    StringBuilder errorBuffer = new StringBuilder();
                    if (urlConnection.getErrorStream() != null) {
                        reader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            errorBuffer.append(line).append("\n");
                        }
                    }
                    final String errorResponse = errorBuffer.toString().isEmpty() ? "Unknown error" : errorBuffer.toString();

                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "API Error (" + responseCode + "): " + errorResponse, Toast.LENGTH_LONG).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        updateUI(isEditMode);

        if (isEditMode) {
            editDiscardButton.setText("Discard");
            saveButton.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Edit mode enabled. Discard to cancel changes.", Toast.LENGTH_SHORT).show();
        } else {
            editDiscardButton.setText("Edit");
            saveButton.setVisibility(View.GONE);
            // Revert changes on Discard
            currentFullName = originalFullName;
            currentAge = originalAge;
            currentGender = originalGender;
            currentEmail = originalEmail;
            currentAllergies = new ArrayList<>(originalAllergies);
            currentHealthConditions = new ArrayList<>(originalHealthConditions);

            // Clear any temporary selections made in the dialogs' maps if discarded
            selectedItemsByMainCategory.get("Allergies").clear();
            selectedItemsByMainCategory.get("Health Conditions").clear();

            // Re-populate the FlexboxLayouts with original data
            populateChips(allergiesFlexboxLayout, currentAllergies, "Allergies");
            populateChips(healthConditionsFlexboxLayout, currentHealthConditions, "Health Conditions");

            Toast.makeText(this, "Changes discarded.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI(boolean inEditMode) {
        cameraIcon.setVisibility(inEditMode ? View.VISIBLE : View.GONE);
        fullNameLabel.setVisibility(inEditMode ? View.VISIBLE : View.GONE);
        fullNameEditText.setEnabled(inEditMode);
        fullNameEditText.setText(currentFullName);
        infoAgeEditText.setEnabled(inEditMode);
        infoGenderEditText.setEnabled(inEditMode);
        infoEmailEditText.setEnabled(inEditMode);
        infoAgeEditText.setText(currentAge);
        infoGenderEditText.setText(currentGender);
        if (currentEmail == null || currentEmail.equals("null")) {
            currentEmail = "N/A";
        }
        infoEmailEditText.setText(currentEmail);

        allergiesCountTextView.setText(String.valueOf(currentAllergies.size()));
        conditionsCountTextView.setText(String.valueOf(currentHealthConditions.size()));

        populateChips(allergiesFlexboxLayout, currentAllergies, "Allergies");
        populateChips(healthConditionsFlexboxLayout, currentHealthConditions, "Health Conditions");

//        emailTextView.setText(emailTextView.getText()); // This is often static, but refresh if needed
//        infoEmailTextView.setText(emailTextView.getText()); // This is often static, but refresh if needed
    }

    private void populateChips(FlexboxLayout flexboxLayout, List<String> items, String mainCategory) {
        flexboxLayout.removeAllViews();

        if (items.isEmpty() && !isEditMode) {
            TextView noItemsText = new TextView(this);
            noItemsText.setText("N/A");
            noItemsText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            noItemsText.setTextSize(14f);
            noItemsText.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            flexboxLayout.addView(noItemsText);
            return;
        }

        for (String item : items) {
            View chipView = LayoutInflater.from(this).inflate(R.layout.item_chip, flexboxLayout, false);
            TextView chipText = chipView.findViewById(R.id.chip_text);
            ImageView chipClose = chipView.findViewById(R.id.chip_close);

            chipText.setText(item);

            if (isEditMode) {
                chipClose.setVisibility(View.VISIBLE);
                chipClose.setOnClickListener(v -> {
                    if (mainCategory.equals("Allergies")) {
                        currentAllergies.remove(item);
                    } else if (mainCategory.equals("Health Conditions")) {
                        currentHealthConditions.remove(item);
                    }
                    populateChips(flexboxLayout, items, mainCategory);
                    allergiesCountTextView.setText(String.valueOf(currentAllergies.size()));
                    conditionsCountTextView.setText(String.valueOf(currentHealthConditions.size()));
                });
            } else {
                chipClose.setVisibility(View.GONE);
            }
            flexboxLayout.addView(chipView);
        }
    }

    private void saveChanges() {
        // 1. Collect data from EditText fields
        currentFullName = fullNameEditText.getText().toString();
        currentAge = infoAgeEditText.getText().toString();
        currentGender = infoGenderEditText.getText().toString();
        currentEmail = infoEmailEditText.getText().toString();

        // 2. Construct the JSON request body
        JSONObject requestBodyJson = new JSONObject();
        try {
            // Assuming your API expects upId or userId to identify the profile
            requestBodyJson.put("upId", Integer.parseInt(userId)); // Assuming userId is an integer for upId
            requestBodyJson.put("fullName", currentFullName);
            requestBodyJson.put("age", currentAge.equals("N/A") ? JSONObject.NULL : Integer.parseInt(currentAge)); // Handle N/A for age
            requestBodyJson.put("gender", currentGender);
            requestBodyJson.put("email", currentEmail);
            // Convert ArrayLists to JSONArrays
            JSONArray allergiesJsonArray = new JSONArray(currentAllergies);
            requestBodyJson.put("allergies", allergiesJsonArray);

            JSONArray conditionsArray = new JSONArray();
            for (String condition : currentHealthConditions) {
                JSONObject obj = new JSONObject();
                obj.put("condition", condition);
                obj.put("status", JSONObject.NULL);
                conditionsArray.put(obj);
            }
            requestBodyJson.put("healthConditions", conditionsArray);

//            requestBodyJson.put("email", emailTextView.getText().toString()); // Assuming email is static
            // requestBodyJson.put("username", "username_from_original_fetch"); // Add if needed
            // requestBodyJson.put("userPicture", "userPicture_from_original_fetch"); // Add if needed

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating JSON for save.", Toast.LENGTH_LONG).show();
            return;
        }

        String requestBodyString = requestBodyJson.toString();
        Toast.makeText(this, "Sending changes to API...", Toast.LENGTH_SHORT).show();

        // 3. Make the API call (assuming a PUT request to update an existing profile)
        String saveApiUrl = "https://appchao.azurewebsites.net/api/UserProfile/" + userId;
        makeApiCall(saveApiUrl, "PUT", requestBodyString);
    }


    @Override
    public void onMainCategorySelectionsUpdated(String mainCategoryTitle, Map<String, ArrayList<String>> updatedSelections) {
        selectedItemsByMainCategory.put(mainCategoryTitle, updatedSelections);

        ArrayList<String> consolidatedList = new ArrayList<>();
        for (ArrayList<String> list : updatedSelections.values()) {
            consolidatedList.addAll(list);
        }

        if (mainCategoryTitle.equals("Allergies")) {
            currentAllergies = consolidatedList;
        } else if (mainCategoryTitle.equals("Health Conditions")) {
            currentHealthConditions = consolidatedList;
        }

        updateUI(isEditMode);
        Toast.makeText(this, "Updated selections for " + mainCategoryTitle, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
