package com.example.mealconnect.data.local;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mealconnect.data.local.ClientEntity;

import java.util.List;

@Dao
public interface ClientDao {

    @Insert
    long insertClient(ClientEntity client);

    @Update
    void updateClient(ClientEntity client);

    @Delete
    void deleteClient(ClientEntity client);

    @Query("SELECT * FROM clients")
    LiveData<List<ClientEntity>> getAllClients();

    @Query("SELECT * FROM clients WHERE dietaryRestrictions LIKE '%' || :restriction || '%'")
    LiveData<List<ClientEntity>> filterByRestriction(String restriction);

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    LiveData<ClientEntity> getClientById(int id);

}
