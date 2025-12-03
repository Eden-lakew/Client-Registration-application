package com.example.mealconnect.ui.login;

import android.app.Application;
import android.content.Context;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.example.mealconnect.data.LoginDataSource;
import com.example.mealconnect.data.LoginRepository;
import com.example.mealconnect.data.local.VolunteerDao;
import com.example.mealconnect.data.local.VolunteerEntity;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-default constructor.
 */
public class LoginViewModelFactory implements ViewModelProvider.Factory {

    private final Application applicationContext;

    public LoginViewModelFactory(Context context) {
        this.applicationContext = (Application) context.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {

            LoginDataSource dataSource = new LoginDataSource();

            // --- 2. Create the VolunteerDao dependency (CRITICAL) ---
            VolunteerDao dummyVolunteerDao = createDummyVolunteerDao();

            // --- 3. Instantiate the Repository with ALL dependencies ---
            LoginRepository repository = LoginRepository.getInstance(dataSource, dummyVolunteerDao);

            // --- 4. Return the ViewModel, passing Application Context and Repository ---
            return (T) new LoginViewModel(applicationContext, repository);
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }

    /**
     * Creates a dummy implementation of VolunteerDao to satisfy the LoginRepository dependency.
     * All methods required by the interface are implemented here.
     */
    private VolunteerDao createDummyVolunteerDao() {
        return new VolunteerDao() {

            // IMPLEMENTATION 1: Corresponds to getVolunteerByCredentials in your DAO
            @Override
            public VolunteerEntity getVolunteerByCredentials(String email, String passwordHash) {
                // Return null since we are not authenticating volunteers locally yet
                return null;
            }

            // IMPLEMENTATION 2: Corresponds to getVolunteerByEmail in your DAO
            @Override
            public VolunteerEntity getVolunteerByEmail(String email) {
                // Return null
                return null;
            }

            // IMPLEMENTATION 3: Corresponds to insertVolunteer in your DAO
            @Override
            public long insertVolunteer(VolunteerEntity volunteer) {
                // Return 0 (a failed insertion ID)
                return 0;
            }
        };
    }
}