package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.dto.request.CategoriaCreateDTO;
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
        model.addAttribute("categoriaDto", new CategoriaCreateDTO());
        // Enviamos las tarifas para el combo <select>
        model.addAttribute("tarifasIva", tarifaIvaRepository.findAll());
        return "categorias/index";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("categoriaDto") CategoriaCreateDTO dto,
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
}