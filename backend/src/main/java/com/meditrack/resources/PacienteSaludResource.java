package com.meditrack.resources;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalTime;

/** Consultas reales del paciente autenticado. Las escrituras se añadirán en sus flujos propios. */
@Path("/api/paciente")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("PACIENTE")
public class PacienteSaludResource {
    @Inject DataSource dataSource;
    @Inject JsonWebToken jwt;

    @GET @Path("/medicamentos")
    public Response medicamentos() throws Exception {
        return consultar("""
                SELECT tm.id_tratamiento_medicamento, m.nombre, m.forma_farmaceutica,
                       m.concentracion_valor, m.concentracion_unidad, tm.dosis,
                       tm.unidad_dosis, tm.frecuencia, tm.hora_programada,
                       tm.via_administracion, tm.fecha_inicio, tm.fecha_fin,
                       tm.indicaciones, tm.estado
                FROM dbo.TRATAMIENTO_MEDICAMENTO tm
                JOIN dbo.MEDICAMENTO m ON m.id_medicamento=tm.id_medicamento
                JOIN dbo.TRATAMIENTO t ON t.id_tratamiento=tm.id_tratamiento
                JOIN dbo.PACIENTE p ON p.id_paciente=t.id_paciente
                WHERE p.id_usuario=? ORDER BY tm.estado, tm.fecha_inicio DESC
                """);
    }

    @GET @Path("/tratamientos")
    public Response tratamientos() throws Exception {
        return consultar("""
                SELECT t.id_tratamiento,t.nombre,t.diagnostico_referencia,
                       t.fecha_inicio,t.fecha_fin,t.estado,t.observaciones,
                       u.nombres+' '+u.apellidos AS medico
                FROM dbo.TRATAMIENTO t
                JOIN dbo.PACIENTE p ON p.id_paciente=t.id_paciente
                LEFT JOIN dbo.MEDICO m ON m.id_medico=t.id_medico
                LEFT JOIN dbo.PERSONAL pe ON pe.id_personal=m.id_personal
                LEFT JOIN dbo.USUARIO u ON u.id_usuario=pe.id_usuario
                WHERE p.id_usuario=? ORDER BY t.fecha_inicio DESC,t.id_tratamiento DESC
                """);
    }

    @GET @Path("/historial")
    public Response historial() throws Exception {
        return consultar("""
                SELECT a.id_atencion,a.fecha,a.hora,a.motivo,a.observaciones,a.estado,
                       u.nombres+' '+u.apellidos AS medico,
                       c.tipo_cita
                FROM dbo.ATENCION a
                JOIN dbo.PACIENTE p ON p.id_paciente=a.id_paciente
                LEFT JOIN dbo.MEDICO m ON m.id_medico=a.id_medico
                LEFT JOIN dbo.PERSONAL pe ON pe.id_personal=m.id_personal
                LEFT JOIN dbo.USUARIO u ON u.id_usuario=pe.id_usuario
                LEFT JOIN dbo.CITA c ON c.id_cita=a.id_cita
                WHERE p.id_usuario=? ORDER BY a.fecha DESC,a.hora DESC,a.id_atencion DESC
                """);
    }

    public record RecordatorioRequest(String nombre,String tipo,String detalle,String frecuencia,String hora,String fecha,Boolean activo) {}

    @GET @Path("/recordatorios")
    public Response recordatorios() throws Exception {
        return consultar("SELECT r.id_recordatorio,r.nombre,r.tipo,r.detalle,r.frecuencia,r.hora,r.fecha,r.activo FROM dbo.RECORDATORIO r JOIN dbo.PACIENTE p ON p.id_paciente=r.id_paciente WHERE p.id_usuario=? ORDER BY r.fecha,r.hora");
    }

    @POST @Path("/recordatorios")
    public Response crearRecordatorio(RecordatorioRequest request) throws Exception {
        if(request==null||request.nombre()==null||request.nombre().isBlank()||request.hora()==null||request.fecha()==null)
            return Response.status(400).entity(Map.of("mensaje","Completa los datos del recordatorio.")).build();
        int usuario=Integer.parseInt(jwt.getSubject());
        try(Connection c=dataSource.getConnection(); PreparedStatement s=c.prepareStatement("INSERT INTO dbo.RECORDATORIO(id_paciente,nombre,tipo,detalle,frecuencia,hora,fecha,activo) SELECT id_paciente,?,?,?,?,?,?,? FROM dbo.PACIENTE WHERE id_usuario=?", java.sql.Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1,request.nombre()); s.setString(2,request.tipo()); s.setString(3,request.detalle()); s.setString(4,request.frecuencia());
            s.setObject(5,LocalTime.parse(request.hora().length()>5?request.hora().substring(0,5):request.hora())); s.setObject(6,LocalDate.parse(request.fecha())); s.setBoolean(7,request.activo()==null||request.activo()); s.setInt(8,usuario);
            if(s.executeUpdate()==0) return Response.status(404).entity(Map.of("mensaje","Paciente no encontrado.")).build();
            try(ResultSet k=s.getGeneratedKeys()){ if(k.next()) return Response.status(201).entity(Map.of("idRecordatorio",k.getInt(1))).build(); }
        }
        return Response.status(500).build();
    }

    private Response consultar(String sql) throws Exception {
        int idUsuario = Integer.parseInt(jwt.getSubject());
        List<Map<String,Object>> filas = new ArrayList<>();
        try (Connection c=dataSource.getConnection(); PreparedStatement s=c.prepareStatement(sql)) {
            s.setInt(1,idUsuario);
            try (ResultSet r=s.executeQuery()) {
                var meta=r.getMetaData();
                while(r.next()) {
                    Map<String,Object> fila=new LinkedHashMap<>();
                    for(int i=1;i<=meta.getColumnCount();i++) fila.put(meta.getColumnLabel(i),r.getObject(i));
                    filas.add(fila);
                }
            }
        }
        return Response.ok(filas).build();
    }
}
