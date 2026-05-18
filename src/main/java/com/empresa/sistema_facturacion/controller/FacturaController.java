package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.entity.Factura;
import com.empresa.sistema_facturacion.service.FacturacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturacionService facturacionService;

    @PostMapping("/generar/{ventaId}")
    public ResponseEntity<?> generarXML(@PathVariable Long ventaId) {
        try {
            Factura factura = facturacionService.generarFacturaXML(ventaId);

            // Devolvemos un mensaje de éxito con los datos clave generados
            return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of(
                    "mensaje", "XML de la Factura generado exitosamente en la BD",
                    "claveAcceso", factura.getClaveAcceso(),
                    "secuencial", factura.getSecuencial(),
                    "estado", factura.getEstadoSri()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
