package com.example.mealconnect.ui.login;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mealconnect.R;
import com.example.mealconnect.data.LoginRepository;
import com.example.mealconnect.data.Result;
import com.example.mealconnect.data.model.LoggedInUser;

public class LoginViewModel extends AndroidViewModel {

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    // CRITICAL: This LiveData will be mapped to the Repository's asynchronous result stream.
    private final LiveData<LoginResult> loginResult;
    private final LoginRepository loginRepository;

    public LoginViewModel(Application application, LoginRepository loginRepository) {
        super(application);
        this.loginRepository = loginRepository;

        // CRITICAL FIX: Map the Repository's new async result LiveData to the ViewModel's LoginResult LiveData
        this.loginResult = new LoginResultLiveData(loginRepository.getAsyncLoginResult());
    }

    /**
     * Helper class to map the generic Repository Result<LoggedInUser>
     * into the UI-specific LoginResult used by the Activity.
     */
    private static class LoginResultLiveData extends LiveData<LoginResult> {
        public LoginResultLiveData(LiveData<Result<LoggedInUser>> source) {
            // This observer is set up to listen forever to the repository's results
            source.observeForever(result -> {
                if (result == null) return;

                if (result instanceof Result.Success) {
                    LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                    // On SUCCESS, set the value that triggers the navigation in LoginActivity
                    setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
                } else if (result instanceof Result.Error) {
                    Exception error = ((Result.Error) result).getError();
                    Log.e("LoginViewModel", "Login Failed (Async): " + error.getMessage());

                    try {
                        // The Repository sends the R.string ID as the error message. We parse it here.
                        int errorResId = Integer.parseInt(error.getMessage());
                        setValue(new LoginResult(errorResId));
                    } catch (NumberFormatException e) {
                        // Fallback error if the message isn't a resource ID
                        setValue(new LoginResult(R.string.login_failed));
                    }
                }
            });
        }
    }

    public LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    /**
     * Calls the asynchronous login method in the repository, which returns immediately
     * but updates loginResult later.
     */
    public void login(String username, String password) {
        loginRepository.login(username, password);
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    private boolean isUserNameValid(String username) {
        return username != null && !username.trim().isEmpty();
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 3;
    }
}