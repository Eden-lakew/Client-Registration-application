package com.example.mealconnect;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VolunteerManagementActivity
 *
 * Allows the Admin to create new Volunteer accounts AND view existing ones.
 */
public class VolunteerManagementActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnCreateAccount;
    private ProgressBar progressBar;

    // List View Components
    private RecyclerView recyclerVolunteers;
    private TextView tvEmptyVolunteers;
    private VolunteerAdapter adapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_management);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Create Account UI
        etEmail = findViewById(R.id.etVolunteerEmail);
        etPassword = findViewById(R.id.etVolunteerPassword);
        btnCreateAccount = findViewById(R.id.btnCreateVolunteer);
        progressBar = findViewById(R.id.progressBar);

        // List UI
        recyclerVolunteers = findViewById(R.id.recyclerVolunteers);
        tvEmptyVolunteers = findViewById(R.id.tvEmptyVolunteers);

        recyclerVolunteers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VolunteerAdapter();
        recyclerVolunteers.setAdapter(adapter);

        btnCreateAccount.setOnClickListener(v -> createVolunteerAccount());

        // Load the list
        fetchVolunteers();

        // Set Toolbar as Action Bar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Volunteer Management");
            }
        }

        // Handle hardware back button
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // Go back to previous screen
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Go back to previous screen
        return true;
    }

    private void fetchVolunteers() {
        db.collection("users")
                .whereEqualTo("role", "volunteer")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> emails = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String email = doc.getString("email");
                        if (email != null) {
                            emails.add(email);
                        }
                    }

                    if (emails.isEmpty()) {
                        tvEmptyVolunteers.setVisibility(View.VISIBLE);
                        recyclerVolunteers.setVisibility(View.GONE);
                    } else {
                        tvEmptyVolunteers.setVisibility(View.GONE);
                        recyclerVolunteers.setVisibility(View.VISIBLE);
                        adapter.setVolunteers(emails);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching volunteers: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void createVolunteerAccount() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnCreateAccount.setEnabled(false);

        // FIX: Use a Secondary Firebase App to create user WITHOUT logging out Admin
        // This prevents the 'PERMISSION_DENIED' error because the Admin remains logged in
        // and performs the write operation to the database.
        FirebaseApp secondaryApp = null;
        try {
            String secondaryAppName = "SecondaryApp";
            try {
                secondaryApp = FirebaseApp.getInstance(secondaryAppName);
            } catch (IllegalStateException e) {
                // App doesn't exist, create it
                FirebaseOptions options = FirebaseApp.getInstance().getOptions();
                secondaryApp = FirebaseApp.initializeApp(this, options, secondaryAppName);
            }

            FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);

            secondaryAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign up success - get the UID from the secondary auth
                                FirebaseUser user = secondaryAuth.getCurrentUser();
                                if (user != null) {
                                    String uid = user.getUid();
                                    // IMPORTANT: Sign out the secondary auth immediately so it doesn't interfere
                                    secondaryAuth.signOut();
                                    // Now save the role using the main db instance (still authenticated as Admin)
                                    saveVolunteerRole(uid, email);
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                progressBar.setVisibility(View.GONE);
                                btnCreateAccount.setEnabled(true);

                                String errorMsg = task.getException().getMessage();
                                if (errorMsg != null && errorMsg.contains("already in use")) {
                                    Toast.makeText(VolunteerManagementActivity.this,
                                            "Email already exists. The account may have been created but role not saved. Please check Firestore or use a different email.",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(VolunteerManagementActivity.this,
                                            "Authentication failed: " + errorMsg,
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            btnCreateAccount.setEnabled(true);
            Toast.makeText(this, "Error initializing secondary app: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveVolunteerRole(String uid, String email) {
        // Create User Document in Firestore with role="volunteer"
        // NOTE: We use the default 'db' instance because we want to write as the currently logged-in ADMIN
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("role", "volunteer");

        db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    btnCreateAccount.setEnabled(true);
                    Toast.makeText(VolunteerManagementActivity.this, "Volunteer Account Created Successfully!", Toast.LENGTH_SHORT).show();

                    // Clear fields
                    etEmail.setText("");
                    etPassword.setText("");

                    // Refresh list to show the new volunteer
                    fetchVolunteers();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnCreateAccount.setEnabled(true);
                    Toast.makeText(VolunteerManagementActivity.this, "Failed to save role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --- Inner Adapter Class ---
    private static class VolunteerAdapter extends RecyclerView.Adapter<VolunteerAdapter.ViewHolder> {
        private List<String> volunteers = new ArrayList<>();

        void setVolunteers(List<String> list) {
            this.volunteers = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.text1.setText(volunteers.get(position));
        }

        @Override
        public int getItemCount() {
            return volunteers.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1;
            ViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
