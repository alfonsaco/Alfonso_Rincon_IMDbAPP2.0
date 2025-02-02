package edu.pruebas.rincon_alfonsoimdbapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class EditUserActivity extends AppCompatActivity {

    EditText etxtNombreCambio;
    EditText etxtEmailCambio;
    EditText etxtDireccionNueva;
    EditText editTextPhone;
    Button btnDireccion;
    Button btnGuardarCambios;
    Button button4;
    ImageView imageView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_user);

        etxtNombreCambio = findViewById(R.id.etxtNombreCambio);
        etxtEmailCambio = findViewById(R.id.etxtEmailCambio);
        etxtDireccionNueva = findViewById(R.id.etxtDireccionNueva);
        editTextPhone = findViewById(R.id.editTextPhone);
        btnDireccion = findViewById(R.id.btnDireccion);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        button4 = findViewById(R.id.button4);
        imageView3 = findViewById(R.id.imageView3);

        // Recuperar datos enviados desde la MainActivity
        String nombre = getIntent().getStringExtra("nombre");
        String email = getIntent().getStringExtra("email");
        String imagen = getIntent().getStringExtra("imagen");

        // Asignar el nombre y el email a los EditText correspondientes
        if (nombre != null) {
            etxtNombreCambio.setText(nombre);
        }
        if (email != null) {
            etxtEmailCambio.setText(email);
        }

        // Cargar la imagen del usuario en el ImageView usando Glide (si se pasó el dato)
        if (imagen != null) {
            Glide.with(this)
                    .load(imagen)
                    .into(imageView3);
        }

        // Manejo de insets para pantallas Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
