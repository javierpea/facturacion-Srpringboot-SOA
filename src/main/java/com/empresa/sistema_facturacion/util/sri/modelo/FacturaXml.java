package com.empresa.sistema_facturacion.util.sri.modelo;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

@XmlRootElement(name = "factura")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class FacturaXml {

    @XmlAttribute
    private String id = "comprobante";

    @XmlAttribute
    private String version = "1.1.0"; // Versión actual de la ficha técnica offline

    //  Información de la Empresa
    @XmlElement(name = "infoTributaria")
    private InfoTributaria infoTributaria;

    // Información de la Venta (Totales, Cliente)
    @XmlElement(name = "infoFactura")
    private InfoFactura infoFactura;

    // Productos vendidos
    @XmlElement(name = "detalles")
    private Detalles detalles;
}
