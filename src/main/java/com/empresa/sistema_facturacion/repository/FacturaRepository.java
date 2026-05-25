package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long>, JpaSpecificationExecutor<Factura> {

    Optional<Factura> findByEstablecimientoAndPuntoEmisionAndSecuencial(
            String establecimiento, String puntoEmision, String secuencial);

    Optional<Factura> findByVentaId(Long ventaId);

    Optional<Factura> findByClaveAcceso(String claveAcceso);

    List<Factura> findByEstadoSri(String estadoSri);

    List<Factura> findByVentaFechaEmisionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Factura> findByVentaClienteIdentificacionAndEstadoSri(String identificacion, String estadoSri);

    List<Factura> findByVentaSucursalId(Long sucursalId);

}