package com.julian.appturnos.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class ManejoHuella {

    private static final String TAG = "ManejoHuella";
    private final BiometricPrompt biometricPrompt;
    private final BiometricPrompt.PromptInfo promptInfo;
    private final BiometricCallback callback;

    public interface BiometricCallback {
        void onBiometricAuthenticationSuccess();
        void onBiometricAuthenticationFailure();
        void onBiometricAuthenticationError(int errorCode, @NonNull CharSequence errString);
    }

    public ManejoHuella(@NonNull FragmentActivity activity, @NonNull BiometricCallback callback) {
        this.callback = callback;
        Executor executor = ContextCompat.getMainExecutor(activity);

        biometricPrompt = new BiometricPrompt(activity,
                executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Log.e(TAG, "Error de autenticación: " + errString + " (Code: " + errorCode + ")");
                        callback.onBiometricAuthenticationError(errorCode, errString);
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Log.i(TAG, "Autenticación biométrica exitosa.");
                        callback.onBiometricAuthenticationSuccess();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Log.w(TAG, "Autenticación biométrica fallida.");
                        callback.onBiometricAuthenticationFailure();
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Inicio de sesión con huella dactilar")
                .setSubtitle("Coloca tu dedo en el sensor para iniciar sesión")

                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
    }

    public boolean esBiometriaDisponible(@NonNull Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(TAG, "Autenticación biométrica disponible y configurada.");
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e(TAG, "No hay hardware biométrico disponible.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e(TAG, "Hardware biométrico no disponible en este momento.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.w(TAG, "El usuario no tiene ninguna biometría configurada.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                Log.e(TAG, "Biometría no compatible en este dispositivo.");
                return false;
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                Log.e(TAG, "Estado biométrico desconocido.");
                return false;
            default:
                Log.e(TAG, "Estado biométrico inesperado: " + canAuthenticate);
                return false;
        }
    }

    public void mostrarPromptBiometrico() {
        biometricPrompt.authenticate(promptInfo);
    }
}
