package com.empresa.sistema_facturacion.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String tipoIdentificacion; // RUC, CEDULA, PASAPORTE

    @Column(nullable = false, unique = true, length = 13)
    private String identificacion;

    @Column(nullable = false, length = 50)
    private String razonSocial;

    @Column(length = 50)
    private String direccion;

    @Column(length = 10)
    private String telefono;

    @Column(length = 30)
    private String email;
}
