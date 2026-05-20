package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.entity.Inventario;
import com.empresa.sistema_facturacion.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BODEGA')") // Blindaje de seguridad a nivel de API REST de datos
public class InventarioRestController {

    private final InventarioService inventarioService;

    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<List<Inventario>> obtenerStockPorSucursal(@PathVariable Long sucursalId) {
        List<Inventario> stock = inventarioService.obtenerInventarioPorSucursal(sucursalId);
        return ResponseEntity.ok(stock);
    }
}