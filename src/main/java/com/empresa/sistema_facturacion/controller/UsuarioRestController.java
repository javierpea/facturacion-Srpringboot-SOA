package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.dto.request.UsuarioRegisterDTO;
import com.empresa.sistema_facturacion.dto.request.UsuarioUpdateDTO;
import com.empresa.sistema_facturacion.entity.Rol;
import com.empresa.sistema_facturacion.entity.Usuario;
import com.empresa.sistema_facturacion.repository.RolRepository;
import com.empresa.sistema_facturacion.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class UsuarioRestController {

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;

    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody UsuarioRegisterDTO dto) {
        Rol rol = rolRepository.findById(dto.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(dto.getPassword());
        usuario.setRol(rol);

        usuarioService.crearUsuario(usuario);
        return ResponseEntity.ok(Map.of("message", "Usuario creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        Rol rol = rolRepository.findById(dto.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(dto.getPassword());
        usuario.setRol(rol);

        usuarioService.actualizarUsuario(id, usuario);
        return ResponseEntity.ok(Map.of("message", "Usuario actualizado exitosamente"));
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<?> toggle(@PathVariable Long id) {
        usuarioService.toggleEstado(id);
        return ResponseEntity.ok(Map.of("message", "Estado de usuario actualizado"));
    }
}
