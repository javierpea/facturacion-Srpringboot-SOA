package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigoPrincipal(String codigoPrincipal);

    List<Producto> findByNombreGenericoContainingIgnoreCase(String nombre);
}
