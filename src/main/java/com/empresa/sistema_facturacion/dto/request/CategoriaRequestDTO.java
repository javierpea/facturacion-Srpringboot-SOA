package com.empresa.sistema_facturacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoriaRequestDTO {
    private Long id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "Debe seleccionar una tarifa de IVA")
    private Long tarifaIvaId;
}
