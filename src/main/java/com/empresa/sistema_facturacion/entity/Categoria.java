package com.empresa.sistema_facturacion.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "categorias")
@Data
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 25, unique = true)
    private String nombre;

    @Column(length = 50)
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "tarifa_iva_id", nullable = false)
    private TarifaIva tarifaIva;
}