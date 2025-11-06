package com.julian.appturnos.apiservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;


public class ApiService {
    public static String BASE_URL = "http://localhost:3000/";


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

    }

}
