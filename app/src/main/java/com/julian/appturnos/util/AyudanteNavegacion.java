package com.julian.appturnos.util;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.julian.appturnos.R;
import com.julian.appturnos.vistas.inicio.InicioFragment;
import com.julian.appturnos.vistas.login.LoginFragment;

public class AyudanteNavegacion {
    public static void showLoginFragment(AppCompatActivity activity, Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            FragmentManager fm = activity.getSupportFragmentManager();
            fm.beginTransaction()
                .replace(R.id.fragment_container_view, LoginFragment.newInstance())
                .commit();
        }
    }

    public static void showInicioFragment(AppCompatActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        fm.beginTransaction()
            .replace(R.id.fragment_container_view, InicioFragment.newInstance())
            .commit();
    }
}
