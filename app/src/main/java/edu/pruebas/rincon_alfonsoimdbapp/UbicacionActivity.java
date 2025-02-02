package edu.pruebas.rincon_alfonsoimdbapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class UbicacionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private AutocompleteSupportFragment autocompleteFragment;
    private Button btnConfirmarUbi;
    // Variable para almacenar la dirección seleccionada
    private String selectedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ubicacion);

        btnConfirmarUbi = findViewById(R.id.btnConfirmarUbi);

        // Aplicar insets para pantallas Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa Places (si aún no se ha hecho)
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        // Obtener el SupportMapFragment y solicitar la carga del mapa
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Obtener el AutocompleteSupportFragment y configurarlo
        FragmentManager fragmentManager = getSupportFragmentManager();
        autocompleteFragment = (AutocompleteSupportFragment)
                fragmentManager.findFragmentById(R.id.autocomplete_fragment);
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS));
            autocompleteFragment.setHint("Buscar dirección");

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    // Guarda la dirección seleccionada
                    selectedAddress = place.getAddress();
                    if (mMap != null && place.getLatLng() != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15f));
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(UbicacionActivity.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Al pulsar el botón confirmar, se devuelve la dirección seleccionada
        btnConfirmarUbi.setOnClickListener(v -> {
            if (!selectedAddress.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("address", selectedAddress);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(UbicacionActivity.this, "Selecciona una dirección primero", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Opcional: establecer una ubicación inicial por defecto
        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.416775, -3.703790), 12f));
    }
}
