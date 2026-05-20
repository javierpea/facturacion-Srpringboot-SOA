package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.dto.request.VentaRequestDTO;
import com.empresa.sistema_facturacion.dto.response.VentaFacturadaResponseDTO;
import com.empresa.sistema_facturacion.dto.response.VentaResponseDTO;
import com.empresa.sistema_facturacion.service.FacturacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final FacturacionService facturacionService;

    @PostMapping
    public ResponseEntity<VentaFacturadaResponseDTO> registrarVenta(
            @Valid @RequestBody VentaRequestDTO request) {
        String usernameCajero = SecurityContextHolder.getContext().getAuthentication().getName();

        VentaFacturadaResponseDTO response = facturacionService.registrarVentaYFacturar(request, usernameCajero);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}