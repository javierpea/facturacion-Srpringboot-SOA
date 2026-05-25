package com.empresa.sistema_facturacion.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sucursales")
@Data
public class Sucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false,length = 50)
    private String nombre;

    @Column(nullable = false,length = 25)
    private String ciudad;

    @Column(length = 50)
    private String direccion;

    @Column(nullable = false, length = 3)
    private String codigoEstablecimiento;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private boolean permiteVentaCruzada = true;
}
