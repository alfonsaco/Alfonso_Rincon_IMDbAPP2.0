package edu.pruebas.rincon_alfonsoimdbapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "users.db";
    // Actualizamos la versión a 4 para incluir nuevos campos
    private static final int DATABASE_VERSION = 6;

    // Tabla Usuarios y sus columnas
    public static final String TABLE_USUARIOS = "Usuarios";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_NOMBRE = "Nombre";
    public static final String COLUMN_EMAIL = "Email";
    public static final String COLUMN_ULTIMO_LOGIN = "UltimoLogin";
    public static final String COLUMN_ULTIMO_LOGOUT = "UltimoLogout";
    // Nuevas columnas:
    public static final String COLUMN_DIRECCION = "Direccion";
    public static final String COLUMN_TELEFONO = "Telefono";
    public static final String COLUMN_IMAGEN = "Imagen";

    // Sentencia SQL para crear la tabla con los nuevos campos
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
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
        // Para este ejemplo se elimina y vuelve a crear la tabla.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
        Log.d("DatabaseHelper", "Tabla Usuarios actualizada");
    }
}
