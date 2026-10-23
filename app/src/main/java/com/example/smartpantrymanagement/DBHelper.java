package com.example.smartpantrymanagement;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.ContentValues;
import android.util.Base64;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import android.database.Cursor;
import java.security.MessageDigest;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SmartPantry.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context){super(context, DATABASE_NAME, null, DATABASE_VERSION);}

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE Users (" +
                    "UserID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "Username TEXT NOT NULL UNIQUE, " +
                    "Email TEXT NOT NULL UNIQUE, " +
                    "PasswordHash TEXT NOT NULL)";

    private static final String CREATE_TABLE_PANTRY =
            "CREATE TABLE PantryIngredients (" +
                    "IngredientID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "UserID INTEGER NOT NULL, " +
                    "IngredientName TEXT NOT NULL," +
                    "UNIQUE(UserId, IngredientName)," +
                    "FOREIGN KEY(UserID) REFERENCES Users(UserID) " +
                    "ON DELETE CASCADE)";

    private static final String CREATE_TABLE_RECIPE_HISTORY =
            "CREATE TABLE RecipeHistory (" +
                    "RecipeID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "UserID INTEGER NOT NULL, " +
                    "RecipeName TEXT NOT NULL, " +
                    "Ingredients TEXT NOT NULL, " +
                    "Instructions TEXT, " +
                    "FOREIGN KEY(UserID) REFERENCES Users(UserID) " +
                    "ON DELETE CASCADE)";


    public long registerUser(String username, String email, String password) {
        String passwordHash = hashPassword(password);

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("Username", username);
        values.put("Email", email);
        values.put("PasswordHash", passwordHash);

        return db.insert("Users", null, values);
    }


    public boolean verifyUser(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT PasswordHash FROM Users WHERE Email = ?",
                new String[]{email})) {

            if (!cursor.moveToFirst()) {
                return false;
            }

            String storedPasswordHash = cursor.getString(0);
            String[] parts = storedPasswordHash.split(":", 2);

            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] expectedHash = Base64.decode(parts[1], Base64.NO_WRAP);

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(), salt, 120000, 256);

            byte[] enteredPasswordHash = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();

            spec.clearPassword();

            return MessageDigest.isEqual(expectedHash, enteredPasswordHash);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Unable to verify password", e);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    private String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(), salt, 120000, 256);

            byte[] hash = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();

            spec.clearPassword();

            return Base64.encodeToString(salt, Base64.NO_WRAP) + ":" +
                    Base64.encodeToString(hash, Base64.NO_WRAP);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to hash password", e);
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db){

        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PANTRY);
        db.execSQL(CREATE_TABLE_RECIPE_HISTORY);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }

}
