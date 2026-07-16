package com.pinetechs.orvix.ims.android.bootstrap.presentation;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pinetechs.orvix.ims.android.R;
import com.pinetechs.orvix.ims.android.bootstrap.data.BootstrapRepository;
import com.pinetechs.orvix.ims.android.bootstrap.data.dto.BootstrapResolveResponse;
import com.pinetechs.orvix.ims.android.core.network.ApiClient;
import com.pinetechs.orvix.ims.android.core.storage.SessionManager;
import com.pinetechs.orvix.ims.android.core.util.Resource;

public class SetupViewModel extends AndroidViewModel {

    private final BootstrapRepository repository;
    private final SessionManager sessionManager;
    private final MutableLiveData<Resource<BootstrapResolveResponse>> setupState = new MutableLiveData<>(Resource.idle());

    public SetupViewModel(@NonNull Application application) {
        super(application);
        this.repository = new BootstrapRepository(application);
        this.sessionManager = new SessionManager(application);
    }

    public LiveData<Resource<BootstrapResolveResponse>> getSetupState() {
        return setupState;
    }

    public void connectByClientCode(String clientCode) {
        if (clientCode == null || clientCode.trim().isEmpty()) {
            setupState.setValue(Resource.error(getApplication().getString(R.string.err_client_code_required)));
            return;
        }

        setupState.setValue(Resource.loading());
        repository.loadByClientCode(clientCode.trim().toUpperCase(), createCallback());
    }

    private BootstrapRepository.RepositoryCallback<BootstrapResolveResponse> createCallback() {
        return new BootstrapRepository.RepositoryCallback<BootstrapResolveResponse>() {
            @Override
            public void onSuccess(BootstrapResolveResponse data) {
                sessionManager.saveClientConfig(data);
                ApiClient.clearCache();
                setupState.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                setupState.setValue(Resource.error(message));
            }
        };
    }
}
