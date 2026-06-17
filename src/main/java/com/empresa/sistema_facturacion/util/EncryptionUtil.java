package com.empresa.sistema_facturacion.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class EncryptionUtil {

    @Value("${encryption.password}")
    private String password;

    @Value("${encryption.salt}")
    private String salt;

    private TextEncryptor encryptor;

    @PostConstruct
    public void init() {
        try {
            // El salt DEBE ser un string hexadecimal válido (0-9, A-F)
            this.encryptor = Encryptors.text(password, salt);
        } catch (Exception e) {
            throw new RuntimeException("ERROR: No se pudo inicializar EncryptionUtil. Verifica que 'encryption.salt' sea un hexadecimal válido.", e);
        }
    }

    public String encriptar(String textoPlano) {
        // Aquí se llama al método de la librería para encriptar
        return encryptor.encrypt(textoPlano);
    }

    public String desencriptar(String textoEncriptado) {
        return encryptor.decrypt(textoEncriptado);
    }
}
