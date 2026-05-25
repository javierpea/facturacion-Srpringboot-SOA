package com.empresa.sistema_facturacion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AjusteStockRequestDTO {
    private Long inventarioId;
    private int cantidad; // Positivo para aumentar, negativo para disminuir
}
