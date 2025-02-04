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

            // Desencriptar los campos de dirección y teléfono
            try {
                KeystoreManager km = new KeystoreManager();
                String direccion = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_DIRECCION));
                String telefono = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_TELEFONO));
                usuario.setDireccion(direccion != null ? km.decrypt(direccion) : "");
                usuario.setTelefono(telefono != null ? km.decrypt(telefono) : "");
            } catch (Exception e) {
                Log.e(TAG, "Error descifrando datos: " + e.getMessage());
                usuario.setDireccion("");
                usuario.setTelefono("");
            }

            usuario.setImagen(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_IMAGEN)));
            cursor.close();
            return usuario;
        }
        return null;
    }

}
