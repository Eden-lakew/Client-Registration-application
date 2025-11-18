package com.example.mealconnect.data.model;

import com.google.gson.annotations.SerializedName;

public class HouseholdMember {
    @SerializedName("type")
    private String type; // e.g., "Adult" or "Child"

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("dateOfBirth")
    private String dateOfBirth; // Storing as String (YYYY-MM-DD)

    @SerializedName("allergyNotes")
    private String allergyNotes;

    @SerializedName("contactNumber")
    private String contactNumber;

    // Default Constructor
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

    // Getters and Setters
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