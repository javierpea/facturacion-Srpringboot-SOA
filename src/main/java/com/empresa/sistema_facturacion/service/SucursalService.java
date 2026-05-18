package com.empresa.sistema_facturacion.service;

import com.empresa.sistema_facturacion.entity.Sucursal;
import com.empresa.sistema_facturacion.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SucursalService {

    private final SucursalRepository sucursalRepository;

    @Transactional(readOnly = true)
    public List<Sucursal> listarTodas() {
        return sucursalRepository.findAll();
    }

    // TODO: crear sucursal
}
