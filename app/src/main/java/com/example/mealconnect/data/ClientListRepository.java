package com.example.mealconnect.data;

import android.util.Log;

import com.example.mealconnect.data.model.Client; // Ensure you have this Client model class
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Repository responsible for fetching the real-time list of all clients from Firestore.
 * It uses a real-time listener (onSnapshot) to automatically update the client list
 * whenever a new client is registered or an existing record is modified.
 */
public class ClientListRepository {
    private static final String TAG = "ClientListRepo";
    // Collection name must match the one used during registration
    private static final String CLIENTS_COLLECTION = "clients";

    private static volatile ClientListRepository instance;
    private final FirebaseFirestore db;

    // A callback interface that handles the asynchronous delivery of the client list.
    public interface ClientListCallback extends Consumer<Result<List<Client>>> {}

    private ClientListRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static ClientListRepository getInstance() {
        if (instance == null) {
            synchronized (ClientListRepository.class) {
                if (instance == null) {
                    instance = new ClientListRepository();
                }
            }
        }
        return instance;
    }

    /**
     * Starts listening for real-time updates on the 'clients' collection.
     * This is an efficient way to keep the list synchronized with the database.
     * * @param callback The callback to deliver the list of clients or an error.
     * @return A ListenerRegistration object to allow the caller (ViewModel) to stop the listener later.
     */
    public ListenerRegistration startListeningForClients(ClientListCallback callback) {

        // This query fetches all documents in the 'clients' collection
        return db.collection(CLIENTS_COLLECTION)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Client list listen failed.", error);
                        // Send the error back to the ViewModel
                        callback.accept(new Result.Error(error));
                        return;
                    }

                    if (value == null) {
                        Log.d(TAG, "Snapshot value is null.");
                        return;
                    }

                    List<Client> clientList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {
                        try {
                            // Convert each Firestore document into a Client object
                            // NOTE: The field names in the Client.java class must match
                            // the keys in the Firestore document exactly.
                            Client client = doc.toObject(Client.class);
                            clientList.add(client);
                        } catch (Exception e) {
                            Log.e(TAG, "Error deserializing client document: " + doc.getId(), e);
                            // Send a failure if the data structure is corrupt
                            callback.accept(new Result.Error(e));
                            return;
                        }
                    }

                    Log.d(TAG, "Successfully fetched " + clientList.size() + " clients.");
                    // Send the list back to the ViewModel
                    callback.accept(new Result.Success<>(clientList));
                });
    }
}