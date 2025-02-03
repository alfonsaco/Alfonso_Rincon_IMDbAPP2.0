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

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertarUsuario(Usuario usuario) {
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.COLUMN_ID, usuario.getId());
        values.put(UserDatabaseHelper.COLUMN_NOMBRE, usuario.getNombre());
        values.put(UserDatabaseHelper.COLUMN_EMAIL, usuario.getEmail());
        values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGIN, usuario.getUltimoLogin());
        values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGOUT, usuario.getUltimoLogout());

        try {
            KeystoreManager km = new KeystoreManager();
            String encryptedDireccion = (usuario.getDireccion() != null && !usuario.getDireccion().isEmpty())
                    ? km.encrypt(usuario.getDireccion()) : "";
            String encryptedTelefono = (usuario.getTelefono() != null && !usuario.getTelefono().isEmpty())
                    ? km.encrypt(usuario.getTelefono()) : "";
            values.put(UserDatabaseHelper.COLUMN_DIRECCION, encryptedDireccion);
            values.put(UserDatabaseHelper.COLUMN_TELEFONO, encryptedTelefono);
        } catch (Exception e) {
            Log.e(TAG, "Error cifrando datos en insertarUsuario: " + e.getMessage());
            e.printStackTrace();
            values.put(UserDatabaseHelper.COLUMN_DIRECCION, "");
            values.put(UserDatabaseHelper.COLUMN_TELEFONO, "");
        }
        values.put(UserDatabaseHelper.COLUMN_IMAGEN, usuario.getImagen());
        return database.insert(UserDatabaseHelper.TABLE_USUARIOS, null, values);
    }


    public int actualizarUltimoLogin(String email, String ultimoLogin) {
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGIN, ultimoLogin);
        return database.update(UserDatabaseHelper.TABLE_USUARIOS, values,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email});
    }

    public int actualizarUltimoLogout(String email, String ultimoLogout) {
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGOUT, ultimoLogout);
        return database.update(UserDatabaseHelper.TABLE_USUARIOS, values,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email});
    }

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

    public Usuario obtenerUsuarioPorEmail(String email) {
        Cursor cursor = database.query(UserDatabaseHelper.TABLE_USUARIOS,
                null,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?",
                new String[]{email},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Usuario usuario = new Usuario();
            // Recuperamos el ID (UID) como String
            usuario.setId(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_ID)));
            usuario.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_NOMBRE)));
            usuario.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_EMAIL)));
            usuario.setUltimoLogin(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_ULTIMO_LOGIN)));
            usuario.setUltimoLogout(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_ULTIMO_LOGOUT)));
            String encryptedDireccion = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_DIRECCION));
            String encryptedTelefono = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_TELEFONO));
            usuario.setImagen(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_IMAGEN)));
            try {
                KeystoreManager km = new KeystoreManager();
                String direccion = (encryptedDireccion != null && !encryptedDireccion.isEmpty()) ? km.decrypt(encryptedDireccion) : "";
                String telefono = (encryptedTelefono != null && !encryptedTelefono.isEmpty()) ? km.decrypt(encryptedTelefono) : "";
                usuario.setDireccion(direccion);
                usuario.setTelefono(telefono);
                Log.d(TAG, "ObtenerUsuario - Dirección descifrada: " + direccion);
                Log.d(TAG, "ObtenerUsuario - Teléfono descifrado: " + telefono);
            } catch (Exception e) {
                Log.e(TAG, "Error descifrando datos en obtenerUsuarioPorEmail: " + e.getMessage());
                e.printStackTrace();
                usuario.setDireccion("");
                usuario.setTelefono("");
            }
            cursor.close();
            return usuario;
        }
        return null;
    }

    public long registrarLogin(String nombre, String email, String loginTime, String uid) {
        // Se espera que uid sea el Firebase UID
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

    public int eliminarUsuario(String email) {
        return database.delete(UserDatabaseHelper.TABLE_USUARIOS,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email});
    }
}
