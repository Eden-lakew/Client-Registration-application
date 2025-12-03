package com.example.mealconnect.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.annotation.Nullable;

@Dao
public interface AdminDao {

    // Query 6.4.1: Check Admin Credentials
    @Query("SELECT * FROM admins WHERE username = :username AND passwordHash = :passwordHash LIMIT 1")
    @Nullable
    AdminEntity getAdminByCredentials(String username, String passwordHash);

    // Optional: Get Admin by username (for fetching user data after successful login)
    @Query("SELECT * FROM admins WHERE username = :username LIMIT 1")
    @Nullable
    AdminEntity getAdminByUsername(String username);

    // Optional: Simple insert for initial setup
    @androidx.room.Insert
    long insertAdmin(AdminEntity admin);
}