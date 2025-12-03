package com.example.mealconnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mealconnect.service.FirebaseManager; // REQUIRES FirebaseManager with RoleCallback
import com.example.mealconnect.ClientRegisterActivity; // *** NEW IMPORT REQUIRED ***
import com.example.mealconnect.ui.login.ClientListActivity;
import com.example.mealconnect.ui.login.LoginActivity;
import com.example.mealconnect.ui.login.volunteer.ScannerActivity; // Import Volunteer destination
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.mealconnect.data.ClientSyncRepository; // Import Sync Repo
import com.example.mealconnect.data.local.ClientDatabase; // Import DB

/**
 * MainActivity: Admin Dashboard and Startup Role Check
 *
 * This activity acts as the main application entry point. It verifies the user's role
 * and only loads the Admin UI if the user is confirmed to be an Admin.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase and FirebaseManager
        mAuth = FirebaseAuth.getInstance();
        firebaseManager = FirebaseManager.getInstance();

        // *** CRITICAL FIX: Check the user role before rendering the Admin UI ***
        checkUserRoleForAccess();
    }

    /**
     * Checks the current user's role. If they are a volunteer, they are redirected
     * to the ScannerActivity.
     */
    private void checkUserRoleForAccess() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // No user signed in, redirect to login
            navigateTo(LoginActivity.class);
            return;
        }

        // Fetch the role to confirm Admin access
        firebaseManager.getUserRole(currentUser.getUid(), new FirebaseManager.RoleCallback() {
            @Override
            public void onRoleFetched(String role) {
                String normalizedRole = role.trim().toLowerCase();
                Log.d(TAG, "Current user role: " + normalizedRole);

                // *** FIX: START DATA SYNC FOR ALL VALID USERS ***
                // This ensures Room DB is populated for both Admin (Client List) and Volunteer (Scanner).
                startDataSync();

                if ("volunteer".equals(normalizedRole)) {
                    // FIX: If the user is a volunteer, send them to the scanner and close this Admin screen.
                    Toast.makeText(MainActivity.this, "Redirecting to Scanner...", Toast.LENGTH_SHORT).show();
                    navigateTo(ScannerActivity.class);
                } else if ("admin".equals(normalizedRole)) {
                    // User is an Admin. Proceed to set up the Admin UI.
                    setupAdminDashboardUI();
                } else {
                    // Unauthorized or unrecognized role
                    Toast.makeText(MainActivity.this, "Unauthorized access. Logging out.", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                    navigateTo(LoginActivity.class);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to check role, logging out.", e);
                Toast.makeText(MainActivity.this, "Authentication failed. Please log in again.", Toast.LENGTH_LONG).show();
                mAuth.signOut();
                navigateTo(LoginActivity.class);
            }
        });
    }

    private void startDataSync() {
        try {
            ClientSyncRepository syncRepo = ClientSyncRepository.getInstance(
                    ClientDatabase.getInstance(getApplicationContext()).clientDao()
            );
            syncRepo.startSync();
            Log.d(TAG, "Data synchronization started.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start data sync", e);
        }
    }

    /**
     * Initializes the UI elements for the Admin Dashboard.
     * This is only called after the role check confirms the user is an Admin.
     */
    private void setupAdminDashboardUI() {
        setContentView(R.layout.activity_main); // Set the layout only if authorized

        // Set Toolbar as Action Bar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Admin Dashboard");
            }
        }

        Button registerClientBtn = findViewById(R.id.registerClientBtn);
        Button clientInfoBtn = findViewById(R.id.clientInfoBtn);
        Button volunteerBtn = findViewById(R.id.volunteerBtn);
        Button logoutBtn = findViewById(R.id.logoutBtn); // NEW: Logout button if you added it to XML, or assume auto-signout?
        // Ideally, add a logout button. For now, we keep existing structure.

        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> {
                // Sign out and return to Admin Login page (same behavior as volunteer logout)
                mAuth.signOut();
                navigateTo(LoginActivity.class);
            });
        }

        // *** FIX APPLIED HERE ***
        registerClientBtn.setOnClickListener(v -> {
            // Now that ClientRegisterActivity is implemented, launch it directly.
            startActivity(new Intent(this, ClientRegisterActivity.class));
        });

        clientInfoBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, ClientListActivity.class));
        });

        volunteerBtn.setOnClickListener(v -> {
            // Launch Volunteer Management Activity
            startActivity(new Intent(this, VolunteerManagementActivity.class));
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // When back button is pressed, sign out and return to login
        mAuth.signOut();
        navigateTo(LoginActivity.class);
        return true;
    }

    private void navigateTo(Class<?> destinationActivity) {
        Intent intent = new Intent(MainActivity.this, destinationActivity);
        // Using flags prevents the user from pressing 'back' and returning to this Admin screen
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
