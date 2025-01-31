package edu.pruebas.rincon_alfonsoimdbapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.pruebas.rincon_alfonsoimdbapp.models.Movie;

/**
 * Clase encargada de gestionar las operaciones relacionadas con las películas favoritas
 * en la base de datos SQLite.
 */
public class FavoritesManager {

    // Instancia del helper para manejar la base de datos
    private final FavoritesDatabaseHelper dbHelper;

    // Constructor
    public FavoritesManager(Context context) {
        dbHelper = new FavoritesDatabaseHelper(context);
    }

    // Método para que el usuario añada una película a sus favoritos.
    public boolean añadirFavorita(String idUsuario, Movie pelicula) {
        Log.d("FavoritesManager", "Añadiendo película: " + pelicula.getTitulo() + ", ID: " + pelicula.getId() + ", Usuario: " + idUsuario);

        // Verificar si la película ya está en favoritos para evitar duplicados
        if (isFavorite(idUsuario, pelicula.getId())) {
            Log.d("FavoritesManager", "La película ya está en favoritos: " + pelicula.getTitulo());
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Preparar los valores a insertar en la base de datos
        ContentValues valores = new ContentValues();
        valores.put(FavoritesDatabaseHelper.COLUMNA_ID_USUARIO, idUsuario);
        valores.put(FavoritesDatabaseHelper.COLUMNA_ID_PELICULA, pelicula.getId());
        valores.put(FavoritesDatabaseHelper.COLUMNA_TITULO, pelicula.getTitulo());
        valores.put(FavoritesDatabaseHelper.COLUMNA_RUTA_POSTER, pelicula.getRutaPoster());
        valores.put(FavoritesDatabaseHelper.COLUMNA_CALIFICACION, pelicula.getPuntuacion());

        // Insertar la nueva película en la tabla de favoritos
        long resultado = db.insert(FavoritesDatabaseHelper.NOMBRE_TABLA, null, valores);

        if (resultado == -1) {
            // Si la inserción falla, registrar un error y cerrar la base de datos
            Log.e("FavoritesManager", "Error al insertar película en la base de datos.");
            db.close();
            return false;
        } else {
            Log.d("FavoritesManager", "Película insertada con éxito. ID de inserción: " + resultado);
        }

        // Verificamos las películas con logs
        logParaVerLasPeliculas();

        db.close();
        return true;
    }

    // Método para verificar si una película ya está en los favoritos de un usuario.
    public boolean isFavorite(String idUsuario, String movieId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Realizar consulta
        Cursor cursor = db.query(
                FavoritesDatabaseHelper.NOMBRE_TABLA,
                new String[]{FavoritesDatabaseHelper.COLUMNA_ID_PELICULA},
                FavoritesDatabaseHelper.COLUMNA_ID_USUARIO + "=? AND " + FavoritesDatabaseHelper.COLUMNA_ID_PELICULA + "=?",
                new String[]{idUsuario, movieId},
                null,
                null,
                null
        );

        // Determinar si la película existe en favoritos
        boolean existe = (cursor.getCount() > 0);

        cursor.close();
        db.close();

        return existe;
    }

    // Método para que el usuario pueda eliminar una película de sus favoritos.
    public void borrarFavorita(String idUsuario, String idPelicula) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int filasAfectadas = db.delete(FavoritesDatabaseHelper.NOMBRE_TABLA,
                FavoritesDatabaseHelper.COLUMNA_ID_USUARIO + "=? AND " + FavoritesDatabaseHelper.COLUMNA_ID_PELICULA + "=?",
                new String[]{idUsuario, idPelicula}
        );

        db.close();
    }

    // Método que devuelve todas las películas favoritas por usuario
    public List<Movie> obtenerFavoritas(String idUsuario) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Movie> favoritos = new ArrayList<>();

        // Consulta para obtener todas las películas favoritas del usuario
        Cursor cursor = db.query(
                FavoritesDatabaseHelper.NOMBRE_TABLA,
                null, //
                FavoritesDatabaseHelper.COLUMNA_ID_USUARIO + "=?",
                new String[]{idUsuario},
                null,
                null,
                FavoritesDatabaseHelper.COLUMNA_TITULO + " ASC"
        );

        // Verificar si la consulta devolvió resultados
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Obtener los datos de cada columna
                String movieId = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMNA_ID_PELICULA));
                String titulo = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMNA_TITULO));
                String rutaPoster = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMNA_RUTA_POSTER));
                float puntuacion = cursor.getFloat(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMNA_CALIFICACION));

                Movie pelicula = new Movie(
                        movieId,
                        titulo,
                        null,
                        rutaPoster,
                        null,
                        puntuacion
                );

                // Añadir la película a la lista de favoritos
                favoritos.add(pelicula);
            }
            cursor.close();
        }
        db.close();

        return favoritos;
    }

    // Método para registrar todas las películas en la base de datos por consola
    public void logParaVerLasPeliculas() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consulta para obtener todas las películas en la tabla de favoritos
        Cursor cursor = db.query(
                FavoritesDatabaseHelper.NOMBRE_TABLA,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Verificar si la consulta devolvió resultados
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Obtener los datos de cada columna para la fila actual
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMNA_ID_USUARIO));
                String movieId = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMNA_ID_PELICULA));
                String titulo = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMNA_TITULO));
                float calificacion = cursor.getFloat(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMNA_CALIFICACION));

                Log.d("FavoritesManager", "- Usuario: " + userId + ", Película: " + titulo + ", ID: " + movieId + ", Calificación: " + calificacion);
            }

            cursor.close();
        } else {
            // Registrar si no se encontraron registros en la tabla
            Log.d("FavoritesManager", "No se encontraron registros en la tabla favoritos.");
        }

        db.close();
    }
}
