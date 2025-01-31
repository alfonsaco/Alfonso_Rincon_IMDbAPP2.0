package edu.pruebas.rincon_alfonsoimdbapp.utils;

import android.app.Activity;
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
                actualizarUltimoLogout(currentUser.getEmail(), System.currentTimeMillis());
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

        // Actualizar UltimoLogin
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            actualizarUltimoLogin(currentUser.getEmail(), System.currentTimeMillis());
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        Log.d("AppLifecycleManager", "Aplicación en segundo plano");

        // Registrar UltimoLogout
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            actualizarUltimoLogout(currentUser.getEmail(), System.currentTimeMillis());
        }

        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_IS_LOGGED_IN, false);
        editor.apply();

        // Registrar logout si es necesario
        registerUserLogout(currentUser);
    }

    private void actualizarUltimoLogin(String email, long timestamp) {
        Usuario usuario = usuarioDAO.obtenerUsuarioPorEmail(email);
        if (usuario != null) {
            usuarioDAO.actualizarUltimoLogin(email, timestamp);
            Log.d("AppLifecycleManager", "UltimoLogin actualizado para: " + email);
        } else {
            // Si el usuario no existe, insertarlo
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                Usuario nuevoUsuario = new Usuario(
                        firebaseUser.getDisplayName(),
                        firebaseUser.getEmail(),
                        timestamp,
                        0
                );
                usuarioDAO.insertarUsuario(nuevoUsuario);
                Log.d("AppLifecycleManager", "Nuevo usuario insertado: " + email);
            }
        }
    }

    private void actualizarUltimoLogout(String email, long timestamp) {
        Usuario usuario = usuarioDAO.obtenerUsuarioPorEmail(email);
        if (usuario != null) {
            usuarioDAO.actualizarUltimoLogout(email, timestamp);
            Log.d("AppLifecycleManager", "UltimoLogout actualizado para: " + email);
        }
    }

    private void registerUserLogout(FirebaseUser user) {
        if (user != null) {
            // Implementa cualquier lógica adicional necesaria para el logout
            // Por ejemplo, puedes cerrar la sesión en Firebase si aún está abierta
            FirebaseAuth.getInstance().signOut();
            Log.d("AppLifecycleManager", "Usuario deslogueado: " + user.getEmail());
        }
    }

    // Asegúrate de cerrar la base de datos cuando ya no se necesite
    public void cleanup() {
        usuarioDAO.close();
    }
}