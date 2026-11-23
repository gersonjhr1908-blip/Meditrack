package com.meditrack.resource;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AuthPerfilResourceTest {

    private String obtenerTokenPaciente() {

        return given()
                .contentType("application/json")
                .body("""
                    {
                        "identificador": "prueba.login@meditrack.com",
                        "contrasena": "MediTrack_Test2026*"
                    }
                    """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");
    }

    // ==========================================
    // 1. RECHAZAR ACCESO SIN TOKEN
    // ==========================================

    @Test
    void debeRechazarAccesoSinToken() {

        given()
                .when()
                .get("/api/auth/perfil")
                .then()
                .statusCode(401);
    }

    // ==========================================
    // 2. PERMITIR ACCESO CON TOKEN VALIDO
    // ==========================================

    @Test
    void debePermitirAccesoConTokenValido() {

        String token = obtenerTokenPaciente();

        var respuesta = given()
                .auth()
                .oauth2(token)
                .when()
                .get("/api/auth/perfil");

        assertEquals(200, respuesta.statusCode());

        assertEquals(
                "7",
                respuesta.jsonPath().getString("idUsuario")
        );

        assertNotNull(
                respuesta.jsonPath().getString("correo")
        );

        assertTrue(
                respuesta.jsonPath()
                        .getList("roles", String.class)
                        .contains("PACIENTE")
        );
    }

    // ==========================================
    // 3. RECHAZAR TOKEN MANIPULADO
    // ==========================================

    @Test
    void debeRechazarTokenManipulado() {

        String token = obtenerTokenPaciente();

        String tokenManipulado = token + "alterado";

        given()
                .auth()
                .oauth2(tokenManipulado)
                .when()
                .get("/api/auth/perfil")
                .then()
                .statusCode(401);
    }
}

