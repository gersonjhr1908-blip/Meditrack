package com.meditrack.service;

import com.meditrack.dto.RegistroPacienteRequest;
import com.meditrack.dto.RegistroPacienteResponse;
import com.meditrack.repository.UsuarioRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@ApplicationScoped
public class RegistroPacienteService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    PasswordService passwordService;

    public RegistroPacienteResponse registrar(RegistroPacienteRequest request)
            throws SQLException {

        if (request == null) {
            throw new IllegalArgumentException("Los datos del paciente son obligatorios.");
        }

        String nombres = obligatorio(request.nombres(), "Los nombres");
        String apellidos = obligatorio(request.apellidos(), "Los apellidos");
        String dni = obligatorio(request.dni(), "El DNI");
        String correo = obligatorio(request.correo(), "El correo");
        String telefono = obligatorio(request.telefono(), "El teléfono");
        String sexo = obligatorio(request.sexo(), "El sexo");
        String contrasena = obligatorio(request.contrasena(), "La contraseña");

        if (!dni.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener 8 dígitos.");
        }
        if (!correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        }
        if (!telefono.matches("\\d{9}")) {
            throw new IllegalArgumentException("El teléfono debe tener 9 dígitos.");
        }
        if (contrasena.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }

        LocalDate fechaNacimiento = leerFecha(request.fechaNacimiento());
        if (!fechaNacimiento.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento debe ser anterior a hoy.");
        }

        String hash = passwordService.generarHash(contrasena);
        return usuarioRepository.crearPaciente(
                dni,
                nombres,
                apellidos,
                correo,
                telefono,
                sexo,
                fechaNacimiento,
                hash
        );
    }

    private String obligatorio(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombreCampo + " es obligatorio.");
        }
        return valor.trim();
    }

    private LocalDate leerFecha(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }

        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(valor.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException error) {
                throw new IllegalArgumentException(
                        "La fecha debe tener formato dd/MM/aaaa o aaaa-MM-dd."
                );
            }
        }
    }
}
