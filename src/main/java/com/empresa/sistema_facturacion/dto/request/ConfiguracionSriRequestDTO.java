package com.empresa.sistema_facturacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfiguracionSriRequestDTO {

    @NotBlank(message = "El RUC es obligatorio")
    @Size(min = 13, max = 13, message = "El RUC debe tener exactamente 13 dígitos")
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    private String razonSocial;

    private String nombreComercial;

    @NotBlank(message = "La dirección matriz es obligatoria")
    private String direccionMatriz;

    @NotBlank(message = "Debe especificar si está obligado a llevar contabilidad (SI/NO)")
    private String obligadoContabilidad;

    @NotBlank(message = "El ambiente es obligatorio (1 = Pruebas, 2 = Producción)")
    private String ambiente;

    @NotBlank(message = "El tipo de emisión es obligatorio (Siempre 1 para normal offline)")
    private String tipoEmision;

}
