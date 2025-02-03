package edu.pruebas.rincon_alfonsoimdbapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import edu.pruebas.rincon_alfonsoimdbapp.MovieDetailsActivity;
import edu.pruebas.rincon_alfonsoimdbapp.R;
import edu.pruebas.rincon_alfonsoimdbapp.models.Movie;
import edu.pruebas.rincon_alfonsoimdbapp.sync.FavoritesSync;
import edu.pruebas.rincon_alfonsoimdbapp.utils.Constants;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    private final Context context;
    private final List<Movie> favoritas;
    private final String source;
    private final String userId;
    private final FavoritesSync favoritesSync;
    private final OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(Movie movie);
    }

    public FavoritesAdapter(Context context, List<Movie> favoritas, String userId, OnItemLongClickListener longClickListener, String source) {
        this.context = context;
        this.favoritas = favoritas;
        this.userId = userId;
        this.longClickListener = longClickListener;
        this.source = source;
        this.favoritesSync = new FavoritesSync(context);
    }

    @NonNull
    @Override
    public FavoritesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new FavoritesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesAdapter.ViewHolder holder, int position) {
        Movie pelicula = favoritas.get(position);

        if (pelicula != null) {
            holder.tituloTextView.setText(pelicula.getTitulo());
            String año = "";
            if (pelicula.getFechaSalida() != null && !pelicula.getFechaSalida().isEmpty()) {
                año = pelicula.getFechaSalida().substring(0, 4);
            }
            holder.anioTextView.setText(año);

            String rutaImagen = pelicula.getRutaPoster();
            if (rutaImagen != null && !rutaImagen.isEmpty()) {
                if (!rutaImagen.startsWith("http://") && !rutaImagen.startsWith("https://")) {
                    if (source.equals(Constants.SOURCE_TMDB)) {
                        rutaImagen = "https://image.tmdb.org/t/p/w500" + rutaImagen;
                    } else if (source.equals(Constants.SOURCE_IMD)) {
                        rutaImagen = "https://image.imdb.com" + rutaImagen;
                    }
                }
                Glide.with(context).load(rutaImagen).placeholder(R.drawable.placeholder).into(holder.posterImageView);
            } else {
                Glide.with(context).load(R.drawable.placeholder).into(holder.posterImageView);
            }

            // Manejar el Click normal para abrir detalles
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, MovieDetailsActivity.class);
                intent.putExtra("pelicula", pelicula);
                intent.putExtra("source", source);
                context.startActivity(intent);
            });

            // Manejar el clic largo para eliminar de favoritos
            holder.itemView.setOnLongClickListener(v -> {
                favoritesSync.removeMovieFromFavorites(userId, pelicula.getId());
                if (longClickListener != null) {
                    longClickListener.onItemLongClick(pelicula);
                }
                return true;
            });
        } else {
            Log.e("FavoritesAdapter", "Película en posición " + position + " es nula.");
        }
    }

    @Override
    public int getItemCount() {
        return (favoritas != null) ? favoritas.size() : 0;
    }

    public void actualizarDatos(List<Movie> nuevasFavoritas) {
        this.favoritas.clear();
        this.favoritas.addAll(nuevasFavoritas);
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
