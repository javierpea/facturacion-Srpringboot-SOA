package com.empresa.sistema_facturacion.service;

import com.empresa.sistema_facturacion.entity.ConfiguracionSRI;
import com.empresa.sistema_facturacion.repository.ConfiguracionSRIRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ConfiguracionService {

    private final ConfiguracionSRIRepository configuracionSRIRepository;
    private final com.empresa.sistema_facturacion.util.EncryptionUtil encryptionUtil;

    public ConfiguracionSRI obtenerConfiguracion() {
        ConfiguracionSRI config = configuracionSRIRepository.findTopByOrderByIdDesc();
        if (config == null) {
            return new ConfiguracionSRI();
        }
        return config;
    }

    public void guardarOActualizar(ConfiguracionSRI entidad, MultipartFile firmaFile, MultipartFile logoFile) throws IOException {
        ConfiguracionSRI existente = configuracionSRIRepository.findTopByOrderByIdDesc();
        
        // Encriptamos la clave del P12 antes de persistirla
        if (entidad.getPasswordP12() != null && !entidad.getPasswordP12().isEmpty()) {
            String claveEncriptada = encryptionUtil.encriptar(entidad.getPasswordP12());
            entidad.setPasswordP12(claveEncriptada);
        }

        if (existente != null) {
            entidad.setId(existente.getId());
            // Si no se suben archivos nuevos, mantenemos los anteriores
            if (firmaFile == null || firmaFile.isEmpty()) {
                entidad.setArchivoP12(existente.getArchivoP12());
            }
            if (logoFile == null || logoFile.isEmpty()) {
                entidad.setLogo(existente.getLogo());
            }
        }

        if (firmaFile != null && !firmaFile.isEmpty()) {
            entidad.setArchivoP12(firmaFile.getBytes());
        }

        if (logoFile != null && !logoFile.isEmpty()) {
            entidad.setLogo(logoFile.getBytes());
        }

        configuracionSRIRepository.save(entidad);
    }
}
