package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.ConfiguracionSRI;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionSRIRepository extends JpaRepository<ConfiguracionSRI,Long> {

    ConfiguracionSRI findTopByOrderByIdDesc();

}
