package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigoPrincipal(String codigoPrincipal);

    Optional<Producto> findByCodigoPrincipalAndEstadoTrue(String codigoPrincipal);

    Page<Producto> findByNombreGenericoContainingIgnoreCase(String nombreGenerico, Pageable pageable);

    Page<Producto> findByEstadoTrue(Pageable pageable);

    Page<Producto> findByNombreGenericoContainingIgnoreCaseAndEstadoTrue(String nombreGenerico, Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE " +
           "(UPPER(p.nombreGenerico) LIKE UPPER(CONCAT('%', :q, '%')) OR " +
           "UPPER(p.codigoPrincipal) LIKE UPPER(CONCAT('%', :q, '%'))) AND " +
           "(:catId IS NULL OR p.categoria.id = :catId) AND " +
           "p.estado = true")
    Page<Producto> buscarPorNombreOCodigoYCategoria(@Param("q") String q, @Param("catId") Long catId, Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE " +
           "UPPER(p.categoria.nombre) LIKE UPPER(CONCAT('%', :catNombre, '%')) AND " +
           "p.estado = true")
    Page<Producto> buscarPorCategoriaNombre(@Param("catNombre") String catNombre, Pageable pageable);
}
