package com.empresa.sistema_facturacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClienteRequestDTO {
    @NotBlank(message = "El tipo de identificación es obligatorio (RUC, CEDULA)")
    private String tipoIdentificacion;

    @NotBlank(message = "La identificación es obligatoria")
    private String identificacion;

    @NotBlank(message = "La razón social o nombre es obligatorio")
    private String razonSocial;

    private String direccion;
    private String telefono;
    private String email;
}