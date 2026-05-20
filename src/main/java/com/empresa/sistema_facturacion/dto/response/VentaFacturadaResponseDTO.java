package com.empresa.sistema_facturacion.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaFacturadaResponseDTO {
    private Long ventaId;
    private LocalDateTime fechaEmision;
    private String clienteIdentificacion;
    private String clienteRazonSocial;
    private BigDecimal subtotal;
    private BigDecimal valorIva;
    private BigDecimal total;

    private List<DetalleVentaResponseDTO> detalles;
    private FacturaSriResponseDTO facturaSri;
}
