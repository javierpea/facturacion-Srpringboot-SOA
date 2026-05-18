package com.empresa.sistema_facturacion.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "facturas")
@Data
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "venta_id", nullable = false, unique = true)
    private Venta venta;

    @Column(nullable = false, length = 3)
    private String establecimiento; // Ej: "001"

    @Column(nullable = false, length = 3)
    private String puntoEmision; // Ej: "001"

    @Column(nullable = false, length = 9)
    private String secuencial; // Ej: "000000123"

    @Column(length = 49)
    private String claveAcceso;

    @Column(nullable = false, length = 20)
    private String estadoSri; // CREADA, FIRMADA, AUTORIZADA, RECHAZADA

    private LocalDateTime fechaAutorizacion;

    @Column(columnDefinition = "TEXT")
    private String xmlAutorizado;
}