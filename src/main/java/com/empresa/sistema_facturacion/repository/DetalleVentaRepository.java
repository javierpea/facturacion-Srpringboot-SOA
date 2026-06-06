package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    @Query("""
        SELECT d.producto.nombreGenerico,
               SUM(d.cantidad)
        FROM DetalleVenta d
        GROUP BY d.producto.nombreGenerico
        ORDER BY SUM(d.cantidad) DESC
    """)
    List<Object[]> obtenerTopProductos();
}