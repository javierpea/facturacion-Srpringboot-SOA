package com.empresa.sistema_facturacion.service;

import com.empresa.sistema_facturacion.dto.request.AjusteStockRequestDTO;
import com.empresa.sistema_facturacion.entity.Inventario;
import com.empresa.sistema_facturacion.entity.Producto;
import com.empresa.sistema_facturacion.entity.Sucursal;
import com.empresa.sistema_facturacion.repository.InventarioRepository;
import com.empresa.sistema_facturacion.repository.ProductoRepository;
import com.empresa.sistema_facturacion.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;

    @Transactional(readOnly = true)
    public List<Inventario> obtenerInventarioPorSucursal(Long sucursalId) {
        return inventarioRepository.findBySucursalId(sucursalId);
    }

    @Transactional(readOnly = true)
    public List<Inventario> obtenerInventarioPorProducto(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }

    @Transactional
    public void registrarAjusteOIngreso(AjusteStockRequestDTO request) {
        Inventario inventario;

        // ESCENARIO 1: Viene de la tabla principal (Modificación de stock existente)
        if (request.getInventarioId() != null) {
            inventario = inventarioRepository.findById(request.getInventarioId())
                    .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));
        }
        // ESCENARIO 2: Viene del modal de "Añadir Producto"
        else {
            // Buscamos si el producto ya existía en esta bodega
            inventario = inventarioRepository
                    .findByProductoIdAndSucursalId(request.getProductoId(), request.getSucursalId())
                    .orElse(null);

            // Si es la primera vez que la sucursal ve este producto, creamos el vínculo
            if (inventario == null) {
                inventario = new Inventario();
                Producto producto = productoRepository.findById(request.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                Sucursal sucursal = sucursalRepository.findById(request.getSucursalId())
                        .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

                inventario.setProducto(producto);
                inventario.setSucursal(sucursal);
                inventario.setCantidadDisponible(0); // Empezamos en 0 y le sumamos abajo
            }
        }

        // Operación matemática central (Aplica tanto para escenarios 1 y 2)
        int nuevaCantidad = inventario.getCantidadDisponible() + request.getCantidad();

        if (nuevaCantidad < 0) {
            throw new RuntimeException("La cantidad no puede resultar en un stock negativo");
        }

        inventario.setCantidadDisponible(nuevaCantidad);
        inventarioRepository.save(inventario);
    }
}