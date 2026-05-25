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

    @Transactional
    public void ajustarStock(Long inventarioId, int cantidad) {
        Inventario inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        int nuevaCantidad = inventario.getCantidadDisponible() + cantidad;

        if (nuevaCantidad < 0) {
            throw new RuntimeException("No se puede reducir el stock por debajo de cero");
        }

        inventario.setCantidadDisponible(nuevaCantidad);
        inventarioRepository.save(inventario);
    }
}