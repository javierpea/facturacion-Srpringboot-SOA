package com.empresa.sistema_facturacion.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FacturaResumenDTO {
    private Long id;
    private String establecimiento;
    private String puntoEmision;
    private String secuencial;
    private String numeroCompleto;
    private String claveAcceso;
    private String estadoSri;
    private LocalDateTime fechaAutorizacion;
}