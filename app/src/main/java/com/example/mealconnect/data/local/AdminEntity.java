package com.example.mealconnect.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "admins")
public class AdminEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String username; // Used for Admin login

    @NonNull
    public String passwordHash; // Store password as a hash (e.g., SHA-256)

    @NonNull
    public String displayName;

    public AdminEntity(@NonNull String username, @NonNull String passwordHash, @NonNull String displayName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }
}