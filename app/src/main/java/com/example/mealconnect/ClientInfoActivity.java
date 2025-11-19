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

public class ClientInfoActivity extends AppCompatActivity {

    private ClientRepository repository;

    private TextView tvName, tvId, tvPhone, tvAddress, tvLanguage,
            tvHousehold, tvQualification, tvRestrictions,
            tvAuthorizedPickup, tvDate;

    private Button btnEdit, btnDelete;
    private ClientEntity loadedClient; // store current client

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_info);

        repository = new ClientRepository(this);

        // UI IDs
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

        int clientId = getIntent().getIntExtra("client_id", -1);
        if (clientId == -1) {
            Toast.makeText(this, "No client ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Observe client
        repository.getClientById(clientId).observe(this, client -> {
            if (client == null) {
                Toast.makeText(this, "Client not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            loadedClient = client; // store for editing/deleting
            bindClient(client);
        });

        // EDIT BUTTON
        btnEdit.setOnClickListener(v -> {
            if (loadedClient == null) return;

            Intent intent = new Intent(this, EditClientActivity.class);
            intent.putExtra("client_id", loadedClient.id);
            startActivity(intent);
        });

        // DELETE BUTTON
        btnDelete.setOnClickListener(v -> {
            if (loadedClient == null) return;

            new AlertDialog.Builder(this)
                    .setTitle("Delete Client")
                    .setMessage("Are you sure you want to delete this client?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        repository.deleteClient(loadedClient);
                        Toast.makeText(this, "Client deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void bindClient(ClientEntity c) {
        tvName.setText(c.name);
        tvId.setText("ID: " + c.clientId);
        tvPhone.setText(c.phone);
        tvAddress.setText(c.address);
        tvLanguage.setText(c.firstLanguage);

        tvHousehold.setText("Adults: " + c.numAdults + ", Children: " + c.numChildren);

        tvQualification.setText(
                (c.lowIncome ? "Low Income " : "") +
                        (c.newcomer ? "Newcomer " : "") +
                        (c.senior ? "Senior" : "")
        );

        tvRestrictions.setText(c.dietaryRestrictions);
        tvAuthorizedPickup.setText(c.authorizedPickup);
        tvDate.setText(c.dateApplied);
    }
}
