package com.example.mealconnect.ui.login.volunteer;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mealconnect.data.model.CheckInEvent;
import com.example.mealconnect.data.local.ClientEntity;
import com.example.mealconnect.data.local.ClientDao;
import com.example.mealconnect.service.FirebaseManager;
import com.example.mealconnect.domain.ScheduleManager;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VolunteerViewModel extends ViewModel {

    private static final String TAG = "VolunteerViewModel";

    private final FirebaseManager firebaseManager;
    private final ClientDao clientDao;
    private final ExecutorService executor;

    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    private final MutableLiveData<String> currentClientId = new MutableLiveData<>();
    private final MutableLiveData<ClientEntity> _scannedClientDetails = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _returnToScanMenu = new MutableLiveData<>();
    public LiveData<Boolean> returnToScanMenu = _returnToScanMenu;

    public LiveData<ClientEntity> getScannedClientDetails() {
        return _scannedClientDetails;
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public LiveData<String> getCurrentClientId() {
        return currentClientId;
    }

    public VolunteerViewModel(ClientDao clientDao) {
        this.firebaseManager = FirebaseManager.getInstance();
        this.clientDao = clientDao;
        this.executor = Executors.newSingleThreadExecutor();
    }


    // ----------------------------------------------------
    // QR CODE SCAN / ID LOOKUP
    // ----------------------------------------------------
    public void onQrCodeScanned(String clientId) {
        if (clientId == null || clientId.trim().isEmpty()) {
            statusMessage.setValue("Error: Invalid ID scanned.");
            _scannedClientDetails.setValue(null);
            return;
        }

        // Validate schedule
        if (!ScheduleManager.isPickupDay()) {
            statusMessage.setValue("Pickup FAILED: Today is NOT a scheduled pickup day.");
            _scannedClientDetails.setValue(null);
            return;
        }

        currentClientId.setValue(clientId);
        statusMessage.setValue("Scanning client: " + clientId + "...");

        executor.execute(() -> {
            try {
                ClientEntity client = clientDao.getClientByBarcodeId(clientId);

                if (client == null) {
                    _scannedClientDetails.postValue(null);
                    statusMessage.postValue("Client NOT found: " + clientId);
                    return;
                }

                // Prevent duplicate check-ins
                if (isAlreadyCheckedInToday(clientId)) {
                    _scannedClientDetails.postValue(null);
                    statusMessage.postValue(
                            "WARNING: " + client.name + " has ALREADY checked in today!"
                    );
                    return;
                }

                // Success
                _scannedClientDetails.postValue(client);
                statusMessage.postValue("Client found: " + client.name + ". Confirm check-in.");

            } catch (Exception e) {
                Log.e(TAG, "Database lookup failed", e);
                statusMessage.postValue("Error: Database access failed.");
                _scannedClientDetails.postValue(null);
            }
        });
    }

    public void resetReturnToScanMenu() {
        _returnToScanMenu.setValue(false);
    }

    // LOOKUP BY PHONE
    public void lookupClientByPhone(String phone) {
        executor.execute(() -> {
            ClientEntity client = clientDao.getClientByPhoneNumber(phone);

            if (client == null) {
                statusMessage.postValue("No client found with this phone number.");
                _scannedClientDetails.postValue(null);
                _returnToScanMenu.postValue(true);
                return;
            }

            // CHECK IF CLIENT ALREADY CHECKED IN TODAY
            if (isAlreadyCheckedInToday(client.clientId)) {
                statusMessage.postValue("WARNING: " + client.name + " has already picked up today.");

                // Reset UI – return to scanning options
                _scannedClientDetails.postValue(null);
                currentClientId.postValue(null);
                _returnToScanMenu.postValue(true);
                return;
            }

            //  VALID (NOT DUPLICATE) CLIENT
            _scannedClientDetails.postValue(client);
            currentClientId.postValue(client.clientId);
            statusMessage.postValue("Client found. Confirm handout.");
        });
    }




    // CHECK-IN LOGIC
    private boolean isAlreadyCheckedInToday(String clientId) {
        Calendar calendar = Calendar.getInstance();

        // Start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        // End of day
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endOfDay = calendar.getTimeInMillis();

        int count = clientDao.countCheckInsForClientToday(clientId, startOfDay, endOfDay);
        return count > 0;
    }


    public void recordCheckIn() {
        ClientEntity client = _scannedClientDetails.getValue();

        if (client == null) {
            statusMessage.setValue("Error: No client selected.");
            return;
        }

        String volunteerId = firebaseManager.getCurrentUserId();
        long timestamp = System.currentTimeMillis();

        CheckInEvent event = new CheckInEvent(
                client.clientId,
                volunteerId,
                timestamp
        );

        statusMessage.setValue("Recording check-in...");

        firebaseManager.saveCheckInEvent(event, new FirebaseManager.StatusCallback() {
            @Override
            public void onSuccess() {
                executor.execute(() -> {
                    try {
                        clientDao.insertCheckInEvent(event);
                        Log.i(TAG, "Local check-in saved.");
                    } catch (Exception e) {
                        Log.e(TAG, "Local save failed", e);
                    }
                });

                statusMessage.postValue("SUCCESS: " + client.name + " checked in.");
                Log.i(TAG, "Check-in complete for " + client.clientId);

                // Reset UI
                currentClientId.postValue(null);
                _scannedClientDetails.postValue(null);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Firestore check-in FAILED", e);
                statusMessage.postValue("ERROR: Could not record check-in.");
            }
        });
    }



    // CLEANUP

    @Override
    protected void onCleared() {
        super.onCleared();
        try {
            executor.shutdownNow();
        } catch (Exception ignored) { }
    }
}