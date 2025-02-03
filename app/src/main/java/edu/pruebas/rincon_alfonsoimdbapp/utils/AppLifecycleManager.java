package edu.pruebas.rincon_alfonsoimdbapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.pruebas.rincon_alfonsoimdbapp.models.Usuario;
import edu.pruebas.rincon_alfonsoimdbapp.models.UsuarioDAO;

public class AppLifecycleManager implements LifecycleObserver {

    private static final String PREF_NAME = "AppPrefs";
    private static final String PREF_IS_LOGGED_IN = "isLoggedIn";

    private Context context;
    private UsuarioDAO usuarioDAO;

    public AppLifecycleManager(Context context) {
        this.context = context.getApplicationContext();
        usuarioDAO = new UsuarioDAO(this.context);
        usuarioDAO.open();
        checkForPendingLogout();
    }

    private void checkForPendingLogout() {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean wasLoggedIn = preferences.getBoolean(PREF_IS_LOGGED_IN, false);

        if (wasLoggedIn) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                registerUserLogout(currentUser);
                actualizarUltimoLogout(currentUser.getEmail(), DateTimeUtils.getCurrentTimestamp());
            }
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREF_IS_LOGGED_IN, false);
            editor.apply();
            Log.d("AppLifecycleManager", "Logout pendiente registrado al reiniciar la app");
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        Log.d("AppLifecycleManager", "Aplicación en primer plano");
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_IS_LOGGED_IN, true);
        editor.apply();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            actualizarUltimoLogin(currentUser.getEmail(), DateTimeUtils.getCurrentTimestamp());
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        Log.d("AppLifecycleManager", "Aplicación en segundo plano");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            actualizarUltimoLogout(currentUser.getEmail(), DateTimeUtils.getCurrentTimestamp());
        }
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_IS_LOGGED_IN, false);
        editor.apply();

        registerUserLogout(currentUser);
    }

    private void actualizarUltimoLogin(String email, String timestamp) {
        Usuario usuario = usuarioDAO.obtenerUsuarioPorEmail(email);
        if (usuario != null) {
            usuarioDAO.actualizarUltimoLogin(email, timestamp);
            Log.d("AppLifecycleManager", "UltimoLogin actualizado para: " + email);
        } else {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                // Se pasan valores vacíos para dirección, teléfono e imagen
                Usuario nuevoUsuario = new Usuario(
                        firebaseUser.getUid(),
                        firebaseUser.getDisplayName(),
                        firebaseUser.getEmail(),
                        timestamp,
                        "",
                        "",
                        "",
                        ""
                );
                usuarioDAO.insertarUsuario(nuevoUsuario);
                Log.d("AppLifecycleManager", "Nuevo usuario insertado: " + email);
            }
        }
    }

    private void actualizarUltimoLogout(String email, String timestamp) {
        Usuario usuario = usuarioDAO.obtenerUsuarioPorEmail(email);
        if (usuario != null) {
            usuarioDAO.actualizarUltimoLogout(email, timestamp);
            Log.d("AppLifecycleManager", "UltimoLogout actualizado para: " + email);
        }
    }

    private void registerUserLogout(FirebaseUser user) {
        if (user != null) {
            FirebaseAuth.getInstance().signOut();
            Log.d("AppLifecycleManager", "Usuario deslogueado: " + user.getEmail());
        }
    }

    public void cleanup() {
        usuarioDAO.close();
    }
}
