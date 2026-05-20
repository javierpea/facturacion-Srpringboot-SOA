package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.entity.Sucursal;
import com.empresa.sistema_facturacion.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalRepository sucursalRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("sucursales", sucursalRepository.findAll());
        model.addAttribute("sucursal", new Sucursal());
        return "sucursales/index";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("sucursal") Sucursal sucursal, RedirectAttributes flash) {
        try {
            sucursalRepository.save(sucursal);
            flash.addFlashAttribute("success", "Sucursal matriz/adicional creada.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Ocurrió un error al guardar la sucursal.");
        }
        return "redirect:/sucursales";
    }
}