    package com.example.mealconnect.data;

    import android.util.Log;

    import androidx.lifecycle.LiveData;
    import androidx.lifecycle.MutableLiveData;

    import com.example.mealconnect.R;
    import com.example.mealconnect.data.model.LoggedInUser;
    import com.example.mealconnect.service.FirebaseManager;
    import com.example.mealconnect.data.local.VolunteerDao;

    import java.io.IOException;
    import java.util.concurrent.Executors;
    import java.util.concurrent.ExecutorService;

    /**
     * Handles the asynchronous login process for Admin (via Firebase/Firestore) and Volunteer.
     * This repository uses LiveData to communicate the final authentication status back to the
     * ViewModel, preventing UI race conditions with Firebase.
     */
    public class LoginRepository {

        private static final String TAG = "LoginRepository";
        private static volatile LoginRepository instance;

        private final LoginDataSource dataSource;
        private final FirebaseManager firebaseManager;
        private final VolunteerDao volunteerDao;
        private final ExecutorService executor;

        // Dedicated Firebase credentials for the Admin (Must match Firebase Console)
        private static final String FIREBASE_ADMIN_EMAIL = "admin@mealconnect.org";
        private static final String FIREBASE_ADMIN_PASSWORD = "adminadmin";

        private final MutableLiveData<Result<LoggedInUser>> asyncLoginResult = new MutableLiveData<>();

        private LoggedInUser user = null;

        private LoginRepository(LoginDataSource dataSource, VolunteerDao volunteerDao) {
            this.dataSource = dataSource;
            this.volunteerDao = volunteerDao;
            this.firebaseManager = FirebaseManager.getInstance();
            this.executor = Executors.newSingleThreadExecutor();
        }

        public static LoginRepository getInstance(LoginDataSource dataSource, VolunteerDao volunteerDao) {
            if (instance == null) {
                instance = new LoginRepository(dataSource, volunteerDao);
            }
            return instance;
        }

        public LiveData<Result<LoggedInUser>> getAsyncLoginResult() {
            return asyncLoginResult;
        }

        public void logout() {
            user = null;
            firebaseManager.signOut();
        }

        private void setLoggedInUser(LoggedInUser user) {
            this.user = user;
        }

        /**
         * General login method. Supports both hardcoded Admin login AND generic Volunteer login.
         *
         * Logic:
         * 1. Check if it matches the hardcoded Admin username ("admin").
         * 2. If NOT admin, assume it's a Volunteer trying to sign in with email/password directly against Firebase.
         */
        public void login(String username, String password) {

            // --- PATH 1: HARDCODED ADMIN LOGIN ---
            if (username.equalsIgnoreCase("admin")) {
                // Use the hardcoded Firebase credentials for Admin
                firebaseManager.signInWithEmailAndPassword(FIREBASE_ADMIN_EMAIL, FIREBASE_ADMIN_PASSWORD,
                        authResult -> {
                            String uid = firebaseManager.getCurrentUserId();
                            if (uid != null) {
                                // Check role to be sure
                                checkAdminRoleAndCompleteLogin(uid, new LoggedInUser(uid, "Site Administrator"));
                            } else {
                                asyncLoginResult.setValue(new Result.Error(new IOException(String.valueOf(R.string.login_failed))));
                            }
                        },
                        e -> {
                            Log.e(TAG, "Admin Firebase sign-in failed: " + e.getMessage());
                            asyncLoginResult.setValue(new Result.Error(new IOException(String.valueOf(R.string.login_failed))));
                        }
                );
                return;
            }

            // --- PATH 2: GENERIC VOLUNTEER LOGIN (FIREBASE DIRECT) ---
            // Any other username is treated as an email for a volunteer
            firebaseManager.signInWithEmailAndPassword(username, password,
                    authResult -> {
                        // Login Success! Now verify they are actually a volunteer.
                        String uid = authResult.getUser().getUid();
                        checkVolunteerRoleAndCompleteLogin(uid, new LoggedInUser(uid, "Volunteer"));
                    },
                    e -> {
                        Log.e(TAG, "Volunteer Firebase sign-in failed: " + e.getMessage());
                        // Pass the specific error message if possible, or generic
                        asyncLoginResult.postValue(new Result.Error(e));
                    });
        }

        /**
         * Final step for Admin: Checks the Firestore role to ensure access is granted.
         */
        private void checkAdminRoleAndCompleteLogin(String uid, LoggedInUser mockUser) {
            firebaseManager.getUserRole(uid, new FirebaseManager.RoleCallback() {
                @Override
                public void onRoleFetched(String role) {
                    if ("admin".equals(role.trim().toLowerCase())) {
                        setLoggedInUser(mockUser);
                        asyncLoginResult.setValue(new Result.Success<>(mockUser));
                    } else {
                        firebaseManager.signOut(); // Access denied
                        asyncLoginResult.setValue(new Result.Error(new IOException("Access denied: User is not an Admin.")));
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    firebaseManager.signOut();
                    Log.e(TAG, "Role check failed: " + e.getMessage());
                    asyncLoginResult.setValue(new Result.Error(new IOException(String.valueOf(R.string.login_failed))));
                }
            });
        }

        /**
         * Final step for Volunteer: Checks the Firestore role to ensure access is granted.
         */
        private void checkVolunteerRoleAndCompleteLogin(String uid, LoggedInUser mockUser) {
            firebaseManager.getUserRole(uid, new FirebaseManager.RoleCallback() {
                @Override
                public void onRoleFetched(String role) {
                    String r = role.trim().toLowerCase();
                    // Allow "volunteer" OR "admin" (admins can act as volunteers for testing)
                    if ("volunteer".equals(r) || "admin".equals(r)) {
                        setLoggedInUser(mockUser);
                        asyncLoginResult.setValue(new Result.Success<>(mockUser));
                    } else {
                        firebaseManager.signOut();
                        asyncLoginResult.setValue(new Result.Error(new IOException("Access denied: User is not a Volunteer.")));
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    firebaseManager.signOut();
                    asyncLoginResult.setValue(new Result.Error(new IOException("Role check failed.")));
                }
            });
        }
    }
