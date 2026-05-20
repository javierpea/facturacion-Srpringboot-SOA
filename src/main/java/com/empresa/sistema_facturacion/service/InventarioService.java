package com.empresa.sistema_facturacion.service;

import com.empresa.sistema_facturacion.entity.Inventario;
import com.empresa.sistema_facturacion.repository.InventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;

    @Transactional(readOnly = true)
    public List<Inventario> obtenerInventarioPorSucursal(Long sucursalId) {
        return inventarioRepository.findBySucursalId(sucursalId);
    }
}