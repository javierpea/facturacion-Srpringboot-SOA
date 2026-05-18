package com.empresa.sistema_facturacion.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoResponseDTO {
    private Long id;
    private String codigoPrincipal;
    private String nombreCompleto;
    private BigDecimal precioUnitario;
    private String categoriaNombre;
    private BigDecimal porcentajeIva;
    private Boolean estado;
}
