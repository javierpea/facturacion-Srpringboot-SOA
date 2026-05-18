package com.empresa.sistema_facturacion.util.sri;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class ClaveAccesoUtil {

    /* Genera la clave de acceso de 49 dígitos según la ficha técnica del SRI.
     *  fechaEmision Fecha en la que se emite la factura
     *  tipoComprobante "01" para Factura
     *  ruc RUC de la empresa (13 dígitos)
     *  ambiente "1" Pruebas, "2" Producción
     *  serie Código de establecimiento + Punto de emisión (Ej: "001001")
     *  secuencial Número de factura (Ej: "000000123")
     *  codigoNumerico Número aleatorio de 8 dígitos
     *  tipoEmision "1" Emisión Normal
     *  Clave de Acceso de 49 dígitos
     */
    public String generarClaveAcceso(LocalDate fechaEmision, String tipoComprobante, String ruc,
                                     String ambiente, String serie, String secuencial,
                                     String codigoNumerico, String tipoEmision) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        String fechaFormateada = fechaEmision.format(formatter);

        StringBuilder clave48 = new StringBuilder();
        clave48.append(fechaFormateada);
        clave48.append(tipoComprobante);
        clave48.append(ruc);
        clave48.append(ambiente);
        clave48.append(serie);
        clave48.append(secuencial);
        clave48.append(codigoNumerico);
        clave48.append(tipoEmision);

        // Calculamos el dígito verificador (Módulo 11) y lo agregamos al final
        int digitoVerificador = calcularModulo11(clave48.toString());
        clave48.append(digitoVerificador);

        return clave48.toString();
    }

    //Algoritmo Módulo 11 estándar del SRI
    private int calcularModulo11(String cadena) {
        int baseMultiplicador = 7;
        int sumatoria = 0;
        int multiplicador = 2;

        for (int i = cadena.length() - 1; i >= 0; i--) {
            sumatoria += Character.getNumericValue(cadena.charAt(i)) * multiplicador;
            multiplicador++;
            if (multiplicador > baseMultiplicador) {
                multiplicador = 2;
            }
        }

        int modulo = sumatoria % 11;
        int digitoVerificador = 11 - modulo;

        if (digitoVerificador == 11) {
            return 0;
        } else if (digitoVerificador == 10) {
            return 1;
        }

        return digitoVerificador;
    }

    //Genera un número aleatorio de 8 dígitos

    public String generarCodigoNumerico() {
        Random random = new Random();
        int numero = random.nextInt(99999999);
        return String.format("%08d", numero);
    }
}
