package com.sr.trackcrack.ui.home;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sr.trackcrack.R;
import com.sr.trackcrack.databinding.FragmentHomeBinding;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private DocumentReference inspectionRef;
    private TextView statusTextView, inspectionDetails, elapsedTimeTextView, expectedFinishTextView, cracksFoundTextView;
    private Button requestInspectionButton;
    private ImageView noLiveInspectionImage,noInspection;
    private ProgressBar inspectionProgressBar;
    private MapView mapView;
    private FloatingActionButton focusButton;
    private Marker focusMarker;
    private Toolbar toolbar;
    private CardView statusCard, detailsCard, timeCard, mapCard;

    private long startTimeMillis;
    private boolean timerRunning = false;
    private Runnable timerRunnable;
    private Handler handler = new Handler();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initializeViews();
        initializeFirestore();
        initializeFocusButton();
        initializeMapView();
        setRequestInspectionButtonListener();
        listenToInspectionChanges();

        return root;
    }

    private void initializeViews() {
        statusTextView = binding.textHome;
        requestInspectionButton = binding.requestInspectionButton;
        inspectionProgressBar = binding.inspectionProgressBar;
        inspectionDetails = binding.inspectionDetails;
        mapView = binding.map;
        focusButton = binding.focusButton;
        toolbar = getActivity().findViewById(R.id.toolbar);
        elapsedTimeTextView = binding.elapsedTime;
        expectedFinishTextView = binding.expectedFinishTime;
        cracksFoundTextView = binding.cracksFound;
        statusCard = binding.statusCard;
        noLiveInspectionImage = binding.noInspection;
        timeCard = binding.timeCard;
        mapCard = binding.mapCard;
        noInspection=binding.noInspectionText;
       setupMapTouchListener();
    }

    private void initializeFirestore() {
        db = FirebaseFirestore.getInstance();
        inspectionRef = db.collection("inspections").document("currentInspection");
    }
    private void setupMapTouchListener() {
        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        // Disallow parent to intercept touch events
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Allow parent to intercept touch events
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false; // Let the map handle the touch event
            }
        });
    }
    private void initializeFocusButton() {
        focusButton.setOnClickListener(v -> {
            if (focusMarker != null) {
                animateToLocation(focusMarker.getPosition());
            } else {
                GeoPoint defaultPoint = new GeoPoint(10.9973, 76.9664);
                animateToLocation(defaultPoint);
            }
        });
    }

    private void animateToLocation(GeoPoint location) {
        // Define the desired zoom level
        final int desiredZoomLevel = 19;

        // Get current zoom level
        double currentZoomLevel = mapView.getZoomLevel();

        // Create an animation for zoom level
        ObjectAnimator zoomAnimator = ObjectAnimator.ofFloat(mapView.getController(), "zoom", (float) currentZoomLevel, desiredZoomLevel);
        zoomAnimator.setDuration(1000); // Duration of 1 second
        zoomAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        zoomAnimator.addUpdateListener(animation -> {
            // Animate the zoom level
            float zoom = (float) animation.getAnimatedValue();
            mapView.getController().setZoom(zoom);
        });

        zoomAnimator.start();

        // Animate the movement to the new location
        mapView.getController().animateTo(location);
    }


    private void initializeMapView() {
        Configuration.getInstance().setUserAgentValue(getContext().getPackageName());
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        GeoPoint startPoint = new GeoPoint(10.9973, 76.9664);
        mapView.getController().setZoom(19);
        mapView.getController().setCenter(startPoint);
        addCustomMarker(startPoint, "Start Point");
        setToolbarTitle();
    }

    private void setRequestInspectionButtonListener() {
        requestInspectionButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Inspection request has been sent.", Toast.LENGTH_SHORT).show();
            simulateInspectionStart();
        });
    }

    private void simulateInspectionStart() {
        // This method simulates the start of an inspection
        handler.postDelayed(() -> showLiveInspection(null), 2000);
    }

    private void listenToInspectionChanges() {
        inspectionRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                String status = snapshot.getString("status");
                if (status != null) {
                    if ("started".equals(status)) {
                        showLiveInspection(snapshot);
                    } else {
                        showNoLiveInspection();
                    }
                }
            } else {
                showNoLiveInspection();
            }
        });
    }

    private void showLiveInspection(DocumentSnapshot snapshot) {
        statusTextView.setText("Inspection is Live");
        noLiveInspectionImage.setVisibility(View.GONE);
        requestInspectionButton.setVisibility(View.GONE);
        inspectionProgressBar.setVisibility(View.VISIBLE);
        noInspection.setVisibility(View.GONE);
        inspectionProgressBar.setIndeterminate(true);
        animateCardVisibility(statusCard, true);
        animateCardVisibility(timeCard, true);
        animateCardVisibility(mapCard, true);
        focusButton.show();

        if (snapshot != null) {
            updateInspectionDetails(snapshot);
        } else {
            inspectionDetails.setText("Inspection in progress");
            startTimeMillis = System.currentTimeMillis();
            updateMapLocation(new GeoPoint(10.9973, 76.9664));
        }

        startInspectionTimer();
    }

    private void updateInspectionDetails(DocumentSnapshot snapshot) {
        String details = snapshot.getString("details");
        if (details != null) {
            inspectionDetails.setText(details);
        }

        Timestamp startTime = snapshot.getTimestamp("startTime");
        if (startTime != null) {
            startTimeMillis = startTime.toDate().getTime();
        } else {
            startTimeMillis = System.currentTimeMillis();
        }

        Long cracksFound = snapshot.getLong("cracksFound");
        if (cracksFound != null) {
            cracksFoundTextView.setText("Cracks Found: " + cracksFound);
        }

         com.google.firebase.firestore.GeoPoint firebaseGeoPoint = snapshot.getGeoPoint("crack_location");
        if (firebaseGeoPoint != null) {
            org.osmdroid.util.GeoPoint osmdroidGeoPoint = new org.osmdroid.util.GeoPoint(
                    firebaseGeoPoint.getLatitude(),
                    firebaseGeoPoint.getLongitude()
            );
            updateMapLocation(osmdroidGeoPoint);
        }
    }

    private void showNoLiveInspection() {
        statusTextView.setText("No Live Inspection");
        requestInspectionButton.setVisibility(View.VISIBLE);
        noLiveInspectionImage.setVisibility(View.VISIBLE);
        noInspection.setVisibility(View.VISIBLE);
        inspectionProgressBar.setIndeterminate(false);
        animateCardVisibility(statusCard, true);
        animateCardVisibility(timeCard, false);
        animateCardVisibility(mapCard, false);
        focusButton.hide();

        mapView.getOverlays().clear();
        mapView.invalidate();

        stopInspectionTimer();
    }

    private void animateCardVisibility(CardView cardView, boolean show) {
        ObjectAnimator animator;
        if (show) {
            cardView.setVisibility(View.VISIBLE);
            animator = ObjectAnimator.ofFloat(cardView, "alpha", 0f, 1f);
        } else {
            animator = ObjectAnimator.ofFloat(cardView, "alpha", 1f, 0f);
        }
        animator.setDuration(500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
        if (!show) {
            animator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    cardView.setVisibility(View.GONE);
                }
            });
        }
    }

    private void startInspectionTimer() {
        timerRunning = true;
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timerRunning) {
                    updateElapsedTime();
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(timerRunnable);
    }

    private void stopInspectionTimer() {
        if (timerRunning) {
            timerRunning = false;
            handler.removeCallbacks(timerRunnable);
        }
    }

    private void updateElapsedTime() {
        long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        String elapsedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(elapsedMillis),
                TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % TimeUnit.MINUTES.toSeconds(1));
        elapsedTimeTextView.setText("Elapsed Time: " + elapsedTime);

        long estimatedDurationMillis = 3600000; // 1 hour
        long expectedFinishMillis = startTimeMillis + estimatedDurationMillis;
        long remainingMillis = expectedFinishMillis - System.currentTimeMillis();
        String expectedFinish = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(remainingMillis),
                TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % TimeUnit.MINUTES.toSeconds(1));
        expectedFinishTextView.setText("Expected Finish: " + expectedFinish);
    }

    private void updateMapLocation(GeoPoint location) {
        mapView.getOverlays().clear();
        addCustomMarker(location, "Inspection Location");
        mapView.getController().animateTo(location);
    }

    private void addCustomMarker(GeoPoint position, String title) {
        Marker marker = new Marker(mapView);
        marker.setPosition(position);
        BitmapDrawable icon = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.marker_3));
        Bitmap bitmap = icon.getBitmap();
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, false);
        marker.setIcon(new BitmapDrawable(getResources(), resizedBitmap));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        mapView.getOverlays().add(marker);
        mapView.invalidate();

        if ("Inspection Location".equals(title)) {
            focusMarker = marker;
        }
    }

    private void setToolbarTitle() {
        if (getActivity() != null) {
            SharedPreferences sharedPref = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String selectedLocation = sharedPref.getString("selectedLocation", "Selected Location");
            if (toolbar != null) {
                toolbar.setTitle(selectedLocation);
            }
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
        }
        stopInspectionTimer();
        binding = null;
    }
}