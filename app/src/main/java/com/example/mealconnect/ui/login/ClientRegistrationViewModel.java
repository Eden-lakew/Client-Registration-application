package com.example.mealconnect.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mealconnect.ClientRegistrationRepository;
import com.example.mealconnect.data.Result;
import com.example.mealconnect.data.model.Client;
import com.example.mealconnect.domain.ClientIdGenerator;

/**
 * ViewModel for handling client registration with async ID generation.
 */
public class ClientRegistrationViewModel extends ViewModel {

    private final MutableLiveData<Result<Boolean>> _registrationStatus = new MutableLiveData<>();
    public LiveData<Result<Boolean>> getRegistrationStatus() {
        return _registrationStatus;
    }

    private String lastRegisteredClientId;

    private final ClientRegistrationRepository repository;

    public ClientRegistrationViewModel() {
        this.repository = ClientRegistrationRepository.getInstance();
    }

    /**
     * Registers a new client after generating a unique ID.
     * First checks for duplicate full name, then duplicate phone number, then generates ID and registers.
     *
     * @param clientData Client info WITHOUT ID (yet)
     * @param siteCode   "VAN", "SURREY", etc.
     */
    public void registerClient(Client clientData, String siteCode) {
        _registrationStatus.setValue(null);

        // Step 1: Validate and check for duplicate full name
        String clientName = clientData.getName();
        if (clientName == null || clientName.trim().isEmpty()) {
            _registrationStatus.setValue(
                    new Result.Error(new Exception("Client name is required"))
            );
            return;
        }

        repository.checkDuplicateName(clientName, new ClientRegistrationRepository.DuplicateCheckCallback() {
            @Override
            public void onDuplicateFound(boolean isDuplicate) {
                if (isDuplicate) {
                    // Duplicate name found - return error
                    _registrationStatus.postValue(
                            new Result.Error(new Exception("A client with this name is already registered"))
                    );
                    return;
                }

                // No duplicate name - proceed to check phone number
                // Step 2: Check for duplicate phone number (phone uniquely identifies a client)
                String phoneNumber = clientData.getPhone();
                if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                    _registrationStatus.postValue(
                            new Result.Error(new Exception("Phone number is required"))
                    );
                    return;
                }

                repository.checkDuplicatePhone(phoneNumber, new ClientRegistrationRepository.DuplicateCheckCallback() {
                    @Override
                    public void onDuplicateFound(boolean isDuplicate) {
                        if (isDuplicate) {
                            // Duplicate phone found - return error
                            _registrationStatus.postValue(
                                    new Result.Error(new Exception("Client already registered"))
                            );
                            return;
                        }

                        // No duplicate phone - proceed with registration
                        // Step 3: Generate ID ASYNCHRONOUSLY
                        ClientIdGenerator.generate(siteCode, new ClientIdGenerator.IdCallback() {
                            @Override
                            public void onSuccess(String generatedId) {

                                // Save for UI and QR Code screen
                                lastRegisteredClientId = generatedId;

                                // Attach ID to client model
                                clientData.setClientId(generatedId);

                                // Step 4: Save the client to Firestore/Room via repository
                                repository.registerClient(clientData, result -> {
                                    if (result instanceof Result.Success) {
                                        _registrationStatus.postValue(
                                                new Result.Success<>(((Result.Success<Boolean>) result).getData())
                                        );
                                    } else if (result instanceof Result.Error) {
                                        _registrationStatus.postValue(
                                                new Result.Error(((Result.Error) result).getError())
                                        );
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                _registrationStatus.postValue(
                                        new Result.Error(new Exception("Failed to generate ID: " + e.getMessage()))
                                );
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        _registrationStatus.postValue(
                                new Result.Error(new Exception("Error checking for duplicate phone: " + e.getMessage()))
                        );
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                _registrationStatus.postValue(
                        new Result.Error(new Exception("Error checking for duplicate name: " + e.getMessage()))
                );
            }
        });
    }

    public String getLastRegisteredClientId() {
        return lastRegisteredClientId;
    }

    public void resetStatus() {
        _registrationStatus.setValue(null);
        lastRegisteredClientId = null;
    }
}
