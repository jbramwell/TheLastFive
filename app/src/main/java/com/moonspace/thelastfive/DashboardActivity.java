package com.moonspace.thelastfive;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.moonspace.thelastfive.helpers.DateHelper;
import com.moonspace.thelastfive.helpers.FormatHelper;
import com.moonspace.thelastfive.models.MainModel;
import com.moonspace.thelastfive.models.WeightEntry;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity
{
    private MainModel mainModel;
    private TextView txtCurrentWeight;
    private TextView txtWeightLossToDate;
    private TextView txtDesiredWeight;
    private TextView txtWeeklyAvgLoss;
    private TextView txtDaysToGoal;
    private TextView txtBMI;
    private LineChart progressChart;
    private Boolean activityStarted = false;
    private List<String> xAxisLabels = new ArrayList<>();

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Configure for use with Visual Studio App Center for analytics
        if (!BuildConfig.APP_CENTER_API_KEY.startsWith("_"))
        {
            AppCenter.start(getApplication(), BuildConfig.APP_CENTER_API_KEY,
                    Analytics.class, Crashes.class);
        }

        // This is necessary to enable URI file functionality with camera feature.
        // Link: https://stackoverflow.com/questions/42251634/android-os-fileuriexposedexception-file-jpg-exposed-beyond-app-through-clipdata
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // Setup the bottom navigation menu (this is the main menu)
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Get a reference to the main model
        mainModel = MainModel.getInstance(getApplicationContext());

        // Locate controls
        txtCurrentWeight = findViewById(R.id.txtCurrentWeight);
        txtWeightLossToDate = findViewById(R.id.txtWeightLossToDate);
        txtDesiredWeight = findViewById(R.id.txtDesiredWeight);
        txtWeeklyAvgLoss = findViewById(R.id.txtWeeklyAvgLoss);
        txtDaysToGoal = findViewById(R.id.txtDaysToGoal);
        txtBMI = findViewById(R.id.txtBMI);
        progressChart = findViewById(R.id.progressChart);

        // Display current values and update progress chart
        refreshDisplay();
    }

    /**
     * Listen for navigation menu actions and act accordingly.
     */
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId())
        {
            case R.id.navigation_new_entry:
                activityStarted = true;
                newWeightEntry();

                return true;

            case R.id.navigation_history:
                activityStarted = true;
                viewHistory();

                return true;
        }

        return true;
    };

    // Listen for toolbar actions and act accordingly.
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                activityStarted = true;
                viewSettings();

                break;

            case R.id.action_create_test_data:
                activityStarted = true;
                createTestData();

                break;
        }

        return true;
    }

    /**
     * Displays the user settings (profile) page.
     *
     * @param view a reference to the calling view.
     */
    public void onSettingsClick(View view)
    {
        viewSettings();
    }

    /**
     * Displays the history page.
     *
     * @param view a reference to the calling view.
     */
    public void onHistoryClick(View view)
    {
        viewHistory();
    }

    // Display (inflate) the toolbar menu. Found instructions on creating the menu here:
    //   https://www.vogella.com/tutorials/AndroidActionBar/article.html
    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.main_toolbar, menu);

        MenuItem menuItem = menu.findItem(R.id.action_create_test_data);

        // Do not display the "Create Test Data" toolbar menu item if the CREATE_TEST_DATA
        // environment variable is set to "Hide"
        menuItem.setVisible(!BuildConfig.CREATE_TEST_DATA.equalsIgnoreCase("hide"));

        return true;
    }

    /**
     * Displays the BMI activity to view more details regarding the user's BMI.
     *
     * @param view the application context.
     */
    public void onBmiClick(View view)
    {
        Analytics.trackEvent("Start BmiActivity");

        Intent intent = new Intent(this, BmiActivity.class);

        startActivity(intent);
    }

    /**
     * Refreshes the display when this activity regains focus (e.g. when returning from
     * another activity).
     */
    @Override
    protected void onResume()
    {
        if (activityStarted)
        {
            activityStarted = false;

            // Refresh displayed values
            refreshDisplay();
        }

        super.onResume();
    }

    /**
     * Display the Activity for entering a new weight entry.
     */
    private void newWeightEntry()
    {
        Analytics.trackEvent("Start NewEntryActivity");

        Intent intent = new Intent(this, NewEntryActivity.class);

        startActivity(intent);
    }

    /**
     * Display the Activity for viewing user history.
     */
    private void viewHistory()
    {
        Analytics.trackEvent("Start HistoryActivity");

        Intent intent = new Intent(this, HistoryActivity.class);

        startActivity(intent);
    }

    /**
     * Display the Activity for viewing app settings.
     */
    private void viewSettings()
    {
        Analytics.trackEvent("Start SettingsActivity");

        Intent intent = new Intent(this, SettingsActivity.class);

        startActivity(intent);
    }

    /**
     * Crate test data to simplify the trial run period.
     */
    private void createTestData()
    {
        Analytics.trackEvent("Create Test Data");

        mainModel.createTestData();
        mainModel.getUserSettings().createTestData();

        refreshDisplay();
    }

    /**
     * Displays the current values stored in the main model.
     */
    private void refreshDisplay()
    {
        double weightDelta = mainModel.getTotalWeightDifference();
        double currentWeight = mainModel.getCurrentWeight();
        double goalWeight = mainModel.getUserSettings().getGoalWeight();
        String indicator;
        int indicatorColor;

        if (currentWeight > goalWeight)
        {
            // Still trying to lose weight so treat negative weight loss as "bad"
            if (weightDelta == 0)
            {
                indicator = "";
                indicatorColor = Color.RED;
            }
            else if (weightDelta < 0)
            {
                indicator = getString(R.string.weight_down_indicator);
                indicatorColor = Color.parseColor("#04af70");
            }
            else
            {
                indicator = getString(R.string.weight_up_indicator);
                indicatorColor = Color.RED;
            }
        }
        else if (currentWeight < goalWeight)
        {
            // Still trying to gain weight so treat negative weight loss as "good"
            if (weightDelta == 0)
            {
                indicator = "";
                indicatorColor = Color.RED;
            }
            else if (weightDelta < 0)
            {
                indicator = getString(R.string.weight_down_indicator);
                indicatorColor = Color.RED;
            }
            else
            {
                indicator = getString(R.string.weight_up_indicator);
                indicatorColor = Color.parseColor("#04af70");
            }
        }
        else
        {
            indicator = "";
            indicatorColor = Color.parseColor("#04af70");
        }

        txtWeightLossToDate.setText(String.format("%s%s", indicator, Math.abs(weightDelta)));
        txtWeightLossToDate.setTextColor(indicatorColor);

        txtCurrentWeight.setText(FormatHelper.doubleToDefaultLocaleString(mainModel.getCurrentWeight()));
        txtWeeklyAvgLoss.setText(FormatHelper.doubleToDefaultLocaleString(mainModel.getAverageWeeklyWeightLoss()));
        txtDesiredWeight.setText(FormatHelper.doubleToDefaultLocaleString(mainModel.getDesiredWeight()));
        txtDaysToGoal.setText(FormatHelper.doubleToDefaultLocaleString(mainModel.getDaysToGoal()));
        txtBMI.setText(FormatHelper.doubleToDefaultLocaleString(mainModel.getCurrentBMI()));

        // Display the progress via a line chart
        displayProgress();
    }

    /**
     * Display the user's progress via a Line Chart and reference lines.
     */
    private void displayProgress()
    {
        // Display up to the 21 most recent entries
        Integer maxEntries = 21;

        if (mainModel.getWeightEntryList().size() > 0)
        {
            // Add/configure the list of weight entries for the chart
            LineDataSet weightEntryDataSet = new LineDataSet(
                    getChartLineDataSet(maxEntries), "Weight");

            weightEntryDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            weightEntryDataSet.setLineWidth(2);
            weightEntryDataSet.setColor(R.color.colorPrimary);
            weightEntryDataSet.setCircleColor(R.color.colorPrimary);
            weightEntryDataSet.setDrawCircleHole(false);
            weightEntryDataSet.setCircleRadius(3);
            weightEntryDataSet.setFillColor(R.color.colorAccentLight);
            weightEntryDataSet.setFillColor(Color.rgb(143, 198, 227)); //D6E9F2
            weightEntryDataSet.setDrawFilled(true);
            weightEntryDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            weightEntryDataSet.setValueTextSize(10);

            progressChart.setData(new LineData(weightEntryDataSet));
            xAxisLabels = getChartLineXAxisLabels(mainModel.getWeightEntryList(), maxEntries);

            // Configure X-axis
            ValueFormatter formatter = new ValueFormatter()
            {
                @Override
                public String getAxisLabel(float value, AxisBase axis)
                {
                    if (value < 0 || value >= xAxisLabels.size())
                        return "";
                    else
                        return xAxisLabels.get((int) value);
                }
            };

            XAxis xAxis = progressChart.getXAxis();
            xAxis.setGranularity(1f);                       // Minimum axis-step (interval) is 1
            xAxis.setValueFormatter(formatter);             // Format X-axis labels as dates
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);  // Show X-axis labels at bottom of chart
            xAxis.setLabelRotationAngle(90);                // Rotate x-axis labels 90 degrees
            xAxis.setTextSize(11);

            YAxis leftAxis = progressChart.getAxisLeft();

            leftAxis.setTextSize(11);

            // Configure limit lines (e.g. min, max and goal)
            if (mainModel.getUserSettings().getShowMaxLine())
            {
                leftAxis.removeAllLimitLines();
                float maxWeight = mainModel.getMaxWeight().floatValue();
                LimitLine maxWeightLine = new LimitLine(maxWeight, "Max.");
                maxWeightLine.setLineColor(Color.RED);
                maxWeightLine.setLineWidth(2);
                maxWeightLine.setTextColor(Color.BLACK);
                maxWeightLine.setTextSize(12);
                leftAxis.addLimitLine(maxWeightLine);
            }

            if (mainModel.getUserSettings().getShowMinLine())
            {
                float minWeight = mainModel.getMinWeight().floatValue();
                LimitLine minWeightLine = new LimitLine(minWeight, "Min.");
                minWeightLine.setLineColor(Color.GREEN);
                minWeightLine.setLineWidth(2);
                minWeightLine.setTextColor(Color.BLACK);
                minWeightLine.setTextSize(12);
                leftAxis.addLimitLine(minWeightLine);
            }

            float goalWeight = mainModel.getUserSettings().getGoalWeight().floatValue();

            if (mainModel.getUserSettings().getShowGoalLine())
            {
                LimitLine goalWeightLine = new LimitLine(goalWeight, "Goal");
                goalWeightLine.setLineColor(R.color.colorPrimary);
                goalWeightLine.setLineWidth(2);
                goalWeightLine.setTextColor(Color.BLACK);
                goalWeightLine.setTextSize(12);
                goalWeightLine.enableDashedLine(10, 10, 0);
                leftAxis.addLimitLine(goalWeightLine);
            }

            if (leftAxis.getAxisMinimum() >= goalWeight)
            {
                leftAxis.setAxisMinimum(goalWeight - 1);
            }

            // Do not display the chart legend
            progressChart.getLegend().setEnabled(false);

            // Turn off right Y-axis labels
            progressChart.getAxisRight().setEnabled(false);

            // Miscellaneous chart settings
            progressChart.setDescription(null);             // Do not display a chart description
            progressChart.animateX(1000, Easing.EaseInCirc);
        }
        else
        {
            progressChart.setNoDataText(
                    "Add entries to see your progress charted.");
            progressChart.setNoDataTextColor(Color.rgb(10, 93, 138));
        }

        // Refresh the chart
        progressChart.invalidate();
    }

    private List<Entry> getChartLineDataSet(Integer maxEntries)
    {
        List<WeightEntry> weightEntries = mainModel.getWeightEntryList();
        List<Entry> lineDataEntries = new ArrayList<>();

        // Calculate the max index - i.e. so we only return maxEntries
        int maxIndex = Math.min(maxEntries, weightEntries.size());
        int index = maxIndex - 1;

        for (int weightEntryIndex = 0; weightEntryIndex < maxIndex; weightEntryIndex++)
        {
            lineDataEntries.add(0,
                    new Entry(index, weightEntries.get(weightEntryIndex).getWeight().floatValue()));
            index--;
        }

        return lineDataEntries;
    }

    private List<String> getChartLineXAxisLabels(List<WeightEntry> weightEntryList, Integer maxEntries)
    {
        List<String> xAxisLabels = new ArrayList<>();

        // Calculate the max index - i.e. so we only return maxEntries
        int maxIndex = Math.min(maxEntries, weightEntryList.size());

        for (int index = 0; index < maxIndex; index++)
        {
            xAxisLabels.add(0,
                    DateHelper.getFormattedDate(mainModel.getWeightEntryList().get(index).getDate()));
        }

        return xAxisLabels;
    }
}