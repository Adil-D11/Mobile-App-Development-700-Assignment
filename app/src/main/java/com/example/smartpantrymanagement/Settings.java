package com.example.smartpantrymanagement;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.AlertDialog;
import android.text.InputType;
import android.util.Patterns;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.content.SharedPreferences;
import android.widget.Switch;

import android.content.Intent;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button logoutButton = findViewById(R.id.signOutButton);

        logoutButton.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences(
                    "SmartPantrySession", MODE_PRIVATE);

            preferences.edit().remove("UserID").apply();

            // allows user to navigate back to startup page
            Intent intent = new Intent(Settings.this, StartupPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        });

        Button changeEmailButton = findViewById(R.id.changeEmailButton);

        changeEmailButton.setOnClickListener(view -> {
            showChangeEmailDialog();
        });

        Button changePasswordButton = findViewById(R.id.changePasswordButton);

        changePasswordButton.setOnClickListener(view -> {
            showChangePasswordDialog();
        });

        Switch expirySwitch = findViewById(R.id.expirySwitch);

        SharedPreferences preferences = getSharedPreferences(
                "SmartPantrySettings",
                MODE_PRIVATE
        );

        // Load the user's saved preference.
        expirySwitch.setChecked(
                preferences.getBoolean("ExpiryNotificationsEnabled", true)
        );

        expirySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit()
                    .putBoolean("ExpiryNotificationsEnabled", isChecked)
                    .apply();

            // if its on a small message will say enabled and if its off it will say disabled
            String message = isChecked
                    ? "Expiry notifications enabled"
                    : "Expiry notifications disabled";

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });


    }

    // user can change email but enter password first
    private void showChangeEmailDialog(){
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);

        // creates like a pop up for user to enter their details
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        dialogLayout.setPadding(padding, padding, padding, 0);

        // user must enter their current password
        EditText currentPasswordInput = new EditText(this);
        currentPasswordInput.setHint("Current Password");
        currentPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        dialogLayout.addView(currentPasswordInput);

        //enter in new email
        EditText newEmailInput = new EditText(this);
        newEmailInput.setHint("New Email Address");
        newEmailInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        dialogLayout.addView(newEmailInput);

        // confirm new email
        EditText confirmEmailInput = new EditText(this);
        confirmEmailInput.setHint("Confirm New Email Address");
        confirmEmailInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        dialogLayout.addView(confirmEmailInput);

        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Change Email").setView(dialogLayout).setNegativeButton("Cancel", (dialogInterface, which) -> {
            dialogInterface.dismiss();
        })
                .setPositiveButton("Save Changes", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            saveButton.setOnClickListener(view -> {
                String currentPassword = currentPasswordInput.getText().toString();
                String newEmail = newEmailInput.getText().toString().trim();
                String confirmEmail = confirmEmailInput.getText().toString().trim();

                if (currentPassword.isEmpty() || newEmail.isEmpty() || confirmEmail.isEmpty()) {
                    Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()){
                    newEmailInput.setError("Enter a valid email address");
                    newEmailInput.requestFocus();
                    return;
                }

                if (!newEmail.equalsIgnoreCase(confirmEmail)){
                    confirmEmailInput.setError("Email addresses don't match");
                    confirmEmailInput.requestFocus();
                    return;
                }


                SharedPreferences session = getSharedPreferences(
                        "SmartPantrySession", MODE_PRIVATE);
                long userId = session.getLong("UserID", -1);

                // creates DBHelper to for update
                DBHelper dbHelper = new DBHelper(this);

                try {
                    boolean updated = userId != -1
                            && dbHelper.updateEmail(userId, currentPassword, newEmail);

                    if (updated) {
                        Toast.makeText(this, "Email updated successfully",
                                Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this,
                                "Incorrect current password or email not updated",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (android.database.sqlite.SQLiteConstraintException e) {
                    newEmailInput.setError("This email address is already registered");
                    newEmailInput.requestFocus();
                }

            });
        });
        dialog.show();
    }

    private void showChangePasswordDialog(){
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        dialogLayout.setPadding(padding, padding, padding, 0);

        EditText currentPasswordInput = new EditText(this);
        currentPasswordInput.setHint("Current Password");
        currentPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        dialogLayout.addView(currentPasswordInput);

        EditText newPasswordInput = new EditText(this);
        newPasswordInput.setHint("New Password (Min 8 Characters)");
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        dialogLayout.addView(newPasswordInput);

        EditText confirmPasswordInput = new EditText(this);
        confirmPasswordInput.setHint("Confirm New Password");
        confirmPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        dialogLayout.addView(confirmPasswordInput);

        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Change Password").setView(dialogLayout).setNegativeButton("Cancel", (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                })
                .setPositiveButton("Save Changes", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            saveButton.setOnClickListener(view -> {
                String currentPassword = currentPasswordInput.getText().toString();
                String newPassword = newPasswordInput.getText().toString();
                String confirmPassword = confirmPasswordInput.getText().toString();

                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(newPassword.length() < 8){
                    newPasswordInput.setError("Password must be at least 8 characters");
                    newPasswordInput.requestFocus();
                    return;
                }

                if(!newPassword.equals(confirmPassword)){
                    confirmPasswordInput.setError("Passwords do not match");
                    confirmPasswordInput.requestFocus();
                    return;
                }


                SharedPreferences session = getSharedPreferences(
                        "SmartPantrySession", MODE_PRIVATE);
                long userId = session.getLong("UserID", -1);

                DBHelper dbHelper = new DBHelper(this);

                boolean updated = userId != -1
                        && dbHelper.updatePassword(userId, currentPassword, newPassword);

                if (updated) {
                    Toast.makeText(this, "Password updated successfully",
                            Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this,
                            "Incorrect current password or password not updated",
                            Toast.LENGTH_LONG).show();
                }

            });
        });
        dialog.show();
    }
}