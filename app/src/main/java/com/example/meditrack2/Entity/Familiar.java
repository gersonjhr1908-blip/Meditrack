package com.example.meditrack2.Entity;

public class Familiar {

    private int id;
    private int usuarioId;

    public Familiar() {
    }

    public Familiar(int id, int usuarioId) {
        this.id = id;
        this.usuarioId = usuarioId;
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
}