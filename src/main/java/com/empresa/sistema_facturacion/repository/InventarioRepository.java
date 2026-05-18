package com.empresa.sistema_facturacion.repository;


import com.empresa.sistema_facturacion.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByProductoIdAndSucursalId(Long productoId, Long sucursalId);
}