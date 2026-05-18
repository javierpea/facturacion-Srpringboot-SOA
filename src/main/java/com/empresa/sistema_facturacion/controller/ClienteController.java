package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.dto.request.ClienteRequestDTO;
import com.empresa.sistema_facturacion.entity.Cliente;
import com.empresa.sistema_facturacion.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<Cliente> crear(@Valid @RequestBody ClienteRequestDTO dto) {
        return new ResponseEntity<>(clienteService.registrarCliente(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{identificacion}")
    public ResponseEntity<Cliente> buscar(@PathVariable String identificacion) {
        return ResponseEntity.ok(clienteService.buscarPorIdentificacion(identificacion));
    }
}