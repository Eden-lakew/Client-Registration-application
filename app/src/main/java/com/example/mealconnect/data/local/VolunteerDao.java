package com.example.mealconnect.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.annotation.Nullable;

@Dao
public interface VolunteerDao {

    // Query 6.4.2: Check Volunteer Credentials
    @Query("SELECT * FROM volunteers WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    @Nullable
    VolunteerEntity getVolunteerByCredentials(String email, String passwordHash);

    // Optional: Get Volunteer by email
    @Query("SELECT * FROM volunteers WHERE email = :email LIMIT 1")
    @Nullable
    VolunteerEntity getVolunteerByEmail(String email);

    // Optional: Simple insert for initial setup
    @androidx.room.Insert
    long insertVolunteer(VolunteerEntity volunteer);
}