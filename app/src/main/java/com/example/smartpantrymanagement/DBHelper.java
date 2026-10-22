package com.example.smartpantrymanagement;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
