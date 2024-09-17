package com.sr.trackcrack.ui.map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.sr.trackcrack.R;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private MapView mapView;
    private Toolbar toolbar;

    // List of crack locations
    private List<GeoPoint> crackLocations = new ArrayList<>();
    private int currentIndex = 0; // Index for the current location

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = root.findViewById(R.id.map);
        toolbar = getActivity().findViewById(R.id.toolbar); // Reference to the existing toolbar

        // Retrieve arguments passed from HistoryFragment
        if (getArguments() != null) {
            String inspectionId = getArguments().getString("inspection_date");
            toolbar.setTitle(inspectionId);
        }

        Configuration.getInstance().setUserAgentValue(getContext().getPackageName());

        initializeMap();

        // Set up button listeners
        root.findViewById(R.id.btn_previous).setOnClickListener(v -> showPreviousLocation());
        root.findViewById(R.id.btn_next).setOnClickListener(v -> showNextLocation());

        return root;
    }

    private void initializeMap() {
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(19.5);

        GeoPoint startPoint = new GeoPoint(10.9973, 76.9664);
        mapView.getController().setCenter(startPoint);

        // Load multiple crack locations
        crackLocations = loadCrackLocations();

        // Add markers for each crack location
        for (GeoPoint location : crackLocations) {
            addCustomMarker(location);
        }

        // Focus on the first location initially
        if (!crackLocations.isEmpty()) {
            focusOnLocation(currentIndex);
        }
    }

    private List<GeoPoint> loadCrackLocations() {
        List<GeoPoint> locations = new ArrayList<>();

        // Add sample locations (replace with actual data)
        locations.add(new GeoPoint(10.9973, 76.9664));
        locations.add(new GeoPoint(10.999787548730463, 76.96489533617505));
        locations.add(new GeoPoint(11.003194519551812, 76.96312775594026));

        return locations;
    }

    private void addCustomMarker(GeoPoint position) {
        Marker marker = new Marker(mapView);

        marker.setPosition(position);

        BitmapDrawable icon = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.marker_3));

        Bitmap bitmap = icon.getBitmap();

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, false);

        marker.setIcon(new BitmapDrawable(getResources(), resizedBitmap));

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        mapView.getOverlays().add(marker);
    }

    private void focusOnLocation(int index) {
        if (index >= 0 && index < crackLocations.size()) {
            GeoPoint targetLocation = crackLocations.get(index);
            mapView.getController().animateTo(targetLocation); // Animate to the target location
            toolbar.setTitle("Crack Location " + (index + 1)); // Update toolbar title
            currentIndex = index; // Update current index
        }
    }

    private void showPreviousLocation() {
        if (currentIndex > 0) {
            focusOnLocation(currentIndex - 1); // Move to previous location
        }
    }

    private void showNextLocation() {
        if (currentIndex < crackLocations.size() - 1) {
            focusOnLocation(currentIndex + 1); // Move to next location
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mapView != null) {
            mapView.onDetach();
            mapView = null;
        }
    }
}