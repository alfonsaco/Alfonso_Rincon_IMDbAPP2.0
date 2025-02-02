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

    // Insertar un nuevo usuario (se insertan también los nuevos campos, que pueden ser vacíos)
    public long insertarUsuario(Usuario usuario) {
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.COLUMN_NOMBRE, usuario.getNombre());
        values.put(UserDatabaseHelper.COLUMN_EMAIL, usuario.getEmail());
        values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGIN, usuario.getUltimoLogin());
        values.put(UserDatabaseHelper.COLUMN_ULTIMO_LOGOUT, usuario.getUltimoLogout());
        values.put(UserDatabaseHelper.COLUMN_DIRECCION, usuario.getDireccion());
        values.put(UserDatabaseHelper.COLUMN_TELEFONO, usuario.getTelefono());
        values.put(UserDatabaseHelper.COLUMN_IMAGEN, usuario.getImagen());
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

    // Actualizar los datos del usuario: dirección, teléfono e imagen
    public int actualizarDatosUsuario(String email, String direccion, String telefono, String imagen) {
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.COLUMN_DIRECCION, direccion);
        values.put(UserDatabaseHelper.COLUMN_TELEFONO, telefono);
        values.put(UserDatabaseHelper.COLUMN_IMAGEN, imagen);
        return database.update(UserDatabaseHelper.TABLE_USUARIOS, values,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email});
    }

    // Obtener un usuario por Email (incluyendo los nuevos campos)
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
            usuario.setDireccion(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_DIRECCION)));
            usuario.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_TELEFONO)));
            usuario.setImagen(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_IMAGEN)));
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
            values.put(UserDatabaseHelper.COLUMN_DIRECCION, "");
            values.put(UserDatabaseHelper.COLUMN_TELEFONO, "");
            values.put(UserDatabaseHelper.COLUMN_IMAGEN, "");
            return database.insert(UserDatabaseHelper.TABLE_USUARIOS, null, values);
        }
    }

    // Eliminar un usuario por Email
    public int eliminarUsuario(String email) {
        return database.delete(UserDatabaseHelper.TABLE_USUARIOS,
                UserDatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email});
    }
}
