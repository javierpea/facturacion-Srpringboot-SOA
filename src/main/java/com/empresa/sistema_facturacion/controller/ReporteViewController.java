package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.entity.Factura;
import com.empresa.sistema_facturacion.service.FacturacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReporteViewController {

    private final FacturacionService facturacionService;

    @GetMapping("/reportes")
    public String mostrarReportes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) String estadoSri,
            @RequestParam(required = false) String clienteIdentificacion,
            Model model) {

        LocalDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : null;

        List<Factura> facturas = facturacionService.listarConFiltros(
                inicio, fin, sucursalId, estadoSri, null, clienteIdentificacion);

        BigDecimal totalVentas = facturas.stream()
                .map(f -> f.getVenta().getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIva = facturas.stream()
                .map(f -> f.getVenta().getValorIva())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSubtotal = facturas.stream()
                .map(f -> f.getVenta().getSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("facturas", facturas);
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalIva", totalIva);
        model.addAttribute("totalSubtotal", totalSubtotal);
        
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("sucursalId", sucursalId);
        model.addAttribute("estadoSri", estadoSri);
        model.addAttribute("clienteIdentificacion", clienteIdentificacion);

        return "reportes";
    }
}
