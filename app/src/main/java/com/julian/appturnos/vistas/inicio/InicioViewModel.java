package com.julian.appturnos.vistas.inicio;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.julian.appturnos.apiservice.ApiService;
import com.julian.appturnos.modelos.Usuario;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InicioViewModel extends AndroidViewModel {

    private final SharedPreferences prefs;
    private final ApiService.ServiceInterface apiService;
    private final MutableLiveData<Usuario> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public InicioViewModel(@NonNull Application application) {
        super(application);
        prefs = application.getSharedPreferences("token_prefs", Context.MODE_PRIVATE);
        OkHttpClient client = ApiService.getDefaultClient();
        apiService = ApiService.getApiService(client);
        loadUserProfile();
    }

    public LiveData<Usuario> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    private void loadUserProfile() {
        String token = prefs.getString("token", null);

        if (token != null) {
            apiService.getPerfilUsuario("Bearer " + token).enqueue(new Callback<Usuario>() {
                @Override
                public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        userLiveData.postValue(response.body());
                        Log.i("InicioViewModel", "Perfil de usuario cargado con éxito.");
                    } else {
                        String errorMsg = "Error al cargar el perfil. Código: " + response.code();
                        if (response.errorBody() != null) {
                            try {
                                errorMsg += ", Cuerpo: " + response.errorBody().string();
                            } catch (Exception e) {
                                Log.e("InicioViewModel", "Error al leer errorBody: " + e.getMessage());
                            }
                        }
                        errorLiveData.postValue(errorMsg);
                        Log.e("InicioViewModel", errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<Usuario> call, Throwable t) {
                    String errorMsg = "Error de red al cargar el perfil: " + t.getMessage();
                    errorLiveData.postValue(errorMsg);
                    Log.e("InicioViewModel", errorMsg, t);
                }
            });
        } else {
            errorLiveData.postValue("No se encontró token. Por favor, inicie sesión de nuevo.");
            Log.e("InicioViewModel", "No se encontró token en SharedPreferences.");
        }
    }
}