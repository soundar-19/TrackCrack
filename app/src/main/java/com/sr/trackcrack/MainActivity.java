package com.sr.trackcrack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.sr.trackcrack.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient googleSignInClient;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private NotificationHelper notificationHelper;
    private String lastInspectionStatus = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestAppPermissions();
        notificationHelper = new NotificationHelper(this);
        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Define the AuthStateListener
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                setContentView(R.layout.login);

                // Handle Email/Password login
                Button loginButton = findViewById(R.id.loginButton);
                EditText emailField = findViewById(R.id.email);
                EditText passwordField = findViewById(R.id.password);
                loginButton.setOnClickListener(v -> {
                    String email = emailField.getText().toString().trim();
                    String password = passwordField.getText().toString().trim();
                    if (!email.isEmpty() && !password.isEmpty()) {
                        loginUser(email, password);
                    } else {
                        Toast.makeText(MainActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                    }
                });

                // Handle Google Sign-In button click
                SignInButton signInButton = findViewById(R.id.googleSignInButton);
                signInButton.setOnClickListener(v -> signInWithGoogle());

            } else {
                // User logged in, show home screen
                showHomeScreen(user);
            }
        };

        mAuth.addAuthStateListener(mAuthListener);
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Google Sign-In intent result
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("Google Sign-In", "Google sign in failed", e);
                Toast.makeText(MainActivity.this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            updateFirestoreUser(user);
                            Intent intent = new Intent(MainActivity.this, LocationSelectionActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Log.w("Google Sign-In", "signInWithCredential:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFirestoreUser(FirebaseUser user) {
        String uid = user.getUid();
        String name = user.getDisplayName();
        String email = user.getEmail();

        // Create a new User object
        User newUser = new User(name, email);

        // Reference to Firestore
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            // Add new user document if it does not exist
                            db.collection("users").document(uid).set(newUser)
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "User successfully added to Firestore."))
                                    .addOnFailureListener(e -> Log.w("Firestore", "Error adding user to Firestore.", e));
                        } else {
                            // Optionally update the document if needed
                            db.collection("users").document(uid).update("name", name, "email", email)
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "User successfully updated in Firestore."))
                                    .addOnFailureListener(e -> Log.w("Firestore", "Error updating user in Firestore.", e));
                        }
                    } else {
                        Log.w("Firestore", "Error checking user existence.", task.getException());
                    }
                });
    }


    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Intent intent = new Intent(MainActivity.this, LocationSelectionActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void requestAppPermissions() {

            // Create a list to hold permissions that haven't been granted yet
            List<String> permissionsToRequest = new ArrayList<>();

            // Check and add location permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }

            // Check and add storage permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            // For API 33 and above, check for the POST_NOTIFICATIONS permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
                }
            }

            // If there are any permissions to request, request them
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            }

    }

    private void showHomeScreen(FirebaseUser user) {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBarMain.toolbar;
        setSupportActionBar(toolbar);
        setActionBarTitle();

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_history, R.id.nav_change_location,R.id.nav_analytics,R.id.nav_map)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                navController.navigate(R.id.nav_home);
            } else if (id == R.id.nav_history) {
                navController.navigate(R.id.nav_history);
            } else if (id == R.id.nav_analytics) {
                navController.navigate(R.id.nav_analytics);
            } else if (id == R.id.nav_change_location) {
                Intent intent = new Intent(MainActivity.this, LocationSelectionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else if (id == R.id.Logout_Button) {
                FirebaseAuth.getInstance().signOut();
                googleSignInClient.signOut(); // Sign out from Google
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        View headerView = navigationView.getHeaderView(0);
        TextView nameTextView = headerView.findViewById(R.id.nav_header_name);
        TextView emailTextView = headerView.findViewById(R.id.nav_header_email);

        String uid = user.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        nameTextView.setText(name);
                        emailTextView.setText(email);
                    }
                })
                .addOnFailureListener(e -> Log.w("Firestore", "Error getting documents.", e));
        // Add Firestore Listener for inspection status
        DocumentReference docRef = db.collection("inspections").document("currentInspection");
        docRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String status = documentSnapshot.getString("status");
                if ("started".equals(status)) {
                    // Use NotificationHelper to send the notification
                    notificationHelper.sendNotification("Inspection Update", "The inspection has started.");
                } else if ("not_started".equals(status) && "started".equals(lastInspectionStatus)) {
                    // Send notification when the inspection ends
                    notificationHelper.sendNotification("Inspection Ended", "The inspection has ended. Click to see the results.");
                }
                // Update the last known status
                lastInspectionStatus = status;
            }
        });
    }
    private void setActionBarTitle() {
        SharedPreferences sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String selectedLocation = sharedPref.getString("selectedLocation", "Selected Location");
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(selectedLocation); // Use Toolbar method
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


        @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setMessage("Do you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    finishAffinity(); // Close all activities
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss(); // Dismiss the dialog
                })
                .show();
    }
}
