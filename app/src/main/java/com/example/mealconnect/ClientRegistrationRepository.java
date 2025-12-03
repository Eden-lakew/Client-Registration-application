package com.example.mealconnect;

import android.util.Log;

import com.example.mealconnect.data.model.Client;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.mealconnect.data.Result;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Repository for managing client registration operations using Firestore.
 * It is dedicated to handling the asynchronous persistence of the Client data model to the cloud.
 */
public class ClientRegistrationRepository {
    private static final String TAG = "ClientRegRepo";

    // The Firestore collection where all client records will be stored
    private static final String CLIENTS_COLLECTION = "clients";

    private static volatile ClientRegistrationRepository instance;

    // Callback interface for asynchronous registration result
    public interface RegistrationCallback extends Consumer<Result<Boolean>> {}

    // Callback interface for duplicate name check
    public interface DuplicateCheckCallback {
        void onDuplicateFound(boolean isDuplicate);
        void onError(Exception e);
    }

    private final FirebaseFirestore db;

    private ClientRegistrationRepository() {
        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();
    }

    public static ClientRegistrationRepository getInstance() {
        if (instance == null) {
            synchronized (ClientRegistrationRepository.class) {
                if (instance == null) {
                    instance = new ClientRegistrationRepository();
                }
            }
        }
        return instance;
    }

    /**
     * Checks if a client with the same full name already exists in Firestore.
     * Name comparison is case-insensitive and trims whitespace.
     *
     * @param clientName The full name to check for duplicates
     * @param callback Callback to receive the result (true if duplicate found, false otherwise)
     */
    public void checkDuplicateName(String clientName, DuplicateCheckCallback callback) {
        if (clientName == null || clientName.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Client name cannot be null or empty"));
            return;
        }

        String normalizedName = clientName.trim().toLowerCase();

        // Query all clients and check names case-insensitively
        // Note: For better performance with large datasets, consider adding a normalizedName field to Firestore
        // For now, we limit to 1000 documents to prevent performance issues
        db.collection(CLIENTS_COLLECTION)
                .limit(1000)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean isDuplicate = false;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String existingName = document.getString("name");
                        if (existingName != null) {
                            String normalizedExistingName = existingName.trim().toLowerCase();
                            if (normalizedName.equals(normalizedExistingName)) {
                                isDuplicate = true;
                                Log.d(TAG, "Duplicate found: '" + clientName + "' matches existing client: '" + existingName + "'");
                                break;
                            }
                        }
                    }
                    Log.d(TAG, "Duplicate check for name '" + clientName + "': " + (isDuplicate ? "FOUND" : "NOT FOUND"));
                    callback.onDuplicateFound(isDuplicate);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for duplicate name", e);
                    callback.onError(e);
                });
    }

    /**
     * Checks if a client with the same phone number already exists in Firestore.
     * Phone number uniquely identifies a client. This method queries Firebase for existing phone numbers.
     *
     * @param phoneNumber The phone number to check for duplicates
     * @param callback Callback to receive the result (true if duplicate found, false otherwise)
     */
    public void checkDuplicatePhone(String phoneNumber, DuplicateCheckCallback callback) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Phone number cannot be null or empty"));
            return;
        }

        String trimmedPhone = phoneNumber.trim();

        // Query Firestore for clients with matching phone number (exact match)
        db.collection(CLIENTS_COLLECTION)
                .whereEqualTo("phone", trimmedPhone)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean isDuplicate = !queryDocumentSnapshots.isEmpty();
                    if (isDuplicate) {
                        Log.d(TAG, "Duplicate phone found: '" + phoneNumber + "'");
                    } else {
                        Log.d(TAG, "Phone number '" + phoneNumber + "' is available");
                    }
                    callback.onDuplicateFound(isDuplicate);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for duplicate phone", e);
                    callback.onError(e);
                });
    }

    /**
     * Registers a new client by saving their comprehensive data model to the Firestore 'clients' collection.
     * This operation is asynchronous.
     * @param newClient The Client object to save.
     * @param callback A callback to receive the asynchronous result (success or error).
     */
    public void registerClient(Client newClient, RegistrationCallback callback) {
        // Trim phone number before saving to remove leading/trailing whitespace
        if (newClient.getPhone() != null) {
            newClient.setPhone(newClient.getPhone().trim());
        }

        db.collection(CLIENTS_COLLECTION)
                .add(newClient)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Client registered successfully with ID: " + documentReference.getId());
                    // Pass true for success
                    callback.accept(new Result.Success<>(true));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Client registration failed.", e);
                    // Pass error result
                    // Note: Result.Error is a general wrapper for exceptions/failures
                    callback.accept(new Result.Error(new IOException("Database error: " + e.getMessage(), e)));
                });
    }
}