package com.meditrack.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AuthResourceTest {

    @Test
    void debePermitirLoginGeneralConCorreo() {

        var respuesta = given()
                .contentType("application/json")
                .body("""
                    {
                        "identificador": "prueba.login@meditrack.com",
                        "contrasena": "MediTrack_Test2026*"
                    }
                    """)
                .when()
                .post("/api/auth/login");

        assertEquals(200, respuesta.statusCode());
        assertNotNull(respuesta.jsonPath().getString("accessToken"));
        assertEquals("Bearer", respuesta.jsonPath().getString("tokenType"));
        assertEquals(
                7,
                (int) respuesta.jsonPath().getInt("usuario.idUsuario")
        );
    }

    @Test
    void debePermitirLoginGeneralConDni() {

        var respuesta = given()
                .contentType("application/json")
                .body("""
                    {
                        "identificador": "99999999",
                        "contrasena": "MediTrack_Test2026*"
                    }
                    """)
                .when()
                .post("/api/auth/login");

        assertEquals(200, respuesta.statusCode());
        assertNotNull(respuesta.jsonPath().getString("accessToken"));
    }

    @Test
    void debeRechazarLoginPersonalDeUnPaciente() {

        given()
                .contentType("application/json")
                .body("""
                    {
                        "identificador": "prueba.login@meditrack.com",
                        "contrasena": "MediTrack_Test2026*"
                    }
                    """)
                .when()
                .post("/api/auth/login-personal")
                .then()
                .statusCode(403);
    }

    @Test
    void debeRechazarContrasenaIncorrecta() {

        given()
                .contentType("application/json")
                .body("""
                    {
                        "identificador": "prueba.login@meditrack.com",
                        "contrasena": "ClaveIncorrecta"
                    }
                    """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    void debeRechazarDatosIncompletos() {

        given()
                .contentType("application/json")
                .body("""
                    {
                        "identificador": "",
                        "contrasena": ""
                    }
                    """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(400);
    }


    @Test
    void debeRechazarContrasenaNula() {

        given()
                .contentType("application/json")
                .body("""
                    {
                        "identificador": "99999999",
                        "contrasena": null
                    }
                    """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(400);
    }

    @Test
    void debeRechazarIdentificadorNulo() {

        given()
                .contentType("application/json")
                .body("""
                    {
                        "identificador": null,
                        "contrasena": "MediTrack_Test2026*"
                    }
                    """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(400);
    }

    @Test
    void debeRechazarContrasenaAusente() {

        given()
                .contentType("application/json")
                .body("""
                    {
                        "identificador": "99999999"
                    }
                    """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(400);
    }

}