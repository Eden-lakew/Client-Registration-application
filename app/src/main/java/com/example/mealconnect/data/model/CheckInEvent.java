package com.example.mealconnect.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a single instance of a meal handout.
 * This object is saved locally in Room and remotely in Firebase Firestore.
 */
@Entity(tableName = "handout_events") // FIX 1: Marks this class as a Room database table
public class CheckInEvent {

    // FIX 2: Defines a unique, auto-generated primary key for the local table
    @PrimaryKey(autoGenerate = true)
    public int id;

    // Data fields for the event
    public String clientId;
    public String volunteerId;
    public long timestamp; // Time in milliseconds since epoch

    // Constructor required by Room/Firebase
    public CheckInEvent(String clientId, String volunteerId, long timestamp) {
        this.clientId = clientId;
        this.volunteerId = volunteerId;
        this.timestamp = timestamp;
    }

    // Default constructor required by some Firebase serialization
    public CheckInEvent() {
        // Required for Firestore/Room if used in combination
    }
}