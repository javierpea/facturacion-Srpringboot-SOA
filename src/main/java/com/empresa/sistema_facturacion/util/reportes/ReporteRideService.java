package com.empresa.sistema_facturacion.util.reportes;

import com.empresa.sistema_facturacion.entity.DetalleVenta;
import com.empresa.sistema_facturacion.entity.Factura;
import com.empresa.sistema_facturacion.entity.Venta;
import com.empresa.sistema_facturacion.entity.ConfiguracionSRI;
import com.empresa.sistema_facturacion.repository.ConfiguracionSRIRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ReporteRideService {

    private final ConfiguracionSRIRepository configuracionRepository;

    public byte[] generarPdfRide(Factura factura) {
        Venta venta = factura.getVenta();

        // 1. Recuperar la configuración viva del emisor desde la base de datos
        ConfiguracionSRI config = configuracionRepository.findTopByOrderByIdDesc();
        if (config == null) {
            throw new RuntimeException("No se puede generar el PDF porque no existen datos de configuración de la empresa en la BD.");
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            // Instancia de fuentes estándar para PDFBox 3.x
            PDFont fontHelvetica = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont fontHelveticaBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont fontCourier = new PDType1Font(Standard14Fonts.FontName.COURIER);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                // =========================================================
                // 1. DISEÑO DE BORDES Y SECCIONES
                // =========================================================
                contentStream.setLineWidth(1f);
                contentStream.addRect(30, 520, 260, 240);
                contentStream.addRect(305, 520, 275, 240);
                contentStream.addRect(30, 420, 550, 80);
                contentStream.stroke();

                // =========================================================
                // 2. CONTENIDO: DATOS DINÁMICOS DE LA EMPRESA (Bloque Izquierdo)
                // =========================================================
                contentStream.beginText();
                contentStream.setFont(fontHelveticaBold, 11); // Reducido un punto por si la razón social es larga
                contentStream.newLineAtOffset(40, 740);
                contentStream.showText(config.getRazonSocial());

                contentStream.setFont(fontHelvetica, 9);
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Nombre Comercial: " + (config.getNombreComercial() != null ? config.getNombreComercial() : config.getRazonSocial()));

                contentStream.newLineAtOffset(0, -15);
                // Si la dirección es muy larga, cortamos el texto para evitar que se desborde del recuadro del PDF
                String dir = config.getDireccionMatriz();
                contentStream.showText("Dir. Matriz: " + (dir.length() > 38 ? dir.substring(0, 35) + "..." : dir));

                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("OBLIGADO A LLEVAR CONTABILIDAD: " + config.getObligadoContabilidad().toUpperCase());
                contentStream.endText();

                // =========================================================
                // 3. CONTENIDO: DATOS COMPROBANTE DEL SRI (Bloque Derecho)
                // =========================================================
                contentStream.beginText();
                contentStream.setFont(fontHelveticaBold, 12);
                contentStream.newLineAtOffset(315, 740);
                contentStream.showText("R.U.C.: " + config.getRuc()); // RUC Dinámico

                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("FACTURA");
                contentStream.setFont(fontHelvetica, 10);
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("No. " + factura.getEstablecimiento() + "-" + factura.getPuntoEmision() + "-" + factura.getSecuencial());

                contentStream.newLineAtOffset(0, -20);
                contentStream.setFont(fontHelveticaBold, 9);
                contentStream.showText("NÚMERO DE AUTORIZACIÓN / CLAVE DE ACCESO:");

                contentStream.setFont(fontCourier, 8);
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText(factura.getClaveAcceso());

                contentStream.setFont(fontHelvetica, 9);
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("AMBIENTE: " + (config.getAmbiente().equals("1") ? "PRUEBAS" : "PRODUCCIÓN"));
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("EMISIÓN: NORMAL");
                contentStream.endText();

                // =========================================================
                // 4. CONTENIDO: DATOS DEL CLIENTE
                // =========================================================
                contentStream.beginText();
                contentStream.setFont(fontHelveticaBold, 9);
                contentStream.newLineAtOffset(40, 485);
                contentStream.showText("Razón Social / Nombres y Apellidos: ");
                contentStream.setFont(fontHelvetica, 9);
                contentStream.showText(venta.getCliente().getRazonSocial());
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(fontHelveticaBold, 9);
                contentStream.newLineAtOffset(420, 485);
                contentStream.showText("Identificación: ");
                contentStream.setFont(fontHelvetica, 9);
                contentStream.showText(venta.getCliente().getIdentificacion());
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(fontHelveticaBold, 9);
                contentStream.newLineAtOffset(40, 465);
                contentStream.showText("Fecha Emisión: ");
                contentStream.setFont(fontHelvetica, 9);
                contentStream.showText(venta.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                contentStream.endText();

                // =========================================================
                // 5. TABLA DE DETALLES (PRODUCTOS)
                // =========================================================
                int tablaY = 390;
                contentStream.beginText();
                contentStream.setFont(fontHelveticaBold, 9);
                contentStream.newLineAtOffset(40, tablaY); contentStream.showText("Cod. Principal");
                contentStream.newLineAtOffset(90, 0);  contentStream.showText("Descripción");
                contentStream.newLineAtOffset(210, 0); contentStream.showText("Cant.");
                contentStream.newLineAtOffset(50, 0);  contentStream.showText("P. Unitario");
                contentStream.newLineAtOffset(60, 0);  contentStream.showText("IVA");
                contentStream.newLineAtOffset(50, 0);  contentStream.showText("Precio Total");
                contentStream.endText();

                contentStream.setLineWidth(0.5f);
                contentStream.moveTo(30, tablaY - 5);
                contentStream.lineTo(580, tablaY - 5);
                contentStream.stroke();

                int filaY = tablaY - 20;
                for (DetalleVenta item : venta.getDetalles()) {
                    contentStream.beginText();
                    contentStream.setFont(fontHelvetica, 9);

                    contentStream.newLineAtOffset(40, filaY);
                    contentStream.showText(item.getProducto().getCodigoPrincipal());

                    contentStream.newLineAtOffset(90, 0);
                    contentStream.showText(item.getProducto().getNombreGenerico());

                    contentStream.newLineAtOffset(210, 0);
                    contentStream.showText(String.valueOf(item.getCantidad()));

                    contentStream.newLineAtOffset(50, 0);
                    contentStream.showText(formatearDecimal(item.getPrecioUnitario()));

                    contentStream.newLineAtOffset(60, 0);
                    contentStream.showText(item.getPorcentajeIvaAplicado().compareTo(BigDecimal.ZERO) == 0 ? "0%" : "15%");

                    contentStream.newLineAtOffset(50, 0);
                    contentStream.showText(formatearDecimal(item.getSubtotal()));
                    contentStream.endText();

                    filaY -= 15;
                }

                // =========================================================
                // 6. BLOQUE DE TOTALES FINALES
                // =========================================================
                int totalesY = filaY - 20;
                contentStream.beginText();
                contentStream.setFont(fontHelveticaBold, 9);
                contentStream.newLineAtOffset(400, totalesY);

                contentStream.showText("SUBTOTAL 15%:");   contentStream.newLineAtOffset(100, 0); contentStream.showText(formatearDecimal(venta.getSubtotal().subtract(calcularSubtotal0(venta))));
                contentStream.newLineAtOffset(-100, -15);
                contentStream.showText("SUBTOTAL 0%:");    contentStream.newLineAtOffset(100, 0); contentStream.showText(formatearDecimal(calcularSubtotal0(venta)));
                contentStream.newLineAtOffset(-100, -15);
                contentStream.showText("SUBTOTAL SIN IMPUESTOS:"); contentStream.newLineAtOffset(100, 0); contentStream.showText(formatearDecimal(venta.getSubtotal()));
                contentStream.newLineAtOffset(-100, -15);
                contentStream.showText("IVA 15%:");        contentStream.newLineAtOffset(100, 0); contentStream.showText(formatearDecimal(venta.getValorIva()));
                contentStream.newLineAtOffset(-100, -15);
                contentStream.showText("IMPORTE TOTAL:");  contentStream.newLineAtOffset(100, 0); contentStream.showText(formatearDecimal(venta.getTotal()));
                contentStream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error fatal al generar el RIDE PDF dinámico desde la BD: " + e.getMessage(), e);
        }
    }

    private String formatearDecimal(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal calcularSubtotal0(Venta venta) {
        return venta.getDetalles().stream()
                .filter(d -> d.getPorcentajeIvaAplicado().compareTo(BigDecimal.ZERO) == 0)
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}