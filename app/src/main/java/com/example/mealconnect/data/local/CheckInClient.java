package com.example.mealconnect.data.local;

/**
 * Data class representing the minimum client record needed for the volunteer check-in process.
 * This version defines public getter methods to resolve the 'red line' errors in the ViewModel.
 */
public class CheckInClient {
    // Private final fields
    private final String clientId;
    private final String name;

    public CheckInClient(String clientId, String name) {
        this.clientId = clientId;
        this.name = name;
    }

    // Public Getters: These methods resolve the errors in VolunteerViewModel
    public String getClientId() {
        return clientId;
    }

    public String getName() {
        return name;
    }

    // Example factory method for a dummy client
    public static CheckInClient createDummyClient(String id) {
        return new CheckInClient(id, "Volunteer Client " + id.substring(id.length() - 4));
    }
}