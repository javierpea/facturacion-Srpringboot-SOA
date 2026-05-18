package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
}