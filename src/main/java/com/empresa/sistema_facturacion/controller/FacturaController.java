package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.entity.Factura;
import com.empresa.sistema_facturacion.service.FacturacionService;
import com.empresa.sistema_facturacion.util.reportes.ReporteRideService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturacionService facturacionService;
    private final ReporteRideService reporteRideService;

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

    @PostMapping("/enviar-sri/{facturaId}")
    public ResponseEntity<?> enviarYAutorizarSRI(@PathVariable Long facturaId) {
        try {
            Factura facturaProcesada = facturacionService.procesarEnvioSRI(facturaId);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Flujo de facturación electrónica completado",
                    "claveAcceso", facturaProcesada.getClaveAcceso(),
                    "estadoFinalSRI", facturaProcesada.getEstadoSri()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "El proceso de facturación electrónica falló",
                    "detalles", e.getMessage()
            ));
        }
    }

    @GetMapping({"/download-ride/{facturaId}", "/pdf/{facturaId}"})
    public ResponseEntity<byte[]> descargarPdfRide(@PathVariable Long facturaId) {
        Factura factura = facturacionService.obtenerFacturaPorId(facturaId);

        byte[] pdfBytes = reporteRideService.generarPdfRide(factura);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "RIDE_" + factura.getClaveAcceso() + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }
}
