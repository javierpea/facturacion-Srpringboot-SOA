package com.empresa.sistema_facturacion.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoCreateDTO {
    @NotBlank(message = "El código principal es obligatorio")
    private String codigoPrincipal;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombreGenerico;

    private String marca;
    private String presentacion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precioUnitario;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;
}