package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.dto.request.ProductoCreateDTO;
import com.empresa.sistema_facturacion.service.ProductoService;
import com.empresa.sistema_facturacion.repository.ProductoRepository;
import com.empresa.sistema_facturacion.repository.CategoriaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("productoDto", new ProductoCreateDTO());
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "productos/index";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("productoDto") ProductoCreateDTO dto,
                          BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Revise los campos del producto.");
            return "redirect:/productos";
        }
        try {
            productoService.crearProducto(dto);
            flash.addFlashAttribute("success", "Producto registrado en el catálogo.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }
}