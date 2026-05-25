package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.repository.ProductoRepository;
import com.empresa.sistema_facturacion.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inventario")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'BODEGA', 'CAJERO', 'ROLE_ADMIN', 'ROLE_BODEGA', 'ROLE_CAJERO')")
public class InventarioViewController {

    private final SucursalRepository sucursalRepository;
    private final ProductoRepository productoRepository;

    @GetMapping
    public String mostrarPantallaInventario(Model model) {
        model.addAttribute("sucursales", sucursalRepository.findAll());
        model.addAttribute("productos", productoRepository.findAll()); // Catálogo para el modal
        return "inventario";
    }
}