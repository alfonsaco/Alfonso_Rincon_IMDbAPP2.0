package edu.pruebas.rincon_alfonsoimdbapp.ui.slideshow;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.pruebas.rincon_alfonsoimdbapp.MovieListActivity;
import edu.pruebas.rincon_alfonsoimdbapp.R;
import edu.pruebas.rincon_alfonsoimdbapp.models.Genre;
import edu.pruebas.rincon_alfonsoimdbapp.models.GenreResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BuscarFragment extends Fragment {

    // Claves de la API, para poder realizar las peticiones
    private static final String TAG = "SlideshowFragment";
    private static final String API_KEY = "aaf2cf26c82660c7a38d10d55ed5c92d";
    private static final String GENRES_URL = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + API_KEY + "&language=en";

    private Spinner spinner;
    private ProgressBar progressBar;
    private Button btnBuscar;
    private EditText etxtAño;

    private OkHttpClient client;
    private Gson gson;

    // Lista para almacenar los géneros
    private List<Genre> listaGeneros = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);

        spinner = root.findViewById(R.id.spinner);
        progressBar = root.findViewById(R.id.progressBar);
        btnBuscar = root.findViewById(R.id.btnBuscar);
        etxtAño = root.findViewById(R.id.etxtAño);

        // OkHttpClient y Gson
        client = new OkHttpClient();
        gson = new Gson();

        // Cargar géneros
        obtenerGenerosYCargarlos();

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fecha = etxtAño.getText().toString().trim();
                int selectedPosition = spinner.getSelectedItemPosition();

                if (selectedPosition < 0 || selectedPosition >= listaGeneros.size()) {
                    Toast.makeText(getContext(), "Seleccione un género válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                Genre selectedGenre = listaGeneros.get(selectedPosition);
                int generoId = selectedGenre.getId();
                String generoNombre = selectedGenre.getName();

                if (fecha.isEmpty()) {
                    Toast.makeText(getContext(), "Ingrese un año válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int fechaNumero = Integer.parseInt(fecha);

                    if (fechaNumero < 1895 || fechaNumero > 2025) {
                        Toast.makeText(getContext(), "Año no válido", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(getContext(), MovieListActivity.class);
                        intent.putExtra("fecha", fechaNumero);
                        intent.putExtra("generoId", generoId);
                        intent.putExtra("generoNombre", generoNombre);
                        startActivity(intent);
                    }

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Ingrese un año válido", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return root;
    }

    // Método para obtener géneros desde la API
    private void obtenerGenerosYCargarlos() {
        // Mientras se hace, se pone una progress bar, para que de un mejor efecto visual
        progressBar.setVisibility(View.VISIBLE);

        Request request = new Request.Builder().url(GENRES_URL).get().addHeader("accept", "application/json").build();
        client.newCall(request).enqueue(new Callback() {
            // Caso en el que falla la solicitud de obtención de generos
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error al cargar géneros", Toast.LENGTH_SHORT).show();
                    });
                }
            }
            // Caso en el que se obtienen correctamente
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    // Analizar JSON
                    GenreResponse genresResponse = gson.fromJson(responseBody, GenreResponse.class);
                    List<Genre> genres = genresResponse.getGenres();
                    // Se almacenan los generos en la lista creada anteriormente
                    listaGeneros = genres;

                    List<String> genreNames = new ArrayList<>();
                    for (Genre genre : genres) {
                        genreNames.add(genre.getName());
                    }

                    // Actualizar Spinner en el hilo principal. Se añaden los generos
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Se oculta el progressbar
                            progressBar.setVisibility(View.GONE);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                    android.R.layout.simple_spinner_item, genreNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);
                        });
                    }
                } else {
                    // Caso de error
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Error al cargar géneros", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
