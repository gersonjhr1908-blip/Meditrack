package com.meditrack.dto;

import com.meditrack.model.Medico;

public record MedicoResponse(
        Integer idMedico,
        String dni,
        String nombres,
        String apellidos,
        String codigoProfesional,
        Integer idEspecialidad,
        String especialidad,
        Boolean especialidadPrincipal
) {

    public static MedicoResponse desdeModelo(Medico medico) {
        return new MedicoResponse(
                medico.idMedico(),
                medico.dni(),
                medico.nombres(),
                medico.apellidos(),
                medico.codigoProfesional(),
                medico.idEspecialidad(),
                medico.especialidad(),
                medico.especialidadPrincipal()
        );
    }
}
