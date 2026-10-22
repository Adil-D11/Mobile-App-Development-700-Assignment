package com.example.smartpantrymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private void OpenActivity(Class activity){
        Intent intent = new Intent(this,activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void initGenerateRecipeBtn(){
        //creating a button
        Button GererateRecipebtn = findViewById(R.id.buttonGenerateRecipe);
        GererateRecipebtn.setOnClickListener(view->{
            OpenActivity(GenerateRecipe.class);
        });
    }

    private void initHelpBtn(){
        ImageButton ImgHelpBtn = findViewById(R.id.ImgBtnHelp);
        ImgHelpBtn.setOnClickListener(view->{
            OpenActivity(Help.class);
        });
    }

    private void initPrevRecipeBtn(){
        ImageButton ImgPrevRecipeBtn = findViewById(R.id.ImgBtnPrevRecipes);
        ImgPrevRecipeBtn.setOnClickListener(view->{
            OpenActivity(PreviousRecipes.class);
        });
    }

    private void initSettingsBtn(){
        ImageButton ImgSettingsBtn = findViewById(R.id.ImgBtnSettings);
        ImgSettingsBtn.setOnClickListener(view->{
            OpenActivity(Settings.class);
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initGenerateRecipeBtn();
        initHelpBtn();
        initPrevRecipeBtn();
        initSettingsBtn();
    }
}