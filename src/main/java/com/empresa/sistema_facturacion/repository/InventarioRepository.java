package com.empresa.sistema_facturacion.repository;


import com.empresa.sistema_facturacion.entity.Inventario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByProductoIdAndSucursalId(Long productoId, Long sucursalId);

    @EntityGraph(attributePaths = {"producto", "producto.categoria", "producto.categoria.tarifaIva"})
    List<Inventario> findBySucursalId(Long sucursalId);

    @EntityGraph(attributePaths = {"sucursal"})
    List<Inventario> findByProductoId(Long productoId);

    @Query("""
    SELECT COALESCE(SUM(i.cantidadDisponible),0)
    FROM Inventario i
""")
    Long stockTotal();

}