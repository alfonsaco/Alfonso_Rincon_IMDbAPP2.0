package edu.pruebas.rincon_alfonsoimdbapp.api;

import java.util.ArrayList;
import java.util.List;

public class RapidApiKeyManager {

    private final List<String> apiKeys;
    private int currentIndex;

    // Singleton para asegurar una única instancia en toda la aplicación
    private static RapidApiKeyManager instance;

    RapidApiKeyManager() {
        apiKeys = new ArrayList<>();
        currentIndex = 0;

        // Añade aquí tus claves API de IMDB
        apiKeys.add("ef4912674dmsh08f7410741bff88p1f2a75jsnd08e5a95fbaa");
        apiKeys.add("e11bbd55cemshd186130e9cc4907p1e01ddjsnd92900b7bab3");
        apiKeys.add("baa03d7902msh645a2f522307498p17800ejsndc8ec5308b1b");
    }

    // Método para obtener la instancia singleton
    public static synchronized RapidApiKeyManager getInstance() {
        if (instance == null) {
            instance = new RapidApiKeyManager();
        }
        return instance;
    }

    // Obtiene la clave API actual
    public String getCurrentKey() {
        if (apiKeys.isEmpty()) {
            throw new IllegalStateException("No hay claves API disponibles.");
        }
        return apiKeys.get(currentIndex);
    }

    // Cambia a la siguiente clave API en la lista
    public String switchToNextKey() {
        if (apiKeys.isEmpty()) {
            throw new IllegalStateException("No hay claves API disponibles para cambiar.");
        }
        currentIndex = (currentIndex + 1) % apiKeys.size();
        return getCurrentKey();
    }

    // Método para añadir una nueva clave API
    public void addApiKey(String apiKey) {
        apiKeys.add(apiKey);
    }

    // Método para obtener el número total de claves API
    public int getTotalKeys() {
        return apiKeys.size();
    }
}
