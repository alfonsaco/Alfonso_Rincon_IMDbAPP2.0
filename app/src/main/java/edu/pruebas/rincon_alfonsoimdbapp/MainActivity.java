package edu.pruebas.rincon_alfonsoimdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import edu.pruebas.rincon_alfonsoimdbapp.utils.DateTimeUtils;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private GoogleSignInClient googleSignInClient;
    private UsuarioDAO usuarioDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        View headerView = navigationView.getHeaderView(0);
        TextView nombreTextView = headerView.findViewById(R.id.nombreEmail);
        TextView emailTextView = headerView.findViewById(R.id.email);
        ImageView imageView = headerView.findViewById(R.id.imagenEmail);

        String nombreUsuario = getIntent().getStringExtra("nombre");
        String emailUsuario = getIntent().getStringExtra("email");
        String imagenUsuario = getIntent().getStringExtra("imagen");

        nombreTextView.setText(nombreUsuario);
        emailTextView.setText(emailUsuario);
        if (imagenUsuario != null && !imagenUsuario.isEmpty()) {
            Glide.with(this).load(imagenUsuario).into(imageView);
        } else {
            imageView.setImageResource(R.mipmap.ic_launcher_round);
        }

        Button btnLogOut = headerView.findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                actualizarUltimoLogout(currentUser.getEmail());
                FirebaseAuth.getInstance().signOut();
                googleSignInClient.signOut().addOnCompleteListener(task -> {
                    LoginManager.getInstance().logOut();
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

    private void actualizarUltimoLogout(String email) {
        String timestamp = DateTimeUtils.getCurrentTimestamp();
        Usuario usuario = usuarioDAO.obtenerUsuarioPorEmail(email);
        if (usuario != null) {
            usuarioDAO.actualizarUltimoLogout(email, timestamp);
            Log.d("MainActivity", "UltimoLogout actualizado para: " + email);
        } else {
            Log.e("MainActivity", "Usuario no encontrado en la BD para actualizar UltimoLogout");
        }
    }

    // Dentro de MainActivity.java, en onStart()
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String timestamp = DateTimeUtils.getCurrentTimestamp();
            Usuario usuario = usuarioDAO.obtenerUsuarioPorEmail(currentUser.getEmail());
            if (usuario != null) {
                usuarioDAO.actualizarUltimoLogin(currentUser.getEmail(), timestamp);
                Log.d("MainActivity", "UltimoLogin actualizado para: " + currentUser.getEmail());
                // Actualización del header, etc.
            } else {
                // Usamos el UID de Firebase en el constructor
                Usuario nuevoUsuario = new Usuario(
                        currentUser.getUid(),  // UID de Firebase
                        currentUser.getDisplayName(),
                        currentUser.getEmail(),
                        timestamp,
                        "", // Último Logout vacío
                        "", // Dirección vacía
                        "", // Teléfono vacío
                        currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : ""
                );
                usuarioDAO.insertarUsuario(nuevoUsuario);
                Log.d("MainActivity", "Nuevo usuario insertado: " + currentUser.getEmail());
            }
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String timestamp = DateTimeUtils.getCurrentTimestamp();
            Usuario usuario = usuarioDAO.obtenerUsuarioPorEmail(currentUser.getEmail());
            if (usuario != null) {
                usuarioDAO.actualizarUltimoLogout(currentUser.getEmail(), timestamp);
                Log.d("MainActivity", "UltimoLogout actualizado para: " + currentUser.getEmail());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioDAO != null) {
            usuarioDAO.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, EditUserActivity.class);
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                intent.putExtra("nombre", currentUser.getDisplayName());
                intent.putExtra("email", currentUser.getEmail());
                intent.putExtra("imagen", currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "");
            }
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
