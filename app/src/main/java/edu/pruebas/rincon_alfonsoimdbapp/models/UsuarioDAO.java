package edu.pruebas.rincon_alfonsoimdbapp.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import edu.pruebas.rincon_alfonsoimdbapp.database.UserDatabaseHelper;
import edu.pruebas.rincon_alfonsoimdbapp.KeystoreManager;

public class UsuarioDAO {

    private UserDatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private static final String TAG = "UsuarioDAO";

    public UsuarioDAO(Context context) {
        dbHelper = new UserDatabaseHelper(context);
    }

    // Abrir la base de datos en modo escritura
    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    // Cerrar la base de datos
    public void close() {
        dbHelper.close();
    }

    // Insertar un nuevo usuario en la base de datos
    public long insertarUsuario(Usuario usuario) {
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.COLUMN_ID, usuario.getId());
        values.put(UserDatabaseHelper.COLUMN_NOMBRE, usuario.getNombre());
        values.put(UserDatabaseHelper.COLUMN_EMAIL, usuario.getEmail());
        values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGIN, usuario.getUltimoLogin());
        values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGOUT, usuario.getUltimoLogout());
        values.put(UserDatabaseHelper.COLUMN_DIRECCION, usuario.getDireccion());
        values.put(UserDatabaseHelper.COLUMN_TELEFONO, usuario.getTelefono());
        values.put(UserDatabaseHelper.COLUMN_IMAGEN, usuario.getImagen());
        return database.insert(UserDatabaseHelper.TABLE_USUARIOS, null, values);
    }

    // Actualizar el último login de un usuario
    public int actualizarUltimoLogin(String email, String ultimoLogin) {
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGIN, ultimoLogin);
        return database.update(UserDatabaseHelper.TABLE_USUARIOS, values,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email});
    }

    // Actualizar el último logout de un usuario
    public int actualizarUltimoLogout(String email, String ultimoLogout) {
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGOUT, ultimoLogout);
        return database.update(UserDatabaseHelper.TABLE_USUARIOS, values,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email});
    }

    // Actualizar la dirección, teléfono y la imagen del usuario
    public int actualizarDatosUsuario(String email, String direccion, String telefono, String imagen) {
        ContentValues values = new ContentValues();
        try {
            KeystoreManager km = new KeystoreManager();
            String encryptedDireccion = (direccion != null && !direccion.isEmpty()) ? km.encrypt(direccion) : "";
            String encryptedTelefono = (telefono != null && !telefono.isEmpty()) ? km.encrypt(telefono) : "";
            values.put(UserDatabaseHelper.COLUMN_DIRECCION, encryptedDireccion);
            values.put(UserDatabaseHelper.COLUMN_TELEFONO, encryptedTelefono);
            Log.d(TAG, "ActualizarDatosUsuario - Dirección cifrada: " + encryptedDireccion);
            Log.d(TAG, "ActualizarDatosUsuario - Teléfono cifrado: " + encryptedTelefono);
        } catch (Exception e) {
            Log.e(TAG, "Error cifrando datos en actualizarDatosUsuario: " + e.getMessage());
            e.printStackTrace();
            values.put(UserDatabaseHelper.COLUMN_DIRECCION, "");
            values.put(UserDatabaseHelper.COLUMN_TELEFONO, "");
        }
        values.put(UserDatabaseHelper.COLUMN_IMAGEN, imagen);
        return database.update(UserDatabaseHelper.TABLE_USUARIOS, values,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email});
    }

    // Obtener un usuario a partir de su email
    public Usuario obtenerUsuarioPorEmail(String email) {
        Cursor cursor = database.query(UserDatabaseHelper.TABLE_USUARIOS,
                null,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?",
                new String[]{email},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Usuario usuario = new Usuario();
            usuario.setId(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_ID)));
            usuario.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_NOMBRE)));
            usuario.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_EMAIL)));
            usuario.setUltimoLogin(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_ULTIMO_LOGIN)));
            usuario.setUltimoLogout(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_ULTIMO_LOGOUT)));
            usuario.setDireccion(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_DIRECCION)));
            usuario.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_TELEFONO)));
            usuario.setImagen(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_IMAGEN)));
            cursor.close();
            return usuario;
        }
        return null;
    }

    // Obtener todos los usuarios de la base de datos
    public Cursor obtenerTodosUsuarios() {
        return database.query(UserDatabaseHelper.TABLE_USUARIOS,
                null,
                null, null, null, null, null);
    }

    // Eliminar un usuario a partir de su email
    public int eliminarUsuario(String email) {
        return database.delete(UserDatabaseHelper.TABLE_USUARIOS,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email});
    }

    // Verificar si existe un usuario por email
    public boolean existeUsuarioPorEmail(String email) {
        Cursor cursor = database.query(UserDatabaseHelper.TABLE_USUARIOS,
                null,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?",
                new String[]{email},
                null, null, null);
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    // Registrar un login si el usuario no existe o actualizar su login si ya existe
    public long registrarLogin(String nombre, String email, String loginTime, String uid) {
        Usuario usuarioExistente = obtenerUsuarioPorEmail(email);
        if (usuarioExistente != null) {
            return actualizarUltimoLogin(email, loginTime);
        } else {
            ContentValues values = new ContentValues();
            values.put(UserDatabaseHelper.COLUMN_ID, uid);
            values.put(UserDatabaseHelper.COLUMN_NOMBRE, nombre);
            values.put(UserDatabaseHelper.COLUMN_EMAIL, email);
            values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGIN, loginTime);
            values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGOUT, "");
            values.put(UserDatabaseHelper.COLUMN_DIRECCION, "");
            values.put(UserDatabaseHelper.COLUMN_TELEFONO, "");
            values.put(UserDatabaseHelper.COLUMN_IMAGEN, "");
            return database.insert(UserDatabaseHelper.TABLE_USUARIOS, null, values);
        }
    }
}
