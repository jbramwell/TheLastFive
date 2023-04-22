package com.moonspace.thelastfive.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class AppDatabaseHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "TheLast5.db";
    private static final int DATABASE_VERSION = 2;

    /**
     * Creates an instance of the AppDatabaseHelper class.
     */
    public AppDatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This method is called when the database needs to be created.
     *
     * @param db a reference to a SQLiteDatabase.
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // Create the 'Entry' table (added in v1)
        createEntryTable(db);

        // Create the 'Profile' table (added in v2)
        createProfileTable(db);
    }

    /**
     * This method is called upon upgrade of the database.
     *
     * @param db         a reference to a SQLiteDatabase.
     * @param oldVersion the old database version number.
     * @param newVersion the new database version number.
     */
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        switch (oldVersion)
        {
            case 1:
                // Create the 'Profile' table (added in v2)
                createProfileTable(db);

                break;
            default:
                throw new IllegalStateException(
                        "onUpgrade() with unknown oldVersion " + oldVersion);
        }
    }

    /**
     * Creates the 'Profile' table in the database. This table is used to store the
     * user's profile data.
     *
     * @param db a reference to a SQLiteDatabase.
     */
    private void createProfileTable(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE Profile (" +
                "gender TEXT, " +
                "height REAL, " +
                "startingWeight REAL, " +
                "goalWeight REAL, " +
                "goalDate TEXT, " +
                "unitOfMeasurement TEXT, " +
                "showGoalLine INT , " +
                "showMinLine INT , " +
                "showMaxLine INT , " +
                "showBMILine INT  " +
                ")");
    }

    /**
     * Creates the 'Entry' table in the database. This table is used to store the
     * user's individual weight entries.
     *
     * @param db a reference to a SQLiteDatabase.
     */
    private void createEntryTable(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE Entry (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT NOT NULL, " +
                "weight REAL NOT NULL, " +
                "notes TEXT, " +
                "photoImage BLOB" +
                ")");
    }

    /**
     * Truncates all tables in the database.
     *
     * @param db a reference to a SQLiteDatabase.
     */
    public void clearTables(SQLiteDatabase db)
    {
        // Truncate table
        db.execSQL("DELETE FROM Entry");
    }

    /**
     * Creates a backup of the application database.
     *
     * @param db a reference to a SQLiteDatabase.
     */
    public void backupDatabase(SQLiteDatabase db)
    {
        if (isExternalStorageWritable())
        {
            if (db.isOpen())
            {
                // Close the database to ensure there are no locks and that everything is flushed
                db.close();
            }

            File sourceDatabasePath = new File(db.getPath());
            File destinationDatabasePath = getBackupStorageFile();

            copyDatabase(sourceDatabasePath, destinationDatabasePath);
        }
        else
        {
            Log.v("backupDatabase", "External storage is not writable");
        }
    }

    /**
     * Restores the application database to the latest backup.
     *
     * @param db a reference to a SQLiteDatabase.
     */
    public void restoreDatabase(SQLiteDatabase db)
    {
        if (isExternalStorageWritable())
        {
            if (db.isOpen())
            {
                // Close the database to ensure there are no locks and that everything is flushed
                db.close();
            }

            File destinationDatabasePath = new File(db.getPath());
            File sourceDatabasePath = getBackupStorageFile();

            copyDatabase(sourceDatabasePath, destinationDatabasePath);
        }
        else
        {
            Log.v("backupDatabase", "External storage is not writable");
        }
    }

    /**
     * Makes a copy of the source file (database).
     *
     * @param sourceDatabasePath      the source file.
     * @param destinationDatabasePath the destination file.
     */
    private void copyDatabase(File sourceDatabasePath, File destinationDatabasePath)
    {
        try
        {
            if (sourceDatabasePath.exists())
            {
                Log.d("TAG", "DatabaseHandler: DB exist");

                FileChannel sourceChannel = new FileInputStream(sourceDatabasePath).getChannel();
                FileChannel destChannel = new FileOutputStream(destinationDatabasePath).getChannel();

                // Transfer file in 10MB blocks
                final long blockSize = Math.min(10485760, sourceChannel.size());
                long position = 0;

                while (destChannel.transferFrom(sourceChannel, position, blockSize) > 0)
                {
                    position += blockSize;
                }

                sourceChannel.close();
                destChannel.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Creates the backup file.
     *
     * @return a reference to the backup file.
     */
    public File getBackupStorageFile()
    {
        String baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String fileName = "TheLast5.backup";

        // Get a reference to the Downloads folder and make sure it exists
        File downloadDir = new File(baseDir);
        if (downloadDir.mkdirs())
        {
            Log.v("", "Directories created");
        }

        // Create an empty backup file
        File backupStorageFile = new File(baseDir, fileName);

        try
        {
            if (backupStorageFile.createNewFile())
            {
                Log.v("getBackupStorageFile", "Backup file created successfully");
            }
            else
            {
                Log.v("getBackupStorageFile", "Backup file already exists");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return backupStorageFile;
    }

    /**
     * Checks to see if external storage is writable.
     *
     * @return true if external storage is writable; Otherwise, false.
     */
    private boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();

        return (Environment.MEDIA_MOUNTED.equals(state));
    }
}
