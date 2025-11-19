package com.example.mealconnect.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealconnect.ClientInfoActivity;
import com.example.mealconnect.R;
import com.example.mealconnect.data.ClientRepository;
import com.example.mealconnect.data.local.ClientEntity;
import com.example.mealconnect.ui.login.ClientListAdapter;

public class ClientListActivity extends AppCompatActivity {

    private ClientRepository repository;
    private ClientListAdapter adapter;

    private EditText etFilterRestriction;
    private Button btnApplyFilter, btnClearFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_list);

        repository = new ClientRepository(this);

        RecyclerView rv = findViewById(R.id.recyclerClients);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // adapter should show a simple list of ClientEntity
        adapter = new ClientListAdapter(client -> openClientInfo(client));
        rv.setAdapter(adapter);

        // Default: show all clients
        observeAllClients();
        /*
        etFilterRestriction = findViewById(R.id.etFilterRestriction);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        btnClearFilter = findViewById(R.id.btnClearFilter);

        // Default: show all clients
        observeAllClients();

        btnApplyFilter.setOnClickListener(v -> {
            String restriction = etFilterRestriction.getText().toString().trim();
            if (restriction.isEmpty()) {
                observeAllClients();
            } else {
                repository.filterByRestriction(restriction)
                        .observe(this, clients -> adapter.submitList(clients));
            }
        });

        btnClearFilter.setOnClickListener(v -> {
            etFilterRestriction.setText("");
            observeAllClients();
        });*/
    }

    private void observeAllClients() {
        repository.getAllClients()
                .observe(this, clients -> adapter.submitList(clients));
    }

    private void openClientInfo(ClientEntity client) {
        Intent intent = new Intent(this, ClientInfoActivity.class);
        intent.putExtra("client_id", client.id);
        startActivity(intent);
    }
}
