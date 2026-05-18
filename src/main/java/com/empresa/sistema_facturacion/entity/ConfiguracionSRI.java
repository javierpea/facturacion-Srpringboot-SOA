package com.empresa.sistema_facturacion.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "configuracion_sri")
@Data
public class ConfiguracionSRI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 13)
    private String ruc;

    @Column(nullable = false, length = 50)
    private String razonSocial;

    @Column(length = 50)
    private String nombreComercial;

    @Column(nullable = false, length = 50)
    private String direccionMatriz;

    @Column(nullable = false, length = 2)
    private String obligadoContabilidad; // "SI" o "NO"

    @Column(nullable = false, length = 1)
    private String ambiente; // "1"  Pruebas, "2"  Producción

    @Column(nullable = false, length = 1)
    private String tipoEmision; // "1" para Emisión Normal Offline

    /* Campos opcionales según el régimen de la empresa
    @Column(length = 20)
    private String contribuyenteEspecial; // Número de resolución si aplica

    @Column(length = 100)
    private String regimenEmpresa; // Ej: "CONTRIBUYENTE RÉGIMEN RIMPE"
    */

}
