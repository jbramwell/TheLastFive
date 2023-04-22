package com.moonspace.thelastfive;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moonspace.thelastfive.helpers.DateHelper;
import com.moonspace.thelastfive.helpers.FormatHelper;
import com.moonspace.thelastfive.helpers.PermissionsHelper;
import com.moonspace.thelastfive.models.MainModel;
import com.moonspace.thelastfive.models.WeightEntry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class NewEntryActivity extends AppCompatActivity
{
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private final int REQUEST_SELFIE_PERMISSIONS = 124;

    private MainModel mainModel;
    private WeightEntry newWeightEntry;

    // Control references
    private BottomNavigationView bottomNavigationView;
    private TextView txtSelectedDate;
    private EditText txtWeight;
    private TextView txtWeightUom;
    private ImageView selfieImage;
    private EditText entryNotes;

    private String tempImagePath;
    private byte[] displayedImageByteArray;

    /**
     * Listen for navigation menu actions and act accordingly.
     */
    private final BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId())
        {
            case R.id.navigation_new_entry_ok:
                saveEntry();
                finish();

                return true;
            case R.id.navigation_new_entry_cancel:
                finish();

                return true;
        }

        return true;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry);

        // Setup the bottom navigation menu (this is the main menu)
        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        // Locate controls
        txtSelectedDate = findViewById(R.id.txtSelectedDate);
        txtWeight = findViewById(R.id.txtWeight);
        txtWeightUom = findViewById(R.id.txtWeightUom);
        selfieImage = findViewById(R.id.selfieImage);
        entryNotes = findViewById(R.id.entryNotes);

        // Configured control callbacks
        txtSelectedDate.setOnClickListener(view -> selectEntryDate());

        txtSelectedDate.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                // Enable/disable the OK button based on whether a date and txtWeight have been entered
                enableDisableOKButton();
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });

        txtWeight.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                // Enable/disable the OK button based on whether a date and txtWeight have been entered
                enableDisableOKButton();
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });

        // Get a reference to the main model
        mainModel = MainModel.getInstance(getApplicationContext());

        // Get a reference to a new WeightEntry instance
        newWeightEntry = new WeightEntry();

        setUomLabels();

        initializeEntry();
    }

    /**
     * Deletes the current photo.
     *
     * @param view a reference to the calling current.
     */
    public void onTakeSelfieClick(View view)
    {
        takeSelfieAfterCheckingPermissions();
    }

    /**
     * Deletes the current photo.
     *
     * @param view a reference to the calling current.
     */
    public void onDeleteSelfieClick(View view)
    {
        deleteSelfie();
    }

    /**
     * Initializes the current Entry reference based on whether we're starting a new entry or
     * editing an existing entry.
     */
    private void initializeEntry()
    {
        Bundle intentParameters = getIntent().getExtras();
        newWeightEntry = null;

        if (intentParameters != null)
        {
            // An Entry index was passed in so let's attempt to get a reference to the
            // specific weight entry to be modified
            Long entryIndex = intentParameters.getLong("ENTRY_ID");

            newWeightEntry = mainModel.getWeightEntryById(entryIndex);

            if (newWeightEntry != null)
            {
                this.setTitle("Edit Weight Entry");

                // Flag this as modified so we don't create duplicate records
                newWeightEntry.setIsModified(true);

                txtSelectedDate.setText(DateHelper.getFormattedDate(newWeightEntry.getDate()));
                txtWeight.setText(FormatHelper.doubleToDefaultLocaleString(newWeightEntry.getWeight()));
                entryNotes.setText(newWeightEntry.getNotes());

                // Display the image
                byte[] image = newWeightEntry.getPhotoImage();

                if (image != null)
                {
                    Bitmap imageBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                    selfieImage.setImageBitmap(imageBitmap);
                }
                else
                {
                    selfieImage.setImageBitmap(null);
                }

            }
        }

        if (newWeightEntry == null)
        {
            // No parameters were passed into this Activity, or we couldn't find the corresponding
            // weight entry, so let's create a new entry
            newWeightEntry = new WeightEntry();

            // Let's default to the current date
            txtSelectedDate.setText(DateHelper.getCurrentFormattedDate());
        }

        // Enable/disable the OK button based on whether a date and txtWeight have been entered
        enableDisableOKButton();
    }

    /**
     * Enables the OK button if a date and txtWeight have been provided; Otherwise, the OK
     * button is disabled.
     */
    private void enableDisableOKButton()
    {
        bottomNavigationView.getMenu().getItem(1).setEnabled(
                txtSelectedDate.length() > 0 && txtWeight.length() > 0);
    }

    /**
     * Update txtWeight statistics and save txtWeight entry.
     */
    private void saveEntry()
    {
        newWeightEntry.setDate(DateHelper.getDateFromFormattedString(txtSelectedDate.getText().toString()));
        newWeightEntry.setWeight(Double.parseDouble(txtWeight.getText().toString()));
        newWeightEntry.setNotes(entryNotes.getText().toString());
        newWeightEntry.setPhotoImage(displayedImageByteArray);

        if (newWeightEntry.getIsNew())
        {
            mainModel.getWeightEntryList().add(newWeightEntry);
            mainModel.sortWeightEntryList();
        }

        mainModel.saveModel();
    }

    /**
     * Display the calendar date picker dialog.
     */
    private void selectEntryDate()
    {
        final Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(NewEntryActivity.this,
                (datePicker, year1, month1, day) -> {
                    // Update the displayed date to the selected date
                    txtSelectedDate.setText(DateHelper.getFormattedDate(year1, month1, day));
                }, year, month, dayOfMonth);

        // Do not allow post-dated entries
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    /**
     * Take a selfie to store with this txtWeight entry.
     */
    private void takeSelfieAfterCheckingPermissions()
    {
        // Looking here for thoughts: https://stackoverflow.com/questions/10473823/android-get-image-from-gallery-into-imageview
//        int RESULT_LOAD_IMAGE = 1;
//        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(i, RESULT_LOAD_IMAGE);

        // Let's make sure the user grants permission for this app to store files and take photos!
        String[] requiredPermissions = PermissionsHelper.buildPermissionsList(NewEntryActivity.this,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE});

        if (requiredPermissions.length == 0)
        {
            // All permissions have been granted by the user so go ahead and take the selfie...
            takeSelfie();
        }
        else
        {
            // We need to ask the user for permission first!
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_SELFIE_PERMISSIONS);
        }
    }

    private void takeSelfie()
    {
        // Based on StackOverflow question/response: https://stackoverflow.com/questions/2729267/android-camera-intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null)
        {
            // Create the File where the photo should go
            File photoFile = null;

            try
            {
                photoFile = createImageFile();
            }
            catch (IOException ex)
            {
                // Error occurred while creating the File
                Log.i("image error", "IOException");
            }

            // Continue only if the File was successfully created
            if (photoFile != null)
            {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));

                try
                {
                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                }
                catch (Exception e)
                {
                    Log.e("NewEntryActivity.takeSelfieAfterCheckingPermissions()", e.getMessage());
                }
            }
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
        if (requestCode == REQUEST_SELFIE_PERMISSIONS)
        {
            if (PermissionsHelper.isAllPermissionsGranted(grantResults))
            {
                // Permission Granted
                takeSelfie();
            }
            else
            {
                // Permission Denied
                Toast.makeText(NewEntryActivity.this, "Permission Denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Creates a file for storing a photo image.
     *
     * @return The created file.
     * @throws IOException an IOException is thrown if a temp file is not able to be created for
     *                     the camera photo image.
     */
    private File createImageFile() throws IOException
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TheLastFive_" + timeStamp + "_";

        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName, // prefix
                ".jpg",  // suffix
                storageDir     // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        tempImagePath = "file:" + image.getAbsolutePath();

        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            displayAndSetImage();
        }
    }

    /**
     * Displays the current photo.
     */
    private void displayAndSetImage()
    {
        try
        {
            // Display the image
            if (tempImagePath != null)
            {
                // Convert the image to a byte array and then WEBP file (because WEBP provides the
                // best quality and file size combination when compared to PNG and JPG)
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(tempImagePath));
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.WEBP, 80, stream);

                // Display the image
                selfieImage.setImageBitmap(imageBitmap);

                // Add image to new weight entry to be stored in the database
                displayedImageByteArray = stream.toByteArray();

                // Delete the source file since it's no longer needed
                if ((new File(tempImagePath.replace("file:", ""))).delete())
                {
                    Log.v("displayAndSetImage", "Temporary image file deleted.");
                }
                else
                {
                    Log.v("displayAndSetImage", "Temporary image file not deleted.");
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the current photo.
     */
    private void deleteSelfie()
    {
        // Let's prompt the user to make sure they actually want to delete this item
        new AlertDialog.Builder(NewEntryActivity.this)
                .setTitle("Delete Image")
                .setMessage("Delete the selected image?")
                .setIcon(android.R.drawable.ic_delete)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    selfieImage.setImageDrawable(getResources()
                            .getDrawable(R.drawable.ic_image_black, null));

                    displayedImageByteArray = null;
                    tempImagePath = null;

                    Toast.makeText(NewEntryActivity.this, "Image Deleted", Toast.LENGTH_SHORT).show();
                }).setNegativeButton(android.R.string.no, null).show();
    }

    /**
     * Displays the appropriate unit of measure labels based on the selected unit of measure
     * preference. For example, (lb)/(in) for imperial and (kg)/(cm) for metric.
     */
    private void setUomLabels()
    {
        if (mainModel.getUserSettings().getUnitOfMeasurement().equalsIgnoreCase("metric"))
        {
            // Just switched to metric
            txtWeightUom.setText(R.string.uom_metric_weight);
        }
        else
        {
            // Just switched to imperial
            txtWeightUom.setText(R.string.uom_imperial_weight);
        }
    }
}
