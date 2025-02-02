package edu.pruebas.rincon_alfonsoimdbapp.models;

public class Usuario {
    private int id;
    private String nombre;
    private String email;
    private String ultimoLogin;
    private String ultimoLogout;
    // Nuevos campos
    private String direccion;
    private String telefono;
    private String imagen;

    // Constructor sin id, con nuevos campos
    public Usuario(String nombre, String email, String ultimoLogin, String ultimoLogout,
                   String direccion, String telefono, String imagen) {
        this.nombre = nombre;
        this.email = email;
        this.ultimoLogin = ultimoLogin;
        this.ultimoLogout = ultimoLogout;
        this.direccion = direccion;
        this.telefono = telefono;
        this.imagen = imagen;
    }

    public Usuario() { }

    // Constructor con id
    public Usuario(int id, String nombre, String email, String ultimoLogin, String ultimoLogout,
                   String direccion, String telefono, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.ultimoLogin = ultimoLogin;
        this.ultimoLogout = ultimoLogout;
        this.direccion = direccion;
        this.telefono = telefono;
        this.imagen = imagen;
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

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
}
