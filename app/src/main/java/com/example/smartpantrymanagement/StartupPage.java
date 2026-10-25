package com.example.smartpantrymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StartupPage extends AppCompatActivity {

    //Allows for navigation when button is pressed
    private void OpenActivity(Class activity){
        Intent intent = new Intent(this,activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void initLoginPage(){
        Button LogPgBtn = findViewById(R.id.btnLogin);
        LogPgBtn.setOnClickListener(view->{
            OpenActivity(LoginPage.class);
        });
    }

    private void initRegisterPage(){
        Button RegPgBtn = findViewById(R.id.btnReg);
        RegPgBtn.setOnClickListener(view->{
            OpenActivity(Register_Page.class);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_startup_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initLoginPage();
        initRegisterPage();
    }
}