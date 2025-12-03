package com.example.mealconnect.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "volunteers")
public class VolunteerEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String email; // Used for Volunteer login

    @NonNull
    public String passwordHash; // Store password as a hash

    @NonNull
    public String displayName;

    public VolunteerEntity(@NonNull String email, @NonNull String passwordHash, @NonNull String displayName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }
}