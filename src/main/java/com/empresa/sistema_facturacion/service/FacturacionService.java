package com.empresa.sistema_facturacion.service;

import com.empresa.sistema_facturacion.dto.response.DetalleVentaResponseDTO;
import com.empresa.sistema_facturacion.dto.response.FacturaSriResponseDTO;
import com.empresa.sistema_facturacion.dto.response.VentaFacturadaResponseDTO;
import com.empresa.sistema_facturacion.entity.*;
import com.empresa.sistema_facturacion.repository.ConfiguracionSRIRepository;
import com.empresa.sistema_facturacion.repository.FacturaRepository;
import com.empresa.sistema_facturacion.repository.FacturaSpecification;
import com.empresa.sistema_facturacion.repository.VentaRepository;
import com.empresa.sistema_facturacion.util.sri.FirmaElectronicaService;
import com.empresa.sistema_facturacion.util.sri.SriSoapService;
import com.empresa.sistema_facturacion.util.sri.modelo.*;
import com.empresa.sistema_facturacion.util.sri.ClaveAccesoUtil;
import com.empresa.sistema_facturacion.util.sri.GeneradorXmlService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacturacionService {

    @Value("${sri.enabled:true}")
    private boolean sriEnabled;

    private final VentaRepository ventaRepository;
    private final FacturaRepository facturaRepository;
    private final ConfiguracionSRIRepository configRepository;
    private final ClaveAccesoUtil claveAccesoUtil;
    private final GeneradorXmlService generadorXmlService;
    private final FirmaElectronicaService firmaElectronicaService;
    private final SriSoapService sriSoapService;
    private final VentaService ventaService;

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
        
        // Si SRI está apagado y no hay config, usamos una de respaldo para evitar crasheos
        if (!sriEnabled && config == null) {
            config = new ConfiguracionSRI();
            config.setRuc("9999999999999");
            config.setRazonSocial("EMPRESA LOCAL");
            config.setAmbiente("1");
            config.setTipoEmision("1");
        } else if (config == null) {
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

        Factura nuevaFactura = new Factura();
        nuevaFactura.setVenta(venta);
        nuevaFactura.setEstablecimiento(establecimiento);
        nuevaFactura.setPuntoEmision(puntoEmision);
        nuevaFactura.setSecuencial(secuencial);
        nuevaFactura.setClaveAcceso(claveAcceso);
        nuevaFactura.setEstadoSri("CREADA");

        // Solo generar y firmar XML si SRI está habilitado
        if (sriEnabled) {
            // ARMAR EL MODELO JAXB
            FacturaXml facturaXml = new FacturaXml();
            facturaXml.setInfoTributaria(construirInfoTributaria(config, claveAcceso, establecimiento, puntoEmision, secuencial));
            facturaXml.setInfoFactura(construirInfoFactura(venta, config, establecimiento));
            facturaXml.setDetalles(construirDetalles(venta));

            // GENERAR EL STRING XML Y GUARDAR
            String xmlPlano = generadorXmlService.convertirObjetoAXml(facturaXml);

            // Tomamos el XML plano, extraemos la firma de la BD y la inyectamos en memoria
            String xmlFirmado = firmaElectronicaService.firmarDocumentoXml(xmlPlano);

            nuevaFactura.setEstadoSri("FIRMADA");
            nuevaFactura.setXmlFirmado(xmlFirmado);
        } else {
            nuevaFactura.setEstadoSri("OFFLINE");
            nuevaFactura.setMensajeErrorSri("Factura generada en modo local (SRI deshabilitado)");
        }

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

        // Determinar tipo de identificación (04=RUC, 05=Cédula, 06=Pasaporte, 07=Consumidor Final)
        String tipoIdentificacion = "07";
        if (venta.getCliente().getTipoIdentificacion().equalsIgnoreCase("CEDULA")) tipoIdentificacion = "05";
        if (venta.getCliente().getTipoIdentificacion().equalsIgnoreCase("RUC")) tipoIdentificacion = "04";
        if (venta.getCliente().getTipoIdentificacion().equalsIgnoreCase("PASAPORTE")) tipoIdentificacion = "06";
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

    @Transactional
    public Factura procesarEnvioSRI(Long facturaId) {
        if (!sriEnabled) {
            throw new RuntimeException("Emisión SRI deshabilitada (Modo Local)");
        }

        // 1. Cargamos la factura
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        ConfiguracionSRI config = configRepository.findTopByOrderByIdDesc();
        String ambiente = config.getAmbiente();

        // 2. Variables para capturar el error incluso si falla el proceso
        String xmlRespuestaRecepcion = null;
        String xmlRespuestaAutorizacion = null;

        try {
            // --- PROCESO DE RECEPCIÓN ---
            try {
                xmlRespuestaRecepcion = sriSoapService.enviarARecepcion(factura.getXmlFirmado(), ambiente);
            } catch (Exception e) {
                xmlRespuestaRecepcion = "ERROR DE CONEXIÓN RECEPCIÓN: " + e.getMessage();
                throw e; // Relanzamos para que el flujo de negocio se detenga
            }

            // Guardamos Recepción
            factura.setMensajeErrorSri("Recepción: " + xmlRespuestaRecepcion);
            facturaRepository.saveAndFlush(factura); // Flush es vital aquí

            String estadoRecepcion = extraerTagXml(xmlRespuestaRecepcion, "estado");

            if ("RECIBIDA".equals(estadoRecepcion)) {
                factura.setEstadoSri("RECIBIDA");

                // --- PROCESO DE AUTORIZACIÓN ---
                try {
                    xmlRespuestaAutorizacion = sriSoapService.consultarAutorizacion(factura.getClaveAcceso(), ambiente);
                } catch (Exception e) {
                    xmlRespuestaAutorizacion = "ERROR DE CONEXIÓN AUTORIZACIÓN: " + e.getMessage();
                    throw e;
                }

                // Concatenamos el XML de Autorización
                factura.setMensajeErrorSri(factura.getMensajeErrorSri() + "\n\nAutorización: " + xmlRespuestaAutorizacion);
                facturaRepository.saveAndFlush(factura);

                // ... (resto de tu lógica para extraer el estado y actualizar a AUTORIZADA o RECHAZADA)
            }

        } catch (Exception e) {
            // <<< AQUÍ ESTÁ EL TRUCO >>>
            // Si todo falla, guardamos el XML que pudimos capturar antes de salir
            String errorFinal = (xmlRespuestaRecepcion != null ? xmlRespuestaRecepcion : "Error antes de recepción")
                    + (xmlRespuestaAutorizacion != null ? "\n" + xmlRespuestaAutorizacion : "");

            factura.setMensajeErrorSri(errorFinal + "\n\nEXCEPCIÓN: " + e.getMessage());
            facturaRepository.save(factura); // Forzamos el guardado final

            throw new RuntimeException("Fallo en la comunicación: " + e.getMessage());
        }

        return factura;
    }

    private String extraerTagXml(String xml, String tagName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            java.io.InputStream is = new java.io.ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            Document doc = factory.newDocumentBuilder().parse(is);

            org.w3c.dom.NodeList list = doc.getElementsByTagName(tagName);
            if (list.getLength() > 0) {
                return list.item(0).getTextContent();
            }
        } catch (Exception e) {
            // Si el parser falla catastróficamente, usamos un fallback por Expresión Regular para salvar la ejecución
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">");
            java.util.regex.Matcher matcher = pattern.matcher(xml);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "DESCONOCIDO";
    }

    private String extraerMensajeErrorSRI(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            java.io.InputStream is = new java.io.ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            Document doc = factory.newDocumentBuilder().parse(is);

            org.w3c.dom.NodeList mensajes = doc.getElementsByTagName("mensaje");
            if (mensajes.getLength() > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mensajes.getLength(); i++) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) mensajes.item(i);
                    String id = element.getElementsByTagName("identificador").item(0).getTextContent();
                    String msg = element.getElementsByTagName("mensaje").item(0).getTextContent();
                    String infoAdicional = element.getElementsByTagName("informacionAdicional") != null && element.getElementsByTagName("informacionAdicional").getLength() > 0
                            ? " -> " + element.getElementsByTagName("informacionAdicional").item(0).getTextContent() : "";

                    sb.append("[").append(id).append("] ").append(msg).append(infoAdicional).append(" | ");
                }
                return sb.toString();
            }
        } catch (Exception e) {
            return "No se pudo deserializar el detalle del error del SRI.";
        }
        return "Error indeterminado.";
    }

    public Factura obtenerFacturaPorId(Long id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con el ID: " + id));
    }

    @Transactional
    public VentaFacturadaResponseDTO registrarVentaYFacturar(com.empresa.sistema_facturacion.dto.request.VentaRequestDTO ventaRequest, String usernameCajero) {

        Venta ventaEntity = ventaService.guardarEntidadVenta(ventaRequest, usernameCajero);
        Long ventaId = ventaEntity.getId();

        // Siempre generamos el registro de Factura (sea para firmar o solo local)
        Factura facturaEntity = generarFacturaXML(ventaId);

        if (sriEnabled) {
            // 3. Enviar y Autorizar en los Web Services SOAP del SRI
            try {
                facturaEntity = procesarEnvioSRI(facturaEntity.getId());
            } catch (Exception e) {
                // Capturamos el error del SRI, pero permitimos que el flujo continúe
                // para que el usuario sepa que la venta SÍ se guardó pero quedó pendiente en el SRI
                facturaEntity.setEstadoSri("DEVUELTA_CON_ERROR");
                facturaRepository.save(facturaEntity);
            }
        }

        // =========================================================
        // 4. MAPEO EXPLÍCITO Y PROFUNDO DEL DTO DE RETORNO (Evita los nulls)
        // =========================================================
        VentaFacturadaResponseDTO response = new VentaFacturadaResponseDTO();
        response.setVentaId(ventaEntity.getId());
        response.setFechaEmision(ventaEntity.getFechaEmision());
        response.setClienteIdentificacion(ventaEntity.getCliente().getIdentificacion());
        response.setClienteRazonSocial(ventaEntity.getCliente().getRazonSocial());
        response.setSubtotal(ventaEntity.getSubtotal());
        response.setValorIva(ventaEntity.getValorIva());
        response.setTotal(ventaEntity.getTotal());

        // Mapeo manual de la lista de detalles de la venta de la BD al DTO
        List<DetalleVentaResponseDTO> listaDetallesDto = new ArrayList<>();
        for (DetalleVenta item : ventaEntity.getDetalles()) {
            DetalleVentaResponseDTO detDto = new DetalleVentaResponseDTO();
            detDto.setCodigoPrincipal(item.getProducto().getCodigoPrincipal());
            detDto.setNombreProducto(item.getProducto().getNombreGenerico());
            detDto.setCantidad(item.getCantidad());
            detDto.setPrecioUnitario(item.getPrecioUnitario());
            detDto.setSubtotal(item.getSubtotal());
            detDto.setValorIva(item.getValorIva());
            listaDetallesDto.add(detDto);
        }
        response.setDetalles(listaDetallesDto);

        // Mapeo de los datos del SRI resultantes
        FacturaSriResponseDTO sriDto = new FacturaSriResponseDTO();
        sriDto.setId(facturaEntity.getId());
        sriDto.setSecuencial(facturaEntity.getSecuencial());
        sriDto.setClaveAcceso(facturaEntity.getClaveAcceso());
        sriDto.setEstadoSri(facturaEntity.getEstadoSri());

        if (sriEnabled) {
            sriDto.setMensaje(facturaEntity.getEstadoSri().equals("AUTORIZADA")
                    ? "Factura autorizada legalmente por el SRI"
                    : "Comprobante guardado pero con incidencias en el SRI.");
        } else {
            sriDto.setMensaje("Emisión SRI deshabilitada (Modo Local)");
        }
        response.setFacturaSri(sriDto);

        return response;
    }

    public List<Factura> listarConFiltros(LocalDateTime inicio, LocalDateTime fin, Long sucursalId, String estadoSri, Long usuarioId, String clienteIdentificacion) {
        return facturaRepository.findAll(FacturaSpecification.conFiltros(inicio, fin, sucursalId, estadoSri, usuarioId, clienteIdentificacion));
    }
}
