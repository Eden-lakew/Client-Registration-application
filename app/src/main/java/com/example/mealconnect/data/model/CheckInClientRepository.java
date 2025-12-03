package com.example.mealconnect.data.model;

import android.util.Log;
import com.example.mealconnect.data.local.ClientDao;
import com.example.mealconnect.data.local.ClientEntity;
import com.example.mealconnect.data.model.CheckInEvent;
// CRITICAL FIX: You MUST change '[FIREBASE_MANAGER_PACKAGE]' below
// to the actual package path where FirebaseManager.java is located.
import com.example.mealconnect.service.FirebaseManager;

import java.util.concurrent.Executors;

/**
 * Repository for the volunteer check-in process.
 * Handles client lookup (ClientDao) and event recording (Room + Firestore via FirebaseManager).
 */
public class CheckInClientRepository {
    private static final String TAG = "CheckInClientRepository";

    private final ClientDao clientDao;
    private final FirebaseManager firebaseManager;
    private final java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface ClientLookupCallback {
        void onClientFound(ClientEntity client);
    }

    // Constructor now requires ClientDao (for Room) and FirebaseManager (for Cloud)
    public CheckInClientRepository(ClientDao clientDao, FirebaseManager firebaseManager) {
        this.clientDao = clientDao;
        this.firebaseManager = firebaseManager;
        Log.i(TAG, "CheckInClientRepository initialized with real DAO and FirebaseManager.");
    }

    /**
     * Finds a ClientEntity by their barcode ID (or client ID) using Room.
     */
    public void getClientByBarcodeId(String barcodeId, ClientLookupCallback callback) {
        // Execute REAL Room lookup on a background thread
        executor.execute(() -> {
            ClientEntity client = clientDao.getClientByBarcodeId(barcodeId);
            callback.onClientFound(client);
        });
    }

    /**
     * Records the check-in event (the WRITE transaction).
     */
    public void recordHandoutEvent(CheckInEvent event) {
        executor.execute(() -> {
            try {
                // 1. Local Write (Room)
                clientDao.insertCheckInEvent(event);
                Log.d(TAG, "Recording handout locally for " + event.clientId + " by " + event.volunteerId);

                // 2. Cloud Write (Firestore) - Calls the real Firebase Manager
                firebaseManager.saveCheckInEvent(event, () -> {
                    // Success callback from FirebaseManager, which logs the final sync message
                });

            } catch (Exception e) {
                Log.e(TAG, "Failed to record or sync handout event.", e);
            }
        });
    }
}