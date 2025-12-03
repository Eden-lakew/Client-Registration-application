package com.example.mealconnect.ui.login;

import androidx.annotation.Nullable;

/**
 * Authentication result : success (user details) or error message.
 */
public class LoginResult { // <<< FIXED: Added 'public' keyword
    @Nullable
    private LoggedInUserView success;
    @Nullable
    private Integer error;

    // You may want to make constructors public too, but package-private is often fine.
    LoginResult(@Nullable Integer error) {
        this.error = error;
    }

    LoginResult(@Nullable LoggedInUserView success) {
        this.success = success;
    }

    @Nullable
    public LoggedInUserView getSuccess() { // This method needs to be public
        return success;
    }

    @Nullable
    public Integer getError() { // This method needs to be public
        return error;
    }
}