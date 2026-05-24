package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.dto.request.VentaRequestDTO;
import com.empresa.sistema_facturacion.dto.response.VentaFacturadaResponseDTO;
import com.empresa.sistema_facturacion.service.FacturacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'CAJERO', 'ROLE_ADMIN', 'ROLE_CAJERO')")
public class VentaController {

    private final FacturacionService facturacionService;

    @PostMapping
    public ResponseEntity<?> registrarVenta(@Valid @RequestBody VentaRequestDTO request) {
        try {
            String usernameCajero = SecurityContextHolder.getContext().getAuthentication().getName();

            VentaFacturadaResponseDTO response = facturacionService.registrarVentaYFacturar(request, usernameCajero);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno al procesar la transacción: " + e.getMessage()));
        }
    }
}