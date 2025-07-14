package com.example.prm392_groupassignment.ProfileContent;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat; // Import for ContextCompat

import com.example.prm392_groupassignment.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IngredientsDialogFragment extends DialogFragment {

    private static final String ARG_DIALOG_TYPE = "dialogType";
    private static final String ARG_INGREDIENT_TYPE_NAME = "ingredientTypeName";
    private static final String ARG_INGREDIENT_TYPE_ID = "ingredientTypeId";
    private static final String ARG_SELECTED_INGREDIENTS = "selectedIngredients";

    private String dialogType;
    private String ingredientTypeName;
    private int ingredientTypeId;
    private ArrayList<String> selectedIngredients;
    private IngredientsAdapter adapter;
    private TextView selectedIngredientsTextView;
    private RecyclerView ingredientsRecyclerView;
    private Button confirmButton;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public interface OnIngredientsSelectedListener {
        void onIngredientsSelected(String ingredientType, ArrayList<String> selectedIngredients);
    }

    private OnIngredientsSelectedListener listener;

    public IngredientsDialogFragment() {
        // Required empty public constructor
    }

    public static IngredientsDialogFragment newInstance(String dialogType, String typeName, int typeId, ArrayList<String> currentSelected) {
        IngredientsDialogFragment fragment = new IngredientsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DIALOG_TYPE, dialogType);
        args.putString(ARG_INGREDIENT_TYPE_NAME, typeName);
        args.putInt(ARG_INGREDIENT_TYPE_ID, typeId);
        args.putStringArrayList(ARG_SELECTED_INGREDIENTS, currentSelected);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.FullScreenDialogStyle);

        if (getArguments() != null) {
            dialogType = getArguments().getString(ARG_DIALOG_TYPE);
            ingredientTypeName = getArguments().getString(ARG_INGREDIENT_TYPE_NAME);
            ingredientTypeId = getArguments().getInt(ARG_INGREDIENT_TYPE_ID, -1);
            selectedIngredients = getArguments().getStringArrayList(ARG_SELECTED_INGREDIENTS);
            if (selectedIngredients == null) {
                selectedIngredients = new ArrayList<>();
            }
        } else {
            dialogType = "Unknown";
            ingredientTypeName = "Items";
            ingredientTypeId = -1;
            selectedIngredients = new ArrayList<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ingredients_dialog, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(getDialog().getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            getDialog().getWindow().setAttributes(layoutParams);
        }

        TextView dialogTitle = view.findViewById(R.id.dialogTitle);
        dialogTitle.setText(ingredientTypeName);

        ImageView closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());

        selectedIngredientsTextView = view.findViewById(R.id.selectedIngredientsTextView);
        updateSelectedIngredientsText(); // Initial update for selected items text

        TextView itemsInTypeTitle = view.findViewById(R.id.ingredientsInTypeTitle);
        if (dialogType.equals("Allergies")) {
            itemsInTypeTitle.setText("Ingredients in " + ingredientTypeName);
        } else {
            itemsInTypeTitle.setText("Conditions in " + ingredientTypeName);
        }

        ingredientsRecyclerView = view.findViewById(R.id.ingredientsRecyclerView);
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String apiUrl = buildApiUrl();
        if (apiUrl != null) {
            makeApiCallForItems(apiUrl);
        } else {
            Toast.makeText(getContext(), "Error: Could not build API URL.", Toast.LENGTH_LONG).show();
        }

        confirmButton = view.findViewById(R.id.confirmButton); // Initialize confirmButton
        confirmButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIngredientsSelected(ingredientTypeName, selectedIngredients);
            }
            dismiss();
        });
        updateConfirmButtonState(); // Set initial button state

        return view;
    }

    private String buildApiUrl() {
        try {
            if (dialogType.equals("Allergies")) {
                if (ingredientTypeId != -1) {
                    return "https://appchao.azurewebsites.net/api/Ingredients/ingredients?typeId=" + ingredientTypeId + "&page=1&pageSize=20";
                } else {
                    Toast.makeText(getContext(), "Error: Ingredient Type ID not found.", Toast.LENGTH_LONG).show();
                    return null;
                }
            } else if (dialogType.equals("Health Conditions")) {
                String encodedTypeName = URLEncoder.encode(ingredientTypeName, "UTF-8");
                return "https://appchao.azurewebsites.net/api/HealthCondition/health-conditions?type=" + encodedTypeName + "&page=1&pageSize=20";
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error encoding URL parameter.", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    private void makeApiCallForItems(String apiUrl) {
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
                                List<String> fetchedItems = new ArrayList<>();

                                if (itemsArray != null) {
                                    for (int i = 0; i < itemsArray.length(); i++) {
                                        JSONObject itemObject = itemsArray.optJSONObject(i);
                                        if (itemObject != null) {
                                            if (dialogType.equals("Allergies")) {
                                                fetchedItems.add(itemObject.optString("ingredientName", "Unknown Ingredient"));
                                            } else if (dialogType.equals("Health Conditions")) {
                                                fetchedItems.add(itemObject.optString("healthConditionName", "Unknown Condition"));
                                            }
                                        }
                                    }
                                }
                                adapter = new IngredientsAdapter(fetchedItems, selectedIngredients);
                                ingredientsRecyclerView.setAdapter(adapter);
                                updateConfirmButtonState(); // Update button state after data is loaded

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Error parsing items data.", Toast.LENGTH_LONG).show();
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

    public void setOnIngredientsSelectedListener(OnIngredientsSelectedListener listener) {
        this.listener = listener;
    }

    private void updateSelectedIngredientsText() {
        if (selectedIngredients.isEmpty()) {
            selectedIngredientsTextView.setText("No selections made");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < selectedIngredients.size(); i++) {
                sb.append(selectedIngredients.get(i));
                if (i < selectedIngredients.size() - 1) {
                    sb.append(", ");
                }
            }
            selectedIngredientsTextView.setText(sb.toString());
        }
        updateConfirmButtonState();
    }


    private void updateConfirmButtonState() {

        if (confirmButton != null) {
            boolean hasSelection = selectedIngredients != null && !selectedIngredients.isEmpty();
            confirmButton.setEnabled(hasSelection);
//            confirmButton.setEnabled(selectedIngredients.size() > 0);
        }
    }

    private class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder> {

        private List<String> items;
        private ArrayList<String> currentSelected;

        public IngredientsAdapter(List<String> items, ArrayList<String> currentSelected) {
            this.items = items;
            this.currentSelected = currentSelected;
        }

        @NonNull
        @Override
        public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient, parent, false);
            return new IngredientViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
            String item = items.get(position);
            holder.ingredientNameTextView.setText(item);
            holder.ingredientCheckBox.setChecked(currentSelected.contains(item));

            holder.ingredientCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!currentSelected.contains(item)) {
                        currentSelected.add(item);
                    }
                } else {
                    currentSelected.remove(item);
                }
                updateSelectedIngredientsText();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class IngredientViewHolder extends RecyclerView.ViewHolder {
            CheckBox ingredientCheckBox;
            TextView ingredientNameTextView;

            public IngredientViewHolder(@NonNull View itemView) {
                super(itemView);
                ingredientCheckBox = itemView.findViewById(R.id.ingredientCheckBox);
                ingredientNameTextView = itemView.findViewById(R.id.ingredientNameTextView);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executorService.shutdownNow();
    }


}
