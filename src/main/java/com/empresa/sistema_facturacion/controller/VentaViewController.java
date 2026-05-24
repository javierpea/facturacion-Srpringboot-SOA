package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ventas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'CAJERO', 'ROLE_ADMIN', 'ROLE_CAJERO')")
public class VentaViewController {

    private final SucursalRepository sucursalRepository;

    @GetMapping("/nuevo")
    public String mostrarPuntoDeVenta(Model model) {
        model.addAttribute("sucursales", sucursalRepository.findAll());
        return "ventasPos";
    }
}