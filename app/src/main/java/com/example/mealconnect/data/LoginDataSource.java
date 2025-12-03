package com.example.mealconnect.data;

import com.example.mealconnect.data.model.LoggedInUser;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

        try {
            // --- NEW LOGIC: Check for Volunteer (email) or Admin (username) ---

            // 1. Volunteer Check (Volunteers use email, which contains '@')
            if (username.contains("@")) {

                // NOTE: This should eventually be replaced by a call to your SQLite database
                // to check the Volunteer table (6.4.2) for the email and password hash.

                // Temporary Mock Volunteer Credentials:
                if (username.equals("volunteer@mealconnect.org") && password.equals("scan123")) {
                    LoggedInUser volunteerUser =
                            new LoggedInUser(
                                    java.util.UUID.randomUUID().toString(),
                                    "Volunteer");
                    return new Result.Success<>(volunteerUser);
                }
            }

            // 2. Admin Check (Admin uses a simple username)
            // NOTE: This should eventually be replaced by a call to your SQLite database
            // to check the Admin table (6.4.1) for the username and password hash.

            // Temporary Mock Admin Credentials:
            if (username.equals("admin") && password.equals("admin")) {
                LoggedInUser adminUser =
                        new LoggedInUser(
                                java.util.UUID.randomUUID().toString(),
                                "Site Administrator");
                return new Result.Success<>(adminUser);
            }

            // 3. Authentication Failed: If neither Admin nor Volunteer credentials matched
            return new Result.Error(new IOException("Invalid credentials or user not found."));

        } catch (Exception e) {
            // Handle database connection errors or other critical exceptions
            return new Result.Error(new IOException("Error accessing user database", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}