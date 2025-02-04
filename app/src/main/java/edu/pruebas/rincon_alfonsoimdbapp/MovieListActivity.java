package edu.pruebas.rincon_alfonsoimdbapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.pruebas.rincon_alfonsoimdbapp.adapters.MovieAdapter;
import edu.pruebas.rincon_alfonsoimdbapp.models.Movie;
import edu.pruebas.rincon_alfonsoimdbapp.models.MovieResponse;
import edu.pruebas.rincon_alfonsoimdbapp.utils.Constants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieListActivity extends AppCompatActivity {

    private static final String TAG = "MovieListActivity";
    private static final String API_KEY = "aaf2cf26c82660c7a38d10d55ed5c92d";
    private static final String URL_BUSQUEDAS = "https://api.themoviedb.org/3/discover/movie";

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private OkHttpClient client;
    private Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar el adaptador con una lista vacía y asignarlo al RecyclerView
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : null;  // Obtener el ID del usuario

        if (userId != null) {
            movieAdapter = new MovieAdapter(this, new ArrayList<>(), Constants.SOURCE_TMDB, userId);
            recyclerView.setAdapter(movieAdapter);
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }

        // OkHttpClient y Gson
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        gson = new Gson();

        // Se obtienen los datos del Intent
        int fecha = getIntent().getIntExtra("fecha", 0);
        int generoId = getIntent().getIntExtra("generoId", 0);

        // Verificar que los datos sean válidos
        if (fecha == 0 || generoId == 0) {
            Toast.makeText(this, "Datos inválidos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Realizar la solicitud a la API
        mostrarPeliculasBusqueda(fecha, generoId);
    }

    private void mostrarPeliculasBusqueda(int año, int idGenero) {
        String url = URL_BUSQUEDAS + "?api_key=" + API_KEY +
                "&language=en-US&sort_by=popularity.desc&include_adult=false&include_video=false" +
                "&page=1&primary_release_year=" + año +
                "&with_genres=" + idGenero;

        // Crear la solicitud
        Request request = new Request.Builder().url(url).get()
                .addHeader("accept", "application/json").build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Manejar fallo en la solicitud
                runOnUiThread(() -> {
                    Toast.makeText(MovieListActivity.this, "Error al cargar películas", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error en la solicitud de películas", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Respuesta de películas: " + responseBody);

                    // Analizar JSON
                    MovieResponse movieResponse = gson.fromJson(responseBody, MovieResponse.class);
                    List<Movie> peliculas = movieResponse.getResults();

                    // Verificar la lista de películas por consola
                    if (peliculas != null) {
                        Log.d(TAG, "Número de películas recibidas: " + peliculas.size());
                        if (!peliculas.isEmpty()) {
                            Log.d(TAG, "Primera película: " + peliculas.get(0).getTitulo());
                        }
                    } else {
                        Log.e(TAG, "La lista de películas es nula.");
                    }

                    // Actualizar RecyclerView
                    runOnUiThread(() -> {
                        if (peliculas != null && !peliculas.isEmpty()) {
                            movieAdapter.setMovies(peliculas);
                        } else {
                            Toast.makeText(MovieListActivity.this, "No se encontraron películas", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Caso de error
                    runOnUiThread(() -> {
                        Toast.makeText(MovieListActivity.this, "Error al cargar películas", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Respuesta no exitosa: " + response.message());
                    });
                }
            }
        });
    }
}
