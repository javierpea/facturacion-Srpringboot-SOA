package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.dto.request.ClienteRequestDTO;
import com.empresa.sistema_facturacion.service.ClienteService;
import com.empresa.sistema_facturacion.repository.ClienteRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteRepository clienteRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteRepository.findAll());
        model.addAttribute("clienteDto", new ClienteRequestDTO());
        return "clientes/index";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("clienteDto") ClienteRequestDTO dto,
                          BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Los datos de identificación o razón social son inválidos.");
            return "redirect:/clientes";
        }
        try {
            clienteService.registrarCliente(dto);
            flash.addFlashAttribute("success", "Cliente guardado exitosamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clientes";
    }
}