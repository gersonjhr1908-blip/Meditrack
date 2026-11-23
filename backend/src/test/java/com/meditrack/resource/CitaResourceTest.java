package com.meditrack.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CitaResourceTest {

    // ==========================================
    // AUTENTICACION
    // ==========================================

    private String obtenerTokenPaciente() {

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

        String token = respuesta.jsonPath()
                .getString("accessToken");

        assertNotNull(token);

        return token;
    }

    // ==========================================
    // 1. SEGURIDAD
    // ==========================================

    @Test
    void debeRechazarConsultaDeEspecialidadesSinToken() {

        given()
                .when()
                .get("/api/citas/especialidades")
                .then()
                .statusCode(401);
    }

    @Test
    void debeRechazarRegistroDeCitaSinToken() {

        given()
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/api/citas")
                .then()
                .statusCode(401);
    }

    @Test
    void debeRechazarAccesoConTokenManipulado() {

        String token = obtenerTokenPaciente();

        given()
                .auth()
                .oauth2(token + "alterado")
                .when()
                .get("/api/citas/especialidades")
                .then()
                .statusCode(401);
    }

    // ==========================================
    // 2. CONSULTAR ESPECIALIDADES
    // ==========================================

    @Test
    void debeListarEspecialidadesConTokenValido() {

        String token = obtenerTokenPaciente();

        var respuesta = given()
                .auth()
                .oauth2(token)
                .when()
                .get("/api/citas/especialidades");

        assertEquals(200, respuesta.statusCode());
        assertNotNull(respuesta.jsonPath().getList("$"));
    }

    // ==========================================
    // 3. CONSULTAR MEDICOS
    // ==========================================

    @Test
    void debeListarMedicosPorEspecialidad() {

        String token = obtenerTokenPaciente();

        var respuesta = given()
                .auth()
                .oauth2(token)
                .when()
                .get("/api/citas/medicos/1");

        assertEquals(200, respuesta.statusCode());
        assertNotNull(respuesta.jsonPath().getList("$"));
    }

    @Test
    void debeRechazarIdentificadorDeEspecialidadInvalido() {

        String token = obtenerTokenPaciente();

        given()
                .auth()
                .oauth2(token)
                .when()
                .get("/api/citas/medicos/0")
                .then()
                .statusCode(400);
    }

    // ==========================================
    // 4. CONSULTAR SERVICIOS
    // ==========================================

    @Test
    void debeListarServiciosConTokenValido() {

        String token = obtenerTokenPaciente();

        var respuesta = given()
                .auth()
                .oauth2(token)
                .when()
                .get("/api/citas/servicios");

        assertEquals(200, respuesta.statusCode());
        assertNotNull(respuesta.jsonPath().getList("$"));
    }

    // ==========================================
    // 5. VALIDAR REGISTRO DE CITAS
    // ==========================================

    @Test
    void debeRechazarRegistroConFechaPasada() {

        String token = obtenerTokenPaciente();

        given()
                .auth()
                .oauth2(token)
                .contentType("application/json")
                .body("""
                    {
                        "tipoCita": "CONSULTA_MEDICA",
                        "idMedico": 1,
                        "idEspecialidad": 1,
                        "fecha": "2020-01-01",
                        "hora": "10:00:00",
                        "motivo": "Prueba automatizada"
                    }
                    """)
                .when()
                .post("/api/citas")
                .then()
                .statusCode(400);
    }

    @Test
    void debeRechazarRegistroSinFecha() {

        String token = obtenerTokenPaciente();

        given()
                .auth()
                .oauth2(token)
                .contentType("application/json")
                .body("""
                    {
                        "tipoCita": "CONSULTA_MEDICA",
                        "idMedico": 1,
                        "idEspecialidad": 1,
                        "hora": "10:00:00"
                    }
                    """)
                .when()
                .post("/api/citas")
                .then()
                .statusCode(400);
    }

    @Test
    void debeRechazarRegistroSinHora() {

        String token = obtenerTokenPaciente();

        given()
                .auth()
                .oauth2(token)
                .contentType("application/json")
                .body("""
                    {
                        "tipoCita": "CONSULTA_MEDICA",
                        "idMedico": 1,
                        "idEspecialidad": 1,
                        "fecha": "2020-01-01"
                    }
                    """)
                .when()
                .post("/api/citas")
                .then()
                .statusCode(400);
    }
}
