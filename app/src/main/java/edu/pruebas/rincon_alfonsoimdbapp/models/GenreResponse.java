package edu.pruebas.rincon_alfonsoimdbapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GenreResponse {

    @SerializedName("genres")
    private List<Genre> genres;

    // Getters y Setters
    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }
}
