package com.example.mealconnect.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

// We need to import the entity for the event history
import com.example.mealconnect.data.model.CheckInEvent;

import java.util.List;

/**
 * ClientDao (Data Access Object)
 *
 * This interface defines the database operations
 * that Room will automatically generate for the "clients" table
 * and the event history table.
 */
@Dao
public interface ClientDao {

    // --- NEW REQUIRED METHODS FOR SCANNER/CHECK-IN ---

    /**
     * Retrieves a single client by their unique barcode or client ID.
     * This method is executed by the repository on a background thread.
     * It does NOT use LiveData.
     *
     * @param barcodeId The ID to look up (e.g., "TEST-SURREY-001").
     * @return The matching ClientEntity or null if not found.
     */
    @Query("SELECT * FROM clients WHERE clientId = :barcodeId LIMIT 1")
    ClientEntity getClientByBarcodeId(String barcodeId);

    /**
     * Checks if a client has already checked in within the specified time range (e.g., today).
     *
     * @param clientId The ID of the client.
     * @param startTimestamp The start of the current day (millis).
     * @param endTimestamp The end of the current day (millis).
     * @return The number of check-ins found (0 means not checked in).
     */
    @Query("SELECT COUNT(*) FROM handout_events WHERE clientId = :clientId AND timestamp BETWEEN :startTimestamp AND :endTimestamp")
    int countCheckInsForClientToday(String clientId, long startTimestamp, long endTimestamp);


    /**
     * Inserts a new CheckInEvent record into the local database history.
     * This is the "Local Write" step in the repository.
     *
     * @param event The event object containing client and volunteer IDs, and timestamp.
     */
    @Insert
    void insertCheckInEvent(CheckInEvent event);


    // --- EXISTING METHODS (UNCHANGED) ---

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

    /**
     * Search clients by name or phone number.
     * @param query The search query string.
     * @return A LiveData list of matching clients.
     */
    @Query("SELECT * FROM clients WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    LiveData<List<ClientEntity>> searchClients(String query);

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    LiveData<ClientEntity> getClientById(int id);

    @Query("SELECT * FROM clients WHERE phone = :phone LIMIT 1")
    ClientEntity getClientByPhoneNumber(String phone);


    @Query("SELECT COUNT(*) FROM clients")
    int countClients(); // Added this helper method which may be needed for seeding
    
    /**
     * Retrieves all CheckInEvents for a specific date range.
     * Useful for determining the status of clients for 'today'.
     *
     * @param startTimestamp The start of the day.
     * @param endTimestamp The end of the day.
     * @return A LiveData list of all check-ins for that day.
     */
    @Query("SELECT * FROM handout_events WHERE timestamp BETWEEN :startTimestamp AND :endTimestamp")
    LiveData<List<CheckInEvent>> getCheckInsForDate(long startTimestamp, long endTimestamp);
}
