package com.empresa.sistema_facturacion.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DetalleVentaResponseDTO {
    private Integer cantidad;
    private String productoNombre;
    private BigDecimal precioUnitario;
    private BigDecimal subtotalItem;
}