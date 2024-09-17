package com.sr.trackcrack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationSelectionActivity extends AppCompatActivity {

    private List<String> locationsList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_selection);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Select the location");
        }

        // Initialize ListView and adapter
        ListView locationList = findViewById(R.id.location_list);
        locationsList = new ArrayList<>(Arrays.asList(
                "Coimbatore Jn, Platform 1", "Coimbatore Jn, Platform 2",
                "Chennai Egmore, Platform 1", "Chennai Egmore, Platform 2",
                "Chennai Central, Platform 1", "Chennai Central, Platform 2",
                "Mumbai CST, Platform 1", "Mumbai CST, Platform 2",
                "Mumbai Lokmanya Tilak, Platform 1", "Mumbai Lokmanya Tilak, Platform 2",
                "New Delhi, Platform 1", "New Delhi, Platform 2",
                "Delhi Junction, Platform 1", "Delhi Junction, Platform 2",
                "Howrah Jn, Platform 1", "Howrah Jn, Platform 2",
                "Sealdah, Platform 1", "Sealdah, Platform 2",
                "Bangalore City, Platform 1", "Bangalore City, Platform 2",
                "Yesvantpur Jn, Platform 1", "Yesvantpur Jn, Platform 2",
                "Hyderabad, Platform 1", "Hyderabad, Platform 2",
                "Secunderabad Jn, Platform 1", "Secunderabad Jn, Platform 2",
                "Pune, Platform 1", "Pune, Platform 2",
                "Ahmedabad, Platform 1", "Ahmedabad, Platform 2",
                "Jaipur, Platform 1", "Jaipur, Platform 2",
                "Kanpur Central, Platform 1", "Kanpur Central, Platform 2",
                "Lucknow Jn, Platform 1", "Lucknow Jn, Platform 2",
                "Varanasi Jn, Platform 1", "Varanasi Jn, Platform 2",
                "Patna Jn, Platform 1", "Patna Jn, Platform 2",
                "Indore, Platform 1", "Indore, Platform 2",
                "Bhopal, Platform 1", "Bhopal, Platform 2"
        ));

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locationsList);
        locationList.setAdapter(adapter);

        // Set up SearchView
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // No action on query submit
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the adapter based on the query
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        // Handle item click
        locationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedLocation = locationsList.get(position);
                saveSelectedLocation(selectedLocation);
            }
        });
    }

    private void saveSelectedLocation(String location) {
        // Save the selected location to shared preferences
        SharedPreferences sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("selectedLocation", location);
        editor.apply();

        // Redirect to the main app screen
        navigateToMainScreen();
    }

    private void navigateToMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Close the LocationSelectionActivity
    }
}
