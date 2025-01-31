package edu.pruebas.rincon_alfonsoimdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import edu.pruebas.rincon_alfonsoimdbapp.databinding.ActivityMainBinding;
import edu.pruebas.rincon_alfonsoimdbapp.models.Usuario;
import edu.pruebas.rincon_alfonsoimdbapp.models.UsuarioDAO;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private GoogleSignInClient googleSignInClient;

    // Instancia de UsuarioDAO
    private UsuarioDAO usuarioDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar UsuarioDAO
        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();

        // Inicializar GoogleSignInClient
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // OBTENER LOS DATOS DEL INTENT
        // Se obtienen las vistas del nav_header. Se utilizará esto para acceder a los elementos
        View headerView = navigationView.getHeaderView(0);

        TextView nombreTextView = headerView.findViewById(R.id.nombreEmail);
        TextView emailTextView = headerView.findViewById(R.id.email);
        ImageView imageView = headerView.findViewById(R.id.imagenEmail);
        // Se obtienen los datos de los intents
        String nombreUsuario = getIntent().getStringExtra("nombre");
        String emailUsuario = getIntent().getStringExtra("email");
        String imagenUsuario = getIntent().getStringExtra("imagen");

        // Ponemos los datos en los componentes
        nombreTextView.setText(nombreUsuario);
        emailTextView.setText(emailUsuario);
        if (imagenUsuario != null) {
            Glide.with(this).load(imagenUsuario).into(imageView);
        } else {
            imageView.setImageResource(R.mipmap.ic_launcher_round);
        }

        // Botón de LogOut
        Button btnLogOut = headerView.findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // Actualizar UltimoLogout
                actualizarUltimoLogout(currentUser.getEmail());

                // Cerrar sesión en Firebase
                FirebaseAuth.getInstance().signOut();

                // Cerrar sesión en Google
                googleSignInClient.signOut().addOnCompleteListener(task -> {
                    // Cerrar sesión en Facebook
                    LoginManager.getInstance().logOut();

                    // Redirigir al usuario al LoginActivity
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            } else {
                Log.e("MainActivity", "No hay usuario actual para cerrar sesión");
            }
        });

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    // Método para actualizar UltimoLogout
    private void actualizarUltimoLogout(String email) {
        long timestamp = System.currentTimeMillis();
        Usuario usuario = usuarioDAO.obtenerUsuarioPorEmail(email);
        if (usuario != null) {
            usuarioDAO.actualizarUltimoLogout(email, timestamp);
            Log.d("MainActivity", "UltimoLogout actualizado para: " + email);
        } else {
            Log.e("MainActivity", "Usuario no encontrado en la base de datos para actualizar UltimoLogout");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Aplicación entró en primer plano
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            long timestamp = System.currentTimeMillis();
            Usuario usuario = usuarioDAO.obtenerUsuarioPorEmail(currentUser.getEmail());
            if (usuario != null) {
                usuarioDAO.actualizarUltimoLogin(currentUser.getEmail(), timestamp);
                Log.d("MainActivity", "UltimoLogin actualizado para: " + currentUser.getEmail());
            } else {
                // Insertar nuevo usuario si no existe
                Usuario nuevoUsuario = new Usuario(currentUser.getDisplayName(), currentUser.getEmail(), timestamp, 0);
                usuarioDAO.insertarUsuario(nuevoUsuario);
                Log.d("MainActivity", "Nuevo usuario insertado: " + currentUser.getEmail());
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Aplicación pasó a segundo plano
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            long timestamp = System.currentTimeMillis();
            Usuario usuario = usuarioDAO.obtenerUsuarioPorEmail(currentUser.getEmail());
            if (usuario != null) {
                usuarioDAO.actualizarUltimoLogout(currentUser.getEmail(), timestamp);
                Log.d("MainActivity", "UltimoLogout actualizado para: " + currentUser.getEmail());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; este método agrega elementos al action bar si está presente.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // Cerrar UsuarioDAO al destruir la actividad
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioDAO != null) {
            usuarioDAO.close();
        }
    }
}
