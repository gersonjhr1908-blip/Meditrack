package com.meditrack;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Path("db-test")
public class DatabaseTestResource {

    @Inject 
    DataSource datasource;

    @GET 
    @Produces(MediaType.TEXT_PLAIN)
    public Response testConnection(){
        String sql = """
                SELECT
                    DB_NAME() AS database_name,
                    SUSER_SNAME() AS login_name,
                    @@SERVERNAME AS server_name
                """;

        try (
            Connection connection = datasource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql)
        ) {
            if (result.next()) {
                String response =
                        "DATABASE: " + result.getString("database_name") + "\n" +
                        "LOGIN: " + result.getString("login_name") + "\n" +
                        "SERVER: " + result.getString("server_name");

                return Response.ok(response).build();
            }

            return Response.status(500)
                    .entity("No se recibieron resultados.")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.status(500)
                    .entity("ERROR DE CONEXION: " + e.getMessage())
                    .build();
        }
    } 
    
}
