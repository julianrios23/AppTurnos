package com.julian.appturnos.vistas.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.julian.appturnos.R;
import com.julian.appturnos.databinding.FragmentLoginBinding;
import com.julian.appturnos.util.AyudanteNavegacion;
import com.julian.appturnos.util.ManejoHuella; // Importar ManejoHuella

public class LoginFragment extends Fragment implements ManejoHuella.BiometricCallback {

    private LoginViewModel mViewModel;
    private FragmentLoginBinding binding;
    private ManejoHuella manejoHuella;
    private SharedPreferences prefs;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        prefs = requireActivity().getSharedPreferences("token_prefs", Context.MODE_PRIVATE);

        // Inicializar ManejoHuella
        manejoHuella = new ManejoHuella(requireActivity(), this);

        binding.loginButton.setOnClickListener(v -> {
            String usuario = binding.usernameEditText.getText().toString().trim();
            String clave = binding.passwordEditText.getText().toString().trim();
            mViewModel.login(usuario, clave);
        });

        // Configurar listener para el botón de huella dactilar
        binding.fingerprintLoginButton.setOnClickListener(v -> {
            if (manejoHuella.esBiometriaDisponible(requireContext())) {
                manejoHuella.mostrarPromptBiometrico();
            } else {
                Toast.makeText(requireContext(), "Biometría no disponible o no configurada.", Toast.LENGTH_SHORT).show();
            }
        });

        mViewModel.getUiEventLiveData().observe(getViewLifecycleOwner(), this::handleUiEvent);
    }

    private void handleUiEvent(LoginUiEvent event) {
        if (event == null) return;
        switch (event.type) {
            case NAVIGATE:
                AyudanteNavegacion.showInicioFragment((AppCompatActivity) requireActivity());
                break;
            case SHOW_MESSAGE:
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // Implementación de la interfaz BiometricCallback
    @Override
    public void onBiometricAuthenticationSuccess() {
        // Autenticación biométrica exitosa. Verificar si hay un token guardado.
        String token = prefs.getString("token", null);
        if (token != null) {
            Toast.makeText(requireContext(), "Autenticación con huella exitosa. Iniciando sesión...", Toast.LENGTH_SHORT).show();
            AyudanteNavegacion.showInicioFragment((AppCompatActivity) requireActivity());
        } else {
            Toast.makeText(requireContext(), "Inicie sesión con usuario y contraseña primero.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBiometricAuthenticationFailure() {
        Toast.makeText(requireContext(), "Autenticación con huella fallida.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBiometricAuthenticationError(int errorCode, @NonNull CharSequence errString) {
        Toast.makeText(requireContext(), "Error de autenticación: " + errString, Toast.LENGTH_LONG).show();
        Log.e("LoginFragment", "Error de biometría: " + errString + " (Code: " + errorCode + ")");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
