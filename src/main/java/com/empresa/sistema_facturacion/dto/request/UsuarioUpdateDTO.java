package com.empresa.sistema_facturacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioUpdateDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    private String password;

    @NotNull(message = "El ID de rol es obligatorio")
    private Long rolId;
}
