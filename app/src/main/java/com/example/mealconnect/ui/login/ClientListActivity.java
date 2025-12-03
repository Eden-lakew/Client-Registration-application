package com.example.mealconnect.ui.login;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealconnect.ClientInfoActivity;
import com.example.mealconnect.R;
import com.example.mealconnect.data.ClientRepository;
import com.example.mealconnect.data.local.ClientEntity;
import com.example.mealconnect.data.model.CheckInEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ClientListActivity extends AppCompatActivity {

    // Repository handles all database reads (clients + check-ins)
    private ClientRepository repository;

    // Adapter for displaying the client list in RecyclerView
    private ClientListAdapter adapter;

    // UI Components for filtering and searching
    private EditText etFilterRestriction;
    private EditText etSearchQuery;
    private Button btnApplyFilter, btnClearFilter;

    // Date Picker Components
    private TextView tvCurrentDate;
    private Button btnPickDate;

    // Tracks the date currently being viewed
    private Calendar currentViewDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_list);

        // Initialize repository and default date to TODAY
        repository = new ClientRepository(this);
        currentViewDate = Calendar.getInstance();

        // Set up RecyclerView
        RecyclerView rv = findViewById(R.id.recyclerClients);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Adapter handles client click events
        adapter = new ClientListAdapter(this::openClientInfo);
        rv.setAdapter(adapter);

        // Bind UI References
        etFilterRestriction = findViewById(R.id.etFilterRestriction);
        etSearchQuery = findViewById(R.id.etSearchQuery);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        btnClearFilter = findViewById(R.id.btnClearFilter);

        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        btnPickDate = findViewById(R.id.btnPickDate);

        // Initial UI Setup
        updateDateLabel();                // Shows today's date
        observeAllClients();              // Loads full client list
        observeCheckInsForDate(currentViewDate); // Loads today's check-ins

        // --- Toolbar Setup ---
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                // Enable back button
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Client List");
            }
        }

        // Handle hardware back button
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // --- UI Listeners ---

        // Date Picker Button
        btnPickDate.setOnClickListener(v -> showDatePicker());

        // Search Field Typing Listener
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Live search functionality
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    observeAllClients();
                } else {
                    // Search across name/phone/email
                    repository.searchClients(query)
                            .observe(ClientListActivity.this, clients -> adapter.submitList(clients));
                }
            }
        });

        // Apply Manual Restriction Filter
        btnApplyFilter.setOnClickListener(v -> {
            String restriction = etFilterRestriction.getText().toString().trim();
            if (restriction.isEmpty()) {
                observeAllClients();
            } else {
                // Clear search when restriction filter is applied
                etSearchQuery.setText("");
                repository.filterByRestriction(restriction)
                        .observe(this, clients -> adapter.submitList(clients));
            }
        });

        // Clear ALL filters and restore full list
        btnClearFilter.setOnClickListener(v -> {
            etFilterRestriction.setText("");
            etSearchQuery.setText("");
            observeAllClients();
        });
    }

    /**
     * Opens a DatePicker dialog to select a check-in date.
     * Refreshes client list with check-in highlights for that date.
     */
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    currentViewDate.set(year, month, dayOfMonth);
                    updateDateLabel();
                    observeCheckInsForDate(currentViewDate);
                },
                currentViewDate.get(Calendar.YEAR),
                currentViewDate.get(Calendar.MONTH),
                currentViewDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Updates the top date label (e.g., "Viewing: 2025-01-03")
     */
    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvCurrentDate.setText("Viewing: " + sdf.format(currentViewDate.getTime()));
    }

    /**
     * Observes ALL clients in the database.
     * This is the default unfiltered list.
     */
    private void observeAllClients() {
        repository.getAllClients()
                .observe(this, clients -> {
                    adapter.submitList(clients);

                    // Empty state message
                    findViewById(R.id.tvEmptyList).setVisibility(
                            clients.isEmpty() ? View.VISIBLE : View.GONE
                    );
                });
    }

    /**
     * Observes check-ins for a specific date.
     * Builds a set of client IDs who checked in, so the RecyclerView can highlight them.
     */
    private void observeCheckInsForDate(Calendar date) {

        // Start of the day (00:00)
        Calendar start = (Calendar) date.clone();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        // End of the day (23:59)
        Calendar end = (Calendar) date.clone();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);

        repository.getCheckInsForDate(start.getTimeInMillis(), end.getTimeInMillis())
                .observe(this, checkIns -> {
                    Set<String> checkedInIds = new HashSet<>();

                    // Build set of IDs checked in on that date
                    if (checkIns != null) {
                        for (CheckInEvent event : checkIns) {
                            checkedInIds.add(event.clientId);
                        }
                    }

                    // Tell adapter which clients should be highlighted
                    adapter.setCheckedInClients(checkedInIds);
                });
    }

    /**
     * When a client row is clicked, open full details.
     */
    private void openClientInfo(ClientEntity client) {
        Toast.makeText(this, "Opening details for: " + client.name, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, ClientInfoActivity.class);
        intent.putExtra("client_id", client.id);
        startActivity(intent);
    }
}
