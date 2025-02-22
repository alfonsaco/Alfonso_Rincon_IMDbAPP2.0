package edu.pruebas.rincon_alfonsoimdbapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

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
    private String userEmail;
    private UsersSync usersSync;

    private static final int RC_CAMERA_PERMISSION = 100;
    private static final int RC_CAMERA = 101;


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

        btnFoto.setOnClickListener(v -> {
            String[] opciones = {"Galería", "Cámara", "URL"};
            new AlertDialog.Builder(EditUserActivity.this)
                    .setTitle("Selecciona una opción")
                    .setItems(opciones, (dialog, which) -> {
                        switch (which) {
                            case 0: // Galería
                                openGallery();
                                break;
                            case 1: // Cámara
                                openCamera();
                                break;
                            case 2: // URL
                                openUrlDialog();
                                break;
                        }
                    })
                    .show();
        });

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

            int updated = usuarioDAO.actualizarDatosUsuario(userEmail, direccion, fullPhone, selectedImageUri);
            if (updated > 0) {
                // Se incluye el parámetro "selectedImageUri" para la imagen
                usersSync.syncUserDataToFirebase(userEmail, direccion, fullPhone, selectedImageUri);
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

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imageChooserLauncher.launch(galleryIntent);
    }

    // Método para abrir la cámara
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, RC_CAMERA_PERMISSION);
        } else {
            launchCameraIntent();
        }
    }

    // Lanza el intent para tomar una foto con la cámara
    private void launchCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile;
        try {
            photoFile = createTempImageFile();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creando archivo para la cámara", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener URI para el archivo temporal usando FileProvider
        cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        startActivityForResult(cameraIntent, RC_CAMERA);
    }
    private File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // Verficiar permisos de cámara
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CAMERA && resultCode == Activity.RESULT_OK) {
            // La imagen se guardó en cameraImageUri
            if (cameraImageUri != null) {
                selectedImageUri = cameraImageUri.toString();
                Glide.with(this).load(cameraImageUri).into(imageView3);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCameraIntent();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }




    private void openUrlDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Introduce URL");

        final EditText input = new EditText(this);
        input.setHint("http://ejemplo.com/imagen.jpg");
        builder.setView(input);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            String url = input.getText().toString().trim();
            if (!url.isEmpty()) {
                selectedImageUri = url;
                Glide.with(EditUserActivity.this).load(url).into(imageView3);
            } else {
                Toast.makeText(EditUserActivity.this, "URL vacía", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private Uri createImageUri() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        return FileProvider.getUriForFile(this, "edu.pruebas.rincon_alfonsoimdbapp.fileprovider", imageFile);
    }

    private boolean isValidPhoneNumber(String phoneNumber, String countryCode) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            // Parseamos el número usando el código del país
            PhoneNumber numberProto = phoneUtil.parse(phoneNumber, countryCode);
            return phoneUtil.isValidNumberForRegion(numberProto, countryCode);

        } catch (NumberParseException e) {
            // Para evitar números demasiado grandes
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioDAO != null) {
            usuarioDAO.close();
        }
    }
}
