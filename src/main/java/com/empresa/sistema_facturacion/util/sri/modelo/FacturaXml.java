package com.empresa.sistema_facturacion.util.sri.modelo;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

@Data
@XmlRootElement(name = "factura")
@XmlAccessorType(XmlAccessType.FIELD)
public class FacturaXml {

    @XmlAttribute(name = "id")
    private String id = "comprobante";

    @XmlAttribute(name = "version")
    private String version = "1.1.0";

    @XmlElement(name = "infoTributaria")
    private InfoTributaria infoTributaria;

    @XmlElement(name = "infoFactura")
    private InfoFactura infoFactura;

    @XmlElement(name = "detalles")
    private Detalles detalles;
}