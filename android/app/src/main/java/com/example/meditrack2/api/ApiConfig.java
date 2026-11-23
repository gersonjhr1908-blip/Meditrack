package com.example.meditrack2.api;

public final class ApiConfig {

    // Requiere adb reverse tcp:8080 tcp:8080 con Quarkus activo en la PC.
    public static final String BASE_URL = "http://localhost:8080";

    private ApiConfig() {
    }
}
