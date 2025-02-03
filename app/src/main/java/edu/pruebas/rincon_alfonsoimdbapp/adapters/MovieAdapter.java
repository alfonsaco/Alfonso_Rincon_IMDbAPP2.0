package edu.pruebas.rincon_alfonsoimdbapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.pruebas.rincon_alfonsoimdbapp.MovieDetailsActivity;
import edu.pruebas.rincon_alfonsoimdbapp.R;
import edu.pruebas.rincon_alfonsoimdbapp.database.FavoritesManager;
import edu.pruebas.rincon_alfonsoimdbapp.models.Movie;
import edu.pruebas.rincon_alfonsoimdbapp.sync.FavoritesSync;
import edu.pruebas.rincon_alfonsoimdbapp.utils.Constants;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private final Context context;
    private final List<Movie> peliculas;
    private final String source;
    private final FavoritesManager favoritesManager;
    private final String userId;
    private final FavoritesSync favoritesSync;

    // Constructor
    public MovieAdapter(Context context, List<Movie> peliculas, String source, String userId) {
        this.context = context;
        this.peliculas = peliculas;
        this.source = source;
        this.favoritesManager = new FavoritesManager(context);
        this.userId = userId;
        this.favoritesSync = new FavoritesSync(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie pelicula = peliculas.get(position);

        if (pelicula != null) {
            holder.tituloTextView.setText(pelicula.getTitulo());
            String año = "Año no disponible";
            if (pelicula.getFechaSalida() != null && !pelicula.getFechaSalida().isEmpty()) {
                año = pelicula.getFechaSalida().substring(0, 4);
            }
            holder.anioTextView.setText(año);

            // Cargar la imagen del póster usando Glide
            String rutaImagen = pelicula.getRutaPoster();
            if (rutaImagen != null && !rutaImagen.isEmpty()) {
                if (!rutaImagen.startsWith("http://") && !rutaImagen.startsWith("https://")) {
                    if (source.equals(Constants.SOURCE_TMDB)) {
                        rutaImagen = "https://image.tmdb.org/t/p/w500" + rutaImagen;
                    } else if (source.equals(Constants.SOURCE_IMD)) {
                        rutaImagen = "https://image.imdb.com" + rutaImagen;
                    }
                }
                Glide.with(context)
                        .load(rutaImagen)
                        .into(holder.posterImageView);
            }

            // Manejar el Click normal para abrir detalles
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, MovieDetailsActivity.class);
                intent.putExtra("pelicula", pelicula);
                intent.putExtra("source", source);
                context.startActivity(intent);
            });

            // Manejar el clic largo para añadir a favoritos
            holder.itemView.setOnLongClickListener(v -> {
                FirebaseUser usuarioFirebase = FirebaseAuth.getInstance().getCurrentUser();
                if (usuarioFirebase != null) {
                    // Añadir o eliminar la película de favoritos
                    if (!favoritesManager.isFavorite(userId, pelicula.getId())) {
                        // Añadir a favoritos
                        favoritesSync.addMovieToFavorites(userId, pelicula);
                        favoritesManager.añadirFavorita(userId, pelicula);
                        Toast.makeText(context, "Película añadida a favoritos", Toast.LENGTH_SHORT).show();
                        Log.d("MovieAdapter", "Película añadida a favoritos: " + pelicula.getTitulo());
                    } else {
                        Toast.makeText(context, "Esta película ya está en favoritos", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            });
        } else {
            Log.e("MovieAdapter", "Película en posición " + position + " es nula.");
        }
    }

    @Override
    public int getItemCount() {
        return (peliculas != null) ? peliculas.size() : 0;
    }

    // Método para actualizar la lista de películas
    public void setMovies(List<Movie> newPeliculas) {
        this.peliculas.clear();
        this.peliculas.addAll(newPeliculas);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tituloTextView;
        TextView anioTextView;
        ImageView posterImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tituloTextView = itemView.findViewById(R.id.txtTituloPelicula);
            anioTextView = itemView.findViewById(R.id.txtAñoPelicula);
            posterImageView = itemView.findViewById(R.id.imageViewPoster);
        }
    }
}
