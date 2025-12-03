package com.example.mealconnect.ui.login.volunteer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.mealconnect.data.local.ClientDatabase;
import com.example.mealconnect.data.local.ClientDao;

import com.example.mealconnect.R;
// CRITICAL FIX: Changed import from the old CheckInClient model to the real Room Entity
import com.example.mealconnect.data.local.ClientEntity;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerActivity extends AppCompatActivity {

    private static final String TAG = "ScannerActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    // UI Elements
    private PreviewView previewView;
    private TextView resultTextView;
    private Button manualEntryButton;
    private Button startScanButton;
    private Button stopScanButton;

    // Manual Entry UI Components
    private LinearLayout manualEntryContainer;
    private EditText manualIdInput;
    private Button confirmIdButton;

    // NEW: Client Confirmation UI
    private LinearLayout clientConfirmationContainer;
    private TextView clientDetailsTextView;
    private Button confirmHandoutButton;

    // Logout Button
    private Button logoutButton;

    // ViewModel and Executors
    private VolunteerViewModel viewModel;
    private ExecutorService cameraExecutor;
    private ExecutorService cameraSetupExecutor;
    private ProcessCameraProvider cameraProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        // Initialize ViewModel with Factory
        ClientDao clientDao = ClientDatabase.getInstance(getApplicationContext()).clientDao();
        VolunteerViewModelFactory factory = new VolunteerViewModelFactory(clientDao);
        viewModel = new ViewModelProvider(this, factory).get(VolunteerViewModel.class);

        // Initialize Views
        previewView = findViewById(R.id.camera_preview_view);
        resultTextView = findViewById(R.id.result_text_view);
        manualEntryButton = findViewById(R.id.manual_entry_button);
        startScanButton = findViewById(R.id.start_scan_button);
        stopScanButton = findViewById(R.id.stop_scan_button);

        // Initialize Manual Entry Views
        manualEntryContainer = findViewById(R.id.manual_entry_container);
        manualIdInput = findViewById(R.id.manual_id_input);
        confirmIdButton = findViewById(R.id.confirm_id_button);

        // NEW: Initialize Client Confirmation Views
        clientConfirmationContainer = findViewById(R.id.client_confirmation_container);
        clientDetailsTextView = findViewById(R.id.client_details_text_view);
        confirmHandoutButton = findViewById(R.id.confirm_handout_button);

        // Initialize Logout Button
        logoutButton = findViewById(R.id.logout_button);

        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraSetupExecutor = Executors.newSingleThreadExecutor();

        // 1. Setup initial UI state
        setupInitialUiState();

        // 2. Set up click listeners
        startScanButton.setOnClickListener(v -> handleStartScanClicked());
        stopScanButton.setOnClickListener(v -> handleStopScanClicked());
        manualEntryButton.setOnClickListener(v -> handleManualEntryClicked());
        confirmIdButton.setOnClickListener(v -> handleConfirmIdClicked());
        // NEW: Confirmation Button Listener
        confirmHandoutButton.setOnClickListener(v -> handleConfirmHandoutClicked());

        // Logout Button Listener
        logoutButton.setOnClickListener(v -> handleLogout());

        // 3. Setup LiveData observers
        setupViewModelObservers();

        // 4. Request permissions if not granted
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE
            );
        }

        // 5. Enable Back Button (Note: Action bar may not be visible due to NoActionBar theme)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Client Scanner");
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
        // Action bar back button: sign out and return to login screen (same as logout)
        handleLogout();
        return true;
    }

    /**
     * Sets up observers for the VolunteerViewModel's LiveData.
     */
    private void setupViewModelObservers() {
        // Observe status messages
        viewModel.getStatusMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String status) {
                resultTextView.setText(status);

                // If the status indicates success, we reset the UI to initial state after a delay
                // FIX: Check for "SUCCESS:" prefix to match VolunteerViewModel's status message
                if (status != null && status.startsWith("SUCCESS:")) {
                    // Use the main looper for scheduling UI state reset after the success message
                    new android.os.Handler(Looper.getMainLooper()).postDelayed(
                            ScannerActivity.this::setupInitialUiState,
                            3500
                    ); // 3.5 seconds delay
                }
            }
        });

        // Use the public getter method to observe the client details LiveData.
        viewModel.getScannedClientDetails().observe(this, new Observer<ClientEntity>() {
            @Override
            public void onChanged(ClientEntity client) {
                if (client != null) {
                    // Client Found: Display details and show confirmation button

                    //  Access fields directly (.name, .clientId) from ClientEntity
                    String details = "Client: " + client.name + "\n" +
                            "ID: " + client.clientId + "\n" +
                            "Adults: " + client.numAdults + ", Children: " + client.numChildren;

                    clientDetailsTextView.setText(details);

                    // Show confirmation UI, hide manual input (if visible)
                    manualEntryContainer.setVisibility(View.GONE);
                    clientConfirmationContainer.setVisibility(View.VISIBLE);

                    // If the camera was running (QR scan), stop it now
                    stopCamera(null);

                } else {
                    // Client is null (either not found, or reset after successful check-in)
                    // Hide the confirmation UI
                    clientConfirmationContainer.setVisibility(View.GONE);
                    // The ViewModel's status observer will handle displaying error/ready messages.
                }
            }
        });

        // Observe returnToScanMenu flag to reset UI after duplicate check-in
        viewModel.returnToScanMenu.observe(this, shouldReturn -> {
            if (Boolean.TRUE.equals(shouldReturn)) {
                // Delay before resetting the UI, e.g., 3.5 seconds
                new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                    setupInitialUiState(); // Show QR scan / manual ID UI
                    viewModel.resetReturnToScanMenu(); // Reset the flag in ViewModel
                }, 3500); // 3500ms delay
            }
        });


    }


    /**
     * Sets the UI state to the initial (stopped) mode, ready for scanning or manual entry.
     */
    private void setupInitialUiState() {
        previewView.setVisibility(View.GONE);
        startScanButton.setVisibility(View.VISIBLE);
        stopScanButton.setVisibility(View.GONE);
        manualEntryContainer.setVisibility(View.GONE);
        clientConfirmationContainer.setVisibility(View.GONE); // NEW: Hide confirmation UI
        manualEntryButton.setVisibility(View.VISIBLE);

        resultTextView.setText("Press 'Start Scanning' or 'Manual ID Entry'.");
    }

    private void handleStartScanClicked() {
        if (allPermissionsGranted()) {
            // UI Change: Hide Start, Show Stop, Hide Manual Entry UI
            startScanButton.setVisibility(View.GONE);
            stopScanButton.setVisibility(View.VISIBLE);
            manualEntryButton.setVisibility(View.GONE);
            manualEntryContainer.setVisibility(View.GONE);
            clientConfirmationContainer.setVisibility(View.GONE); // Ensure confirmation UI is hidden

            startCamera();
        } else {
            Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void handleStopScanClicked() {
        // UI Change: Show Start, Hide Stop
        setupInitialUiState(); // Simpler to use the initial setup for stop
        // Stop camera
        stopCamera(() -> Log.d(TAG, "Camera manually stopped."));
    }

    private void handleManualEntryClicked() {
        // 1. If camera is running, stop it
        if (stopScanButton.getVisibility() == View.VISIBLE) {
            stopCamera(null);
        }

        // 2. Hide camera controls and the Manual Entry toggle button
        startScanButton.setVisibility(View.GONE);
        stopScanButton.setVisibility(View.GONE);
        manualEntryButton.setVisibility(View.GONE);
        clientConfirmationContainer.setVisibility(View.GONE); // Ensure confirmation UI is hidden

        resultTextView.setText("Type the Client phone number below.");

        // 3. Show manual entry input
        manualIdInput.setText("");
        manualEntryContainer.setVisibility(View.VISIBLE);
        manualIdInput.requestFocus();
    }
    private void processClientPhoneLookup(String phone) {
        viewModel.lookupClientByPhone(phone);
    }


    private void handleConfirmIdClicked() {
        String phone = manualIdInput.getText().toString().trim();

        if (phone.isEmpty()) {
            Toast.makeText(this, "Please enter a client phone number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Hide manual entry UI
        manualEntryContainer.setVisibility(View.GONE);

        // 2. Process the result (Initiate lookup)
        processClientPhoneLookup(phone);
    }

    /**
     * Handles the click of the "Confirm Handout" button after client details are displayed.
     */
    private void handleConfirmHandoutClicked() {
        // FIX: Correct ViewModel method name from confirmFoodHandout to recordCheckIn
        viewModel.recordCheckIn();
        // The ViewModel will update the status LiveData, which updates resultTextView.
        // The ViewModel also sets scannedClientDetails to null, which hides the confirmation UI.
    }

    // --- Core Logic for ViewModel Interaction ---

    /**
     * Handles the scanned or manually entered result.
     * It ensures the camera is stopped and then initiates the lookup process via ViewModel.
     */
    private void handleScanResult(String clientID) {
        boolean wasScanning = stopScanButton.getVisibility() == View.VISIBLE;

        // Step 1: Stop camera if it was running (QR scan case)
        if (wasScanning) {
            // Stop the camera first, then process the ID using a callback
            stopCamera(() -> processClientCheckIn(clientID));
        } else {
            // Manual entry case: camera is already stopped, proceed directly
            processClientCheckIn(clientID);
        }
    }

    /**
     * Sends the Client ID to the ViewModel for processing (lookup).
     */
    private void processClientCheckIn(String clientID) {
        // ViewModel handles the lookup and updates the LiveData.
        // FIX: Correct ViewModel method name from processClientId to onQrCodeScanned
        viewModel.onQrCodeScanned(clientID);
        // The UI will be updated by the LiveData observers.
    }


    // --- CameraX Setup and Permissions (Mostly Unchanged) ---
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                Toast.makeText(this, "Permission granted. Press 'Start Scanning' to begin.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Camera permission required for scanning.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        Log.d(TAG, "Attempting to start camera...");
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();

                runOnUiThread(() -> {
                    previewView.setVisibility(View.VISIBLE);
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
                });

                CameraSelector baseSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                final CameraSelector finalCameraSelector;

                if (!cameraProvider.hasCamera(baseSelector)) {
                    Log.w(TAG, "Back camera not found. Falling back to front camera.");
                    CameraSelector frontSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build();
                    if (cameraProvider.hasCamera(frontSelector)) {
                        finalCameraSelector = frontSelector;
                    } else {
                        Log.e(TAG, "Neither front nor back camera is available.");
                        return;
                    }
                } else {
                    finalCameraSelector = baseSelector;
                }

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build();
                BarcodeScanner scanner = BarcodeScanning.getClient(options);

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    processImageProxy(scanner, imageProxy);
                });

                runOnUiThread(() -> {
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(
                            (LifecycleOwner) this,
                            finalCameraSelector,
                            preview,
                            imageAnalysis
                    );
                    Log.d(TAG, "Camera use cases successfully bound and streaming.");
                });

            } catch (Exception exc) {
                Log.e(TAG, "Use case binding FAILED during START attempt!", exc);
                runOnUiThread(() -> previewView.setVisibility(View.GONE));
            }
        }, cameraSetupExecutor);
    }

    private void processImageProxy(BarcodeScanner scanner, ImageProxy imageProxy) {
        @SuppressWarnings("UnsafeOptInUsageError")
        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodes.isEmpty()) {
                        String rawValue = barcodes.get(0).getRawValue();
                        if (rawValue != null) {
                            // Only proceed if confirmation UI is NOT visible, preventing redundant scans
                            if (clientConfirmationContainer.getVisibility() != View.VISIBLE) {
                                runOnUiThread(() -> handleScanResult(rawValue));
                            } else {
                                Log.d(TAG, "Scan ignored: Client confirmation is already active.");
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Barcode scanning failed", e);
                })
                .addOnCompleteListener(task -> {
                    imageProxy.close();
                });
    }

    private void stopCamera(Runnable onStopComplete) {
        if (cameraProvider == null) {
            if (onStopComplete != null) onStopComplete.run();
            return;
        }

        runOnUiThread(() -> {
            cameraProvider.unbindAll();
            previewView.setVisibility(View.GONE);
            Log.d(TAG, "Camera stopped asynchronously. Running callback.");

            if (onStopComplete != null) {
                onStopComplete.run();
            }
        });
    }

    /**
     * Handles logout action - signs out and returns to login screen.
     */
    private void handleLogout() {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(ScannerActivity.this, com.example.mealconnect.ui.login.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        cameraSetupExecutor.shutdown();
    }
}