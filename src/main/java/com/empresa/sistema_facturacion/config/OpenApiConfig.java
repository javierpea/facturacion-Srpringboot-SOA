package com.empresa.sistema_facturacion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API del Sistema de Facturación y POS")
                        .version("1.0")
                        .description("Documentación oficial de los endpoints para el control de sucursales, inventario y facturación electrónica."));
    }
}