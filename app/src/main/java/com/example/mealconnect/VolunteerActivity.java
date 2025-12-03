package com.example.mealconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mealconnect.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

/**
 *
 * This activity handles the volunteer management section of the MealConnect app.
 * It displays information or actions related to volunteers (e.g., viewing, adding,
 * or managing volunteer profiles — functionality to be implemented).
 *
 * The layout file used is activity_volunteer.xml. Edge-to-edge mode is enabled to
 * allow UI content to extend behind system bars. Insets are applied dynamically to
 * prevent UI overlap with the status and navigation bars.
 *


 */
public class VolunteerActivity extends AppCompatActivity {

    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set Toolbar as Action Bar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Volunteer");
            }
        }

        // Setup Logout Button (if exists in layout)
        logoutButton = findViewById(R.id.logout_button);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> handleLogout());
        }

        // Handle hardware back button: sign out and return to login (same as logout)
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleLogout();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        handleLogout(); // Sign out and return to login
        return true;
    }

    /**
     * Handles logout action - signs out and returns to login screen.
     */
    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(VolunteerActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}