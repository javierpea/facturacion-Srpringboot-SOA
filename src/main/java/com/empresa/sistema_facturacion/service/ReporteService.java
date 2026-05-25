package com.empresa.sistema_facturacion.service;

import com.empresa.sistema_facturacion.dto.response.CierreCajaResponseDTO;
import com.empresa.sistema_facturacion.dto.response.FacturaResumenReporteDTO;
import com.empresa.sistema_facturacion.entity.DetalleVenta;
import com.empresa.sistema_facturacion.entity.Factura;
import com.empresa.sistema_facturacion.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final FacturaRepository facturaRepository;

    /**
     * REPORT 1: Cierre de Caja Financiero por Rango de Fechas
     */
    public CierreCajaResponseDTO obtenerCierreCaja(LocalDate fechaInicio, LocalDate fechaFin) {
        // Convertimos las fechas locales a LocalDateTime para cubrir todo el rango de horas (00:00:00 a 23:59:59)
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

// Obtener el usuario y su rol
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String usernameConnected = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        // Traer las facturas autorizadas
        List<Factura> facturas = facturaRepository.findByVentaFechaEmisionBetween(inicio, fin).stream()
                .filter(f -> f.getEstadoSri().equals("AUTORIZADA"))
                // FILTRO CRUCIAL: Si es CAJERO, solo pasan las facturas donde el usuario de la venta coincida con él
                .filter(f -> isAdmin || f.getVenta().getUsuario().getUsername().equals(usernameConnected))
                .collect(Collectors.toList());

        BigDecimal subtotal15 = BigDecimal.ZERO;
        BigDecimal subtotal0 = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;
        BigDecimal totalGeneral = BigDecimal.ZERO;

        for (Factura f : facturas) {
            totalIva = totalIva.add(f.getVenta().getValorIva());
            totalGeneral = totalGeneral.add(f.getVenta().getTotal());

            // Desglosamos los subtotales dinámicamente iterando sobre los detalles de cada venta
            for (DetalleVenta detalle : f.getVenta().getDetalles()) {
                if (detalle.getPorcentajeIvaAplicado().compareTo(BigDecimal.ZERO) == 0) {
                    subtotal0 = subtotal0.add(detalle.getSubtotal());
                } else {
                    subtotal15 = subtotal15.add(detalle.getSubtotal());
                }
            }
        }

        CierreCajaResponseDTO cierre = new CierreCajaResponseDTO();
        cierre.setFechaReporte(fechaInicio);
        cierre.setCantidadFacturasEmitidas((long) facturas.size());
        cierre.setSubtotal15(subtotal15);
        cierre.setSubtotal0(subtotal0);
        cierre.setTotalSinImpuestos(subtotal15.add(subtotal0));
        cierre.setTotalIvaRecaudado(totalIva);
        cierre.setTotalGeneral(totalGeneral);

        return cierre;
    }

    /**
     * REPORT 2: Listar facturas con incidencias o por un estado específico del SRI
     */
    public List<FacturaResumenReporteDTO> obtenerFacturasPorEstado(String estado) {
        return facturaRepository.findByEstadoSri(estado.toUpperCase()).stream()
                .map(this::mapearAResumenDTO)
                .collect(Collectors.toList());
    }

    /**
     * REPORT 3: Historial de facturas autorizadas de un cliente por su Cédula/RUC
     */
    public List<FacturaResumenReporteDTO> obtenerHistorialCliente(String identificacion) {
        return facturaRepository.findByVentaClienteIdentificacionAndEstadoSri(identificacion, "AUTORIZADA").stream()
                .map(this::mapearAResumenDTO)
                .collect(Collectors.toList());
    }

    /**
     * Helper para transformar la entidad compleja en un resumen plano ideal para tablas del Frontend
     */
    private FacturaResumenReporteDTO mapearAResumenDTO(Factura factura) {
        FacturaResumenReporteDTO dto = new FacturaResumenReporteDTO();
        dto.setFacturaId(factura.getId());
        dto.setSecuenciaCompleta(factura.getEstablecimiento() + "-" + factura.getPuntoEmision() + "-" + factura.getSecuencial());
        dto.setClaveAcceso(factura.getClaveAcceso());
        dto.setFechaEmision(factura.getVenta().getFechaEmision());
        dto.setClienteRazonSocial(factura.getVenta().getCliente().getRazonSocial());
        dto.setClienteIdentificacion(factura.getVenta().getCliente().getIdentificacion());
        dto.setTotalFactura(factura.getVenta().getTotal());
        dto.setEstadoSri(factura.getEstadoSri());
        return dto;
    }
}
