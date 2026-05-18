package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.dto.request.CategoriaCreateDTO;
import com.empresa.sistema_facturacion.entity.Categoria;
import com.empresa.sistema_facturacion.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<Categoria> crear(@Valid @RequestBody CategoriaCreateDTO dto) {
        return new ResponseEntity<>(categoriaService.crearCategoria(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> listar() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }
}