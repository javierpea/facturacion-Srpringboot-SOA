package com.empresa.sistema_facturacion.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Data
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 25)
    private String codigoPrincipal;

    @Column(nullable = false, length = 100)
    private String nombreGenerico;

    @Column(length = 50)
    private String marca;

    @Column(length = 50)
    private String presentacion;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private Boolean estado = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

}