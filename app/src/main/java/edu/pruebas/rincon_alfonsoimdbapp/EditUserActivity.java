package edu.pruebas.rincon_alfonsoimdbapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.hbb20.CountryCodePicker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.pruebas.rincon_alfonsoimdbapp.models.Usuario;
import edu.pruebas.rincon_alfonsoimdbapp.models.UsuarioDAO;
import edu.pruebas.rincon_alfonsoimdbapp.sync.UsersSync;

public class EditUserActivity extends AppCompatActivity {

    private EditText etxtNombreCambio;
    private EditText etxtEmailCambio;
    private EditText etxtDireccionNueva;
    private EditText editTextPhone;
    private Button btnDireccion;
    private Button btnGuardarCambios;
    private Button btnFoto;
    private ImageView imageView3;
    private CountryCodePicker countryCodePicker;

    private String selectedImageUri = null;

    private ActivityResultLauncher<Intent> imageChooserLauncher;
    private ActivityResultLauncher<Intent> ubicacionLauncher;

    private Uri cameraImageUri = null;

    private UsuarioDAO usuarioDAO;
    private String userEmail; // se usa como clave
    private UsersSync usersSync; // Se usa para sincronizar con Firebase

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
        btnFoto = findViewById(R.id.btnFoto);
        imageView3 = findViewById(R.id.imageView3);
        countryCodePicker = findViewById(R.id.countryCodePicker);

        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();
        usersSync = new UsersSync(usuarioDAO);

        // Recibir datos de MainActivity
        String nombre = getIntent().getStringExtra("nombre");
        String email = getIntent().getStringExtra("email");
        String imagen = getIntent().getStringExtra("imagen");
        userEmail = email;

        if (nombre != null) etxtNombreCambio.setText(nombre);
        if (email != null) etxtEmailCambio.setText(email);
        if (imagen != null && !imagen.isEmpty()) {
            Glide.with(this).load(imagen).into(imageView3);
            selectedImageUri = imagen;
        }

        // Cargar datos adicionales de la BD
        Usuario usuario = usuarioDAO.obtenerUsuarioPorEmail(email);
        if (usuario != null) {
            if (usuario.getDireccion() != null && !usuario.getDireccion().isEmpty()) {
                etxtDireccionNueva.setText(usuario.getDireccion());
            }
            if (usuario.getTelefono() != null && !usuario.getTelefono().isEmpty()) {
                editTextPhone.setText(usuario.getTelefono());
            }
            if (usuario.getImagen() != null && !usuario.getImagen().isEmpty()) {
                Glide.with(this).load(usuario.getImagen()).into(imageView3);
                selectedImageUri = usuario.getImagen();
            }
        }

        imageChooserLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri resultUri = null;
                        if (data != null && data.getData() != null) {
                            resultUri = data.getData();
                        }
                        if (resultUri == null && cameraImageUri != null) {
                            resultUri = cameraImageUri;
                        }
                        if (resultUri != null) {
                            selectedImageUri = resultUri.toString();
                            Glide.with(EditUserActivity.this)
                                    .load(resultUri)
                                    .into(imageView3);
                        }
                    }
                }
        );

        ubicacionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String address = result.getData().getStringExtra("address");
                        if(address != null && !address.isEmpty()) {
                            etxtDireccionNueva.setText(address);
                        }
                    }
                }
        );

        btnFoto.setOnClickListener(v -> openImageChooser());

        btnDireccion.setOnClickListener(v -> {
            Intent intent = new Intent(EditUserActivity.this, UbicacionActivity.class);
            ubicacionLauncher.launch(intent);
        });

        btnGuardarCambios.setOnClickListener(v -> {
            String direccion = etxtDireccionNueva.getText().toString().trim();
            String telefono = editTextPhone.getText().toString().trim();
            String fullPhone = countryCodePicker.getSelectedCountryCodeWithPlus() + telefono;
            if (!isValidPhoneNumber(fullPhone, countryCodePicker.getSelectedCountryNameCode())) {
                Toast.makeText(this, "Número de teléfono inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            // Actualizar los datos en la base de datos local (SQLite)
            int updated = usuarioDAO.actualizarDatosUsuario(userEmail, direccion, fullPhone, selectedImageUri);
            if (updated > 0) {
                // Sincronizar con Firebase
                usersSync.syncUserDataToFirebase(userEmail, direccion, fullPhone);
                Toast.makeText(EditUserActivity.this, "Datos actualizados", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditUserActivity.this, "Error al actualizar datos", Toast.LENGTH_SHORT).show();
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("imagen", selectedImageUri);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void openImageChooser() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            try {
                cameraImageUri = createImageUri();
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Intent chooserIntent = Intent.createChooser(galleryIntent, "Selecciona imagen o toma foto");
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
        }
        imageChooserLauncher.launch(chooserIntent);
    }

    private Uri createImageUri() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        return FileProvider.getUriForFile(this, "edu.pruebas.rincon_alfonsoimdbapp.fileprovider", imageFile);
    }

    private boolean isValidPhoneNumber(String fullPhone, String countryCode) {
        String digits = fullPhone.replaceAll("\\D+", "");
        return digits.length() >= 10;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioDAO != null) {
            usuarioDAO.close();
        }
    }
}
