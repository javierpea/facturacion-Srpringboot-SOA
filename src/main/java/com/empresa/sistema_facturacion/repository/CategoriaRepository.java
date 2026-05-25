package com.empresa.sistema_facturacion.repository;
import com.empresa.sistema_facturacion.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByNombre(String nombre);
    Optional<Categoria> findByNombreAndIdNot(String nombre, Long id);
    List<Categoria> findByActivoTrue();
}