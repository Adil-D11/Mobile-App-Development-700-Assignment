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

public class Register_Page extends AppCompatActivity {

    private EditText Email;
    private EditText Username;
    private EditText Password;
    private EditText ConPassword;
    private DBHelper dbHelper;

    private void OpenActivity(Class activity){
        Intent intent = new Intent(this,activity);
        // Removes the login page from stack when user logs in so they can't go back to that page without pressing logout button
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void initRegBtn(){
        Button RegBtn = findViewById(R.id.btnReg);

        Email = findViewById(R.id.editEmail);
        Username = findViewById(R.id.editUsername);
        Password = findViewById(R.id.editPassword);
        ConPassword = findViewById(R.id.editConPassword);

        RegBtn.setOnClickListener(view->{
            String email = Email.getText().toString().trim();
            String username = Username.getText().toString().trim();
            String password = Password.getText().toString().trim();
            String conPassword = ConPassword.getText().toString().trim();

            if(email.isEmpty() || username.isEmpty() || password.isEmpty() || conPassword.isEmpty()){
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Email.setError("Enter valid email address");
                Email.requestFocus();
                return;
            }

            if(password.length() < 8){
                Password.setError("Password needs to be 8 characters or more");
                Password.requestFocus();
                return;
            }

            if(!password.equals(conPassword)){
                ConPassword.setError("Passwords do not match");
                ConPassword.requestFocus();
                return;
            }

            long result = dbHelper.registerUser(username, email, password);

            if (result == -1) {
                Toast.makeText(this,
                        "Username or email already exists",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(this,
                    "Registration successful!",
                    Toast.LENGTH_SHORT).show();

            OpenActivity(MainActivity.class);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DBHelper(this);
        initRegBtn();
    }
}