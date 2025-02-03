package edu.pruebas.rincon_alfonsoimdbapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoritesDatabaseHelper extends SQLiteOpenHelper {

    private static final String NOMBRE_DATABASE = "favoritos.db";
    private static final int DATABASE_VERSION = 3;

    // Variables estáticas para cada dato de la tabla
    public static final String NOMBRE_TABLA = "favoritos";
    public static final String COLUMNA_ID = "id";
    public static final String COLUMNA_ID_USUARIO = "id_usuario";
    public static final String COLUMNA_ID_PELICULA = "id_pelicula";
    public static final String COLUMNA_TITULO = "titulo";
    public static final String COLUMNA_RUTA_POSTER = "ruta_poster";
    public static final String COLUMNA_CALIFICACION = "calificacion";

    public FavoritesDatabaseHelper(Context context) {
        super(context, NOMBRE_DATABASE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        crearTablaFavoritos(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        eliminarTablaFavoritos(db);
        onCreate(db);
    }

    // Método para crear la tabla
    public void crearTablaFavoritos(SQLiteDatabase db) {
        String query = "CREATE TABLE " + NOMBRE_TABLA + " (" +
                COLUMNA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMNA_ID_USUARIO + " TEXT NOT NULL, " +
                COLUMNA_ID_PELICULA + " TEXT NOT NULL, " +
                COLUMNA_TITULO + " TEXT, " +
                COLUMNA_RUTA_POSTER + " TEXT, " +
                COLUMNA_CALIFICACION + " REAL," +
                "UNIQUE(" + COLUMNA_ID_USUARIO + ", " + COLUMNA_ID_PELICULA + "));";
        db.execSQL(query);
    }

    // Método para borrar la tabla
    public void eliminarTablaFavoritos(SQLiteDatabase db) {
        String query = "DROP TABLE IF EXISTS " + NOMBRE_TABLA;
        db.execSQL(query);
    }
}
