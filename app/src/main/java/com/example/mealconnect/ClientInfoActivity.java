package com.example.mealconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mealconnect.data.ClientRepository;
import com.example.mealconnect.data.local.ClientEntity;

/**
 * ClientInfoActivity
 * --------------------
 * Displays all details of a selected client.
 * Allows editing or deleting the client entry.
 * Loads client data from the database using LiveData.
 */
public class ClientInfoActivity extends AppCompatActivity {

    private ClientRepository repository;

    // UI Elements
    private TextView tvName, tvId, tvPhone, tvAddress, tvLanguage,
            tvHousehold, tvQualification, tvRestrictions,
            tvAuthorizedPickup, tvDate;

    private Button btnEdit, btnDelete;

    private ClientEntity loadedClient; // Stores the currently loaded client object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_info);

        // Initialize repository for DB operations
        repository = new ClientRepository(this);

        // --- Bind UI ---
        tvName = findViewById(R.id.tvClientNameDetail);
        tvId = findViewById(R.id.tvClientIdDetail);
        tvPhone = findViewById(R.id.tvPhoneDetail);
        tvAddress = findViewById(R.id.tvAddressDetail);
        tvLanguage = findViewById(R.id.tvLanguageDetail);
        tvHousehold = findViewById(R.id.tvHouseholdDetail);
        tvQualification = findViewById(R.id.tvQualificationDetail);
        tvRestrictions = findViewById(R.id.tvRestrictionsDetail);
        tvAuthorizedPickup = findViewById(R.id.tvAuthorizedPickupDetail);
        tvDate = findViewById(R.id.tvDateDetail);

        btnEdit = findViewById(R.id.btnEditClient);
        btnDelete = findViewById(R.id.btnDeleteClient);

        // Get client ID passed from previous activity
        int clientId = getIntent().getIntExtra("client_id", -1);
        if (clientId == -1) {
            Toast.makeText(this, "No client ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- Observe the client from the database ---
        repository.getClientById(clientId).observe(this, client -> {
            if (client == null) {
                Toast.makeText(this, "Client not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            loadedClient = client; // Save reference for edit/delete actions
            bindClient(client);    // Populate UI
        });

        // --- EDIT BUTTON ---
        btnEdit.setOnClickListener(v -> {
            if (loadedClient == null) return;

            // Open EditClientActivity with the same client ID
            Intent intent = new Intent(this, EditClientActivity.class);
            intent.putExtra("client_id", loadedClient.id);
            startActivity(intent);
        });

        // --- DELETE BUTTON ---
        btnDelete.setOnClickListener(v -> {
            if (loadedClient == null) return;

            // Confirm deletion with user
            new AlertDialog.Builder(this)
                    .setTitle("Delete Client")
                    .setMessage("Are you sure you want to delete this client?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        repository.deleteClient(loadedClient);
                        Toast.makeText(this, "Client deleted", Toast.LENGTH_SHORT).show();
                        finish(); // Close screen after deletion
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // --- Setup Toolbar as Action Bar ---
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Client Details");
            }
        }

        // Handle device back button
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // Just close this page
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Back arrow in toolbar
        return true;
    }

    /**
     * Binds ALL client data fields to the UI.
     */
    private void bindClient(ClientEntity c) {
        tvName.setText(c.name);
        tvId.setText("ID: " + c.clientId);
        tvPhone.setText(c.phone);
        tvAddress.setText(c.address);
        tvLanguage.setText(c.firstLanguage);

        // Household breakdown
        tvHousehold.setText("Adults: " + c.numAdults + ", Children: " + c.numChildren);

        // Qualification tags combined into a single readable line
        tvQualification.setText(
                (c.lowIncome ? "Low Income " : "") +
                        (c.newcomer ? "Newcomer " : "") +
                        (c.senior ? "Senior" : "")
        );

        tvRestrictions.setText(c.dietaryRestrictions);
        tvAuthorizedPickup.setText(c.authorizedPickup);
        tvDate.setText(c.dateApplied); // Original date they applied
    }
}
