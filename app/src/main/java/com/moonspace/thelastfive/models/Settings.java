package com.moonspace.thelastfive.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.moonspace.thelastfive.helpers.DateHelper;

import java.io.Serializable;
import java.util.Date;

public class Settings implements Serializable
{
    private String gender;
    private Double height;
    private Double goalWeight;
    private Double startingWeight;
    private Date goalDate;
    private String unitOfMeasurement;
    private Boolean showGoalLine;
    private Boolean showMinLine;
    private Boolean showMaxLine;
    private Boolean showBMILine;
    private SQLiteDatabase database;

    /**
     * Creates an instance of the Settings class.
     *
     * @param database a reference to a SQLite database.
     */
    Settings(SQLiteDatabase database)
    {
        this.setDatabase(database);

        loadUserProfile();
    }

    /**
     * Gets the gender for the current user as 'male' or 'female'.
     *
     * @return the gender for the current user as 'male' or 'female'.
     */
    public String getGender()
    {
        return gender;
    }

    /**
     * Sets the gender for the current user.
     *
     * @param gender specifies the user's gender as 'male' or 'female'.
     */
    public void setGender(String gender)
    {
        if (gender.equalsIgnoreCase("male")
                || gender.equalsIgnoreCase("female"))
        {
            this.gender = gender;
        }
        else
        {
            throw new IllegalArgumentException("Gender must be set to 'male' or 'female'.");

        }
    }

    /**
     * Gets the user's height.
     *
     * @return the user's height.
     */
    public Double getHeight()
    {
        return height;
    }

    /**
     * Sets the user's height.
     *
     * @param height specifies the user's height.
     */
    public void setHeight(Double height)
    {
        if (height <= 0)
        {
            throw new IllegalArgumentException("Height must be greater than zero.");
        }

        this.height = height;
    }

    /**
     * Gets the user's goal weight.
     *
     * @return the user's goal weight.
     */
    public Double getGoalWeight()
    {
        return goalWeight;
    }

    /**
     * Sets the user's goal weight.
     *
     * @param goalWeight specifies the user's goal weight.
     */
    public void setGoalWeight(Double goalWeight)
    {
        if (goalWeight <= 0 || goalWeight > 999)
        {
            throw new IllegalArgumentException("Desired weight must be between 1 and 999.");
        }

        this.goalWeight = goalWeight;
    }

    /**
     * Gets the user's goal date.
     *
     * @return the user's goal date.
     */
    public Date getGoalDate()
    {
        return goalDate;
    }

    /**
     * Sets the user's goal date.
     *
     * @param goalDate specifies the user's goal date.
     */
    public void setGoalDate(Date goalDate)
    {
        this.goalDate = goalDate;
    }

    /**
     * Gets the user's desired unit of measurement as 'imperial' or 'metric'.
     *
     * @return the user's desired unit of measurement as 'imperial' or 'metric'.
     */
    public String getUnitOfMeasurement()
    {
        return unitOfMeasurement;
    }

    /**
     * Sets the user's desired unit of measurement as 'imperial' or 'metric'.
     *
     * @param unitOfMeasurement specifies the user's desired unit of measurement as 'imperial'
     *                          or 'metric'.
     */
    public void setUnitOfMeasurement(String unitOfMeasurement)
    {
        if (unitOfMeasurement.equalsIgnoreCase("imperial") ||
                unitOfMeasurement.equalsIgnoreCase("metric"))
        {
            this.unitOfMeasurement = unitOfMeasurement;
        }
        else
        {
            throw new IllegalArgumentException("Gender must be set to 'male' or 'female'.");
        }
    }

    /**
     * Gets a value indicating if a reference line for the goal weight should be displayed on
     * the progress chart.
     *
     * @return a value indicating if a reference line for the goal weight should be displayed on
     * * the progress chart.
     */
    public Boolean getShowGoalLine()
    {
        return showGoalLine;
    }

    /**
     * Sets a value indicating if a reference line for the goal weight should be displayed on
     * the progress chart.
     *
     * @param showGoalLine true if the reference line should be displayed; Otherwise, false.
     */
    public void setShowGoalLine(Boolean showGoalLine)
    {
        this.showGoalLine = showGoalLine;
    }

    /**
     * Gets a value indicating if a reference line for the minimum weight should be displayed on
     * the progress chart.
     *
     * @return a value indicating if a reference line for the minimum weight should be displayed on
     * * the progress chart.
     */
    public Boolean getShowMinLine()
    {
        return showMinLine;
    }

    /**
     * Sets a value indicating if a reference line for the minimum weight should be displayed on
     * the progress chart.
     *
     * @param showMinLine true if the reference line should be displayed; Otherwise, false.
     */
    public void setShowMinLine(Boolean showMinLine)
    {
        this.showMinLine = showMinLine;
    }

    /**
     * Gets a value indicating if a reference line for the maximum weight should be displayed on
     * the progress chart.
     *
     * @return a value indicating if a reference line for the maximum weight should be displayed on
     * * the progress chart.
     */
    public Boolean getShowMaxLine()
    {
        return showMaxLine;
    }

    /**
     * Sets a value indicating if a reference line for the maximum weight should be displayed on
     * the progress chart.
     *
     * @param showMaxLine true if the reference line should be displayed; Otherwise, false.
     */
    public void setShowMaxLine(Boolean showMaxLine)
    {
        this.showMaxLine = showMaxLine;
    }

    /**
     * Gets a value indicating if BMI should be displayed on the progress chart.
     *
     * @return a value indicating if BMI should be displayed on the progress chart.
     */
    private Boolean getShowBMILine()
    {
        return showBMILine;
    }

    /**
     * Sets a value indicating if BMI should be displayed on the progress chart.
     *
     * @param showBMILine true if BMI should be displayed; Otherwise, false.
     */
    private void setShowBMILine(Boolean showBMILine)
    {
        this.showBMILine = showBMILine;
    }

    public Double getStartingWeight()
    {
        return startingWeight;
    }

    public void setStartingWeight(Double startingWeight)
    {
        if (startingWeight <= 0 || startingWeight > 999)
        {
            throw new IllegalArgumentException("Starting weight must be between 1 and 999.");
        }

        this.startingWeight = startingWeight;
    }

    /**
     * Gets an instance of the current SQLite database.
     *
     * @return an instance of the current SQLite database.
     */
    private SQLiteDatabase getDatabase()
    {
        return database;
    }

    /**
     * Sets the current SQLite database instance.
     *
     * @param database an instance of a SQLite database.
     */
    void setDatabase(SQLiteDatabase database)
    {
        this.database = database;
    }

    /**
     * Creates a test user profile  to simplify the development and testing process.
     */
    public void createTestData()
    {
        this.setGender("male");
        this.setHeight(70.5);
        this.setStartingWeight(188.0);
        this.setGoalWeight(178.0);
        this.setGoalDate(DateHelper.getDateFromFormattedString("2019-10-31"));
        this.setUnitOfMeasurement("imperial");
        this.setShowGoalLine(true);
        this.setShowMinLine(true);
        this.setShowMaxLine(true);
        this.setShowBMILine(false);

        this.saveUserProfile();
    }

    /**
     * Loads the user's profile from the database. If the profile does not yet exist, default
     * values will be provided.
     */
    void loadUserProfile()
    {
        try
        {
            // Retrieve all records from the database in reverse-sorted order (newest first)
            Cursor cursor = getDatabase().rawQuery("SELECT * FROM Profile", null);

            if (cursor.getCount() > 0)
            {
                while (cursor.moveToNext())
                {
                    this.setGender(cursor.getString(cursor.getColumnIndex("gender")));
                    this.setHeight(cursor.getDouble(cursor.getColumnIndex("height")));
                    this.setStartingWeight(cursor.getDouble(cursor.getColumnIndex("startingWeight")));
                    this.setGoalWeight(cursor.getDouble(cursor.getColumnIndex("goalWeight")));
                    this.setGoalDate(DateHelper.getDateFromFormattedString(
                            cursor.getString(cursor.getColumnIndex("goalDate"))));
                    this.setUnitOfMeasurement(cursor.getString(cursor.getColumnIndex("unitOfMeasurement")));
                    this.setShowGoalLine(cursor.getInt(cursor.getColumnIndex("showGoalLine")) == 1);
                    this.setShowMinLine(cursor.getInt(cursor.getColumnIndex("showMinLine")) == 1);
                    this.setShowMaxLine(cursor.getInt(cursor.getColumnIndex("showMaxLine")) == 1);
                    this.setShowBMILine(cursor.getInt(cursor.getColumnIndex("showBMILine")) == 1);
                }
            }
            else
            {
                // First time through so let's set some default values
                this.setGender("male");
                this.setHeight(70.0);
                this.setStartingWeight(150.0);
                this.setGoalWeight(150.0);
                this.setGoalDate(DateHelper.getCurrentDate());
                this.setUnitOfMeasurement("imperial");
                this.setShowGoalLine(true);
                this.setShowMinLine(false);
                this.setShowMaxLine(false);
                this.setShowBMILine(false);
            }

            cursor.close();
        }
        catch (Exception e)
        {
            Log.e("MainModel.loadModel()", e.getMessage());
        }
    }

    /**
     * Save current user's profile to the database.
     */
    public void saveUserProfile()
    {
        ContentValues columnValues = new ContentValues();

        columnValues.put("gender", getGender());
        columnValues.put("height ", getHeight());
        columnValues.put("startingWeight ", getStartingWeight());
        columnValues.put("goalWeight ", getGoalWeight());
        columnValues.put("goalDate", DateHelper.getFormattedDate(getGoalDate()));
        columnValues.put("unitOfMeasurement ", getUnitOfMeasurement());
        columnValues.put("showGoalLine", getShowGoalLine() ? 1 : 0);
        columnValues.put("showMinLine", getShowMinLine() ? 1 : 0);
        columnValues.put("showMaxLine", getShowMaxLine() ? 1 : 0);
        columnValues.put("showBMILine", getShowBMILine() ? 1 : 0);

        // There is only one row stored in the Profile table so let's remove the data and
        // then re-insert the latest values
        getDatabase().delete("Profile", null, null);
        getDatabase().insertOrThrow("Profile", null, columnValues);
    }
}
