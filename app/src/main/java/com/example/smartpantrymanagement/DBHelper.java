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
    private static final int DATABASE_VERSION = 2;

    public DBHelper(Context context){super(context, DATABASE_NAME, null, DATABASE_VERSION);}


    // Created the tables with fields
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
                    "ExpiryDate TEXT, " +
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

//Handles creating a new users account and storing details in database
    public long registerUser(String username, String email, String password) {
        String passwordHash = hashPassword(password);

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("Username", username);
        values.put("Email", email);
        values.put("PasswordHash", passwordHash);

        return db.insert("Users", null, values);
    }



    //Manages the logging in by checking email and password
    public long getUserId(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();

        //searches for the user in database
        try (Cursor cursor = db.rawQuery(
                "SELECT UserID, PasswordHash FROM Users WHERE Email = ?",
                new String[]{email})) {

            //checks if user exists
            if (!cursor.moveToFirst()) {
                return -1;
            }

            //retrives the details if found
            long userId = cursor.getLong(0);
            String storedPasswordHash = cursor.getString(1);
            String[] parts = storedPasswordHash.split(":", 2);

            if (parts.length != 2) {
                return -1;
            }

            byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] expectedHash = Base64.decode(parts[1], Base64.NO_WRAP);

            // verifies password
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(), salt, 120000, 256);

            byte[] enteredPasswordHash = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();

            spec.clearPassword();

            if (MessageDigest.isEqual(expectedHash, enteredPasswordHash)) {
                return userId;
            }

            return -1;

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Unable to verify password", e);
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }



    //Hases the users password before storing in database
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


    public boolean updateEmail(long userId, String currentPassword, String newEmail) {
        SQLiteDatabase db = getWritableDatabase();

        // Verify the current password before changing the email.
        try (Cursor cursor = db.rawQuery(
                "SELECT PasswordHash FROM Users WHERE UserID = ?",
                new String[]{String.valueOf(userId)})) {

            if (!cursor.moveToFirst()) {
                return false;
            }

            if (!verifyPassword(currentPassword, cursor.getString(0))) {
                return false;
            }
        }

        ContentValues values = new ContentValues();
        values.put("Email", newEmail);

        return db.update(
                "Users",
                values,
                "UserID = ?",
                new String[]{String.valueOf(userId)}
        ) > 0;
    }

    // The user can update their email
    public boolean updatePassword(long userId, String currentPassword, String newPassword) {
        SQLiteDatabase db = getWritableDatabase();

        // Verifies the current password before saving a new hash.
        try (Cursor cursor = db.rawQuery(
                "SELECT PasswordHash FROM Users WHERE UserID = ?",
                new String[]{String.valueOf(userId)})) {

            if (!cursor.moveToFirst()) {
                return false;
            }

            if (!verifyPassword(currentPassword, cursor.getString(0))) {
                return false;
            }
        }

        ContentValues values = new ContentValues();
        values.put("PasswordHash", hashPassword(newPassword));

        return db.update(
                "Users",
                values,
                "UserID = ?",
                new String[]{String.valueOf(userId)}
        ) > 0;
    }

    //Checks if password entered by user matches the hashed password
    private boolean verifyPassword(String password, String storedPasswordHash) {
        String[] parts = storedPasswordHash.split(":", 2);

        if (parts.length != 2) {
            return false;
        }

        try {
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

        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }


    // Adds a new ingredient to database
    public long addIngredient(long userId, String ingredientName, String expiryDate) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("UserID", userId);
        values.put("IngredientName", ingredientName);

        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            values.putNull("ExpiryDate");
        } else {
            values.put("ExpiryDate", expiryDate);
        }

        return db.insert("PantryIngredients", null, values);
    }

    // displays the ingredients from the database
    public Cursor getPantryIngredients(long userId) {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT IngredientID, IngredientName, ExpiryDate " +
                        "FROM PantryIngredients WHERE UserID = ? " +
                        "ORDER BY IngredientName COLLATE NOCASE",
                new String[]{String.valueOf(userId)}
        );
    }

    // removes the ingredient from the list
    public int deleteIngredient(long userId, String ingredientName) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                "PantryIngredients",
                "UserID = ? AND IngredientName = ?",
                new String[]{
                        String.valueOf(userId),
                        ingredientName
                }
        );
    }

    // saves the generated recipe page and displays it in the previous recipe page
    public long saveRecipeHistory(long userId, String recipeName, String ingredients) {
        SQLiteDatabase db = getWritableDatabase();

        // Check whether this user has already saved this recipe
        Cursor cursor = db.rawQuery(
                "SELECT RecipeID FROM RecipeHistory " +
                        "WHERE UserID = ? AND RecipeName = ? COLLATE NOCASE",
                new String[]{
                        String.valueOf(userId),
                        recipeName
                }
        );

        try {
            if (cursor.moveToFirst()) {
                //Return the existing ID instead of inserting a duplicate
                return cursor.getLong(0);
            }
        } finally {
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put("UserID", userId);
        values.put("RecipeName", recipeName);
        values.put("Ingredients", ingredients);

        return db.insert("RecipeHistory", null, values);
    }

    public Cursor getRecipeHistory(long userId) {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT MIN(RecipeID) AS RecipeID, RecipeName, Ingredients " +
                        "FROM RecipeHistory " +
                        "WHERE UserID = ? " +
                        "GROUP BY RecipeName COLLATE NOCASE " +
                        "ORDER BY MAX(RecipeID) DESC",
                new String[]{String.valueOf(userId)}
        );
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
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            db.execSQL(
                    "ALTER TABLE PantryIngredients ADD COLUMN ExpiryDate TEXT"
            );
        }
    }

}
