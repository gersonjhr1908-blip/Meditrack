package com.meditrack.dto;

public class RegistroPacienteRequest {

    private String nombres;
    private String apellidos;
    private String dni;
    private String correo;
    private String telefono;
    private String sexo;
    private String fechaNacimiento;
    private String contrasena;

    public RegistroPacienteRequest() {
    }

    public String nombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String apellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String dni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String correo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String telefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String sexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String fechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String contrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
