package edu.pruebas.rincon_alfonsoimdbapp.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import edu.pruebas.rincon_alfonsoimdbapp.R;
import edu.pruebas.rincon_alfonsoimdbapp.adapters.MovieAdapter;
import edu.pruebas.rincon_alfonsoimdbapp.api.IMDBApiClient;
import edu.pruebas.rincon_alfonsoimdbapp.api.IMDBApiService;
import edu.pruebas.rincon_alfonsoimdbapp.models.Movie;
import edu.pruebas.rincon_alfonsoimdbapp.models.PopularMoviesResponse;
import edu.pruebas.rincon_alfonsoimdbapp.utils.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Top10Fragment extends Fragment {

    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private IMDBApiService api;
    private List<Movie> listaPeliculas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Configurar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewTopMovies);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Obtener el userId desde Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : null;

        // Verificar que el userId no sea nulo y asignar el adaptador
        if (userId != null) {
            adapter = new MovieAdapter(getContext(), listaPeliculas, Constants.SOURCE_IMD, userId);
        } else {
            // Si no hay un usuario autenticado, maneja el caso (puedes mostrar un mensaje de advertencia, etc.)
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }

        recyclerView.setAdapter(adapter);

        // Llamada al método que obtiene los datos de la API
        mostrarPeliculas();

        return view;
    }

    // Método que mostrará las películas o series en cuestión
    private void mostrarPeliculas() {
        Call<PopularMoviesResponse> call = IMDBApiClient.getApiService().top10("US");
        call.enqueue(new Callback<PopularMoviesResponse>() {
            @Override
            public void onResponse(@NonNull Call<PopularMoviesResponse> call, @NonNull Response<PopularMoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PopularMoviesResponse.Edge> edges = response.body().getData().getTopMeterTitles().getEdges();
                    // Verificamos que haya películas y series, y si es así, se vacía la lista para poder agregar nuevas
                    if (edges != null && !edges.isEmpty()) {
                        listaPeliculas.clear();
                        // Insertamos las películas y series
                        for (int i = 0; i < Math.min(edges.size(), 10); i++) {
                            PopularMoviesResponse.Edge edge = edges.get(i);
                            PopularMoviesResponse.Node node = edge.getNode();

                            Movie movie = new Movie();

                            // Verificar y asignar el ID
                            if (node.getId() != null) {
                                movie.setId(node.getId());
                            } else {
                                movie.setId("ID no disponible");
                            }

                            // Verificar y asignar el Título
                            if (node.getTitleText() != null && node.getTitleText().getText() != null) {
                                movie.setTitulo(node.getTitleText().getText());
                            } else {
                                movie.setTitulo("Título no disponible");
                            }

                            // Verificar y asignar la Fecha de Salida
                            if (node.getReleaseDate() != null) {
                                movie.setFechaSalida(String.valueOf(node.getReleaseDate().getYear()));
                            } else {
                                movie.setFechaSalida("Año no disponible");
                            }

                            // Verificar y asignar la Ruta del Póster
                            if (node.getPrimaryImage() != null && node.getPrimaryImage().getUrl() != null) {
                                movie.setRutaPoster(node.getPrimaryImage().getUrl());
                            } else {
                                // No pondremos nada
                                movie.setRutaPoster("");
                            }

                            // Asignar la fuente
                            //movie.setSource(Constants.SOURCE_IMD);

                            listaPeliculas.add(movie);
                        }

                        adapter.notifyDataSetChanged();
                    }
                } else {
                    if (response.code() == 429) { // Código HTTP para "Too Many Requests"
                        Log.e("HomeFragment", "Límite de solicitudes alcanzado. Cambiando API Key.");
                        IMDBApiClient.switchApiKey();
                        Toast.makeText(getContext(), "Límite de solicitudes alcanzado. Cambiando clave API.", Toast.LENGTH_SHORT).show();
                        mostrarPeliculas(); // Reintentar con la siguiente clave
                    } else {
                        Log.e("HomeFragment", "Error al cargar películas: " + response.message());
                        Toast.makeText(getContext(), "Error al cargar películas: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PopularMoviesResponse> call, @NonNull Throwable t) {
                Log.e("HomeFragment", "Error al llamar la API: " + t.getMessage());
                Toast.makeText(getContext(), "Error al cargar películas: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
