package edu.pruebas.rincon_alfonsoimdbapp.models;

import java.sql.Date;

public class Usuario {
    private int id;
    private String nombre;
    private String email;
    private long ultimoLogin;   // Almacenado como timestamp
    private long ultimoLogout;  // Almacenado como timestamp

    // Constructores
    public Usuario() {}

    public Usuario(String nombre, String email, long ultimoLogin, long ultimoLogout) {
        this.nombre = nombre;
        this.email = email;
        this.ultimoLogin = ultimoLogin;
        this.ultimoLogout = ultimoLogout;
    }

    public Usuario(int id, String nombre, String email, long ultimoLogin, long ultimoLogout) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.ultimoLogin = ultimoLogin;
        this.ultimoLogout = ultimoLogout;
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getUltimoLogin() {
        return ultimoLogin;
    }

    public void setUltimoLogin(long ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }

    public long getUltimoLogout() {
        return ultimoLogout;
    }

    public void setUltimoLogout(long ultimoLogout) {
        this.ultimoLogout = ultimoLogout;
    }
}