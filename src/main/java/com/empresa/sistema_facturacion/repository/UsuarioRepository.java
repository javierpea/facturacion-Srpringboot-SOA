package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
