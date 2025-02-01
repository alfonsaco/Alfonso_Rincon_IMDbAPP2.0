package edu.pruebas.rincon_alfonsoimdbapp.models;

public class Usuario {
    private int id;
    private String nombre;
    private String email;
    private String ultimoLogin;
    private String ultimoLogout;

    // Constructor sin id
    public Usuario(String nombre, String email, String ultimoLogin, String ultimoLogout) {
        this.nombre = nombre;
        this.email = email;
        this.ultimoLogin = ultimoLogin;
        this.ultimoLogout = ultimoLogout;
    }

    public Usuario() { }

    // Constructor con id
    public Usuario(int id, String nombre, String email, String ultimoLogin, String ultimoLogout) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.ultimoLogin = ultimoLogin;
        this.ultimoLogout = ultimoLogout;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUltimoLogin() { return ultimoLogin; }
    public void setUltimoLogin(String ultimoLogin) { this.ultimoLogin = ultimoLogin; }

    public String getUltimoLogout() { return ultimoLogout; }
    public void setUltimoLogout(String ultimoLogout) { this.ultimoLogout = ultimoLogout; }
}
