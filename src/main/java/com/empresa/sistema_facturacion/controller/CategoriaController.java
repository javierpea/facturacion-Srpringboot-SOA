package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.dto.request.CategoriaRequestDTO;
import com.empresa.sistema_facturacion.service.CategoriaService;
import com.empresa.sistema_facturacion.repository.TarifaIvaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final TarifaIvaRepository tarifaIvaRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        // Enviamos un DTO vacío para que Thymeleaf dibuje el formulario
        model.addAttribute("categoriaDto", new CategoriaRequestDTO());
        // Enviamos las tarifas para el combo <select>
        model.addAttribute("tarifasIva", tarifaIvaRepository.findAll());
        return "gestionCategorias";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("categoriaDto") CategoriaRequestDTO dto,
                          BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Por favor, revise los datos ingresados.");
            return "redirect:/categorias";
        }
        try {
            categoriaService.crearCategoria(dto);
            flash.addFlashAttribute("success", "Categoría creada con éxito.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categorias";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("categoriaDto") CategoriaRequestDTO dto,
                             BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Datos de actualización inválidos.");
            return "redirect:/categorias";
        }
        try {
            categoriaService.actualizarCategoria(id, dto);
            flash.addFlashAttribute("success", "Categoría actualizada con éxito.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categorias";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id, RedirectAttributes flash) {
        try {
            categoriaService.toggleEstado(id);
            flash.addFlashAttribute("success", "Estado de categoría actualizado.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categorias";
    }
}