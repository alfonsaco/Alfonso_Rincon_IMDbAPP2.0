package edu.pruebas.rincon_alfonsoimdbapp.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import edu.pruebas.rincon_alfonsoimdbapp.database.UserDatabaseHelper;

public class UsuarioDAO {

    private UserDatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public UsuarioDAO(Context context) {
        dbHelper = new UserDatabaseHelper(context);
    }

    // Abrir la base de datos para escritura
    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    // Cerrar la base de datos
    public void close() {
        dbHelper.close();
    }

    // Insertar un nuevo usuario
    public long insertarUsuario(Usuario usuario) {
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.COLUMN_NOMBRE, usuario.getNombre());
        values.put(UserDatabaseHelper.COLUMN_EMAIL, usuario.getEmail());
        values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGIN, usuario.getUltimoLogin());
        values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGOUT, usuario.getUltimoLogout());
        return database.insert(UserDatabaseHelper.TABLE_USUARIOS, null, values);
    }

    // Actualizar el último login de un usuario por Email
    public int actualizarUltimoLogin(String email, String ultimoLogin) {
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGIN, ultimoLogin);
        return database.update(UserDatabaseHelper.TABLE_USUARIOS, values,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email});
    }

    // Actualizar el último logout de un usuario por Email
    public int actualizarUltimoLogout(String email, String ultimoLogout) {
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGOUT, ultimoLogout);
        return database.update(UserDatabaseHelper.TABLE_USUARIOS, values,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email});
    }

    // Obtener un usuario por Email
    public Usuario obtenerUsuarioPorEmail(String email) {
        Cursor cursor = database.query(UserDatabaseHelper.TABLE_USUARIOS,
                null,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?",
                new String[]{email},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Usuario usuario = new Usuario();
            usuario.setId(cursor.getInt(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_ID)));
            usuario.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_NOMBRE)));
            usuario.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_EMAIL)));
            usuario.setUltimoLogin(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_ULTIMO_LOGIN)));
            usuario.setUltimoLogout(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_ULTIMO_LOGOUT)));
            cursor.close();
            return usuario;
        }
        return null;
    }

    // Registrar un nuevo login: actualiza si existe; de lo contrario, inserta un nuevo registro.
    public long registrarLogin(String nombre, String email, String loginTime) {
        Usuario usuarioExistente = obtenerUsuarioPorEmail(email);
        if (usuarioExistente != null) {
            return actualizarUltimoLogin(email, loginTime);
        } else {
            ContentValues values = new ContentValues();
            values.put(UserDatabaseHelper.COLUMN_NOMBRE, nombre);
            values.put(UserDatabaseHelper.COLUMN_EMAIL, email);
            values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGIN, loginTime);
            values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGOUT, "");
            return database.insert(UserDatabaseHelper.TABLE_USUARIOS, null, values);
        }
    }

    // Eliminar un usuario por Email
    public int eliminarUsuario(String email) {
        return database.delete(UserDatabaseHelper.TABLE_USUARIOS,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email});
    }
}
