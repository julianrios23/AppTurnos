package com.julian.appturnos.vistas.inicio;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.julian.appturnos.R;
import com.julian.appturnos.modelos.Usuario;

public class InicioFragment extends Fragment {

    private static final String TAG = "InicioFragment";
    private InicioViewModel mViewModel;
    private TextView tvnombre;

    public static InicioFragment newInstance() {
        return new InicioFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);
        tvnombre = view.findViewById(R.id.tvnombre);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(InicioViewModel.class);

        mViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                String welcomeMessage = "¡Bienvenido " + user.getNombre() + " " + user.getApellido() + "!";
                tvnombre.setText(welcomeMessage);
            } else {
                tvnombre.setText(R.string.welcome_message);
            }
        });

        mViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                // Optionally display a Toast or SnackBar with the error message
            }
        });
    }
}
