package com.julian.appturnos;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import com.julian.appturnos.util.AyudanteNavegacion;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // delego la navegación al helper x si necesito usar algun condicional o logica
        AyudanteNavegacion.showLoginFragment(this, savedInstanceState);
    }
}