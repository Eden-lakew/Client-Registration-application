package com.example.mealconnect.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.List; // Requires TypeConverter for List


@Entity(tableName = "clients")
public class ClientEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String clientId;       // generated unique ID matches Firestore
    public String name;
    public String phone;
    public String address;
    public String firstLanguage;
    public String authorizedPickup;

    public int numAdults;
    public int numChildren;

    public boolean isLowIncome; // Renamed from lowIncome to match Client.java
    public boolean isNewcomer;  // Renamed from newcomer to match Client.java
    public boolean isSenior;    // Renamed from senior to match Client.java
    public boolean lowIncome;
    public boolean newcomer;
    public boolean senior;

    // Storing as a simple comma-separated string for simplicity in Room without complex converters
    public String dietaryRestrictions;   
    
    // Not storing complex List<HouseholdMember> locally for now to keep Room simple
    // unless needed for offline viewing details. 
    
    public String siteCode;       
    public String dateApplied;
}
