package com.example.mealconnect.data.model;

/**
 * Data model for a single household member, nested within the main Client object.
 * Designed for standard Firestore serialization, matching the required field names.
 */
public class HouseholdMember {
    private String type; // e.g., "Adult" or "Child"
    private String firstName;
    private String lastName;
    private String dateOfBirth; // Storing as String (YYYY-MM-DD)
    private String allergyNotes;
    private String contactNumber;

    // Required public no-argument constructor for Firestore deserialization
    public HouseholdMember() {}

    // Parameterized Constructor
    public HouseholdMember(String type, String firstName, String lastName, String dateOfBirth, String allergyNotes, String contactNumber) {
        this.type = type;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.allergyNotes = allergyNotes;
        this.contactNumber = contactNumber;
    }

    // Getters and Setters (Required for Firestore serialization)

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAllergyNotes() { return allergyNotes; }
    public void setAllergyNotes(String allergyNotes) { this.allergyNotes = allergyNotes; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
}