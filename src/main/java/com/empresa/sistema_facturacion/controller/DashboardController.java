package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        model.addAttribute(
                "username",
                auth.getName()
        );

        model.addAttribute(
                "ventasHoy",
                dashboardService.obtenerVentasHoy()
        );

        model.addAttribute(
                "facturasHoy",
                dashboardService.obtenerFacturasHoy()
        );

        model.addAttribute(
                "clientesRegistrados",
                dashboardService.obtenerClientes()
        );

        model.addAttribute(
                "productosRegistrados",
                dashboardService.obtenerProductos()
        );

        model.addAttribute(
                "usuariosActivos",
                dashboardService.obtenerUsuariosActivos()
        );

        model.addAttribute(
                "sucursalesActivas",
                dashboardService.obtenerSucursalesActivas()
        );

        model.addAttribute(
                "ultimasFacturas",
                dashboardService.obtenerUltimasFacturas()
        );

        model.addAttribute(
                "ventasMensuales",
                dashboardService.obtenerVentasMensuales()
        );

        return "dashboard";
    }
}