package com.example.meditrack2.Entity;

public class Medico {

    private int id;
    private int usuarioId;
    private String cmp;
    private String especialidad;

    public Medico() {
    }

    public Medico(int id, int usuarioId, String cmp, String especialidad) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.cmp = cmp;
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

    public String getCmp() {
        return cmp;
    }

    public void setCmp(String cmp) {
        this.cmp = cmp;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }
}