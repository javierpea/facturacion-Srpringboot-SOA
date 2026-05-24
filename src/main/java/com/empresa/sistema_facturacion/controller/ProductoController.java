package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.dto.request.ProductoCreateDTO;
import com.empresa.sistema_facturacion.entity.Producto;
import com.empresa.sistema_facturacion.service.ProductoService;
import com.empresa.sistema_facturacion.repository.ProductoRepository;
import com.empresa.sistema_facturacion.repository.CategoriaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
        return "gestionProductos";
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

    @GetMapping("/api/buscar")
    @ResponseBody
    public ResponseEntity<Page<Producto>> buscarProductos(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (!query.isBlank()) {
            java.util.Optional<Producto> porCodigo = productoRepository.findByCodigoPrincipal(query);
            if (porCodigo.isPresent()) {
                return ResponseEntity.ok(new PageImpl<>(List.of(porCodigo.get()), pageable, 1));
            }
        }

        Page<Producto> productos = query.isBlank()
                ? productoRepository.findAll(pageable)
                : productoRepository.findByNombreGenericoContainingIgnoreCase(query, pageable);

        return ResponseEntity.ok(productos);
    }
}