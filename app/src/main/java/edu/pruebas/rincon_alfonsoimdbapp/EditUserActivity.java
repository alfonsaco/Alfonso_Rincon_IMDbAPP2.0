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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.pruebas.rincon_alfonsoimdbapp.models.Usuario;
import edu.pruebas.rincon_alfonsoimdbapp.models.UsuarioDAO;

public class EditUserActivity extends AppCompatActivity {

    private EditText etxtNombreCambio;
    private EditText etxtEmailCambio;
    private EditText etxtDireccionNueva;
    private EditText editTextPhone;
    private Button btnDireccion;
    private Button btnGuardarCambios;
    private Button btnFoto;
    private ImageView imageView3;

    // Almacenará la URI seleccionada (como String)
    private String selectedImageUri = null;

    // Launcher para el chooser que combine cámara y galería
    private ActivityResultLauncher<Intent> imageChooserLauncher;

    // Para almacenar temporalmente la URI de la foto tomada con la cámara
    private Uri cameraImageUri = null;

    // UsuarioDAO para cargar y guardar datos del usuario
    private UsuarioDAO usuarioDAO;
    private String userEmail; // se usará el email como clave

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_user);

        // Inicializar vistas
        etxtNombreCambio = findViewById(R.id.etxtNombreCambio);
        etxtEmailCambio = findViewById(R.id.etxtEmailCambio);
        etxtDireccionNueva = findViewById(R.id.etxtDireccionNueva);
        editTextPhone = findViewById(R.id.editTextPhone);
        btnDireccion = findViewById(R.id.btnDireccion);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        btnFoto = findViewById(R.id.btnFoto);
        imageView3 = findViewById(R.id.imageView3);

        // Inicializar el DAO
        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();

        // Recuperar datos enviados desde MainActivity (por ejemplo, en el Intent inicial)
        String nombre = getIntent().getStringExtra("nombre");
        String email = getIntent().getStringExtra("email");
        String imagen = getIntent().getStringExtra("imagen");
        userEmail = email;  // clave para actualizar en la BD

        if (nombre != null) {
            etxtNombreCambio.setText(nombre);
        }
        if (email != null) {
            etxtEmailCambio.setText(email);
        }
        // Se usa la imagen que viene en el Intent (por defecto)
        if (imagen != null && !imagen.isEmpty()) {
            Glide.with(this)
                    .load(imagen)
                    .into(imageView3);
            selectedImageUri = imagen;
        }

        // Consultar en la BD los datos actuales (dirección, teléfono e imagen) para sobreescribir los valores
        Usuario usuario = usuarioDAO.obtenerUsuarioPorEmail(email);
        if (usuario != null) {
            if (usuario.getDireccion() != null && !usuario.getDireccion().isEmpty()) {
                etxtDireccionNueva.setText(usuario.getDireccion());
            }
            if (usuario.getTelefono() != null && !usuario.getTelefono().isEmpty()) {
                editTextPhone.setText(usuario.getTelefono());
            }
            // Si en la BD se ha actualizado la imagen, la mostramos
            if (usuario.getImagen() != null && !usuario.getImagen().isEmpty()) {
                Glide.with(this).load(usuario.getImagen()).into(imageView3);
                selectedImageUri = usuario.getImagen();
            }
        }

        // Registrar el launcher para el chooser de imagen (cámara y galería)
        imageChooserLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri resultUri = null;
                        // Intent para obtener imagen desde la galería (ACTION_GET_CONTENT)
                        if (data != null && data.getData() != null) {
                            resultUri = data.getData();
                        }
                        // Si no se obtuvo desde la galería, se asume que fue de la cámara
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

        // Al pulsar el botón de foto se abre el chooser típico del sistema (con cámara y galería)
        btnFoto.setOnClickListener(v -> openImageChooser());

        // Al pulsar "Guardar Cambios", se actualizan los datos en la base de datos
        btnGuardarCambios.setOnClickListener(v -> {
            String direccion = etxtDireccionNueva.getText().toString().trim();
            String telefono = editTextPhone.getText().toString().trim();

            int updated = usuarioDAO.actualizarDatosUsuario(userEmail, direccion, telefono, selectedImageUri);
            if (updated > 0) {
                Toast.makeText(EditUserActivity.this, "Datos actualizados", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditUserActivity.this, "Error al actualizar datos", Toast.LENGTH_SHORT).show();
            }
            // Enviar el resultado a MainActivity para que actualice la imagen
            Intent resultIntent = new Intent();
            resultIntent.putExtra("imagen", selectedImageUri);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        // Manejo de insets para pantallas Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Abre un chooser que permite al usuario elegir entre tomar una foto o seleccionar una imagen.
     * Se utilizan intenciones típicas: ACTION_IMAGE_CAPTURE para cámara y ACTION_GET_CONTENT para galería.
     */
    private void openImageChooser() {
        // Intent para la galería usando ACTION_GET_CONTENT
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");

        // Intent para la cámara
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            try {
                cameraImageUri = createImageUri();
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Combinar ambas intenciones en un chooser
        Intent chooserIntent = Intent.createChooser(galleryIntent, "Selecciona imagen o toma foto");
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
        }
        imageChooserLauncher.launch(chooserIntent);
    }

    /**
     * Crea un URI temporal para almacenar la foto tomada con la cámara.
     * Es imprescindible tener configurado el FileProvider en el Manifest y un archivo file_paths.xml.
     */
    private Uri createImageUri() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        return FileProvider.getUriForFile(this, "edu.pruebas.rincon_alfonsoimdbapp.fileprovider", imageFile);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioDAO != null) {
            usuarioDAO.close();
        }
    }
}
