package com.example.mealconnect.ui.login.volunteer;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.mealconnect.data.local.ClientDao;

/**
 * Factory to create the VolunteerViewModel with its required ClientDao dependency.
 */
public class VolunteerViewModelFactory implements ViewModelProvider.Factory {

    private final ClientDao clientDao;

    public VolunteerViewModelFactory(ClientDao clientDao) {
        this.clientDao = clientDao;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(VolunteerViewModel.class)) {
            return (T) new VolunteerViewModel(clientDao);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}

