package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    Optional<Factura> findByEstablecimientoAndPuntoEmisionAndSecuencial(
            String establecimiento, String puntoEmision, String secuencial);

    Optional<Factura> findByVentaId(Long ventaId);

    Optional<Factura> findByClaveAcceso(String claveAcceso);
}