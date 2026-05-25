package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
public class UsuarioViewController {

    private final RolRepository rolRepository;

    @GetMapping
    public String gestionarUsuarios(Model model) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("roles", rolRepository.findAll());
        return "gestionUsuarios";
    }
}
