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

    private void displaySelectedIngredients(){
        selectedIngredientContainer.removeViews( 1, selectedIngredientContainer.getChildCount() - 1);

        for (String ingredient : selectedIngredients){
            TextView ingredientText = new TextView(this);
            ingredientText.setText("- " + ingredient);
            ingredientText.setTextSize(18);
            ingredientText.setPadding(8, 8, 8, 8);

            selectedIngredientContainer.addView(ingredientText);
        }

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
        initRecipe();
    }
}