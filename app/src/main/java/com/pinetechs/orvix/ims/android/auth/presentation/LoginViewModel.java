package com.pinetechs.orvix.ims.android.auth.presentation;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pinetechs.orvix.ims.android.auth.data.AuthRepository;
import com.pinetechs.orvix.ims.android.auth.data.dto.LoginResponse;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.core.util.Resource;

public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository repository;
    private final SessionManager sessionManager;
    private final MutableLiveData<Resource<LoginResponse>> loginState = new MutableLiveData<>(Resource.idle());

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.repository = new AuthRepository(application);
        this.sessionManager = new SessionManager(application);
    }

    public LiveData<Resource<LoginResponse>> getLoginState() {
        return loginState;
    }

    public void login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            loginState.setValue(Resource.error("Username is required"));
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            loginState.setValue(Resource.error("Password is required"));
            return;
        }

        loginState.setValue(Resource.loading());

        repository.login(username.trim(), password, new AuthRepository.RepositoryCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse data) {
                if (data.getAccessToken() == null || data.getAccessToken().trim().isEmpty()) {
                    loginState.setValue(Resource.error("Invalid login response: token is missing"));
                    return;
                }

                sessionManager.saveSession(data.getAccessToken(), data.getUser());
                loginState.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                loginState.setValue(Resource.error(message));
            }
        });
    }
}
