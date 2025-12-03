package com.example.mealconnect.data;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.mealconnect.data.local.ClientDao;
import com.example.mealconnect.data.local.ClientDatabase;
import com.example.mealconnect.data.local.ClientEntity;
import com.example.mealconnect.data.model.CheckInEvent;
import com.example.mealconnect.domain.ClientIdGenerator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientRepository {

    private final ClientDao clientDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String TAG = "ClientRepository";

    public ClientRepository(Context context) {
        ClientDatabase db = ClientDatabase.getInstance(context);
        clientDao = db.clientDao();
    }

    /** Insert new client with generated clientId */
    public void addClient(ClientEntity client, String siteCode) {

        ClientIdGenerator.generate(siteCode, new ClientIdGenerator.IdCallback() {
            @Override
            public void onSuccess(String generatedId) {
                client.clientId = generatedId;

                executorService.execute(() -> {
                    clientDao.insertClient(client);
                    Log.i(TAG, "Client inserted with ID: " + generatedId);
                });
            }

            @Override
            public void onError(Exception e) {   // <-- FIXED
                Log.e(TAG, "Failed to generate client ID", e);

                // fallback
                client.clientId = siteCode + "-" + System.currentTimeMillis();

                executorService.execute(() -> {
                    clientDao.insertClient(client);
                    Log.w(TAG, "Client inserted with fallback ID");
                });
            }
        });
    }

    /** Get all clients */
    public LiveData<List<ClientEntity>> getAllClients() {
        return clientDao.getAllClients();
    }

    /** Filter by restriction */
    public LiveData<List<ClientEntity>> filterByRestriction(String restriction) {
        return clientDao.filterByRestriction(restriction);
    }

    /** Search clients */
    public LiveData<List<ClientEntity>> searchClients(String query) {
        return clientDao.searchClients(query);
    }

    /** Update */
    public void updateClient(ClientEntity client) {
        executorService.execute(() -> clientDao.updateClient(client));
    }

    /** Delete */
    public void deleteClient(ClientEntity client) {
        executorService.execute(() -> clientDao.deleteClient(client));
    }

    public LiveData<ClientEntity> getClientById(int id) {
        return clientDao.getClientById(id);
    }

    /** Get check-in events */
    public LiveData<List<CheckInEvent>> getCheckInsForDate(long start, long end) {
        return clientDao.getCheckInsForDate(start, end);
    }
}
