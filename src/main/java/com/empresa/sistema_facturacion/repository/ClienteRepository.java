package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
