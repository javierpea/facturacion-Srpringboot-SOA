package com.empresa.sistema_facturacion.util.sri.modelo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ImpuestoDetalle {

    @XmlElement(name = "codigo")
    private String codigo; // "2" para IVA

    @XmlElement(name = "codigoPorcentaje")
    private String codigoPorcentaje;

    @XmlElement(name = "tarifa")
    private String tarifa; // "15.00" o "0.00"

    @XmlElement(name = "baseImponible")
    private String baseImponible;

    @XmlElement(name = "valor")
    private String valor;
}
