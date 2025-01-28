// package edu.pruebas.rincon_alfonsoimdbapp.ui.home;

package edu.pruebas.rincon_alfonsoimdbapp.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.pruebas.rincon_alfonsoimdbapp.R;
import edu.pruebas.rincon_alfonsoimdbapp.adapters.MovieAdapter;
import edu.pruebas.rincon_alfonsoimdbapp.api.IMDBApiService;
import edu.pruebas.rincon_alfonsoimdbapp.models.Movie;
import edu.pruebas.rincon_alfonsoimdbapp.models.PopularMoviesResponse;
import edu.pruebas.rincon_alfonsoimdbapp.utils.Constants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

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
        // Pasar la fuente "IMD" al adaptador
        adapter = new MovieAdapter(getContext(), listaPeliculas, Constants.SOURCE_IMD);
        recyclerView.setAdapter(adapter);

        // Configuración de Retrofit y OkHttp para IMD. Se da la URL y la clase
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request modifiedRequest = chain.request().newBuilder()
                            .addHeader("X-RapidAPI-Key", "e11bbd55cemshd186130e9cc4907p1e01ddjsnd92900b7bab3")
                            .addHeader("X-RapidAPI-Host", "imdb-com.p.rapidapi.com").build();
                    return chain.proceed(modifiedRequest);
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://imdb-com.p.rapidapi.com/").client(client)
                .addConverterFactory(GsonConverterFactory.create()).build();

        api = retrofit.create(IMDBApiService.class);

        // Llamada al método que obtiene los datos de la API
        mostrarPeliculas();

        return view;
    }

    // Método que mostrará las películas o series en cuestión
    private void mostrarPeliculas() {
        Call<PopularMoviesResponse> call = api.top10("US");
        call.enqueue(new Callback<PopularMoviesResponse>() {
            @Override
            public void onResponse(Call<PopularMoviesResponse> call, Response<PopularMoviesResponse> response) {
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

                            listaPeliculas.add(movie);
                        }

                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Log.e("HomeFragment", "Ha habido un error al cargar las películas: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PopularMoviesResponse> call, Throwable t) {
                Log.e("HomeFragment", "Ha habido un error al llamar la API: " + t.getMessage());
            }
        });
    }
}
