package com.meditrack.resources;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Path("/api/vinculacion")
@Authenticated
@Produces("application/json")
@Consumes("application/json")
public class VinculacionResource {
    @Inject DataSource dataSource;
    @Inject JsonWebToken jwt;
    public record CodigoRequest(String codigo) {}
    public record Decision(int idTutorPaciente, boolean aceptar) {}

    @GET @Path("/familiares")
    public Response familiares() throws SQLException {
        int usuario = Integer.parseInt(jwt.getSubject());
        try (Connection c=dataSource.getConnection();
             PreparedStatement s=c.prepareStatement("""
            SELECT tp.id_tutor_paciente,u.nombres,u.apellidos,tp.estado
               FROM dbo.TUTOR_PACIENTE tp
               JOIN dbo.PACIENTE p ON p.id_paciente=tp.id_paciente
               JOIN dbo.TUTOR t ON t.id_tutor=tp.id_tutor
               JOIN dbo.USUARIO u ON u.id_usuario=t.id_usuario
               WHERE p.id_usuario=? AND tp.estado='CONFIRMADA'
               ORDER BY tp.fecha_solicitud DESC
               """)) {
            s.setInt(1,usuario);
            try(ResultSet r=s.executeQuery()) {
                List<Map<String,Object>> lista=new ArrayList<>();
                while(r.next()) lista.add(Map.of("id",r.getInt("id_tutor_paciente"),
                        "nombres",r.getString("nombres"),"apellidos",r.getString("apellidos"),
                        "estado",r.getString("estado")));
                return Response.ok(lista).build();
            }
        }
    }

    @GET @Path("/solicitudes")
    public Response solicitudes() throws SQLException {
        int usuario=Integer.parseInt(jwt.getSubject());
        try(Connection c=dataSource.getConnection(); PreparedStatement s=c.prepareStatement("""
            SELECT tp.id_tutor_paciente,u.nombres,u.apellidos
            FROM dbo.TUTOR_PACIENTE tp JOIN dbo.PACIENTE p ON p.id_paciente=tp.id_paciente
            JOIN dbo.TUTOR t ON t.id_tutor=tp.id_tutor JOIN dbo.USUARIO u ON u.id_usuario=t.id_usuario
            WHERE p.id_usuario=? AND tp.estado='PENDIENTE'
            ORDER BY tp.fecha_solicitud
            """)) {
            s.setInt(1,usuario); try(ResultSet r=s.executeQuery()) {
                List<Map<String,Object>> rows=new ArrayList<>();
                while(r.next()) rows.add(Map.of("id",r.getInt(1),"nombres",r.getString(2),"apellidos",r.getString(3)));
                return Response.ok(rows).build();
            }
        }
    }

    @POST @Path("/decidir")
    public Response decidir(Decision d) throws SQLException {
        int usuario=Integer.parseInt(jwt.getSubject());
        if(d==null || d.idTutorPaciente()<=0) return Response.status(400).entity(Map.of("mensaje","Solicitud inválida.")).build();
        try(Connection c=dataSource.getConnection(); PreparedStatement s=c.prepareStatement("""
            UPDATE tp SET estado=?,fecha_confirmacion=CASE WHEN ?=1 THEN GETDATE() ELSE NULL END
            FROM dbo.TUTOR_PACIENTE tp JOIN dbo.PACIENTE p ON p.id_paciente=tp.id_paciente
            WHERE tp.id_tutor_paciente=? AND tp.estado='PENDIENTE' AND p.id_usuario=?
            """)) {
            s.setString(1,d.aceptar()?"CONFIRMADA":"RECHAZADA");
            s.setInt(2,d.aceptar()?1:0); s.setInt(3,d.idTutorPaciente()); s.setInt(4,usuario);
            if(s.executeUpdate()==0) return Response.status(404).entity(Map.of("mensaje","Solicitud no encontrada.")).build();
            return Response.ok(Map.of("mensaje",d.aceptar()?"Solicitud aceptada.":"Solicitud rechazada.")).build();
        }
    }

    @DELETE @Path("/familiares/{id}")
    @Consumes("*/*")
    public Response eliminar(@PathParam("id") int id) throws SQLException {
        int usuario=Integer.parseInt(jwt.getSubject());
        try(Connection c=dataSource.getConnection(); PreparedStatement s=c.prepareStatement("""
            DELETE tp
            FROM dbo.TUTOR_PACIENTE tp
            JOIN dbo.PACIENTE p ON p.id_paciente=tp.id_paciente
            WHERE tp.id_tutor_paciente=? AND p.id_usuario=? AND tp.estado='CONFIRMADA'
            """)) {
            s.setInt(1,id); s.setInt(2,usuario);
            if(s.executeUpdate()==0) return Response.status(404).entity(Map.of("mensaje","Vínculo no encontrado.")).build();
            return Response.noContent().build();
        }
    }

    @GET @Path("/pacientes")
    public Response pacientes() throws SQLException {
        int usuarioTutor = Integer.parseInt(jwt.getSubject());
        try (Connection c=dataSource.getConnection();
             PreparedStatement s=c.prepareStatement("""
               SELECT u.nombres,u.apellidos,u.dni
               FROM dbo.TUTOR_PACIENTE tp
               JOIN dbo.TUTOR t ON t.id_tutor=tp.id_tutor
               JOIN dbo.PACIENTE p ON p.id_paciente=tp.id_paciente
               JOIN dbo.USUARIO u ON u.id_usuario=p.id_usuario
               WHERE t.id_usuario=? AND tp.estado='CONFIRMADA'
               ORDER BY u.apellidos,u.nombres
               """)) {
            s.setInt(1,usuarioTutor);
            try(ResultSet r=s.executeQuery()) {
                List<Map<String,String>> lista=new ArrayList<>();
                while(r.next()) lista.add(Map.of("nombres",r.getString("nombres"),
                        "apellidos",r.getString("apellidos"),"dni",r.getString("dni")));
                return Response.ok(lista).build();
            }
        }
    }

    @POST @Path("/generar")
    public Response generar() throws SQLException {
        int usuario = Integer.parseInt(jwt.getSubject());
        String codigo = "MT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        try (Connection c=dataSource.getConnection();
             PreparedStatement s=c.prepareStatement("""
                 INSERT INTO dbo.INVITACION_TUTOR
                 (id_paciente,codigo,fecha_vencimiento,estado)
                 SELECT id_paciente,?,DATEADD(MINUTE,10,GETDATE()),'PENDIENTE'
                 FROM dbo.PACIENTE WHERE id_usuario=?
                 """)) {
            s.setString(1,codigo); s.setInt(2,usuario);
            if(s.executeUpdate()==0) return Response.status(403).entity(Map.of("mensaje","Solo un paciente puede generar el código.")).build();
        }
        return Response.ok(Map.of("codigo",codigo,"venceEnMinutos",10)).build();
    }

    @POST @Path("/aceptar")
    public Response aceptar(CodigoRequest request) throws SQLException {
        if(request==null || request.codigo()==null || request.codigo().isBlank())
            return Response.status(400).entity(Map.of("mensaje","Ingresa un código.")).build();
        int usuarioTutor=Integer.parseInt(jwt.getSubject());
        try(Connection c=dataSource.getConnection()) {
            c.setAutoCommit(false);
            int tutor;
            try(PreparedStatement s=c.prepareStatement("SELECT id_tutor FROM dbo.TUTOR WHERE id_usuario=?")) {
                s.setInt(1,usuarioTutor); try(ResultSet r=s.executeQuery()) {
                    if(!r.next()) return Response.status(403).entity(Map.of("mensaje","No existe un perfil familiar.")).build();
                    tutor=r.getInt(1);
                }
            }
            int paciente;
            try(PreparedStatement s=c.prepareStatement("""
                SELECT id_paciente FROM dbo.INVITACION_TUTOR
                WHERE codigo=? AND estado='PENDIENTE' AND fecha_vencimiento>GETDATE()
                """)) {
                s.setString(1,request.codigo().trim().toUpperCase()); try(ResultSet r=s.executeQuery()) {
                    if(!r.next()) return Response.status(400).entity(Map.of("mensaje","Código inválido o vencido.")).build();
                    paciente=r.getInt(1);
                }
            }
            try(PreparedStatement s=c.prepareStatement("INSERT INTO dbo.TUTOR_PACIENTE(id_tutor,id_paciente,fecha_solicitud,estado) VALUES(?,?,GETDATE(),'PENDIENTE')")) {
                s.setInt(1,tutor); s.setInt(2,paciente); s.executeUpdate();
            }
            try(PreparedStatement s=c.prepareStatement("UPDATE dbo.INVITACION_TUTOR SET estado='USADA' WHERE codigo=?")) {
                s.setString(1,request.codigo().trim().toUpperCase()); s.executeUpdate();
            }
            c.commit();
            return Response.ok(Map.of("mensaje","Paciente vinculado correctamente.")).build();
        }
    }
}
