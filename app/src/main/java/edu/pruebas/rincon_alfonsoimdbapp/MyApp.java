package edu.pruebas.rincon_alfonsoimdbapp;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.ProcessLifecycleOwner;

import edu.pruebas.rincon_alfonsoimdbapp.utils.AppLifecycleManager;

public class MyApp extends Application {

    private AppLifecycleManager appLifecycleManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyApp", "Aplicación iniciada");

        // Inicializar AppLifecycleManager
        appLifecycleManager = new AppLifecycleManager(this);

        // Registrar como observador del ciclo de vida
        ProcessLifecycleOwner.get().getLifecycle().addObserver(appLifecycleManager);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d("MyApp", "Aplicación terminada");

        if (appLifecycleManager != null) {
            appLifecycleManager.cleanup();
        }
    }
}
