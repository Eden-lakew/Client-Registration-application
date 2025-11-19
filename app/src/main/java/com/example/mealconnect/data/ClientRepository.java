package com.example.mealconnect.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.mealconnect.data.local.ClientDao;
import com.example.mealconnect.data.local.ClientDatabase;
import com.example.mealconnect.data.local.ClientEntity;
import com.example.mealconnect.domain.ClientIdGenerator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientRepository {

    private final ClientDao clientDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ClientRepository(Context context) {
        ClientDatabase db = ClientDatabase.getInstance(context);
        clientDao = db.clientDao();
    }

    /** Insert a new client */
    public void addClient(ClientEntity client, String siteCode) {
        executorService.execute(() -> {
            // generate unique ID
            client.clientId = ClientIdGenerator.generate(siteCode);
            clientDao.insertClient(client);
        });
    }

    /** Get all clients */
    public LiveData<List<ClientEntity>> getAllClients() {
        return clientDao.getAllClients();
    }

    /** Filter clients by dietary restriction */
    public LiveData<List<ClientEntity>> filterByRestriction(String restriction) {
        return clientDao.filterByRestriction(restriction);
    }

    /** Update client */
    public void updateClient(ClientEntity client) {
        executorService.execute(() -> {
            clientDao.updateClient(client);
        });
    }

    /** Delete client */
    public void deleteClient(ClientEntity client) {
        executorService.execute(() -> {
            clientDao.deleteClient(client);
        });


    }
    public LiveData<ClientEntity> getClientById(int id) {
        return clientDao.getClientById(id);
    }

}
