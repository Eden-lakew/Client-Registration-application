package com.example.mealconnect.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "clients")
public class ClientEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String clientId;       // generated unique ID
    public String name;
    public String phone;
    public String address;
    public String firstLanguage;

    public int numAdults;
    public int numChildren;

    public boolean lowIncome;
    public boolean newcomer;
    public boolean senior;

    public String dietaryRestrictions;   // "Chicken,Beef,Turkey"
    public String authorizedPickup;
    public String siteCode;       // location (Surrey, Abbotsford, etc.)
    public String dateApplied;
}
