package com.empresa.sistema_facturacion.service;

import com.empresa.sistema_facturacion.entity.Factura;
import com.empresa.sistema_facturacion.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VentaRepository ventaRepository;
    private final FacturaRepository facturaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;

    public BigDecimal obtenerVentasHoy() {

        LocalDate hoy = LocalDate.now();

        return ventaRepository.totalVentasDia(
                hoy.atStartOfDay(),
                hoy.atTime(23,59,59)
        );
    }

    public Long obtenerFacturasHoy() {

        LocalDate hoy = LocalDate.now();

        return ventaRepository.countByFechaEmisionBetween(
                hoy.atStartOfDay(),
                hoy.atTime(23,59,59)
        );
    }

    public Long obtenerClientes() {
        return clienteRepository.count();
    }

    public Long obtenerProductos() {
        return productoRepository.count();
    }

    public Long obtenerUsuariosActivos() {
        return usuarioRepository.countByActivoTrue();
    }

    public Long obtenerSucursalesActivas() {
        return sucursalRepository.countByActivoTrue();
    }

    public List<Factura> obtenerUltimasFacturas() {
        return facturaRepository.findAll()
                .stream()
                .sorted((a,b) ->
                        b.getVenta().getFechaEmision()
                                .compareTo(a.getVenta().getFechaEmision()))
                .limit(10)
                .toList();
    }

    public List<Object[]> obtenerVentasMensuales() {
        return ventaRepository.ventasMensuales();
    }
}