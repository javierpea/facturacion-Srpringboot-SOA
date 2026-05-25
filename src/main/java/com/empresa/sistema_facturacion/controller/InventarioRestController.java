package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.dto.request.AjusteStockRequestDTO;
import com.empresa.sistema_facturacion.entity.Inventario;
import com.empresa.sistema_facturacion.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioRestController {

    private final InventarioService inventarioService;

    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<List<Inventario>> getInventarioPorSucursal(@PathVariable Long sucursalId) {
        return ResponseEntity.ok(inventarioService.obtenerInventarioPorSucursal(sucursalId));
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<Inventario>> getInventarioPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.obtenerInventarioPorProducto(productoId));
    }

    @PostMapping("/ajustar")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BODEGA', 'ROLE_ADMIN', 'ROLE_BODEGA')")
    public ResponseEntity<?> ajustarInventarioAPI(@RequestBody AjusteStockRequestDTO dto) {
        try {
            inventarioService.registrarAjusteOIngreso(dto);
            return ResponseEntity.ok(Map.of("message", "Stock actualizado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}