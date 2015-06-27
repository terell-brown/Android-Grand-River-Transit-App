package tbrown.com.woodbuffalotransitmockup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import tbrown.com.woodbuffalotransitmockup.Constants;
import tbrown.com.woodbuffalotransitmockup.R;
import tbrown.com.woodbuffalotransitmockup.adapters.StopTimesAdapter;
import tbrown.com.woodbuffalotransitmockup.database.DBHelper;
import tbrown.com.woodbuffalotransitmockup.util.SimpleDividerItemDecoration;

/**
 * Displays list of times for specific stop and service time
 */
public class StopTimesActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

    // UI
    private Toolbar tab;
    private TextView tabTitle;
    private Spinner spin;
    private int spinnerSelection;
    private static final String[] spinnerItems = {"Weekdays","Saturday", "Sunday & Holidays"};

    // List of Stop Times
    private StopTimesAdapter mAdapter;
    private RecyclerView mRecyclerView;

    // Business Logic
    private String routeInfo;
    private int routeId;
    private String stopInfo;
    private int stopId;
    private String serviceId;
    private int directionId;
    private boolean isFirstTime = true;

    // Constants
    private static final String WEEKDAYS_ALL = Constants.WEEKDAYS_ALL;
    private static final String SATURDAY = Constants.SATURDAY;
    private static final String SUNDAY = Constants.SUNDAY;
    private static final String[] SERVICES = Constants.SERVICES;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityContext = getBaseContext();
        setContentView(R.layout.activity_stop_details);
        getTransitInfo();
        setupToolbar(stopInfo);
        setupTab();
        setupSpinner(); // used to select service id
        setupRecyclerView();
    }

    private void getTransitInfo() {
        Intent intent = getIntent();
        routeInfo = intent.getStringExtra("ROUTE_INFO");
        routeId = intent.getIntExtra("ROUTE_NO", 400);
        stopInfo = intent.getStringExtra("STOP_INFO");
        stopId = intent.getIntExtra("STOP_ID", 5425);
        serviceId = intent.getStringExtra("SERVICE_ID");
        directionId = intent.getIntExtra("DIRECTION_ID", 0);
        spinnerSelection = intent.getIntExtra("SPINNER_SELECTION",1);
    }

    private void setupTab() {
        tab = (Toolbar) findViewById(R.id.tab);
        setSupportActionBar(tab);
        getSupportActionBar().setTitle("");
        tabTitle = (TextView) tab.findViewById(R.id.tvTabTitle);
        tabTitle.setText(routeId + " - To Downtown");
    }

    private void setupRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rvScheduleTo);
        mAdapter = new StopTimesAdapter(activityContext,routeInfo,getTimes());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activityContext));
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(activityContext));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private String[] getTimes() {
        String[] times = DBHelper.getInstance(activityContext).getTimes(routeId, serviceId, 1, stopId);
        if (times.length < 1) {
            // no stop times in the next 4 hrs
            switch (serviceId) {
                case WEEKDAYS_ALL:
                    return new String[]{"No service in this direction from Mon - Fri."};
                case SATURDAY:
                    return new String[]{"No service in this direction on Saturday"};
                case SUNDAY:
                    return new String[]{"No service in this direction Sundays & Holidays"};
            }
        }
        return times;
    }

    private void setupSpinner () {
        spin = (Spinner) findViewById(R.id.spinSchedule);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(activityContext,
                android.R.layout.simple_spinner_item,
                spinnerItems);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(arrayAdapter);
        spin.setOnItemSelectedListener(this);
        spin.setSelection(spinnerSelection);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (!isFirstTime) {
            Intent intent = new Intent(getApplicationContext(), StopTimesActivity.class);
            intent.putExtra("ROUTE_INFO", routeInfo);
            intent.putExtra("ROUTE_NO", routeId);
            intent.putExtra("STOP_INFO", stopInfo);
            intent.putExtra("STOP_ID", stopId);
            intent.putExtra("SERVICE_ID", SERVICES[position]);
            intent.putExtra("DIRECTION_ID", directionId);
            intent.putExtra("SPINNER_SELECTION",position);
            Log.i("MyActivity", "Spinner item selected");
            startActivity(intent);
            finish();
        }
        isFirstTime = false;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}


