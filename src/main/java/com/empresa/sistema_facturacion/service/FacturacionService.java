package com.empresa.sistema_facturacion.service;

import com.empresa.sistema_facturacion.entity.*;
import com.empresa.sistema_facturacion.repository.ConfiguracionSRIRepository;
import com.empresa.sistema_facturacion.repository.FacturaRepository;
import com.empresa.sistema_facturacion.repository.VentaRepository;
import com.empresa.sistema_facturacion.util.sri.modelo.*;
import com.empresa.sistema_facturacion.util.sri.ClaveAccesoUtil;
import com.empresa.sistema_facturacion.util.sri.GeneradorXmlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacturacionService {

    private final VentaRepository ventaRepository;
    private final FacturaRepository facturaRepository;
    private final ConfiguracionSRIRepository configRepository;
    private final ClaveAccesoUtil claveAccesoUtil;
    private final GeneradorXmlService generadorXmlService;

    @Transactional
    public Factura generarFacturaXML(Long ventaId) {

        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        // Validar que no se haya facturado antes
        if (facturaRepository.findByVentaId(ventaId).isPresent()) {
            throw new RuntimeException("Esta venta ya tiene una factura electrónica generada.");
        }

        // Obtener la empresa
        ConfiguracionSRI config = configRepository.findTopByOrderByIdDesc();
        if (config == null) {
            throw new RuntimeException("No se ha registrado la configuración del SRI (RUC, Ambiente, etc.)");
        }

        //  Generar Secuencial
        long cantidadFacturas = facturaRepository.count();
        String secuencial = String.format("%09d", cantidadFacturas + 1);
        String establecimiento = venta.getSucursal().getCodigoEstablecimiento();
        String puntoEmision = "001"; // Por ahora estático, luego puede venir de la caja del usuario

        // Generar Clave de Acceso
        String codigoNumerico = claveAccesoUtil.generarCodigoNumerico();
        String claveAcceso = claveAccesoUtil.generarClaveAcceso(
                venta.getFechaEmision().toLocalDate(),
                "01", // 01 = Factura
                config.getRuc(),
                config.getAmbiente(),
                establecimiento + puntoEmision,
                secuencial,
                codigoNumerico,
                config.getTipoEmision()
        );

        // ARMAR EL MODELO JAXB
        FacturaXml facturaXml = new FacturaXml();

        facturaXml.setInfoTributaria(construirInfoTributaria(config, claveAcceso, establecimiento, puntoEmision, secuencial));

        facturaXml.setInfoFactura(construirInfoFactura(venta, config, establecimiento));

        facturaXml.setDetalles(construirDetalles(venta));


        // GENERAR EL STRING XML Y GUARDAR

        String xmlGenerado = generadorXmlService.convertirObjetoAXml(facturaXml);

        Factura nuevaFactura = new Factura();
        nuevaFactura.setVenta(venta);
        nuevaFactura.setEstablecimiento(establecimiento);
        nuevaFactura.setPuntoEmision(puntoEmision);
        nuevaFactura.setSecuencial(secuencial);
        nuevaFactura.setClaveAcceso(claveAcceso);
        nuevaFactura.setEstadoSri("CREADA");
        nuevaFactura.setXmlFirmado(xmlGenerado); // Temporalmente guardamos el XML sin firmar aquí para depurar

        return facturaRepository.save(nuevaFactura);
    }

    // --- MÉTODOS AUXILIARES PARA LIMPIEZA DE CÓDIGO ---

    private InfoTributaria construirInfoTributaria(ConfiguracionSRI config, String claveAcceso, String estab, String ptoEmi, String secuencial) {
        InfoTributaria info = new InfoTributaria();
        info.setAmbiente(config.getAmbiente());
        info.setTipoEmision(config.getTipoEmision());
        info.setRazonSocial(config.getRazonSocial());
        info.setNombreComercial(config.getNombreComercial() != null ? config.getNombreComercial() : config.getRazonSocial());
        info.setRuc(config.getRuc());
        info.setClaveAcceso(claveAcceso);
        info.setCodDoc("01"); // Factura
        info.setEstab(estab);
        info.setPtoEmi(ptoEmi);
        info.setSecuencial(secuencial);
        info.setDirMatriz(config.getDireccionMatriz());
        return info;
    }

    private InfoFactura construirInfoFactura(Venta venta, ConfiguracionSRI config, String estab) {
        InfoFactura info = new InfoFactura();
        info.setFechaEmision(venta.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        info.setDirEstablecimiento(venta.getSucursal().getDireccion());
        info.setObligadoContabilidad(config.getObligadoContabilidad());

        // Determinar tipo de identificación (04=RUC, 05=Cédula, 07=Consumidor Final)
        String tipoIdentificacion = "07";
        if (venta.getCliente().getTipoIdentificacion().equalsIgnoreCase("CEDULA")) tipoIdentificacion = "05";
        if (venta.getCliente().getTipoIdentificacion().equalsIgnoreCase("RUC")) tipoIdentificacion = "04";
        info.setTipoIdentificacionComprador(tipoIdentificacion);

        info.setRazonSocialComprador(venta.getCliente().getRazonSocial());
        info.setIdentificacionComprador(venta.getCliente().getIdentificacion());
        info.setTotalSinImpuestos(formatearDecimal(venta.getSubtotal()));
        info.setTotalDescuento("0.00");
        info.setImporteTotal(formatearDecimal(venta.getTotal()));

        // Mapa para agrupar: Key = codigoPorcentaje ("0", "2", "3", "4"), Value = [SumaBaseImponible, SumaValorIva]
        java.util.Map<String, BigDecimal[]> impuestosAgrupados = new java.util.HashMap<>();

        for (DetalleVenta item : venta.getDetalles()) {
            // Determinar el código según el porcentaje aplicado
            String codigoPorcentaje = item.getPorcentajeIvaAplicado().compareTo(BigDecimal.ZERO) == 0 ? "0" : "4";

            BigDecimal baseImponibleItem = item.getSubtotal();
            BigDecimal valorIvaItem = item.getValorIva();

            if (impuestosAgrupados.containsKey(codigoPorcentaje)) {
                // Si ya existe la tarifa, sumamos a los acumuladores
                BigDecimal[] acumulados = impuestosAgrupados.get(codigoPorcentaje);
                acumulados[0] = acumulados[0].add(baseImponibleItem);
                acumulados[1] = acumulados[1].add(valorIvaItem);
            } else {
                // Si es la primera vez que vemos esta tarifa, la inicializamos
                impuestosAgrupados.put(codigoPorcentaje, new BigDecimal[]{baseImponibleItem, valorIvaItem});
            }
        }

        // Convertir el Mapa agrupado a la lista de etiquetas <totalImpuesto> de JAXB
        List<TotalImpuesto> listaTotalImpuestos = new ArrayList<>();
        for (java.util.Map.Entry<String, BigDecimal[]> entry : impuestosAgrupados.entrySet()) {
            TotalImpuesto totalImpuesto = new TotalImpuesto();
            totalImpuesto.setCodigo("2"); // "2" significa que el impuesto es IVA
            totalImpuesto.setCodigoPorcentaje(entry.getKey());
            totalImpuesto.setBaseImponible(formatearDecimal(entry.getValue()[0]));
            totalImpuesto.setValor(formatearDecimal(entry.getValue()[1]));

            listaTotalImpuestos.add(totalImpuesto);
        }

        info.setTotalConImpuestos(listaTotalImpuestos);

        // Armar la forma de pago
        Pago pago = new Pago();
        pago.setFormaPago("01");
        pago.setTotal(formatearDecimal(venta.getTotal()));
        info.setPagos(List.of(pago));

        return info;
    }

    private Detalles construirDetalles(Venta venta) {
        Detalles detallesJAXB = new Detalles();
        List<Detalle> lista = new ArrayList<>();

        for (DetalleVenta item : venta.getDetalles()) {
            Detalle det = new Detalle();
            det.setCodigoPrincipal(item.getProducto().getCodigoPrincipal());
            det.setDescripcion(item.getProducto().getNombreGenerico());
            det.setCantidad(formatearDecimal(new BigDecimal(item.getCantidad())));
            det.setPrecioUnitario(formatearDecimal(item.getPrecioUnitario()));
            det.setDescuento("0.00");
            det.setPrecioTotalSinImpuesto(formatearDecimal(item.getSubtotal()));

            ImpuestoDetalle imp = new ImpuestoDetalle();
            imp.setCodigo("2"); // IVA
            // Mapeo dinámico del porcentaje (0% -> "0", 15% -> "4")
            imp.setCodigoPorcentaje(item.getPorcentajeIvaAplicado().compareTo(BigDecimal.ZERO) == 0 ? "0" : "4");
            imp.setTarifa(formatearDecimal(item.getPorcentajeIvaAplicado()));
            imp.setBaseImponible(formatearDecimal(item.getSubtotal()));
            imp.setValor(formatearDecimal(item.getValorIva()));

            det.setImpuestos(List.of(imp));
            lista.add(det);
        }

        detallesJAXB.setDetalle(lista);
        return detallesJAXB;
    }

    private String formatearDecimal(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
