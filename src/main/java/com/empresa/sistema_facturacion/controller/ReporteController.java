package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.dto.response.CierreCajaResponseDTO;
import com.empresa.sistema_facturacion.dto.response.FacturaResumenReporteDTO;
import com.empresa.sistema_facturacion.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/cierre-caja")
    public ResponseEntity<CierreCajaResponseDTO> getCierreCaja(
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        // Fallback: Si el frontend no manda fechas, asumimos el día de hoy automáticamente
        if (fechaInicio == null) fechaInicio = LocalDate.now();
        if (fechaFin == null) fechaFin = LocalDate.now();

        CierreCajaResponseDTO reporte = reporteService.obtenerCierreCaja(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/sri/estado/{estado}")
    public ResponseEntity<List<FacturaResumenReporteDTO>> getFacturasPorEstado(@PathVariable String estado) {
        List<FacturaResumenReporteDTO> lista = reporteService.obtenerFacturasPorEstado(estado);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/cliente/{identificacion}")
    public ResponseEntity<List<FacturaResumenReporteDTO>> getHistorialCliente(@PathVariable String identificacion) {
        List<FacturaResumenReporteDTO> historial = reporteService.obtenerHistorialCliente(identificacion);
        return ResponseEntity.ok(historial);
    }
}
