package com.meditrack.dto;

import com.meditrack.model.Usuario;

public record UsuarioResponse (
    Integer idUsuario,
    String dni,
    String nombres,
    String apellidos,
    String correo,
    String rol,
    String estado
){

     public static UsuarioResponse desdeModelo(Usuario usuario) {
        return new UsuarioResponse(
                usuario.idUsuario(),
                usuario.dni(),
                usuario.nombres(),
                usuario.apellidos(),
                usuario.correo(),
                usuario.rol(),
                usuario.estado()
        );
    }
    
}
