package edu.pruebas.rincon_alfonsoimdbapp.sync;

import android.content.Context;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import edu.pruebas.rincon_alfonsoimdbapp.database.FavoritesManager;
import edu.pruebas.rincon_alfonsoimdbapp.models.Movie;
import java.util.List;

public class FavoritesSync {

    private static final String TAG = "FavoritesSync";
    private final FirebaseFirestore firestore;
    private final FavoritesManager favoritesManager;

    public FavoritesSync(Context context) {
        firestore = FirebaseFirestore.getInstance();
        favoritesManager = new FavoritesManager(context);
    }

    // Sincroniza las películas favoritas de Firestore a SQLite
    public void syncFavoritesFromFirestore(String userId) {
        firestore.collection("favorites")
                .document(userId)
                .collection("movies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documentSnapshots = task.getResult();
                        if (documentSnapshots != null && !documentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : documentSnapshots) {
                                Movie movie = document.toObject(Movie.class);
                                if (movie != null) {
                                    // Verificar si la película ya está en SQLite
                                    if (!favoritesManager.isFavorite(userId, movie.getId())) {
                                        // Si no está, la añadimos
                                        favoritesManager.añadirFavorita(userId, movie);
                                    }
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "Error al sincronizar desde Firestore", task.getException());
                    }
                });
    }

    // Sincroniza las películas favoritas de SQLite a Firestore
    public void syncFavoritesToFirestore(String userId) {
        List<Movie> favoritas = favoritesManager.obtenerFavoritas(userId);

        if (favoritas.isEmpty()) {
            Log.d(TAG, "No hay películas favoritas para sincronizar.");
            return;
        }

        for (Movie movie : favoritas) {
            firestore.collection("favorites")
                    .document(userId)
                    .collection("movies")
                    .document(movie.getId()) // Usamos el ID de la película como identificador
                    .set(movie)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Película sincronizada a Firestore: " + movie.getTitulo()))
                    .addOnFailureListener(e -> Log.e(TAG, "Error al sincronizar a Firestore: " + e.getMessage()));
        }
    }

    public void addMovieToFavorites(String userId, Movie movie) {
        firestore.collection("favorites")
                .document(userId)
                .collection("movies")
                .document(movie.getId()) // El ID de la película como identificador único
                .set(movie) // Añadir los datos de la película
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Película añadida a Firestore: " + movie.getTitulo());
                    // Opcionalmente, puedes agregarla a SQLite también
                    favoritesManager.añadirFavorita(userId, movie);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al añadir película a Firestore: " + e.getMessage()));
    }

    // Eliminar película de favoritos en Firestore
    public void removeMovieFromFavorites(String userId, String movieId) {
        firestore.collection("favorites")
                .document(userId)
                .collection("movies")
                .document(movieId) // Usamos el ID de la película para eliminarla
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Película eliminada de Firestore: " + movieId);
                    // Opcionalmente, puedes eliminarla de SQLite también
                    favoritesManager.borrarFavorita(userId, movieId);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar película de Firestore: " + e.getMessage()));
    }
}