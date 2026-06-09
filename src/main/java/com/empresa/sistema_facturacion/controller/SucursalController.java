package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.entity.Sucursal;
import com.empresa.sistema_facturacion.service.SucursalService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalService sucursalService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("sucursales", sucursalService.listarTodas());
        model.addAttribute("sucursal", new Sucursal());
        return "gestionSucursales";
    }

    @PostMapping("/guardar")
public String guardar(@Valid @ModelAttribute("sucursal") Sucursal sucursal, BindingResult result, RedirectAttributes flash) {
    if (result.hasErrors()) {
        // AGREGA ESTO PARA VER EL ERROR EN LA CONSOLA DE TU IDE:
        System.out.println("--- ERRORES DE VALIDACIÓN ---");
        result.getFieldErrors().forEach(err -> {
            System.out.println("Campo fallido: " + err.getField() + " - Motivo: " + err.getDefaultMessage() + " - Valor rechazado: " + err.getRejectedValue());
        });
        System.out.println("-----------------------------");

        flash.addFlashAttribute("error", "Error de validación: Por favor, revise los campos del formulario.");
        return "redirect:/sucursales";
    }
    
    try {
        sucursalService.guardar(sucursal);
        flash.addFlashAttribute("success", "Sucursal guardada exitosamente.");
    } catch (Exception e) {
        flash.addFlashAttribute("error", "Ocurrió un error al guardar la sucursal.");
    }
    return "redirect:/sucursales";
}

    @PostMapping("/toggle/{id}")
    public String toggleEstado(@PathVariable Long id, RedirectAttributes flash) {
        try {
            sucursalService.toggleEstado(id);
            flash.addFlashAttribute("success", "Estado de sucursal actualizado.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo cambiar el estado.");
        }
        return "redirect:/sucursales";
    }
}