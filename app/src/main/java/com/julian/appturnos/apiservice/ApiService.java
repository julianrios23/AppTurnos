package com.julian.appturnos.apiservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public class ApiService {
    public static String BASE_URL = "https://inmobiliariaulp-amb5hwfqaraweyga.canadacentral-01.azurewebsites.net/";


    public static ServiceInterface getApiService(OkHttpClient client) {
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit rt = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(gson))
                .build();
        return rt.create(ServiceInterface.class);
    }
    /*ese bloque crea y configura un cliente okhttp para las solicitudes http. le pongo tiempos de espera de 30 segundos
         para conectar, leer y escribir. agrego un interceptor que añade la cabecera "content-type: application/json" a cada solicitud,
          asegurando que los datos se envien en formato json.*/
    public static OkHttpClient getDefaultClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Content-Type", "application/json")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();
    }



    public interface ServiceInterface {
        @FormUrlEncoded
        @POST("api/Propietarios/login")
        Call<String> loginForm(@Field("Usuario") String usuario, @Field("Clave") String clave);

        @GET("api/Propietarios")
        Call<Propietario> getPropietarios(@Header("Authorization") String token);

        @GET("api/Inmuebles")
        Call<List<Inmueble>> obtenerInmuebles(@Header("Authorization") String token);

        @PUT("api/Propietarios/actualizar")
        Call<Propietario> actualizarPropietario(@Header("Authorization") String token, @Body Propietario propietario);

        @GET("api/Inmuebles/GetContratoVigente")
        Call<List<Inmueble>> contratoVigente(@Header("Authorization") String token);

        @GET("api/contratos/inmueble/{id}")
        Call<Contrato> obtenerContratoPorInmueble(@Header("Authorization") String token, @Path("id") int idInmueble);

        @GET("api/pagos/contrato/{id}")
        Call<List<Pagos>> obtenerPagosPorContrato(@Header("Authorization") String token, @Path("id") int idContrato);

        @POST("api/Inmuebles/cargar")
        Call<Inmueble> nuevoInmueble(@Header("Authorization") String token, @Body Inmueble inmueble);

        @Multipart
        @POST("api/Inmuebles/cargar")
        Call<Inmueble> cargarInmueble(
                @Header("Authorization") String token,
                @Part MultipartBody.Part imagen,
                @Part("inmueble") RequestBody inmuebleJson
        );

        @PUT("api/Inmuebles/actualizar")
        Call<Inmueble> actualizarInmueble(@Header("Authorization") String token, @Body Inmueble inmueble);
    }

}
