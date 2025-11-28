package com.julian.appturnos.apiservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.julian.appturnos.modelos.Usuario;
import com.julian.appturnos.modelos.Paciente;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import okhttp3.logging.HttpLoggingInterceptor;


public class ApiService {
    public static String BASE_URL = "http://192.168.1.4:3000/";


    public static ServiceInterface getApiService(OkHttpClient client) {
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit rt = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(gson))
                .build();
        return rt.create(ServiceInterface.class);
    }

    public static OkHttpClient getDefaultClient() {
        // Configurar el interceptor de logging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Esto mostrará el cuerpo de la solicitud y la respuesta

        return new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .header("Content-Type", "application/json")
                            .build();
                    return chain.proceed(request);
                })
                .addInterceptor(logging) // Añadir el interceptor de logging aquí
                .build();
    }

    public interface ServiceInterface {
        @FormUrlEncoded
        @POST("api/auth/login")
        Call<LoginResponse> login(
            @retrofit2.http.Field("Usuario") String usuario,
            @retrofit2.http.Field("Clave") String clave
        );

        @GET("api/usuarios/perfil")
        Call<Usuario> getPerfilUsuario(@Header("Authorization") String authToken);

        @GET("api/pacientes/perfil")
        Call<Paciente> getPerfilPaciente(@Header("Authorization") String authToken);
    }

}