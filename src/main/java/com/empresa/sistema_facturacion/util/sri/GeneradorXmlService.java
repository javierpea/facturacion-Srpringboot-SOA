package com.empresa.sistema_facturacion.util.sri;

import com.empresa.sistema_facturacion.util.sri.modelo.FacturaXml;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.springframework.stereotype.Service;

import java.io.StringWriter;

@Service
public class GeneradorXmlService {

    //Convierte el objeto FacturaXml en un String con formato XML del SRI.

    public String convertirObjetoAXml(FacturaXml factura) {
        try {
            JAXBContext context = JAXBContext.newInstance(FacturaXml.class);
            Marshaller marshaller = context.createMarshaller();

            // Configuraciones de formato para el SRI
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // Tabulaciones y saltos de línea
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            // XML en memoria
            StringWriter sw = new StringWriter();
            marshaller.marshal(factura, sw);

            String xmlGenerado = sw.toString();

            // Limpieza de cabeceras
            xmlGenerado = xmlGenerado.replace(" standalone=\"yes\"", "");

            return xmlGenerado;

        } catch (JAXBException e) {
            throw new RuntimeException("Error crítico al generar el XML de la factura: " + e.getMessage(), e);
        }
    }
}
