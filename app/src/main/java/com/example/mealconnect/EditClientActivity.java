package com.example.mealconnect;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mealconnect.data.ClientRepository;
import com.example.mealconnect.data.local.ClientEntity;

public class EditClientActivity extends AppCompatActivity {

    private EditText etName, etPhone, etAddress, etLang, etRestrictions, etAuthorizedPickup;
    private Button btnSave;

    private ClientRepository repository;
    private ClientEntity client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_client);

        repository = new ClientRepository(this);

        etName = findViewById(R.id.etEditName);
        etPhone = findViewById(R.id.etEditPhone);
        etAddress = findViewById(R.id.etEditAddress);
        etLang = findViewById(R.id.etEditLanguage);
        etRestrictions = findViewById(R.id.etEditRestrictions);
        etAuthorizedPickup = findViewById(R.id.etEditAuthorizedPickup);

        btnSave = findViewById(R.id.btnSaveClient);

        int clientId = getIntent().getIntExtra("client_id", -1);

        repository.getClientById(clientId).observe(this, c -> {
            client = c;
            etName.setText(c.name);
            etPhone.setText(c.phone);
            etAddress.setText(c.address);
            etLang.setText(c.firstLanguage);
            etRestrictions.setText(c.dietaryRestrictions);
            etAuthorizedPickup.setText(c.authorizedPickup);
        });

        btnSave.setOnClickListener(v -> {
            client.name = etName.getText().toString();
            client.phone = etPhone.getText().toString();
            client.address = etAddress.getText().toString();
            client.firstLanguage = etLang.getText().toString();
            client.dietaryRestrictions = etRestrictions.getText().toString();
            client.authorizedPickup = etAuthorizedPickup.getText().toString();

            repository.updateClient(client);

            Toast.makeText(this, "Client updated", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
