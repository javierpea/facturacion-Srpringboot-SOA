package com.empresa.sistema_facturacion.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "inventarios")
@Data
public class Inventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Column(nullable = false)
    private int cantidadDisponible;
}
