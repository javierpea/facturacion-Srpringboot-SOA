package com.empresa.sistema_facturacion.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model) {
        // Recuperamos el usuario autenticado desde el contexto seguro
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Evaluamos los roles para pasarlos de forma explícita (opcional, ya que usaremos sec:authorize)
        model.addAttribute("username", username);

        return "dashboard";
    }
}