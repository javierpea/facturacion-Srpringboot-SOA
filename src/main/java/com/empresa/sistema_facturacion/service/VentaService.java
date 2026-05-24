package com.empresa.sistema_facturacion.service;

import com.empresa.sistema_facturacion.dto.request.DetalleVentaRequestDTO;
import com.empresa.sistema_facturacion.dto.request.VentaRequestDTO;
import com.empresa.sistema_facturacion.entity.*;
import com.empresa.sistema_facturacion.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final SucursalRepository sucursalRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public Venta guardarEntidadVenta(VentaRequestDTO request, String usernameCajero) {

        Sucursal sucursalFacturacion = sucursalRepository.findById(request.getSucursalId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Usuario cajero = usuarioService.buscarPorUsername(usernameCajero);

        Venta venta = new Venta();
        venta.setSucursal(sucursalFacturacion); // La factura se emite en la sucursal actual
        venta.setCliente(cliente);
        venta.setUsuario(cajero);
        venta.setFechaEmision(LocalDateTime.now());

        BigDecimal subtotalVenta = BigDecimal.ZERO;
        BigDecimal ivaVenta = BigDecimal.ZERO;

        for (DetalleVentaRequestDTO item : request.getDetalles()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // LÓGICA DINÁMICA DE STOCK: Determinar de qué sucursal se sustrae el producto
            Long sucursalDespachoId = item.getSucursalId() != null ? item.getSucursalId() : sucursalFacturacion.getId();

            Inventario inventario = inventarioRepository.findByProductoIdAndSucursalId(producto.getId(), sucursalDespachoId)
                    .orElseThrow(() -> new RuntimeException("El producto '" + producto.getNombreGenerico() + "' no tiene un inventario asignado en la sucursal de despacho seleccionada."));

            if (inventario.getCantidadDisponible() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente en la sucursal elegida para: " + producto.getNombreGenerico());
            }

            // Decrementar stock de la sucursal de origen real
            inventario.setCantidadDisponible(inventario.getCantidadDisponible() - item.getCantidad());
            inventarioRepository.save(inventario);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioUnitario());

            BigDecimal subtotalItem = producto.getPrecioUnitario().multiply(new BigDecimal(item.getCantidad()));
            detalle.setSubtotal(subtotalItem);

            TarifaIva tarifaDelProducto = producto.getCategoria().getTarifaIva();
            detalle.setCodigoIvaSriAplicado(tarifaDelProducto.getCodigoSri());
            detalle.setPorcentajeIvaAplicado(tarifaDelProducto.getPorcentaje());

            BigDecimal multiplicadorIva = tarifaDelProducto.getPorcentaje().divide(new BigDecimal("100"));
            BigDecimal valorIvaItem = subtotalItem.multiply(multiplicadorIva).setScale(2, RoundingMode.HALF_UP);
            detalle.setValorIva(valorIvaItem);

            venta.getDetalles().add(detalle);

            subtotalVenta = subtotalVenta.add(subtotalItem);
            ivaVenta = ivaVenta.add(valorIvaItem);
        }

        venta.setSubtotal(subtotalVenta);
        venta.setValorIva(ivaVenta);
        venta.setTotal(subtotalVenta.add(ivaVenta));

        return ventaRepository.saveAndFlush(venta);
    }
}