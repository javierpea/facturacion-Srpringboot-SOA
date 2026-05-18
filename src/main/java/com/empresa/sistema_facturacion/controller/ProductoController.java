package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.dto.request.ProductoCreateDTO;
import com.empresa.sistema_facturacion.dto.response.ProductoResponseDTO;
import com.empresa.sistema_facturacion.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoCreateDTO dto) {
        return new ResponseEntity<>(productoService.crearProducto(dto), HttpStatus.CREATED);
    }
}
