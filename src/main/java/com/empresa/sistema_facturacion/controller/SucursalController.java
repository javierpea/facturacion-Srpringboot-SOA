package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.entity.Sucursal;
import com.empresa.sistema_facturacion.service.SucursalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalService sucursalService;

    @GetMapping
    public ResponseEntity<List<Sucursal>> listarTodas() {
        return ResponseEntity.ok(sucursalService.listarTodas());
    }
}
