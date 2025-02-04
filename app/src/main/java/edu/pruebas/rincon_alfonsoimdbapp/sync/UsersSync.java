package edu.pruebas.rincon_alfonsoimdbapp.sync;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;

import edu.pruebas.rincon_alfonsoimdbapp.models.Usuario;
import edu.pruebas.rincon_alfonsoimdbapp.models.UsuarioDAO;
import edu.pruebas.rincon_alfonsoimdbapp.utils.DateTimeUtils;

public class UsersSync {

    private final FirebaseFirestore firestore;
    private final UsuarioDAO usuarioDAO;

    public UsersSync(UsuarioDAO usuarioDAO) {
        this.firestore = FirebaseFirestore.getInstance();
        this.usuarioDAO = usuarioDAO;
    }

    // Método para sincronizar el login de un usuario
    public void syncUserLogin(FirebaseUser firebaseUser) {
        String email = firebaseUser.getEmail();
        String timestamp = DateTimeUtils.getCurrentTimestamp();
        Usuario usuario = new Usuario(
                firebaseUser.getUid(),
                firebaseUser.getDisplayName(),
                email,
                timestamp,
                "", // Último Logout vacío
                "", // Dirección vacía
                "", // Teléfono vacío
                firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : ""
        );

        // Actualizar Firebase
        DocumentReference userRef = firestore.collection("users").document(firebaseUser.getUid());
        userRef.set(usuario, SetOptions.merge());  // Utiliza merge para no sobrescribir datos existentes

        // Sincronizar la base de datos local
        usuarioDAO.insertarUsuario(usuario);
    }

    // Método para sincronizar el logout de un usuario
    public void syncUserLogout(FirebaseUser firebaseUser) {
        String email = firebaseUser.getEmail();
        String timestamp = DateTimeUtils.getCurrentTimestamp();

        // Actualizar Firebase
        DocumentReference userRef = firestore.collection("users").document(firebaseUser.getUid());
        userRef.update("ultimoLogout", timestamp);

        // Actualizar la base de datos local
        usuarioDAO.actualizarUltimoLogout(email, timestamp);
    }
}
