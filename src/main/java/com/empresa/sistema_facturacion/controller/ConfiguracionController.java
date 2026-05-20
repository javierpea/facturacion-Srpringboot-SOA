package com.empresa.sistema_facturacion.controller;

import com.empresa.sistema_facturacion.dto.request.ConfiguracionSriRequestDTO;
import com.empresa.sistema_facturacion.entity.ConfiguracionSRI;
import com.empresa.sistema_facturacion.repository.ConfiguracionSRIRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
@RequiredArgsConstructor
public class ConfiguracionController {

    private final ConfiguracionSRIRepository configRepository;

    @PostMapping("/sri")
    public ResponseEntity<?> guardarConfiguracionBase(@Valid @RequestBody ConfiguracionSriRequestDTO dto) {
        try {
            ConfiguracionSRI config = configRepository.findTopByOrderByIdDesc();

            if (config == null) {
                config = new ConfiguracionSRI();
            }

            config.setRuc(dto.getRuc());
            config.setRazonSocial(dto.getRazonSocial());
            config.setNombreComercial(dto.getNombreComercial() != null ? dto.getNombreComercial() : dto.getRazonSocial());
            config.setDireccionMatriz(dto.getDireccionMatriz());
            config.setObligadoContabilidad(dto.getObligadoContabilidad().toUpperCase());
            config.setAmbiente(dto.getAmbiente());
            config.setTipoEmision(dto.getTipoEmision());

            configRepository.save(config);
            return ResponseEntity.ok(Map.of("mensaje", "Datos generales del SRI configurados con éxito."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping(value = "/sri/firma", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirFirmaElectronica(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El archivo de la firma está vacío."));
            }

            ConfiguracionSRI config = configRepository.findTopByOrderByIdDesc();
            if (config == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Primero debe configurar los datos generales de la empresa usando el método POST."));
            }

            config.setArchivoP12(file.getBytes());
            config.setPasswordP12(password);

            configRepository.save(config);
            return ResponseEntity.ok(Map.of("mensaje", "Firma electrónica (.p12) cargada y resguardada en la BD con éxito."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/sri")
    public ResponseEntity<?> obtenerConfiguracionActual() {
        ConfiguracionSRI config = configRepository.findTopByOrderByIdDesc();
        if (config == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "No hay ninguna configuración registrada en el sistema."));
        }

        return ResponseEntity.ok(Map.of(
                "ruc", config.getRuc(),
                "razonSocial", config.getRazonSocial(),
                "nombreComercial", config.getNombreComercial(),
                "direccionMatriz", config.getDireccionMatriz(),
                "ambiente", config.getAmbiente().equals("1") ? "1 (Pruebas)" : "2 (Producción)",
                "obligadoContabilidad", config.getObligadoContabilidad(),
                "hasFirmaCargada", config.getArchivoP12() != null
        ));
    }
}
