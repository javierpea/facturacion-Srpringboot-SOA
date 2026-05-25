package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.entity.Factura;
import com.empresa.sistema_facturacion.entity.Usuario;
import com.empresa.sistema_facturacion.service.FacturacionService;
import com.empresa.sistema_facturacion.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'CAJERO', 'ROLE_ADMIN', 'ROLE_CAJERO')")
public class FacturaViewController {

    private final FacturacionService facturacionService;
    private final UsuarioService usuarioService;

    @GetMapping("/facturas")
    public String listarFacturas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) String estadoSri,
            @RequestParam(required = false) String clienteIdentificacion,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario usuario = usuarioService.buscarPorUsername(username);

        Long usuarioIdFiltro = null;
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            // Si no es admin, solo ve sus propias facturas
            usuarioIdFiltro = usuario.getId();
        }

        LocalDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : null;

        List<Factura> facturas = facturacionService.listarConFiltros(
                inicio, fin, sucursalId, estadoSri, usuarioIdFiltro, clienteIdentificacion);

        model.addAttribute("facturas", facturas);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("sucursalId", sucursalId);
        model.addAttribute("estadoSri", estadoSri);
        model.addAttribute("clienteIdentificacion", clienteIdentificacion);

        return "facturas";
    }
}
