package com.moonspace.thelastfive;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.moonspace.thelastfive.models.MainModel;
import com.sccomponents.gauges.gr009.GR009;
import com.sccomponents.gauges.gr009.ScGauges.ScArcGauge;
import com.sccomponents.gauges.gr009.ScGauges.ScFeature;
import com.sccomponents.gauges.gr009.ScGauges.ScNotches;
import com.sccomponents.gauges.gr009.ScGauges.ScPointer;

public class BmiActivity extends AppCompatActivity
{
    private ScArcGauge mGaugeManager;
    private static final float NEEDLE_WIDTH = 0.01f;
    private static final float NEEDLE_HEIGHT = 0.15f;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        // Get a reference to the main model
        MainModel mainModel = MainModel.getInstance(getApplicationContext());

        // Configure the BMI gauge control
        GR009 bmiGauge = findViewById(R.id.bmiMeter);

        bmiGauge.setShowPlaceholder(true);
        bmiGauge.setShowContour(false);
        bmiGauge.setText(getString(R.string.bmi));
        bmiGauge.setSections(50);
        bmiGauge.setEnableTouch(false);
        bmiGauge.setGaugeColor(Color.WHITE);
        bmiGauge.setForeColor(ContextCompat.getColor(BmiActivity.this, R.color.colorPrimaryDark));
        bmiGauge.addProgressColor(0, 18, ContextCompat.getColor(BmiActivity.this, R.color.colorUnderweight));
        bmiGauge.addProgressColor(18, 25, ContextCompat.getColor(BmiActivity.this, R.color.colorHealthyWeight));
        bmiGauge.addProgressColor(25, 29, ContextCompat.getColor(BmiActivity.this, R.color.colorOverweight));
        bmiGauge.addProgressColor(29, 40, ContextCompat.getColor(BmiActivity.this, R.color.colorObese));
        bmiGauge.addProgressColor(40, 50, ContextCompat.getColor(BmiActivity.this, R.color.colorExtremeObese));

        bmiGauge.setValue(mainModel.getCurrentBMI().floatValue());

        // Configure needle for gauge
        mGaugeManager = bmiGauge.getGauge();

        // Create the needle
        ScPointer needle = mGaugeManager.getHighPointer();
        needle.setVisible(true);
        needle.setColors(ContextCompat.getColor(BmiActivity.this, R.color.colorPrimaryDark));
        needle.setType(ScNotches.NotchTypes.RECTANGLE);
        needle.setPosition(ScFeature.Positions.INSIDE);
        needle.setHaloAlpha(100);
        needle.setHaloWidth(40);

        // Set the dimension when the gauge is already rendered
        mGaugeManager.post(BmiActivity.this::setNeedleDimension);
    }

    /**
     * Set the needle dimension proportionally to the gauge manager dimension.
     */
    private void setNeedleDimension()
    {
        // Check for empty values
        if (mGaugeManager != null)
        {
            // Get and set the needle
            ScPointer needle = this.mGaugeManager.getHighPointer();

            needle.setWidths(this.mGaugeManager.getWidth() * NEEDLE_WIDTH);
            needle.setHeights(this.mGaugeManager.getHeight() * NEEDLE_HEIGHT);

            // Put at top
            this.mGaugeManager.bringOnTop(needle);
        }
    }
}
