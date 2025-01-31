package edu.pruebas.rincon_alfonsoimdbapp.ui.gallery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import edu.pruebas.rincon_alfonsoimdbapp.R;
import edu.pruebas.rincon_alfonsoimdbapp.adapters.FavoritesAdapter;
import edu.pruebas.rincon_alfonsoimdbapp.database.FavoritesManager;
import edu.pruebas.rincon_alfonsoimdbapp.models.Movie;
import edu.pruebas.rincon_alfonsoimdbapp.utils.Constants;

public class SearchFragment extends Fragment {

    private FavoritesManager favoritesManager;
    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private String idUsuario;

    private Button btnCompartir;
    private ActivityResultLauncher<String[]> permissionsLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar el launcher para los permisos
        permissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = result.values().stream().allMatch(granted -> granted);
                    if (allGranted) {
                        Log.d("Permisos", "Todos los permisos concedidos.");
                        mostrarDialogoJSON();
                    } else {
                        Log.d("Permisos", "Algunos permisos fueron denegados.");
                        Toast.makeText(requireContext(), "Permisos denegados. No se puede compartir.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Obtener usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            idUsuario = currentUser.getUid();
        } else {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            idUsuario = null;
        }

        // Inicializar RecyclerView y FavoritesManager
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        favoritesManager = new FavoritesManager(requireContext());
        cargarFavoritas();

        // Configurar botón Compartir
        btnCompartir = view.findViewById(R.id.btnCompartir);
        btnCompartir.setOnClickListener(v -> verificarYPedirPermisos());

        return view;
    }

    // Método para poder verifiar lo spermisos Bluetooth
    private void verificarYPedirPermisos() {
        List<String> permisosNecesarios = new ArrayList<>();

        // Verificar permisos de Bluetooth
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permisosNecesarios.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permisosNecesarios.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permisosNecesarios.add(Manifest.permission.BLUETOOTH);
            }
        }

        if (!permisosNecesarios.isEmpty()) {
            Log.d("Permisos", "Solicitando permisos: " + permisosNecesarios);
            permissionsLauncher.launch(permisosNecesarios.toArray(new String[0]));
        } else {
            Log.d("Permisos", "Todos los permisos ya están concedidos.");
            mostrarDialogoJSON();
        }
    }

    // Método para mostra rel JSOn tras haber aceptado los permisos
    private void mostrarDialogoJSON() {
        List<Movie> favorites = favoritesManager.obtenerFavoritas(idUsuario);
        // Debe haber películas para poder mostrarlo
        if (favorites.isEmpty()) {
            Toast.makeText(requireContext(), "No tienes películas favoritas", Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();
        String jsonPeliculas = gson.toJson(favorites);

        // Creamos e inflamos el diálogo
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_favoritas_json, null);
        TextView txtJSON = dialogView.findViewById(R.id.txtJSON);
        txtJSON.setText(jsonPeliculas);

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    // Se cargan las películas favoritaas en el Fragment
    private void cargarFavoritas() {
        List<Movie> peliculasFavoritas = favoritesManager.obtenerFavoritas(idUsuario);
        if (peliculasFavoritas.isEmpty()) {
            Toast.makeText(requireContext(), "No hay películas favoritas.", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("SearchFragment", "Películas favoritas cargadas.");
        }

        if (adapter == null) {
            adapter = new FavoritesAdapter(requireContext(), peliculasFavoritas, movie -> {
                favoritesManager.borrarFavorita(idUsuario, movie.getId());
                cargarFavoritas();
            }, Constants.SOURCE_IMD);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.actualizarDatos(peliculasFavoritas);
        }
    }
}
