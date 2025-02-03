package edu.pruebas.rincon_alfonsoimdbapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "users.db";
    // Versión actualizada para incluir nuevos campos
    private static final int DATABASE_VERSION = 8;

    // Tabla Usuarios y sus columnas.
    // La columna ID es de tipo TEXT y se usará como PRIMARY KEY (se asigna el UID de Firebase)
    public static final String TABLE_USUARIOS = "Usuarios";
    public static final String COLUMN_ID = "ID";  // Aquí se almacenará el UID
    public static final String COLUMN_NOMBRE = "Nombre";
    public static final String COLUMN_EMAIL = "Email";
    public static final String COLUMN_ULTIMO_LOGIN = "UltimoLogin";
    public static final String COLUMN_ULTIMO_LOGOUT = "UltimoLogout";
    // Nuevos campos:
    public static final String COLUMN_DIRECCION = "Direccion";
    public static final String COLUMN_TELEFONO = "Telefono";
    public static final String COLUMN_IMAGEN = "Imagen";

    // Sentencia SQL para crear la tabla usando el ID proporcionado (no autoincrementable)
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    COLUMN_EMAIL + " TEXT NOT NULL, " +
                    COLUMN_ULTIMO_LOGIN + " TEXT, " +
                    COLUMN_ULTIMO_LOGOUT + " TEXT, " +
                    COLUMN_DIRECCION + " TEXT, " +
                    COLUMN_TELEFONO + " TEXT, " +
                    COLUMN_IMAGEN + " TEXT" +
                    ");";

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        Log.d("DatabaseHelper", "Tabla Usuarios creada");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Se elimina y se recrea la tabla
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
        Log.d("DatabaseHelper", "Tabla Usuarios actualizada");
    }
}
