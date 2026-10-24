
package com.example.smartpantrymanagement;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class PreviousRecipes extends AppCompatActivity {

    private DBHelper dbHelper;
    private long userId;
    private LinearLayout recipeHistoryContainer;
    private TextView emptyRecipesMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_previous_recipes);

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
        android.widget.Toast.makeText(
                this,
                "Current UserID: " + userId,
                android.widget.Toast.LENGTH_LONG
        ).show();

        recipeHistoryContainer =
                findViewById(R.id.recipeHistoryContainer);
        emptyRecipesMessage =
                findViewById(R.id.emptyRecipesMessage);

        loadPreviousRecipes();
    }

    private void loadPreviousRecipes() {
        if (userId == -1) {
            emptyRecipesMessage.setText("Please log in again.");
            return;
        }

        Cursor cursor = dbHelper.getRecipeHistory(userId);
        android.widget.Toast.makeText(
                this,
                "Saved recipes: " + cursor.getCount(),
                android.widget.Toast.LENGTH_LONG
        ).show();

        try {
            if (cursor.getCount() == 0) {
                emptyRecipesMessage.setVisibility(View.VISIBLE);
                return;
            }

            emptyRecipesMessage.setVisibility(View.GONE);

            while (cursor.moveToNext()) {
                String recipeName = cursor.getString(1);
                String ingredients = cursor.getString(2);

                addRecipeEntry(recipeName, ingredients);
            }
        } finally {
            cursor.close();
        }
    }

    private void addRecipeEntry(String recipeName, String ingredients) {
        LinearLayout recipeEntry = new LinearLayout(this);
        recipeEntry.setOrientation(LinearLayout.VERTICAL);
        recipeEntry.setPadding(20, 16, 20, 16);

        LinearLayout.LayoutParams entryParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        entryParams.bottomMargin = 24;
        recipeEntry.setLayoutParams(entryParams);

        TextView nameText = new TextView(this);
        nameText.setText(recipeName);
        nameText.setTextSize(20);
        nameText.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView ingredientsText = new TextView(this);
        ingredientsText.setText("Ingredients: " + ingredients);
        ingredientsText.setTextSize(16);
        ingredientsText.setPadding(0, 8, 0, 12);

        Button viewButton = new Button(this);
        viewButton.setText("View Recipe");
        viewButton.setGravity(Gravity.CENTER);

        viewButton.setOnClickListener(view -> {
            ArrayList<String> selectedIngredients =
                    new ArrayList<>();

            for (String ingredient : ingredients.split(",")) {
                selectedIngredients.add(ingredient.trim());
            }

            Intent intent = new Intent(
                    PreviousRecipes.this, YourRecipe.class);

            intent.putStringArrayListExtra(
                    "selectedIngredients", selectedIngredients);
            intent.putExtra("viewingHistory", true);
            intent.putExtra("savedRecipeName", recipeName);

            startActivity(intent);
        });

        recipeEntry.addView(nameText);
        recipeEntry.addView(ingredientsText);
        recipeEntry.addView(viewButton);

        recipeHistoryContainer.addView(recipeEntry);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (recipeHistoryContainer != null) {
            recipeHistoryContainer.removeViews(
                    1, recipeHistoryContainer.getChildCount() - 1);
            loadPreviousRecipes();
        }
    }
}
