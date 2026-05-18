package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Long> {
}
