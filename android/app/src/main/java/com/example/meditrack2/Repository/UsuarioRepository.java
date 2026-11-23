package com.example.meditrack2.Repository;

import com.example.meditrack2.Entity.Paciente;
import com.example.meditrack2.Entity.Usuario;
import com.example.meditrack2.Entity.Familiar;

import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {

    private static final List<Usuario> usuarios = new ArrayList<>();
    private static final List<Paciente> pacientes = new ArrayList<>();
    private static final List<Familiar> familiares = new ArrayList<>();

    public static boolean existeDni(String dni) {

        for (Usuario usuario : usuarios) {

            if (usuario.getDni().equalsIgnoreCase(dni)) {
                return true;
            }
        }

        return false;
    }

    public static boolean existeCorreo(String correo) {

        for (Usuario usuario : usuarios) {

            if (usuario.getCorreo().equalsIgnoreCase(correo)) {
                return true;
            }
        }

        return false;
    }

    public static void agregarUsuario(Usuario usuario) {
        usuarios.add(usuario);
    }

    public static void agregarPaciente(Paciente paciente) {
        pacientes.add(paciente);
    }
    public static void agregarFamiliar(Familiar familiar) {
        familiares.add(familiar);
    }
    public static List<Usuario> obtenerUsuarios() {
        return usuarios;
    }

    public static List<Paciente> obtenerPacientes() {
        return pacientes;
    }
    public static List<Familiar> obtenerFamiliares() {
        return familiares;
    }

    public static Familiar obtenerFamiliarPorUsuarioId(int usuarioId) {
        for (Familiar familiar : familiares) {
            if (familiar.getUsuarioId() == usuarioId) {
                return familiar;
            }
        }
        return null;
    }

    public static Usuario obtenerUsuarioPorId(int usuarioId) {
        for (Usuario usuario : usuarios) {
            if (usuario.getId() == usuarioId) {
                return usuario;
            }
        }
        return null;
    }

}