package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.entity.ConfiguracionSRI;
import com.empresa.sistema_facturacion.service.ConfiguracionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/configuracion-sri")
@RequiredArgsConstructor
public class ConfiguracionViewController {

    private final ConfiguracionService configuracionService;

    @GetMapping("")
    public String verConfiguracion(Model model) {
        model.addAttribute("configuracion", configuracionService.obtenerConfiguracion());
        return "configurationPanel";
    }

    @PostMapping("/guardar")
    public String guardarConfiguracion(@ModelAttribute ConfiguracionSRI configuracion,
                                       @RequestParam(value = "firmaFile", required = false) MultipartFile firmaFile,
                                       @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                                       RedirectAttributes redirectAttributes) {
        try {
            configuracionService.guardarOActualizar(configuracion, firmaFile, logoFile);
            redirectAttributes.addFlashAttribute("success", "Configuración guardada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la configuración: " + e.getMessage());
        }
        return "redirect:/configuracion-sri";
    }
}
