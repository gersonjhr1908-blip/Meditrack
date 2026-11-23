package com.meditrack.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class GenerarClavesRSA {

    public static void main(String[] args) throws Exception {

        KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);

        KeyPair keyPair = generator.generateKeyPair();

        String clavePrivada = Base64.getMimeEncoder(
                64, "\n".getBytes()
        ).encodeToString(
                keyPair.getPrivate().getEncoded()
        );

        String clavePublica = Base64.getMimeEncoder(
                64, "\n".getBytes()
        ).encodeToString(
                keyPair.getPublic().getEncoded()
        );

        String privadaPem =
                "-----BEGIN PRIVATE KEY-----\n"
                + clavePrivada
                + "\n-----END PRIVATE KEY-----";

        String publicaPem =
                "-----BEGIN PUBLIC KEY-----\n"
                + clavePublica
                + "\n-----END PUBLIC KEY-----";

        Path carpeta = Path.of("C:/meditrack/keys");

        Files.createDirectories(carpeta);

        Files.writeString(
                carpeta.resolve("privateKey.pem"),
                privadaPem
        );

        Files.writeString(
                carpeta.resolve("publicKey.pem"),
                publicaPem
        );

        System.out.println("Claves RSA generadas correctamente.");
    }
}