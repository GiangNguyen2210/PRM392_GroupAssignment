package com.example.prm392_groupassignment.ProfileContent;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.prm392_groupassignment.R;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.view.WindowManager; // Added import

public class AllergiesDialogFragment extends DialogFragment implements IngredientsDialogFragment.OnIngredientsSelectedListener {

    private static final String ARG_DIALOG_TITLE = "dialogTitle";
    private static final String ARG_CATEGORY_MAP = "categoryMap";

    private TextView dialogTitleTextView;
    private ChipGroup selectedIngredientsChipGroup;
    private MaterialButton confirmButton;
    private Map<String, ArrayList<String>> currentMainCategorySelections;
    private FlexboxLayout flexboxLayout;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public interface OnMainCategorySelectionsUpdatedListener {
        void onMainCategorySelectionsUpdated(String mainCategoryTitle, Map<String, ArrayList<String>> updatedSelections);
    }

    private OnMainCategorySelectionsUpdatedListener mainCategoryListener;

    public AllergiesDialogFragment() {
        // Required empty public constructor
    }

    public static AllergiesDialogFragment newInstance(String title, Map<String, ArrayList<String>> categorySelections) {
        AllergiesDialogFragment fragment = new AllergiesDialogFragment();
        Bundle args = new Bundle(2);
        args.putString(ARG_DIALOG_TITLE, title);
        args.putSerializable(ARG_CATEGORY_MAP, (Serializable) categorySelections);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnMainCategorySelectionsUpdatedListener(OnMainCategorySelectionsUpdatedListener listener) {
        this.mainCategoryListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.FullScreenDialogStyle);

        if (getArguments() != null) {
            currentMainCategorySelections = (Map<String, ArrayList<String>>) getArguments().getSerializable(ARG_CATEGORY_MAP);
        }
        if (currentMainCategorySelections == null) {
            currentMainCategorySelections = new HashMap<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_allergies_dialog, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(getDialog().getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            getDialog().getWindow().setAttributes(layoutParams);
        }

        dialogTitleTextView = view.findViewById(R.id.dialogTitle);
        String dialogTitle = "Dialog";
        if (getArguments() != null) {
            dialogTitle = getArguments().getString(ARG_DIALOG_TITLE, "Dialog");
            dialogTitleTextView.setText(dialogTitle);
        }

        ImageView closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());

        selectedIngredientsChipGroup =
                view.findViewById(R.id.selectedIngredientsChipGroup);

        confirmButton = view.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(v -> {
            if (mainCategoryListener != null) {
                mainCategoryListener.onMainCategorySelectionsUpdated(
                        dialogTitleTextView.getText().toString(),
                        currentMainCategorySelections
                );
            }
            dismiss();
        });
        updateConfirmButtonState();

        flexboxLayout = view.findViewById(R.id.flexboxLayout);
        flexboxLayout.removeAllViews();

        String apiUrl;
        if (dialogTitle.equals("Allergies")) {
            apiUrl = "https://appchao.azurewebsites.net/api/Ingredients/ingredient-types?page=1&pageSize=20";
        } else if (dialogTitle.equals("Health Conditions")) {
            apiUrl = "https://appchao.azurewebsites.net/api/HealthCondition/health-condition-types?page=1&pageSize=20";
        } else {
            Toast.makeText(getContext(), "Unknown dialog type, cannot fetch data.", Toast.LENGTH_SHORT).show();
            return view;
        }

        makeApiCallForTypes(apiUrl, dialogTitle); // Renamed method for clarity

        return view;
    }

    private void makeApiCallForTypes(String apiUrl, final String dialogType) {
        executorService.execute(() -> {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(apiUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    StringBuilder buffer = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }
                    final String responseJson = buffer.toString();

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            try {
                                JSONObject jsonResponse = new JSONObject(responseJson);
                                JSONArray itemsArray = jsonResponse.optJSONArray("items");
                                // Changed to hold Type objects
                                List<TypeData> fetchedTypes = new ArrayList<>();

                                if (itemsArray != null) {
                                    for (int i = 0; i < itemsArray.length(); i++) {
                                        if (dialogType.equals("Allergies")) { // Xấu do đây
                                            JSONObject itemObject = itemsArray.optJSONObject(i);
                                            if (itemObject != null) {
                                                String typeName = itemObject.optString("typeName", "Unknown Type");
                                                int typeId = itemObject.optInt("ingredientTypeId", -1); // Get typeId
                                                fetchedTypes.add(new TypeData(typeName, typeId));
                                            }
                                        } else if (dialogType.equals("Health Conditions")) {
                                            String typeName = itemsArray.optString(i, "Unknown Type");
                                            fetchedTypes.add(new TypeData(typeName, -1)); // No typeId for health conditions
                                        }
                                    }
                                }
                                populateTypeButtons(fetchedTypes, dialogType); // Pass dialogType

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Error parsing " + dialogType + " data.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "API Error: " + responseCode, Toast.LENGTH_LONG).show());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
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
            }
        });
    }

    // Helper class to store type data (name and ID)
    private static class TypeData implements Serializable {
        String name;
        int id;

        TypeData(String name, int id) {
            this.name = name;
            this.id = id;
        }
    }

    private void populateTypeButtons(List<TypeData> types, String dialogType) {
        flexboxLayout.removeAllViews();

        for (TypeData typeData : types) {
            Button btn = new Button(getContext());
            btn.setText(typeData.name);
            btn.setBackgroundResource(R.drawable.bg_category_button);
            btn.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            btn.setTextSize(14f);
            btn.setAllCaps(false);

            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
            btn.setLayoutParams(lp);
            btn.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
            btn.setMinimumHeight(0);
            btn.setStateListAnimator(null);

            btn.setOnClickListener(v -> {
                ArrayList<String> currentSelectedForType = currentMainCategorySelections.get(typeData.name);
                if (currentSelectedForType == null) {
                    currentSelectedForType = new ArrayList<>();
                    currentMainCategorySelections.put(typeData.name, currentSelectedForType);
                }

                // Pass all necessary info to IngredientsDialogFragment
                IngredientsDialogFragment ingredientsDialog = IngredientsDialogFragment.newInstance(
                        dialogType, // "Allergies" or "Health Conditions"
                        typeData.name, // Type name (e.g., "Meat & Seafood", "Others...")
                        typeData.id, // Type ID (e.g., 1, -1 for health conditions)
                        currentSelectedForType
                );
                ingredientsDialog.setOnIngredientsSelectedListener(this);
                ingredientsDialog.show(getParentFragmentManager(), "IngredientsDialog");
            });
            flexboxLayout.addView(btn);
        }
    }

    private void updateConfirmButtonState() {
        boolean hasAny = false;
        for (List<String> selections : currentMainCategorySelections.values()) {
            if (selections != null && !selections.isEmpty()) {
                hasAny = true;
                break;
            }
        }
        confirmButton.setEnabled(hasAny);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onIngredientsSelected(String ingredientType, ArrayList<String> selectedIngredientsList) {

        // store the new selections
        currentMainCategorySelections.put(ingredientType, selectedIngredientsList);

        // rebuild the chips
        selectedIngredientsChipGroup.removeAllViews();
        for (String ing : selectedIngredientsList) {
            View chipView = getLayoutInflater()
                    .inflate(R.layout.item_chip, selectedIngredientsChipGroup, false);
            TextView tv = chipView.findViewById(R.id.chip_text);
            ImageView close = chipView.findViewById(R.id.chip_close);
            tv.setText(ing);
            // Optional: let user remove a chip directly
            close.setOnClickListener(c -> {
                selectedIngredientsChipGroup.removeView(chipView);
                selectedIngredientsList.remove(ing);
                updateConfirmButtonState();
            });
            selectedIngredientsChipGroup.addView(chipView);
        }

        updateConfirmButtonState();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ARG_CATEGORY_MAP, (Serializable) currentMainCategorySelections);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executorService.shutdownNow();
    }
}