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
        ConfiguracionSRI config = configuracionRepository.findTopByOrderByIdDesc();
        
        // Si no hay config, usamos una de respaldo para evitar crasheos (útil en modo local/offline)
        if (config == null) {
            config = new ConfiguracionSRI();
            config.setRuc("9999999999999");
            config.setRazonSocial("EMPRESA LOCAL (SIN CONFIGURACIÓN)");
            config.setAmbiente("1");
            config.setObligadoContabilidad("NO");
            config.setDireccionMatriz("Dirección no configurada");
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDFont fontHelvetica = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont fontHelveticaBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont fontCourier = new PDType1Font(Standard14Fonts.FontName.COURIER);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                // =========================================================
                // 1. DISEÑO DE BORDES Y SECCIONES
                // =========================================================
                contentStream.setLineWidth(1f);
                contentStream.addRect(30, 520, 260, 240); // Caja Emisor
                contentStream.addRect(305, 520, 275, 240); // Caja SRI
                contentStream.addRect(30, 420, 550, 80); // Caja Cliente
                contentStream.stroke();

                // =========================================================
                // 2. CONTENIDO: DATOS DINÁMICOS DEL EMISOR
                // =========================================================
                float currentY = 740;
                String razonSocial = config.getRazonSocial();

                // LÓGICA DE DOS LÍNEAS PARA LA RAZÓN SOCIAL
                if (razonSocial != null && razonSocial.length() > 30) {
                    int splitIndex = razonSocial.lastIndexOf(" ", 30);
                    if (splitIndex == -1) splitIndex = 30;
                    drawTextLeft(contentStream, fontHelveticaBold, 11, 40, currentY, razonSocial.substring(0, splitIndex));
                    currentY -= 13;
                    drawTextLeft(contentStream, fontHelveticaBold, 11, 40, currentY, razonSocial.substring(splitIndex));
                } else {
                    drawTextLeft(contentStream, fontHelveticaBold, 11, 40, currentY, razonSocial);
                }

                currentY -= 20;
                String nComercial = config.getNombreComercial() != null ? config.getNombreComercial() : config.getRazonSocial();
                drawTextLeft(contentStream, fontHelvetica, 9, 40, currentY, "Nombre Comercial: " + (nComercial.length() > 25 ? nComercial.substring(0, 22) + "..." : nComercial));

                currentY -= 15;
                String dir = config.getDireccionMatriz();
                drawTextLeft(contentStream, fontHelvetica, 9, 40, currentY, "Dir. Matriz: " + (dir != null && dir.length() > 35 ? dir.substring(0, 32) + "..." : dir));

                currentY -= 15;
                drawTextLeft(contentStream, fontHelvetica, 9, 40, currentY, "OBLIGADO A LLEVAR CONTABILIDAD: " + (config.getObligadoContabilidad() != null ? config.getObligadoContabilidad().toUpperCase() : "NO"));

                // =========================================================
                // 3. CONTENIDO: DATOS COMPROBANTE DEL SRI
                // =========================================================
                drawTextLeft(contentStream, fontHelveticaBold, 12, 315, 740, "R.U.C.: " + config.getRuc());
                drawTextLeft(contentStream, fontHelveticaBold, 12, 315, 720, "FACTURA");
                drawTextLeft(contentStream, fontHelvetica, 10, 315, 705, "No. " + factura.getEstablecimiento() + "-" + factura.getPuntoEmision() + "-" + factura.getSecuencial());
                drawTextLeft(contentStream, fontHelveticaBold, 9, 315, 685, "NÚMERO DE AUTORIZACIÓN / CLAVE DE ACCESO:");
                drawTextLeft(contentStream, fontCourier, 8, 315, 670, factura.getClaveAcceso());

                drawTextLeft(contentStream, fontHelvetica, 9, 315, 650, "AMBIENTE: " + ("1".equals(config.getAmbiente()) ? "PRUEBAS" : "PRODUCCIÓN"));
                drawTextLeft(contentStream, fontHelvetica, 9, 315, 635, "EMISIÓN: NORMAL");

                if (factura.getEstadoSri() == null || !factura.getEstadoSri().equalsIgnoreCase("AUTORIZADO")) {
                    drawTextLeft(contentStream, fontHelveticaBold, 8, 315, 615, "FACTURA GENERADA DE MANERA LOCAL (OFFLINE)");
                }

                // =========================================================
                // 4. CONTENIDO: DATOS DEL CLIENTE
                // =========================================================
                float clientY = 485;

                // Línea 1: Razón social e Identificación
                drawTextLeft(contentStream, fontHelveticaBold, 9, 40, clientY, "Razón Social / Nombres:");
                drawTextLeft(contentStream, fontHelvetica, 9, 165, clientY, venta.getCliente().getRazonSocial());
                drawTextLeft(contentStream, fontHelveticaBold, 9, 400, clientY, "Identificación:");
                drawTextLeft(contentStream, fontHelvetica, 9, 475, clientY, venta.getCliente().getIdentificacion());

                // Línea 2: Fecha y Correo Electrónico
                clientY -= 15;
                drawTextLeft(contentStream, fontHelveticaBold, 9, 40, clientY, "Fecha Emisión:");
                drawTextLeft(contentStream, fontHelvetica, 9, 115, clientY, venta.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                drawTextLeft(contentStream, fontHelveticaBold, 9, 400, clientY, "Email:");
                String email = venta.getCliente().getEmail() != null ? venta.getCliente().getEmail() : "S/N";
                drawTextLeft(contentStream, fontHelvetica, 9, 440, clientY, email.length() > 20 ? email.substring(0,18)+"..." : email);

                // Línea 3: Dirección y Teléfono
                clientY -= 15;
                drawTextLeft(contentStream, fontHelveticaBold, 9, 40, clientY, "Dirección:");
                String dirCl = venta.getCliente().getDireccion() != null ? venta.getCliente().getDireccion() : "S/N";
                drawTextLeft(contentStream, fontHelvetica, 9, 95, clientY, dirCl.length() > 40 ? dirCl.substring(0,38)+"..." : dirCl);
                drawTextLeft(contentStream, fontHelveticaBold, 9, 400, clientY, "Teléfono:");
                drawTextLeft(contentStream, fontHelvetica, 9, 455, clientY, venta.getCliente().getTelefono() != null ? venta.getCliente().getTelefono() : "S/N");

                // =========================================================
                // 5. CABECERAS DE LA TABLA (TEXTOS IZQ, NÚMEROS DER)
                // =========================================================
                int tablaY = 390;
                drawTextLeft(contentStream, fontHelveticaBold, 9, 40, tablaY, "Cód. Principal");
                drawTextLeft(contentStream, fontHelveticaBold, 9, 130, tablaY, "Descripción");

                drawTextRight(contentStream, fontHelveticaBold, 9, 350, tablaY, "Cant.");
                drawTextRight(contentStream, fontHelveticaBold, 9, 420, tablaY, "P. Unitario");
                drawTextRight(contentStream, fontHelveticaBold, 9, 480, tablaY, "IVA");
                drawTextRight(contentStream, fontHelveticaBold, 9, 550, tablaY, "Total");

                contentStream.setLineWidth(0.5f);
                contentStream.moveTo(30, tablaY - 5);
                contentStream.lineTo(580, tablaY - 5);
                contentStream.stroke();

                // =========================================================
                // 6. DETALLES DE PRODUCTOS
                // =========================================================
                int filaY = tablaY - 20;
                for (DetalleVenta item : venta.getDetalles()) {
                    // Letras a la izquierda
                    drawTextLeft(contentStream, fontHelvetica, 9, 40, filaY, item.getProducto().getCodigoPrincipal());
                    String desc = item.getProducto().getNombreGenerico();
                    drawTextLeft(contentStream, fontHelvetica, 9, 130, filaY, desc != null && desc.length() > 30 ? desc.substring(0, 28) + "..." : desc);

                    // Números a la derecha
                    drawTextRight(contentStream, fontHelvetica, 9, 350, filaY, String.valueOf(item.getCantidad()));
                    drawTextRight(contentStream, fontHelvetica, 9, 420, filaY, formatearDecimal(item.getPrecioUnitario()));
                    drawTextRight(contentStream, fontHelvetica, 9, 480, filaY, item.getPorcentajeIvaAplicado() != null && item.getPorcentajeIvaAplicado().compareTo(BigDecimal.ZERO) == 0 ? "0%" : "15%");
                    drawTextRight(contentStream, fontHelvetica, 9, 550, filaY, formatearDecimal(item.getSubtotal()));

                    filaY -= 15;
                }

                // =========================================================
                // 7. TOTALES FINALES
                // =========================================================
                int totalesY = filaY - 20;
                float coordLabels = 480;
                float coordValues = 550;

                BigDecimal sub0 = calcularSubtotal0(venta);
                BigDecimal sub15 = venta.getSubtotal() != null ? venta.getSubtotal().subtract(sub0) : BigDecimal.ZERO;

                drawTextRight(contentStream, fontHelveticaBold, 9, coordLabels, totalesY, "SUBTOTAL 15%:");
                drawTextRight(contentStream, fontHelvetica, 9, coordValues, totalesY, formatearDecimal(sub15));
                totalesY -= 15;

                drawTextRight(contentStream, fontHelveticaBold, 9, coordLabels, totalesY, "SUBTOTAL 0%:");
                drawTextRight(contentStream, fontHelvetica, 9, coordValues, totalesY, formatearDecimal(sub0));
                totalesY -= 15;

                drawTextRight(contentStream, fontHelveticaBold, 9, coordLabels, totalesY, "SUBTOTAL SIN IVA:");
                drawTextRight(contentStream, fontHelvetica, 9, coordValues, totalesY, formatearDecimal(venta.getSubtotal()));
                totalesY -= 15;

                drawTextRight(contentStream, fontHelveticaBold, 9, coordLabels, totalesY, "IVA:");
                drawTextRight(contentStream, fontHelvetica, 9, coordValues, totalesY, formatearDecimal(venta.getValorIva()));
                totalesY -= 15;

                drawTextRight(contentStream, fontHelveticaBold, 10, coordLabels, totalesY, "IMPORTE TOTAL:");
                drawTextRight(contentStream, fontHelveticaBold, 10, coordValues, totalesY, formatearDecimal(venta.getTotal()));
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error fatal al generar PDF: " + e.getMessage(), e);
        }
    }

    // =========================================================
    // MÉTODOS AUXILIARES BLINDADOS (A PRUEBA DE ERRORES)
    // =========================================================

    private void drawTextLeft(PDPageContentStream contentStream, PDFont font, int fontSize, float x, float y, String text) {
        try {
            if (text == null) text = "";
            text = text.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim();

            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(text);
            contentStream.endText();
        } catch (Exception e) {
            System.err.println("Error al imprimir texto izquierdo: " + e.getMessage());
        }
    }

    private void drawTextRight(PDPageContentStream contentStream, PDFont font, int fontSize, float rightX, float y, String text) {
        try {
            if (text == null) text = "";
            text = text.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim();

            float textWidth;
            try {
                // Cálculo matemático exacto
                textWidth = (font.getStringWidth(text) / 1000.0f) * fontSize;
            } catch (IllegalArgumentException e) {
                // Rescate si hay un caracter especial no soportado por la fuente Helvetica
                textWidth = text.length() * (fontSize * 0.5f);
            }

            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(rightX - textWidth, y);
            contentStream.showText(text);
            contentStream.endText();
        } catch (Exception e) {
            System.err.println("Error al imprimir texto derecho: " + e.getMessage());
        }
    }

    private String formatearDecimal(BigDecimal valor) {
        if (valor == null) {
            return "0.00";
        }
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal calcularSubtotal0(Venta venta) {
        if (venta == null || venta.getDetalles() == null) return BigDecimal.ZERO;

        return venta.getDetalles().stream()
                .filter(d -> d.getPorcentajeIvaAplicado() != null && d.getPorcentajeIvaAplicado().compareTo(BigDecimal.ZERO) == 0)
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}