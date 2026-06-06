package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByIdentificacion(String identificacion);

    Page<Cliente> findByIdentificacionContainingIgnoreCaseOrRazonSocialContainingIgnoreCase(String identificacion, String razonSocial, Pageable pageable);

    Page<Cliente> findByActivoTrue(Pageable pageable);

    Page<Cliente> findByIdentificacionContainingIgnoreCaseAndActivoTrueOrRazonSocialContainingIgnoreCaseAndActivoTrue(String identificacion, String razonSocial, Pageable pageable);

    java.util.List<Cliente> findByActivoTrue();

    long countByActivoTrue();
}
