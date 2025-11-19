package com.example.mealconnect.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mealconnect.data.local.ClientEntity;

import java.util.List;

/**
 * ClientDao (Data Access Object)
 *
 * This interface defines the database operations
 * that Room will automatically generate for the "clients" table.
 *
 * LiveData is used so that UI updates automatically whenever the data changes.
 */
@Dao
public interface ClientDao {

    /**
     * Insert a new client into the database.
     * Room returns the row ID of the newly inserted client.
     *
     * @param client - the client object to insert
     * @return the generated primary key (long id)
     */
    @Insert
    long insertClient(ClientEntity client);

    /**
     * Update an existing client's information.
     * Room uses the primary key in ClientEntity (id) to find the row to update.
     *
     * @param client - a client with updated field values
     */
    @Update
    void updateClient(ClientEntity client);

    /**
     * Delete a client from the database.
     *
     * @param client - the exact row to delete (must contain a valid id)
     */
    @Delete
    void deleteClient(ClientEntity client);

    /**
     * Retrieve ALL clients from the database.
     * Returned as LiveData so the UI updates automatically.
     *
     * @return LiveData list of all clients
     */
    @Query("SELECT * FROM clients")
    LiveData<List<ClientEntity>> getAllClients();

    /**
     * Filter clients by dietary restriction.
     * Example: "Chicken", "Beef", "Pork"
     * Uses SQL LIKE to match partial text.
     *
     * @param restriction - the restriction text to search for
     * @return LiveData list of matching clients
     */
    @Query("SELECT * FROM clients WHERE dietaryRestrictions LIKE '%' || :restriction || '%'")
    LiveData<List<ClientEntity>> filterByRestriction(String restriction);

    /**
     * Get a SINGLE client based on their internal ID (primary key).
     * LIMIT 1 ensures only one result is returned.
     *
     * @param id - the primary key of the client
     * @return LiveData containing the client record
     */
    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    LiveData<ClientEntity> getClientById(int id);

}
