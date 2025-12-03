package com.example.mealconnect.ui.login.volunteer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mealconnect.R;
import com.example.mealconnect.data.ClientSyncRepository; // Import Sync Repository
import com.example.mealconnect.data.local.ClientDatabase;
import com.example.mealconnect.ui.login.LoginFormState;
import com.example.mealconnect.ui.login.LoginResult;
import com.example.mealconnect.ui.login.LoginViewModel;
import com.example.mealconnect.ui.login.LoginViewModelFactory;
import com.example.mealconnect.ui.login.LoggedInUserView;
import com.example.mealconnect.ui.login.volunteer.ScannerActivity;
import com.example.mealconnect.databinding.ActivityVolunteerLoginBinding;

/**
 * VolunteerLoginActivity
 * Handles Volunteer authentication and navigates to the ScannerActivity upon success.
 * NOW STARTS THE DATA SYNC ON SUCCESSFUL LOGIN.
 */
public class VolunteerLoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityVolunteerLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Setup Layout Binding
        binding = ActivityVolunteerLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Setup ViewModel (Reusing the existing ViewModel)
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory(this.getApplicationContext())).get(LoginViewModel.class);

        // 3. UI references (Must match IDs in activity_volunteer_login.xml)
        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        // 4. Observe Form State for Validation
        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        // 5. Observe Login Result Success or Failure.
        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
            }
            if (loginResult.getSuccess() != null) {
                updateUiWithUser(loginResult.getSuccess());

                // *** NEW: START SYNC BEFORE NAVIGATING ***
                // Initialize the Sync Repository and start the background listener.
                // This ensures that by the time the scanner opens, data is flowing in.
                ClientSyncRepository syncRepo = ClientSyncRepository.getInstance(
                        ClientDatabase.getInstance(getApplicationContext()).clientDao()
                );
                syncRepo.startSync();
                // ******************************************

                // *** SUCCESSFUL LOGIN: REDIRECT TO QR SCANNER ***
                Intent intent = new Intent(VolunteerLoginActivity.this, ScannerActivity.class);
                startActivity(intent);
                finish(); // Close login activity once successful
            }
            setResult(Activity.RESULT_OK);
        });

        // 6. TextWatchers for Real-time Validation
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        // Handle keyboard 'Done' action
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
            return false;
        });

        // 7. Handle Login Button Click
        loginButton.setOnClickListener(v -> {
            // Trigger authentication via ViewModel
            loginViewModel.login(usernameEditText.getText().toString(),
                    passwordEditText.getText().toString());
        });

        // 8. NO Back Button on Login Screen (root screen)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("Volunteer Login");
        }
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
