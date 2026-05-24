package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigoPrincipal(String codigoPrincipal);

    Page<Producto> findByNombreGenericoContainingIgnoreCase(String nombreGenerico, Pageable pageable);}
