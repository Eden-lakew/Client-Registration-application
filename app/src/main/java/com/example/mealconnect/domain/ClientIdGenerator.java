package com.example.mealconnect.domain;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Generates sequential client IDs:
 * Format: SITECODE-001, SITECODE-002, ...
 * Uses Firestore transactions for atomic increments.
 */
public class ClientIdGenerator {

    private static final String TAG = "ClientIdGenerator";
    private static final String COUNTERS_COLLECTION = "site_counters";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Callback for returning the generated ID.
     */
    public interface IdCallback {
        void onSuccess(String id);   // <-- Unified naming
        void onError(Exception e);
    }

    /**
     * Generate a sequential ID for a site (e.g., VAN → VAN-001)
     */
    public static void generate(String siteCode, IdCallback callback) {
        final String code = siteCode.toUpperCase(Locale.US);

        db.runTransaction((Transaction.Function<Long>) transaction -> {

            DocumentSnapshot snapshot = transaction.get(
                    db.collection(COUNTERS_COLLECTION).document(code)
            );

            long current = 0;

            if (snapshot.exists()) {
                Number val = snapshot.getLong("current_count");
                if (val != null) current = val.longValue();
            }

            long next = current + 1;

            Map<String, Object> data = new HashMap<>();
            data.put("current_count", next);

            transaction.set(
                    db.collection(COUNTERS_COLLECTION).document(code),
                    data
            );

            return next;

        }).addOnSuccessListener(newNumber -> {
            String id = String.format(Locale.US, "%s-%03d", code, newNumber);
            Log.d(TAG, "Generated ID: " + id);
            callback.onSuccess(id);   // <-- Correct name

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Sequential ID failed, using fallback", e);

            String fallback = code + "-" + System.currentTimeMillis();
            callback.onError(e);       // <-- Correct name
        });
    }
}
