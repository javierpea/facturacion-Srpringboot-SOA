package com.empresa.sistema_facturacion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class VentaRequestDTO {

    @NotNull(message = "Debe indicar la sucursal desde donde se vende")
    private Long sucursalId;

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    @NotEmpty(message = "El carrito no puede estar vacío")
    @Valid
    private List<DetalleVentaRequestDTO> detalles;
}
