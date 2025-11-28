package com.julian.appturnos.vistas.login;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.julian.appturnos.apiservice.ApiService;
import com.julian.appturnos.apiservice.LoginResponse;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {

    private final ApiService.ServiceInterface loginApi;
    private final SharedPreferences prefs;

    private final MutableLiveData<LoginResult> loginResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<LoginUiEvent> uiEventLiveData = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        OkHttpClient client = ApiService.getDefaultClient();
        loginApi = ApiService.getApiService(client);
        prefs = application.getSharedPreferences("token_prefs", Context.MODE_PRIVATE);
    }

    public LiveData<LoginResult> getLoginResultLiveData() {
        return loginResultLiveData;
    }

    public LiveData<LoginUiEvent> getUiEventLiveData() {
        return uiEventLiveData;
    }

    public void login(String usuario, String clave) {
        loginApi.login(usuario, clave).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Log.d("LoginViewModelDebug", "Código de respuesta HTTP: " + response.code());
                if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "N/A";
                        Log.e("LoginViewModelDebug", "Respuesta HTTP no exitosa. Cuerpo de error: " + errorBody);
                    } catch (Exception e) {
                        Log.e("LoginViewModelDebug", "Error al leer errorBody: " + e.getMessage());
                    }
                }

                if (response.isSuccessful() && response.body() != null && response.body().token != null) {
                    String token = response.body().token;

                    prefs.edit()
                            .putString("token", token)
                            .apply();

                    Log.i("LoginViewModel", "Login exitoso. Token: " + token);

                    loginResultLiveData.postValue(new LoginResult(LoginResult.Status.SUCCESS, null, token));
                    uiEventLiveData.postValue(new LoginUiEvent(LoginUiEvent.Type.SHOW_MESSAGE, "¡Login exitoso!", 0));
                    uiEventLiveData.postValue(new LoginUiEvent(LoginUiEvent.Type.NAVIGATE, null, com.julian.appturnos.R.id.fragment_inicio));
                } else {
                    String errorMsg = "Credenciales incorrectas o error de respuesta";

                    if (response.errorBody() != null) {
                        try {
                            // Intenta parsear el error body si no es null
                            errorMsg = response.errorBody().string();
                            // Aquí podrías añadir lógica para parsear JSON de error si el backend lo envía
                        } catch (Exception e) {
                            Log.e("LoginViewModel", "Error al leer errorBody: " + e.getMessage());
                        }
                    }

                    Log.e("LoginViewModel", errorMsg);
                    loginResultLiveData.postValue(new LoginResult(LoginResult.Status.ERROR, errorMsg, null));
                    uiEventLiveData.postValue(new LoginUiEvent(LoginUiEvent.Type.SHOW_MESSAGE, errorMsg, 0));
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                String errorMsg = "Error de red: " + t.getMessage();
                Log.e("LoginViewModel", errorMsg, t);
                loginResultLiveData.postValue(new LoginResult(LoginResult.Status.ERROR, errorMsg, null));
                uiEventLiveData.postValue(new LoginUiEvent(LoginUiEvent.Type.SHOW_MESSAGE, errorMsg, 0));
            }
        });
    }
}