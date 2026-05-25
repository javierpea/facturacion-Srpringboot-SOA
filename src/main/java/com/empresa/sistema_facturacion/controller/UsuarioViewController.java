package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class UsuarioViewController {

    private final RolRepository rolRepository;

    @GetMapping
    public String gestionarUsuarios(Model model) {
        model.addAttribute("roles", rolRepository.findAll());
        return "gestionUsuarios";
    }
}
