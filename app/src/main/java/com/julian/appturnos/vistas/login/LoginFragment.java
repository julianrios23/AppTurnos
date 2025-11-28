package com.julian.appturnos.vistas.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.lifecycle.Observer;

import com.julian.appturnos.R;
import com.julian.appturnos.util.AyudanteNavegacion;
import com.julian.appturnos.vistas.login.LoginResult;
import com.julian.appturnos.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private LoginViewModel mViewModel;
    private FragmentLoginBinding binding;

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

        binding.loginButton.setOnClickListener(v -> {
            String usuario = binding.usernameEditText.getText().toString().trim();
            String clave = binding.passwordEditText.getText().toString().trim();
            mViewModel.login(usuario, clave);
        });

        mViewModel.getUiEventLiveData().observe(getViewLifecycleOwner(), this::handleUiEvent);
    }

    private void handleUiEvent(LoginUiEvent event) {
        if (event == null) return;
        switch (event.type) {
            case NAVIGATE:
                // Usamos el ayudante de navegación en lugar de NavController
                AyudanteNavegacion.showInicioFragment((AppCompatActivity) requireActivity());
                break;
            case SHOW_MESSAGE:
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}