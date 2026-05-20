package com.empresa.sistema_facturacion.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FacturaResumenReporteDTO {
    private Long facturaId;
    private String secuenciaCompleta; // Ej: 001-001-000000005
    private String claveAcceso;
    private LocalDateTime fechaEmision;
    private String clienteRazonSocial;
    private String clienteIdentificacion;
    private BigDecimal totalFactura;
    private String estadoSri;
}
