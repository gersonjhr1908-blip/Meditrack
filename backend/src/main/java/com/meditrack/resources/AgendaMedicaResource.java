package com.meditrack.resources;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Path("/api/personal/agenda")
@Produces("application/json")
@RolesAllowed("MEDICO")
public class AgendaMedicaResource {
    @Inject DataSource db;
    @Inject JsonWebToken jwt;

    @GET
    public List<String> citas() throws SQLException {
        String sql = """
            SELECT c.fecha, c.hora, u.nombres, u.apellidos, c.estado
            FROM dbo.CITA c JOIN dbo.MEDICO m ON m.id_medico=c.id_medico
            JOIN dbo.PERSONAL p ON p.id_personal=m.id_personal
            JOIN dbo.PACIENTE pa ON pa.id_paciente=c.id_paciente
            JOIN dbo.USUARIO u ON u.id_usuario=pa.id_usuario
            WHERE p.id_usuario=? ORDER BY c.fecha,c.hora,c.id_cita
            """;
        List<String> agenda = new ArrayList<>();
        try (Connection conn=db.getConnection(); PreparedStatement stmt=conn.prepareStatement(sql)) {
            stmt.setInt(1,Integer.parseInt(jwt.getSubject()));
            try(ResultSet rs=stmt.executeQuery()) {
                while(rs.next()) agenda.add(rs.getDate(1)+" · "+rs.getTime(2)+"\n"+rs.getString(3)+" "+rs.getString(4)+" · "+rs.getString(5));
            }
        }
        return agenda;
    }
}
