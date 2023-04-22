package com.moonspace.thelastfive;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ListView;

import com.moonspace.thelastfive.models.MainModel;

public class HistoryActivity extends AppCompatActivity
{
    // Control references
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Locate controls
        listView = findViewById(R.id.listView);

        // Get a reference to the main model
        MainModel mainModel = MainModel.getInstance(getApplicationContext());

        CustomHistoryListAdapter adapter = new CustomHistoryListAdapter(mainModel, HistoryActivity.this);

        listView.setAdapter(adapter);
    }

    /**
     * Refreshes the display when this activity regains focus (e.g. when returning from
     * another activity).
     */
    @Override
    protected void onResume()
    {
        // Refresh the ListView
        ((CustomHistoryListAdapter) listView.getAdapter()).notifyDataSetChanged();

        super.onResume();
    }
}