package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.entity.Sucursal;
import com.empresa.sistema_facturacion.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/inventario")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BODEGA')") // Protección estricta a nivel de controlador
public class InventarioViewController {

    private final SucursalRepository sucursalRepository;

    @GetMapping
    public String mostrarPantallaInventario(Model model) {
        // Traemos las sucursales para llenar el filtro principal del inventario
        List<Sucursal> sucursales = sucursalRepository.findAll();
        model.addAttribute("sucursales", sucursales);
        return "inventario";
    }
}