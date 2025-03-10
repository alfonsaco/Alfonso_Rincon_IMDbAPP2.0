package edu.pruebas.rincon_alfonsoimdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

import edu.pruebas.rincon_alfonsoimdbapp.models.Usuario;
import edu.pruebas.rincon_alfonsoimdbapp.models.UsuarioDAO;
import edu.pruebas.rincon_alfonsoimdbapp.utils.DateTimeUtils;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN_GOOGLE = 9001;
    private static final int RC_SIGN_IN_COLLISION = 9002;

    // Variable para almacenar las credenciales de Facebook pendientes de vincular
    private AuthCredential pendingFacebookCredential;

    // Instancia de UsuarioDAO
    private UsuarioDAO usuarioDAO;

    // Elementos para login y registro vía email y contraseña
    private Button btnLogin;
    private Button btnRegistro;
    private EditText etxtEmail;
    private EditText etxtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Inicializar vistas para login/registro por email
        btnLogin = findViewById(R.id.btnLogin);
        btnRegistro = findViewById(R.id.btnRegistrar);
        etxtEmail = findViewById(R.id.etxtEmail);
        etxtPassword = findViewById(R.id.etxtPassword);

        // Inicializar UsuarioDAO
        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();

        // Inicializar Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Configurar Google Sign-In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // Botón de Google
        SignInButton signInButton = findViewById(R.id.btnSignIn);
        ((TextView) signInButton.getChildAt(0)).setText("Sign in with Google");
        signInButton.setOnClickListener(view -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE);
        });

        // Configurar Facebook Login
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setPermissions(Arrays.asList("email", "public_profile"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Facebook login successful");
                handleFacebookAccessToken(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Facebook login canceled");
            }
            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Error en el inicio de sesión: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Facebook login error: " + error.getMessage());
            }
        });

        // Configurar los botones para login y registro vía email y contraseña
        btnLogin.setOnClickListener(v -> loginWithEmailPassword());
        btnRegistro.setOnClickListener(v -> registerWithEmailPassword());

        // Comprobar si el usuario ya está autenticado
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            insertarOActualizarUsuario(currentUser);
            irAMainActivity();
        }

        // Manejo de Insets para pantallas Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Login con email y contraseña
    private void loginWithEmailPassword() {
        String email = etxtEmail.getText().toString().trim();
        String password = etxtPassword.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser usuario = firebaseAuth.getCurrentUser();
                        if (usuario != null) {
                            insertarOActualizarUsuario(usuario);
                            irAMainActivity();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Error de autenticación: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error de login con email: " + task.getException().getMessage());
                    }
                });
    }

    // Registro con email y contraseña
    private void registerWithEmailPassword() {
        String email = etxtEmail.getText().toString().trim();
        String password = etxtPassword.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser usuario = firebaseAuth.getCurrentUser();
                        if (usuario != null) {
                            insertarOActualizarUsuario(usuario);
                            Toast.makeText(LoginActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                            irAMainActivity();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Error de registro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error de registro con email: " + task.getException().getMessage());
                    }
                });
    }

    // Método para insertar o actualizar usuario (verifica si ya existe para actualizar el login)
    private void insertarOActualizarUsuario(FirebaseUser usuarioFirebase) {
        String email = usuarioFirebase.getEmail();
        String nombre = usuarioFirebase.getDisplayName() != null ? usuarioFirebase.getDisplayName() : "Usuario";
        String timestamp = DateTimeUtils.getCurrentTimestamp();

        // Obtenemos el UID de Firebase
        String uid = usuarioFirebase.getUid();

        Usuario usuarioExistente = usuarioDAO.obtenerUsuarioPorEmail(email);
        if (usuarioExistente != null) {
            usuarioDAO.actualizarUltimoLogin(email, timestamp);
            Log.d("LoginActivity", "Login actualizado para: " + email);
        } else {
            // Insertamos el usuario con el UID correcto
            String imagen = (usuarioFirebase.getPhotoUrl() != null) ? usuarioFirebase.getPhotoUrl().toString() : "";
            usuarioDAO.insertarUsuario(new Usuario(uid, nombre, email, timestamp, "", "", "", imagen));
            Log.d("LoginActivity", "Nuevo usuario insertado: " + email);
        }
    }


    // Manejar el token de acceso de Facebook y autenticar en Firebase
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "Handling Facebook Access Token");
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase authentication successful (Facebook)");
                        FirebaseUser usuario = firebaseAuth.getCurrentUser();
                        if (usuario != null) {
                            insertarOActualizarUsuario(usuario);
                            irAMainActivity();
                        }
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            FirebaseAuthUserCollisionException collisionException = (FirebaseAuthUserCollisionException) task.getException();
                            String email = collisionException.getEmail();
                            Log.e(TAG, "FirebaseAuthUserCollisionException: " + collisionException.getMessage());
                            pendingFacebookCredential = credential;
                            showAccountCollisionDialog(email);
                        } else {
                            Toast.makeText(LoginActivity.this, "Error de autenticación con Facebook: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Firebase Authentication Failed: " + task.getException().getMessage());
                        }
                    }
                });
    }

    // Mostrar diálogo para colisión de cuentas
    private void showAccountCollisionDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cuenta existente");
        builder.setMessage("Ya existe una cuenta con el correo " + email + ". ¿Quieres vincular tu cuenta de Facebook con tu cuenta de Google?");
        builder.setPositiveButton("Sí", (dialog, which) -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN_COLLISION);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
            Toast.makeText(this, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Manejar el resultado de actividades iniciadas (Google y Facebook)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    Log.d(TAG, "Google sign-in successful");
                    autentificarFirebaseGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Error en el inicio de sesión con Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Google sign-in failed: " + e.getMessage());
            }
        } else if (requestCode == RC_SIGN_IN_COLLISION) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    Log.d(TAG, "Google sign-in for collision resolution successful");
                    autentificarFirebaseGoogleLink(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Error en el inicio de sesión con Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Google sign-in for collision resolution failed: " + e.getMessage());
            }
        }
    }

    // Autenticación con Firebase usando Google
    private void autentificarFirebaseGoogle(String idToken) {
        Log.d(TAG, "Authenticating with Firebase using Google ID Token");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Firebase authentication with Google successful");
                FirebaseUser usuario = firebaseAuth.getCurrentUser();
                if (usuario != null) {
                    insertarOActualizarUsuario(usuario);
                    irAMainActivity();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Autenticación fallida con Google", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Firebase authentication with Google failed: " + task.getException().getMessage());
            }
        });
    }

    // Autenticación con Firebase usando Google para vincular con Facebook
    private void autentificarFirebaseGoogleLink(String idToken) {
        Log.d(TAG, "Authenticating with Firebase using Google ID Token for linking");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Firebase authentication with Google successful for linking");
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && pendingFacebookCredential != null) {
                    user.linkWithCredential(pendingFacebookCredential)
                            .addOnCompleteListener(linkTask -> {
                                if (linkTask.isSuccessful()) {
                                    Log.d(TAG, "Cuenta de Facebook vinculada exitosamente");
                                    Toast.makeText(this, "Cuenta de Facebook vinculada exitosamente", Toast.LENGTH_SHORT).show();
                                    insertarOActualizarUsuario(user);
                                    irAMainActivity();
                                } else {
                                    Log.e(TAG, "Error al vincular la cuenta de Facebook: " + linkTask.getException().getMessage());
                                    Toast.makeText(this, "Error al vincular la cuenta de Facebook: " + linkTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Log.e(TAG, "No hay credenciales de Facebook pendientes para vincular");
                    Toast.makeText(this, "No se encontraron credenciales de Facebook para vincular", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Autenticación fallida durante la vinculación con Google", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Firebase authentication with Google failed during linking: " + task.getException().getMessage());
            }
        });
    }

    // Redirigir a MainActivity
    private void irAMainActivity() {
        FirebaseUser usuario = firebaseAuth.getCurrentUser();
        if (usuario != null) {
            insertarOActualizarUsuario(usuario);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("nombre", usuario.getDisplayName());
            intent.putExtra("email", usuario.getEmail());
            intent.putExtra("imagen", usuario.getPhotoUrl() != null ? usuario.getPhotoUrl().toString() : "");
            startActivity(intent);
            finish();
        } else {
            Log.e(TAG, "Usuario es null en irAMainActivity");
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
