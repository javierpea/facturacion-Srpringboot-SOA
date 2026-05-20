package com.empresa.sistema_facturacion.dto.response;

import lombok.Data;

@Data
public class FacturaSriResponseDTO {
    private String secuencial;
    private String claveAcceso;
    private String estadoSri;
    private String mensaje;
}
