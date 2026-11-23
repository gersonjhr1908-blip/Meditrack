package com.example.meditrack2.Entity;

public class Enfermera {

    private int id;
    private int usuarioId;
    private String colegiatura;
    private String especialidad;

    public Enfermera() {
    }

    public Enfermera(int id, int usuarioId, String colegiatura, String especialidad) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.colegiatura = colegiatura;
        this.especialidad = especialidad;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getColegiatura() {
        return colegiatura;
    }

    public void setColegiatura(String colegiatura) {
        this.colegiatura = colegiatura;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }
}