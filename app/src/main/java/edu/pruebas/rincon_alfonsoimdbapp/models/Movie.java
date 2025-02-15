package edu.pruebas.rincon_alfonsoimdbapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class Movie implements Parcelable {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String titulo;

    @SerializedName("release_date")
    private String fechaSalida;

    @SerializedName("poster_path")
    private String rutaPoster;

    @SerializedName("overview")
    private String descripcion;

    @SerializedName("vote_average")
    private float puntuacion;

    // Constructores
    public Movie() {
    }
    public Movie(String id, String titulo, String fechaSalida, String rutaPoster, String descripcion, float puntuacion) {
        this.id = id;
        this.titulo = titulo;
        this.fechaSalida = fechaSalida;
        this.rutaPoster = rutaPoster;
        this.descripcion = descripcion;
        this.puntuacion = puntuacion;
    }

    // Constructor para el Parcel
    protected Movie(Parcel in) {
        id = in.readString();
        titulo = in.readString();
        fechaSalida = in.readString();
        rutaPoster = in.readString();
        descripcion = in.readString();
        puntuacion = in.readFloat();
    }

    // Métodos para la serialización con Parcelable
    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(String fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public String getRutaPoster() {
        return rutaPoster;
    }

    public void setRutaPoster(String rutaPoster) {
        this.rutaPoster = rutaPoster;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public float getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(float puntuacion) {
        this.puntuacion = puntuacion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(titulo);
        dest.writeString(fechaSalida);
        dest.writeString(rutaPoster);
        dest.writeString(descripcion);
        dest.writeFloat(puntuacion);
    }
}
