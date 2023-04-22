package com.moonspace.thelastfive.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.moonspace.thelastfive.R;
import com.moonspace.thelastfive.helpers.AppDatabaseHelper;
import com.moonspace.thelastfive.helpers.DateHelper;
import com.moonspace.thelastfive.helpers.MathHelper;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MainModel implements Serializable
{
    private static MainModel instance;
    private Settings userSettings;
    private List<WeightEntry> weightEntryList;
    private final Context context;
    private final AppDatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    /**
     * The default constructor (made private to support the Singleton pattern).
     */
    private MainModel(Context context)
    {
        this.context = context.getApplicationContext();

        // Get a reference to the SQLite helper/database
        databaseHelper = new AppDatabaseHelper(context);
        database = getDatabaseHelper().getWritableDatabase();
    }

    /**
     * Gets an instance of the MainModel. This method is provided to support the Singleton pattern.
     *
     * @param context A reference to the applicaiton context.
     * @return A Singleton instance of the MainModel.
     */
    public static MainModel getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new MainModel(context);
        }

        if (instance.userSettings == null)
        {
            // Load the persisted user settings
            Settings settings = new Settings(instance.getDatabase());

            instance.setUserSettings(settings);

            // Load entries and other values from database
            instance.loadModel();
        }

        return instance;
    }

    /**
     * Make sure the database connection is closed prior to cleaning up this instance.
     */
    @Override
    protected void finalize()
    {
        if (getDatabase() != null)
        {
            getDatabase().close();
        }
    }

    /**
     * Loads the model from the database.
     */
    private void loadModel()
    {
        try
        {
            // Create an empty list for entries
            setWeightEntryList(new ArrayList<>());

            // Retrieve all records from the database in reverse-sorted order (newest first)
            Cursor cursor = getDatabase().rawQuery(
                    "SELECT * FROM Entry ORDER BY date DESC, id DESC", null);

            if (cursor.getCount() > 0)
            {
                while (cursor.moveToNext())
                {
                    WeightEntry weightEntry = new WeightEntry();

                    weightEntry.setIsModified(false);
                    weightEntry.setIsNew(false);

                    weightEntry.setId(cursor.getLong(cursor.getColumnIndex("id")));
                    weightEntry.setDate(DateHelper.getDateFromFormattedString(cursor.getString(
                            cursor.getColumnIndex("date"))));
                    weightEntry.setWeight(cursor.getDouble(cursor.getColumnIndex("weight")));
                    weightEntry.setNotes(cursor.getString(cursor.getColumnIndex("notes")));
                    weightEntry.setPhotoImage(cursor.getBlob(cursor.getColumnIndex("photoImage")));

                    getWeightEntryList().add(weightEntry);
                }
            }

            cursor.close();
        }
        catch (Exception e)
        {
            Log.e("MainModel.loadModel()", e.getMessage());
        }
    }

    /**
     * Saves the current model to the database.
     */
    public void saveModel()
    {
        try
        {
            // Loop through weight entries and perform inserts, updates and deletes as needed.
            // NOTE: We loop backward so we can perform the deletes correctly.
            for (int index = 0; index < weightEntryList.size(); index++)
            {
                WeightEntry weightEntry = weightEntryList.get(index);

                if (weightEntry.getIsNew())
                {
                    insertNewWeightItems(weightEntry);  // New Entry
                }
                else if (weightEntry.getIsModified())
                {
                    updateModifiedItems(weightEntry);   // Updated Entry
                }
                else if (weightEntry.getIsDeleted())
                {
                    removeDeletedItems(weightEntry);    // Deleted Entry
                }
            }
        }
        catch (Exception e)
        {
            Log.e("MainModel.saveModel()", e.getMessage());
        }
    }

    /**
     * Create a backup of the app database.
     */
    public void backupDatabase()
    {
        getDatabaseHelper().backupDatabase(getDatabase());
    }

    /**
     * Restore the latest backup.
     */
    public void restoreDatabase()
    {
        getDatabaseHelper().restoreDatabase(getDatabase());

        this.getUserSettings().setDatabase(instance.getDatabase());
        this.getUserSettings().loadUserProfile();
        this.loadModel();
    }

    /**
     * Inserts all new weight entry items into the database.
     *
     * @param weightEntry a reference to the WeightEntry object being inserted.
     */
    private void insertNewWeightItems(WeightEntry weightEntry)
    {
        ContentValues columnValues = new ContentValues();

        columnValues.put("date", DateHelper.getFormattedDate(weightEntry.getDate()));
        columnValues.put("weight ", weightEntry.getWeight());
        columnValues.put("notes", weightEntry.getNotes().replace("'", "''"));
        columnValues.put("photoImage", weightEntry.getPhotoImage());

        // Set the ID for the weight entry to the ID returned by the INSERT statement
        weightEntry.setId(getDatabase().insertOrThrow("Entry", null, columnValues));

        weightEntry.setIsNew(false);
    }

    /**
     * Updates all weight entry items in the database for those that have been flagged as modified.
     *
     * @param weightEntry a reference to the WeightEntry object being updated.
     */
    private void updateModifiedItems(WeightEntry weightEntry)
    {
        ContentValues columnValues = new ContentValues();

        columnValues.put("date", DateHelper.getFormattedDate(weightEntry.getDate()));
        columnValues.put("weight ", weightEntry.getWeight());
        columnValues.put("notes", weightEntry.getNotes().replace("'", "''"));
        columnValues.put("photoImage", weightEntry.getPhotoImage());

        getDatabase().update("Entry", columnValues, "id = ?",
                new String[]{weightEntry.getId().toString()});

        weightEntry.setIsModified(false);
    }

    /**
     * Removes all weight entry items from the database that have been flagged as deleted (i.e.
     * soft deleted).
     *
     * @param weightEntry a reference to the WeightEntry object being deleted.
     */
    private void removeDeletedItems(WeightEntry weightEntry)
    {
        getDatabase().delete("Entry", "id = ?",
                new String[]{weightEntry.getId().toString()});

        weightEntryList.remove(weightEntry);
    }

    /**
     * Creates a set of initial test data to simplify the development and testing process.
     */
    public void createTestData()
    {
        // First, clear all entries from the list and database
        clearAllEntries();

        // Create test data
        String[] dates = {
                "2019-06-10", "2019-06-17", "2019-06-24", "2019-07-01", "2019-07-08",
                "2019-07-15", "2019-07-22", "2019-07-29", "2019-08-05", "2019-08-16",
                "2019-08-23", "2019-08-31", "2019-09-03", "2019-09-10", "2019-09-14",
                "2019-09-21", "2019-09-28", "2019-10-03", "2019-10-09", "2019-10-16",
                "2019-10-20", "2019-10-23", "2019-11-01", "2019-11-08", "2019-11-27"
        };
        Double[] weights = {
                200.0, 199.5, 199.0, 195.0, 196.5,
                191.0, 189.0, 189.0, 188.0, 188.0,
                187.0, 187.0, 186.0, 185.5, 185.0,
                184.0, 184.0, 183.0, 183.0, 182.0,
                181.0, 180.0, 179.0, 179.0, 175.0
        };
        String[] notes = {
                "Whew... Need to lose a few!", "Slow start!", "No worries... I got this!",
                "OK, off to a good start!", "Let's keep it up!", "Still going strong!",
                "Really missing my ice cream :-/", "Well... found the plateau!", "Broke through!",
                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""
        };

        // Convert the image to a byte array and then WEBP file (because WEBP provides the
        // best quality and file size combination when compared to PNG and JPG)
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_image_black);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP, 80, stream);

        // Build out the entry list
        for (int index = 0; index < dates.length; index++)
        {
            WeightEntry entry = new WeightEntry();

            entry.setDate(DateHelper.getDateFromFormattedString(dates[index]));
            entry.setWeight(weights[index]);
            entry.setNotes(notes[index]);

            // Add a test "selfie" to even-numbered entries
            if (index % 2 == 0)
            {
                entry.setPhotoImage(stream.toByteArray());
            }

            this.getWeightEntryList().add(entry);
        }

        // Ensure data is sorted correctly
        this.sortWeightEntryList();

        // Save the test data to the database
        this.saveModel();
    }

    /**
     * Removes all weight entries from the ArrayList as well as the database.
     */
    private void clearAllEntries()
    {
        getDatabaseHelper().clearTables(getDatabase());

        this.getWeightEntryList().clear();
    }

    /**
     * @return Returns the most-recently recorded weight.
     */
    public Double getCurrentWeight()
    {
        Double currentWeight = 0.0;

        if (weightEntryList.size() > 0)
        {
            currentWeight = weightEntryList.get(0).getWeight();
        }

        return MathHelper.roundToNearestTenth(currentWeight);
    }

    /**
     * @return Returns the difference between the most-recently recorded weight and the
     * starting weight set within the user profile.
     */
    public Double getTotalWeightDifference()
    {
        return MathHelper.roundToNearestTenth(
                getCurrentWeight() - getUserSettings().getStartingWeight());
    }

    /**
     * Returns the average number of pounds lost each week based on all entries.
     *
     * @return the average number of pounds lost each week based on all entries.
     */
    public Double getAverageWeeklyWeightLoss()
    {
        if (weightEntryList.size() > 0)
        {
            double totalNumberOfWeeks =
                    DateHelper.getDaysBetweenDates(weightEntryList.get(0).getDate(),
                            weightEntryList.get(weightEntryList.size() - 1).getDate()) / 7;

            return MathHelper.roundToNearestTenth(
                    getTotalWeightDifference() / Math.abs(totalNumberOfWeeks));
        }
        else
        {
            return 0.0;
        }
    }

    /**
     * @return Returns the BMI based on current height and weight.
     */
    public Double getCurrentBMI()
    {
        double bmi = 0.0;

        if (weightEntryList.size() > 0)
        {
            // Calculate BMI
            if (userSettings.getUnitOfMeasurement().equalsIgnoreCase("imperial"))
            {
                // When using imperial units, you need to multiply by a factor of 703 (kg/m^2)/(lb/in^2)
                bmi = 703 * (this.getCurrentWeight() / Math.pow(userSettings.getHeight(), 2));
            }
            else
            {
                // We have to convert height (in centimeters) to meters
                bmi = (this.getCurrentWeight() / Math.pow(userSettings.getHeight() / 100, 2));
            }
        }

        return MathHelper.roundToNearestTenth(bmi);
    }

    /**
     * Gets the weight entry list.
     *
     * @return the weight entry list.
     */
    public List<WeightEntry> getWeightEntryList()
    {
        return weightEntryList;
    }

    /**
     * Sets the desired (goal) weight.
     *
     * @param weightEntryList the desired (goal) weight.
     */
    private void setWeightEntryList(List<WeightEntry> weightEntryList)
    {
        this.weightEntryList = weightEntryList;
    }

    /**
     * Returns the desired (goal) weight.
     *
     * @return the desired (goal) weight.
     */
    public Double getDesiredWeight()
    {
        return MathHelper.roundToNearestTenth(getUserSettings().getGoalWeight());
    }

    /**
     * Returns the number of days to the user-specified goal date.
     *
     * @return the number of days to the user-specified goal date.
     */
    public Double getDaysToGoal()
    {
        return MathHelper.roundToNearestTenth(
                DateHelper.getDaysFromNow(this.getUserSettings().getGoalDate()));
    }

    /**
     * Gets a reference to a specific weight entry based on its ID.
     *
     * @param id the weight entry ID.
     * @return a reference to a specific weight entry based on its ID.
     */
    public WeightEntry getWeightEntryById(Long id)
    {
        return this.getWeightEntryList().stream()
                .filter(x -> x.getId().equals(id))
                .findAny()
                .orElse(null);
    }

    /**
     * Returns the minimum weight entry from all recorded entries.
     *
     * @return the minimum weight entry.
     */
    public Double getMinWeight()
    {
        Optional<WeightEntry> weightEntry = this.getWeightEntryList()
                .stream()
                .min(Comparator.comparing(WeightEntry::getWeight));

        return weightEntry.isPresent() ? weightEntry.get().getWeight() : 0.0;
    }

    /**
     * Returns the maximum weight entry from all recorded entries.
     *
     * @return the maximum weight entry.
     */
    public Double getMaxWeight()
    {
        Optional<WeightEntry> weightEntry = this.getWeightEntryList()
                .stream()
                .max(Comparator.comparing(WeightEntry::getWeight));

        return weightEntry.isPresent() ? weightEntry.get().getWeight() : 0.0;
    }

    /**
     * Gets a reference to the user settings.
     *
     * @return the user settings.
     */
    public Settings getUserSettings()
    {
        return userSettings;
    }

    /**
     * Sets a reference to the user settings.
     *
     * @param userSettings the user settings.
     */
    private void setUserSettings(Settings userSettings)
    {
        this.userSettings = userSettings;
    }

    /**
     * Sorts all entries in ascending date order (i.e. oldest first)
     */
    public void sortWeightEntryList()
    {
        Collections.sort(weightEntryList, (o2, o1) -> o1.getDate().compareTo(o2.getDate()));
    }

    private SQLiteDatabase getDatabase()
    {
        if (database == null || !database.isOpen())
        {
            database = getDatabaseHelper().getWritableDatabase();
        }

        return database;
    }

    public AppDatabaseHelper getDatabaseHelper()
    {
        return databaseHelper;
    }
}
