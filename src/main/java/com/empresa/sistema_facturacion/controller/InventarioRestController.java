package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.entity.Inventario;
import com.empresa.sistema_facturacion.service.InventarioService;
import com.empresa.sistema_facturacion.repository.InventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'BODEGA', 'CAJERO', 'ROLE_ADMIN', 'ROLE_BODEGA', 'ROLE_CAJERO')")
public class InventarioRestController {

    private final InventarioService inventarioService;
    private final InventarioRepository inventarioRepository;

    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<List<Inventario>> obtenerStockPorSucursal(@PathVariable Long sucursalId) {
        List<Inventario> stock = inventarioService.obtenerInventarioPorSucursal(sucursalId);
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<Inventario>> obtenerStockGlobalPorProducto(@PathVariable Long productoId) {
        List<Inventario> stockGlobal = inventarioRepository.findAll().stream()
                .filter(inv -> inv.getProducto().getId().equals(productoId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(stockGlobal);
    }
}