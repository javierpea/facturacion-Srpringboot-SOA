package com.empresa.sistema_facturacion.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CierreCajaResponseDTO {
    private LocalDate fechaReporte;
    private Long cantidadFacturasEmitidas;
    private BigDecimal subtotal15;
    private BigDecimal subtotal0;
    private BigDecimal totalSinImpuestos;
    private BigDecimal totalIvaRecaudado;
    private BigDecimal totalGeneral;
}
