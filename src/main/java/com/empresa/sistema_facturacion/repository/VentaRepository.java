package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    Long countByFechaEmisionBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    @Query("""
        SELECT COALESCE(SUM(v.total),0)
        FROM Venta v
        WHERE v.fechaEmision BETWEEN :inicio AND :fin
    """)
    BigDecimal totalVentasDia(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    @Query("""
        SELECT MONTH(v.fechaEmision),
               SUM(v.total)
        FROM Venta v
        GROUP BY MONTH(v.fechaEmision)
        ORDER BY MONTH(v.fechaEmision)
    """)
    List<Object[]> ventasMensuales();
}