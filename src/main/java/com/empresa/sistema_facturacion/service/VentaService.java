package com.empresa.sistema_facturacion.service;

import com.empresa.sistema_facturacion.dto.request.DetalleVentaRequestDTO;
import com.empresa.sistema_facturacion.dto.request.VentaRequestDTO;
import com.empresa.sistema_facturacion.dto.response.VentaResponseDTO;
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

    // usernameCajero vendrá luego del token JWT de Spring Security
    @Transactional
    public VentaResponseDTO procesarVenta(VentaRequestDTO request, String usernameCajero) {

        Sucursal sucursal = sucursalRepository.findById(request.getSucursalId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Usuario cajero = usuarioService.buscarPorUsername(usernameCajero);

        // cabecera de la Venta
        Venta venta = new Venta();
        venta.setSucursal(sucursal);
        venta.setCliente(cliente);
        venta.setUsuario(cajero);
        venta.setFechaEmision(LocalDateTime.now());

        BigDecimal subtotalVenta = BigDecimal.ZERO;
        BigDecimal ivaVenta = BigDecimal.ZERO;

        // detalle venta
        for (DetalleVentaRequestDTO item : request.getDetalles()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // Inventario en  sucursal específica
            Inventario inventario = inventarioRepository.findByProductoIdAndSucursalId(producto.getId(), sucursal.getId())
                    .orElseThrow(() -> new RuntimeException("El producto no está asignado al inventario de esta sucursal"));

            if (inventario.getCantidadDisponible() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombreGenerico());
            }

            inventario.setCantidadDisponible(inventario.getCantidadDisponible() - item.getCantidad());
            inventarioRepository.save(inventario);


            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());

            detalle.setPrecioUnitario(producto.getPrecioUnitario());
            BigDecimal subtotalItem = producto.getPrecioUnitario().multiply(new BigDecimal(item.getCantidad()));
            detalle.setSubtotal(subtotalItem);

            // Snapshot del IVA
            TarifaIva tarifaDelProducto = producto.getCategoria().getTarifaIva();

            detalle.setCodigoIvaSriAplicado(tarifaDelProducto.getCodigoSri());
            detalle.setPorcentajeIvaAplicado(tarifaDelProducto.getPorcentaje());

            BigDecimal multiplicadorIva = tarifaDelProducto.getPorcentaje().divide(new BigDecimal("100"));
            BigDecimal valorIvaItem = subtotalItem.multiply(multiplicadorIva).setScale(2, RoundingMode.HALF_UP);
            detalle.setValorIva(valorIvaItem);

            // Agregar a la lista
            venta.getDetalles().add(detalle);

            subtotalVenta = subtotalVenta.add(subtotalItem);
            ivaVenta = ivaVenta.add(valorIvaItem);
        }

        venta.setSubtotal(subtotalVenta);
        venta.setValorIva(ivaVenta);
        venta.setTotal(subtotalVenta.add(ivaVenta));

        Venta ventaGuardada = ventaRepository.save(venta);

        //TODO: generar XML

        return mapearAResponse(ventaGuardada);
    }

    private VentaResponseDTO mapearAResponse(Venta venta) {
        VentaResponseDTO response = new VentaResponseDTO();
        response.setVentaId(venta.getId());
        response.setFechaEmision(venta.getFechaEmision());
        response.setClienteRazonSocial(venta.getCliente().getRazonSocial());
        response.setClienteIdentificacion(venta.getCliente().getIdentificacion());
        response.setSubtotal(venta.getSubtotal());
        response.setValorIva(venta.getValorIva());
        response.setTotal(venta.getTotal());
        return response;
    }
}