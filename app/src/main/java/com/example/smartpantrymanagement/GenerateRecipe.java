package com.example.smartpantrymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.Toast;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class GenerateRecipe extends AppCompatActivity {

    private ArrayList<String> selectedIngredients = new ArrayList<>();
    private LinearLayout selectedIngredientContainer;
    private CheckBox[] ingredientCheckboxes;
    private DBHelper dbHelper;
    private long userId;

    private void OpenActivity(Class activity){
        Intent intent = new Intent(this,activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void initRecipe(){
        Button RecipeGenBtn = findViewById(R.id.GenerateRecipebtn);
        Button AddIngredientBtn = findViewById(R.id.btnAddIngredient);

        EditText Searchbar = findViewById(R.id.editSearchbar);

        selectedIngredientContainer = findViewById(R.id.selectedIngredientsContainer);

        AddIngredientBtn.setOnClickListener(view ->{
            String ingredient = Searchbar.getText().toString().trim();

            if(ingredient.isEmpty()){
                Toast.makeText(this, "Please Enter an ingredient", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean alreadyAdded = false;

            for (String selected : selectedIngredients){
                if (selected.equalsIgnoreCase(ingredient)){
                    alreadyAdded = true;
                    break;
                }
            }
            if(alreadyAdded){
                Toast.makeText(this, "Ingredient already added", Toast.LENGTH_SHORT).show();
                return;
            }

            long result = dbHelper.addIngredient(userId, ingredient);

            if (result == -1) {
                Toast.makeText(this,
                        "Unable to add ingredient",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            selectedIngredients.add(ingredient);
            displaySelectedIngredients();

            Searchbar.setText("");
        });


        RecipeGenBtn.setOnClickListener(view->{
            if(selectedIngredients.isEmpty()){
                Toast.makeText(this, "Please add or select at least one ingredient", Toast.LENGTH_SHORT).show();
                return;
            }

            OpenActivity(YourRecipe.class);
        });
    }

    private void initIngredientCheckboxes() {
        ingredientCheckboxes = new CheckBox[]{
                findViewById(R.id.chkTomato),
                findViewById(R.id.chkOnion),
                findViewById(R.id.chkPotato),
                findViewById(R.id.chkEggs),
                findViewById(R.id.chkMilk),
                findViewById(R.id.chkCheese),
                findViewById(R.id.chkRice),
                findViewById(R.id.chkPasta)
        };

        for (CheckBox checkbox : ingredientCheckboxes) {
            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String ingredient = buttonView.getText().toString();

                if (isChecked) {
                    if (!selectedIngredients.contains(ingredient)) {
                        selectedIngredients.add(ingredient);
                    }
                } else {
                    selectedIngredients.removeIf(
                            item -> item.equalsIgnoreCase(ingredient)
                    );
                }

                displaySelectedIngredients();
            });
        }
    }



    private void displaySelectedIngredients() {
        selectedIngredientContainer.removeViews(
                1, selectedIngredientContainer.getChildCount() - 1
        );

        for (String ingredient : selectedIngredients) {
            // Create a row for each ingredient.
            LinearLayout ingredientRow = new LinearLayout(this);
            ingredientRow.setOrientation(LinearLayout.HORIZONTAL);
            ingredientRow.setPadding(8, 8, 8, 8);
            ingredientRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            // Display the ingredient name.
            TextView ingredientText = new TextView(this);
            ingredientText.setText("- " + ingredient);
            ingredientText.setTextSize(18);
            ingredientText.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    )
            );

            // Create a button to remove the ingredient.
            Button removeButton = new Button(this);
            removeButton.setText("Remove");


            removeButton.setOnClickListener(view -> {
                int deleted = dbHelper.deleteIngredient(userId, ingredient);

                // Remove it from the selected list even if it wasn't saved in the database.
                selectedIngredients.removeIf(
                        item -> item.equalsIgnoreCase(ingredient)
                );

                // Uncheck the matching checkbox, if there is one.
                for (CheckBox checkbox : ingredientCheckboxes) {
                    if (checkbox.getText().toString()
                            .equalsIgnoreCase(ingredient)) {
                        checkbox.setOnCheckedChangeListener(null);
                        checkbox.setChecked(false);

                        checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            String name = buttonView.getText().toString();

                            if (isChecked) {
                                if (!selectedIngredients.contains(name)) {
                                    selectedIngredients.add(name);
                                }
                            } else {
                                selectedIngredients.removeIf(
                                        item -> item.equalsIgnoreCase(name)
                                );
                            }

                            displaySelectedIngredients();
                        });

                        break;
                    }
                }

                displaySelectedIngredients();

                if (deleted > 0) {
                    Toast.makeText(this, ingredient + " removed",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, ingredient + " removed from selection",
                            Toast.LENGTH_SHORT).show();
                }
            });


            ingredientRow.addView(ingredientText);
            ingredientRow.addView(removeButton);

            selectedIngredientContainer.addView(ingredientRow);
        }
    }

    private void loadPantryIngredients() {
        selectedIngredients.clear();

        android.content.SharedPreferences preferences =
                getSharedPreferences("SmartPantrySession", MODE_PRIVATE);

        userId = preferences.getLong("UserID", -1);

        if (userId == -1) {
            Toast.makeText(this,
                    "Please log in again",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        android.database.Cursor cursor =
                dbHelper.getPantryIngredients(userId);

        try {
            while (cursor.moveToNext()) {
                selectedIngredients.add(cursor.getString(1));
            }
        } finally {
            cursor.close();
        }

        displaySelectedIngredients();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_generate_recipe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DBHelper(this);
        initRecipe();
        loadPantryIngredients();
        initIngredientCheckboxes();
    }
}