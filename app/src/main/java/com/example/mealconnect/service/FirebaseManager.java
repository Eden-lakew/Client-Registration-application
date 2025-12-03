package com.example.mealconnect.service;

import android.util.Log;

import com.example.mealconnect.data.model.CheckInEvent;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Central Firebase manager (Auth, Firestore, Roles, Check-In Events)
 * Singleton pattern.
 */
public class FirebaseManager {

    private static final String TAG = "FirebaseManager";
    private static final String USERS_COLLECTION = "users";
    private static final String HANDOUT_EVENTS_COLLECTION = "handout_events";

    private static FirebaseManager instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    // ---------------------------------------------------------------------------------------------
    //  CALLBACK INTERFACES
    // ---------------------------------------------------------------------------------------------

    /** Auth success callback */
    public interface AuthSuccessCallback {
        void onSuccess(AuthResult result);
    }

    /** Auth failure callback */
    public interface AuthFailureCallback {
        void onFailure(Exception e);
    }

    /** User role callback */
    public interface RoleCallback {
        void onRoleFetched(String role);
        void onFailure(Exception e);
    }

    /** NEW: Firestore write status callback */
    public interface StatusCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    // ---------------------------------------------------------------------------------------------
    //  SINGLETON
    // ---------------------------------------------------------------------------------------------

    private FirebaseManager() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        Log.i(TAG, "FirebaseManager initialized (Singleton).");
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    // ---------------------------------------------------------------------------------------------
    //  AUTHENTICATION
    // ---------------------------------------------------------------------------------------------

    public void signInWithEmailAndPassword(String email, String password,
                                           AuthSuccessCallback successCallback,
                                           AuthFailureCallback failureCallback) {

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Log.d(TAG, "Sign-in OK: " + authResult.getUser().getEmail());
                    if (successCallback != null) successCallback.onSuccess(authResult);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Sign-in FAILED: " + e.getMessage());
                    if (failureCallback != null) failureCallback.onFailure(e);
                });
    }

    // ---------------------------------------------------------------------------------------------
    //  ROLE HANDLING
    // ---------------------------------------------------------------------------------------------

    public void getUserRole(String userId, RoleCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("User ID is null."));
            return;
        }

        DocumentReference ref = db.collection(USERS_COLLECTION).document(userId);

        ref.get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        callback.onRoleFetched(role != null ? role : "unknown");
                    } else {
                        callback.onRoleFetched("unknown");
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // ---------------------------------------------------------------------------------------------
    //  AUTH HELPERS
    // ---------------------------------------------------------------------------------------------

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public void signOut() {
        auth.signOut();
    }

    // ---------------------------------------------------------------------------------------------
    //  CHECK-IN EVENT WRITING
    // ---------------------------------------------------------------------------------------------

    /**
     * NEW OVERLOADED METHOD
     * Supports StatusCallback (used by VolunteerViewModel).
     */
    public void saveCheckInEvent(CheckInEvent event, StatusCallback callback) {

        db.collection(HANDOUT_EVENTS_COLLECTION)
                .document()
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Event saved (StatusCallback)");
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Event save FAILED: " + e.getMessage());
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * OLD VERSION — still works for backwards compatibility.
     * Do NOT delete; some screens still call this.
     */
    public void saveCheckInEvent(CheckInEvent event, Runnable onSuccess) {

        db.collection(HANDOUT_EVENTS_COLLECTION)
                .add(event)
                .addOnSuccessListener(doc -> {
                    Log.d(TAG, "Event saved (Runnable)");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Event save FAILED: " + e.getMessage()));
    }
}
