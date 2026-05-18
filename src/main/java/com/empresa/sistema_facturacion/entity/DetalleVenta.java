package com.empresa.sistema_facturacion.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_venta")
@Data
public class DetalleVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // --- SNAPSHOT DEL IVA ---
    @Column(nullable = false, length = 5)
    private String codigoIvaSriAplicado;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeIvaAplicado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorIva;
}
