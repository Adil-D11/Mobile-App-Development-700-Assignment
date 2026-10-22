package com.example.smartpantrymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.EditText;
import android.widget.Toast;
import android.util.Patterns;

public class LoginPage extends AppCompatActivity {

    private EditText Email;
    private EditText Password;

    private void OpenActivity(Class activity){
        Intent intent = new Intent(this,activity);
        // Removes the login page from stack when user logs in so they can't go back to that page without pressing logout button
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void initLoginBtn(){
        Button LoginBtn = findViewById(R.id.btnLogin);

        Email = findViewById(R.id.editEmail);
        Password = findViewById(R.id.editPassword);

        LoginBtn.setOnClickListener(view->{

            String email = Email.getText().toString().trim();
            String password = Password.getText().toString().trim();

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Email.setError("Enter valid email address");
                Email.requestFocus();
                return;
            }

            OpenActivity(MainActivity.class);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initLoginBtn();
    }
}