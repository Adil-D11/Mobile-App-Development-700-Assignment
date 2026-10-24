
package com.example.smartpantrymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class YourRecipe extends AppCompatActivity {

    private static class Recipe {
        String name;
        String time;
        List<String> ingredients;

        Recipe(String name, String time, String... ingredients) {
            this.name = name;
            this.time = time;
            this.ingredients = Arrays.asList(ingredients);
        }
    }

    private final List<Recipe> recipes = Arrays.asList(
            new Recipe("Tomato Cheese Pasta", "20 minutes",
                    "Tomato", "Cheese", "Pasta"),

            new Recipe("Egg Fried Rice", "15 minutes",
                    "Eggs", "Rice", "Onion"),

            new Recipe("Cheesy Mashed Potatoes", "25 minutes",
                    "Potato", "Milk", "Cheese"),

            new Recipe("Tomato Omelette", "10 minutes",
                    "Tomato", "Eggs")
    );

    private DBHelper dbHelper;
    private long userId;

    private void initRecipeGen() {
        Button doneButton = findViewById(R.id.DoneRecipebtn);

        doneButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, GenerateRecipe.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void displayMatchingRecipe() {
        TextView recipeName = findViewById(R.id.recipeName);
        TextView recipeTime = findViewById(R.id.recipeTime);
        LinearLayout ingredientsContainer =
                findViewById(R.id.IngredientsContainer);

        ArrayList<String> availableIngredients =
                getIntent().getStringArrayListExtra("selectedIngredients");

        if (availableIngredients == null) {
            availableIngredients = new ArrayList<>();
        }

        Set<String> available = new HashSet<>();

        for (String ingredient : availableIngredients) {
            available.add(ingredient.trim().toLowerCase(Locale.ROOT));
        }

        Recipe matchedRecipe = null;

        for (Recipe recipe : recipes) {
            boolean hasAllIngredients = true;

            for (String ingredient : recipe.ingredients) {
                if (!available.contains(
                        ingredient.toLowerCase(Locale.ROOT))) {
                    hasAllIngredients = false;
                    break;
                }
            }

            if (hasAllIngredients) {
                matchedRecipe = recipe;
                break;
            }
        }

        ingredientsContainer.removeAllViews();

        if (matchedRecipe == null) {
            recipeName.setText("No matching recipes found");
            recipeTime.setText(
                    "Try adding or selecting more ingredients."
            );
            return;
        }

        recipeName.setText(matchedRecipe.name);
        recipeTime.setText("Preparation time: " + matchedRecipe.time);

        if (userId != -1) {
            long result = dbHelper.saveRecipeHistory(
                    userId,
                    matchedRecipe.name,
                    android.text.TextUtils.join(", ", matchedRecipe.ingredients)
            );

            if (result == -1) {
                android.widget.Toast.makeText(
                        this,
                        "Unable to save recipe history",
                        android.widget.Toast.LENGTH_SHORT
                ).show();
            }
        }

        for (String ingredient : matchedRecipe.ingredients) {
            TextView ingredientText = new TextView(this);
            ingredientText.setText("• " + ingredient);
            ingredientText.setTextSize(18);
            ingredientText.setPadding(0, 6, 0, 6);

            ingredientsContainer.addView(ingredientText);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_your_recipe);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom);
                    return insets;
                });

        dbHelper = new DBHelper(this);

        android.content.SharedPreferences preferences =
                getSharedPreferences("SmartPantrySession", MODE_PRIVATE);

        userId = preferences.getLong("UserID", -1);

        initRecipeGen();
        displayMatchingRecipe();
    }
}
