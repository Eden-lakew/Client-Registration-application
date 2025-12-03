package com.example.mealconnect.ui.login;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mealconnect.data.ClientListRepository;
import com.example.mealconnect.data.Result;
import com.example.mealconnect.data.model.Client;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * ViewModel for the ClientListActivity. Handles fetching and storing the real-time list of clients.
 */
public class ClientListViewModel extends ViewModel {

    private static final String TAG = "ClientListVM";
    private final ClientListRepository repository;

    // LiveData to hold the list of clients.
    private final MutableLiveData<List<Client>> _clientList = new MutableLiveData<>();
    public LiveData<List<Client>> getClientList() {
        return _clientList;
    }

    // LiveData to handle error messages.
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    // Holds the Firestore listener so we can detach it later.
    private ListenerRegistration clientListener;

    public ClientListViewModel() {
        this.repository = ClientListRepository.getInstance();
        startClientListListener();
    }

    /**
     * Initiates the real-time listener to fetch clients from Firestore.
     */
    public void startClientListListener() {
        // Check if a listener is already attached to prevent duplicates
        if (clientListener != null) {
            clientListener.remove();
        }

        // Use the repository to start listening
        clientListener = repository.startListeningForClients(result -> {
            if (result instanceof Result.Success) {
                // Cast the generic Result.Success to the specific type
                Result.Success<List<Client>> successResult = (Result.Success<List<Client>>) result;
                _clientList.setValue(successResult.getData());
                _errorMessage.setValue(null); // Clear any previous error
                Log.d(TAG, "Client list updated successfully. Count: " + successResult.getData().size());

            } else if (result instanceof Result.Error) {
                Result.Error<List<Client>> errorResult = (Result.Error<List<Client>>) result;
                String errorMsg = "Failed to fetch clients: " + errorResult.getError().getLocalizedMessage();
                Log.e(TAG, errorMsg, errorResult.getError());
                _errorMessage.setValue(errorMsg);
            }
        });
    }

    /**
     * Called when the ViewModel is destroyed. Ensures the Firestore listener is removed
     * to prevent memory leaks and unnecessary billing.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (clientListener != null) {
            clientListener.remove();
            Log.d(TAG, "Firestore client list listener removed.");
        }
    }
}