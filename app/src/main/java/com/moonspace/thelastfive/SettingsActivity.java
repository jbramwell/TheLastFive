package com.moonspace.thelastfive;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.appcenter.analytics.Analytics;
import com.moonspace.thelastfive.helpers.DateHelper;
import com.moonspace.thelastfive.helpers.FolderHelper;
import com.moonspace.thelastfive.helpers.FormatHelper;
import com.moonspace.thelastfive.helpers.PermissionsHelper;
import com.moonspace.thelastfive.helpers.VersionHelper;
import com.moonspace.thelastfive.models.MainModel;

import java.io.File;
import java.util.Date;

public class SettingsActivity extends AppCompatActivity
{
    private final int REQUEST_BACKUP_FILE_PERMISSIONS = 124;
    private final int REQUEST_RESTORE_FILE_PERMISSIONS = 125;

    private MainModel mainModel;
    private RadioButton radioMale;
    private RadioButton radioFemale;
    private EditText txtHeight;
    private TextView txtHeightUom;
    private EditText txtStartingWeight;
    private TextView txtStartingWeightUom;
    private EditText txtDesiredWeight;
    private TextView txtDesiredWeightUom;
    private TextView txtGoalDate;
    private RadioButton radioImperial;
    private RadioButton radioMetric;
    private Switch switchShowGoalLine;
    private Switch switchShowMinLine;
    private Switch switchShowMaxLine;
    private TextView txtVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Setup the bottom navigation menu (this is the main menu)
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Locate controls
        radioMale = findViewById(R.id.radio_male);
        radioFemale = findViewById(R.id.radio_female);
        txtHeight = findViewById(R.id.txtHeight);
        txtHeightUom = findViewById(R.id.txtHeightUom);
        txtStartingWeight = findViewById(R.id.txtStartingWeight);
        txtStartingWeightUom = findViewById(R.id.txtStartingWeightUom);
        txtDesiredWeight = findViewById(R.id.txtDesiredWeight);
        txtDesiredWeightUom = findViewById(R.id.txtDesiredWeightUom);
        txtGoalDate = findViewById(R.id.txtGoalDate);
        switchShowGoalLine = findViewById(R.id.switchShowGoalLine);
        switchShowMinLine = findViewById(R.id.switchShowMinLine);
        switchShowMaxLine = findViewById(R.id.switchShowMaxLine);
        radioImperial = findViewById(R.id.radio_imperial);
        radioMetric = findViewById(R.id.radio_metric);
        txtVersion = findViewById(R.id.txtVersion);

        // Get a reference to the main model
        mainModel = MainModel.getInstance(getApplicationContext());

        // Display user settings
        displayUserSettings();
    }

    /**
     * Displays the appropriate unit of measure labels based on the selected unit of measure
     * preference. For example, (lb)/(in) for imperial and (kg)/(cm) for metric.
     */
    private void setUomLabels()
    {
        if (radioMetric.isChecked())
        {
            // Just switched to metric
            txtHeightUom.setText(getString(R.string.uom_metric_length));
            txtStartingWeightUom.setText(getString(R.string.uom_metric_weight));
            txtDesiredWeightUom.setText(getString(R.string.uom_metric_weight));
        }
        else
        {
            // Just switched to imperial
            txtHeightUom.setText(getString(R.string.uom_imperial_length));
            txtStartingWeightUom.setText(getString(R.string.uom_imperial_weight));
            txtDesiredWeightUom.setText(getString(R.string.uom_imperial_weight));
        }
    }

    /**
     * Listen for navigation menu actions and act accordingly.
     */
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId())
        {
            case R.id.navigation_settings_ok:
                saveSettings();
                finish();

                return true;
            case R.id.navigation_settings_cancel:
                finish();

                return true;
        }

        return true;
    };

    /**
     * Update unit of measure labels as the preferred unit of measure is selected.
     *
     * @param view a reference to the calling view.
     */
    public void onUnitOfMeasureClick(View view)
    {
        setUomLabels();
    }

    /**
     * Backup the current database.
     *
     * @param view a reference to the calling view.
     */
    public void onBackupNowClick(View view)
    {
        backupNowAfterCheckingPermissions();
    }

    /**
     * Restore the current backup.
     *
     * @param view a reference to the calling view.
     */
    public void onRestoreClick(View view)
    {
        restoreAfterCheckingPermissions();
    }

    /**
     * Displays the current backup status - e.g. date, time and size of the backup.
     */
    private void displayBackupStatus()
    {
        TextView txtLastBackedUp = findViewById(R.id.txtLastBackedUp);
        File backupFile = mainModel.getDatabaseHelper().getBackupStorageFile();

        if (backupFile.length() == 0)
        {
            txtLastBackedUp.setText(getString(R.string.never));
        }
        else
        {
            String backupDate = DateHelper.getFormattedDate(new Date(backupFile.lastModified()));
            String backupTime = DateHelper.getFormattedTime(new Date(backupFile.lastModified()));

            txtLastBackedUp.setText(String.format("%s %s (%s)", backupDate, backupTime,
                    FolderHelper.getFolderSizeLabel(backupFile)));
        }
    }

    /**
     * Checks for the appropriate permissions and then performs a backup of the database.
     */
    private void backupNowAfterCheckingPermissions()
    {
        // Let's make sure the user grants permission for this app to store files!
        String[] requiredPermissions = PermissionsHelper.buildPermissionsList(SettingsActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});

        if (requiredPermissions.length == 0)
        {
            // All permissions have been granted by the user so go ahead and take the selfie...
            backupNow();
        }
        else
        {
            // We need to ask the user for permission first!
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_BACKUP_FILE_PERMISSIONS);
        }

    }

    /**
     * Creates a backup of the current database (permissions are assumed at this point).
     */
    private void backupNow()
    {
        mainModel.backupDatabase();

        displayBackupStatus();

        Toast.makeText(SettingsActivity.this, "Backup Complete",
                Toast.LENGTH_SHORT).show();

        Analytics.trackEvent("Database Backed Up");
    }

    /**
     * Checks for the appropriate permissions and then performs a restore of the backup.
     */
    private void restoreAfterCheckingPermissions()
    {
        new AlertDialog.Builder(SettingsActivity.this)
                .setTitle("Restore Backup")
                .setMessage("Restore the latest backup (this will replace all current data)?")
                .setIcon(android.R.drawable.ic_delete)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    // Let's make sure the user grants permission for this app to store files!
                    String[] requiredPermissions = PermissionsHelper.buildPermissionsList(SettingsActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});

                    if (requiredPermissions.length == 0)
                    {
                        // All permissions have been granted by the user so go ahead and take the selfie...
                        restoreNow();
                    }
                    else
                    {
                        // We need to ask the user for permission first!
                        ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_RESTORE_FILE_PERMISSIONS);
                    }
                }).setNegativeButton(android.R.string.no, null).show();
    }

    /**
     * Restores the current backup (permissions are assumed at this point).
     */
    private void restoreNow()
    {
        File backupFile = mainModel.getDatabaseHelper().getBackupStorageFile();

        if (backupFile.length() > 0)
        {
            mainModel.restoreDatabase();

            displayUserSettings();

            Toast.makeText(SettingsActivity.this, "Restore Complete",
                    Toast.LENGTH_SHORT).show();

            Analytics.trackEvent("Database Restored");
        }
        else
        {
            Toast.makeText(SettingsActivity.this, "No Backup Found!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Make a final determination as to whether the user granted all necessary permissions. If
     * so,then continue with original task; Otherwise, display alert message.
     *
     * @param requestCode  the request code assigned to the task being checked.
     * @param permissions  a list of permissions that were granted.
     * @param grantResults a list of permission grant results.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        boolean permissionsGranted = false;

        switch (requestCode)
        {
            case REQUEST_BACKUP_FILE_PERMISSIONS:
                if (PermissionsHelper.isAllPermissionsGranted(grantResults))
                {
                    permissionsGranted = true;
                    backupNow();
                }

                break;

            case REQUEST_RESTORE_FILE_PERMISSIONS:
                if (PermissionsHelper.isAllPermissionsGranted(grantResults))
                {
                    permissionsGranted = true;
                    restoreNow();
                }

                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (!permissionsGranted)
        {
            // Permission Denied
            Toast.makeText(SettingsActivity.this, "Permission Denied",
                    Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Displays a Date Picker and updates the Goal date.
     *
     * @param view the calling view.
     */
    public void onGoalDateClick(View view)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(mainModel.getUserSettings().getGoalDate());

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(SettingsActivity.this,
                (datePicker, year1, month1, day) -> {
                    // Update the displayed date to the selected date
                    txtGoalDate.setText(DateHelper.getFormattedDate(year1, month1, day));
                }, year, month, dayOfMonth);

        // Do not allow pre-dated entries (i.e. you can't set your goal date to yesterday
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    /**
     * Display current user settings.
     */
    private void displayUserSettings()
    {
        radioFemale.setChecked(mainModel.getUserSettings().getGender().equals("female"));
        radioMale.setChecked(mainModel.getUserSettings().getGender().equals("male"));
        txtHeight.setText(FormatHelper.doubleToDefaultLocaleString(mainModel.getUserSettings().getHeight()));
        txtStartingWeight.setText(FormatHelper.doubleToDefaultLocaleString(mainModel.getUserSettings().getStartingWeight()));
        txtDesiredWeight.setText(FormatHelper.doubleToDefaultLocaleString(mainModel.getUserSettings().getGoalWeight()));
        txtGoalDate.setText(DateHelper.getFormattedDate(mainModel.getUserSettings().getGoalDate()));
        radioMetric.setChecked(mainModel.getUserSettings().getUnitOfMeasurement().equals("metric"));
        radioImperial.setChecked(mainModel.getUserSettings().getUnitOfMeasurement().equals("imperial"));
        switchShowGoalLine.setChecked(mainModel.getUserSettings().getShowGoalLine());
        switchShowMinLine.setChecked(mainModel.getUserSettings().getShowMinLine());
        switchShowMaxLine.setChecked(mainModel.getUserSettings().getShowMaxLine());

        // Display app version
        txtVersion.setText(VersionHelper.getVersionName(SettingsActivity.this));

        setUomLabels();

        displayBackupStatus();
    }

    /**
     * Updates the user settings.
     */
    private void saveSettings()
    {
        mainModel.getUserSettings().setGender(radioFemale.isChecked() ? "female" : "male");
        mainModel.getUserSettings().setHeight(Double.parseDouble(txtHeight.getText().toString()));
        mainModel.getUserSettings().setStartingWeight(Double.parseDouble(txtStartingWeight.getText().toString()));
        mainModel.getUserSettings().setGoalWeight(Double.parseDouble(txtDesiredWeight.getText().toString()));
        mainModel.getUserSettings().setGoalDate(DateHelper.getDateFromFormattedString(txtGoalDate.getText().toString()));
        mainModel.getUserSettings().setUnitOfMeasurement(radioMetric.isChecked() ? "metric" : "imperial");
        mainModel.getUserSettings().setShowGoalLine(switchShowGoalLine.isChecked());
        mainModel.getUserSettings().setShowMinLine(switchShowMinLine.isChecked());
        mainModel.getUserSettings().setShowMaxLine(switchShowMaxLine.isChecked());

        mainModel.getUserSettings().saveUserProfile();
    }
}
