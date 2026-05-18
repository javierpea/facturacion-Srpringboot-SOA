package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
}
