package com.empresa.sistema_facturacion.util.sri.modelo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class TotalImpuesto {

    @XmlElement(name = "codigo")
    private String codigo; // "2" para IVA

    @XmlElement(name = "codigoPorcentaje")
    private String codigoPorcentaje; // "0" para 0%, "2" para 12%, "4" para 15%

    @XmlElement(name = "baseImponible")
    private String baseImponible;

    @XmlElement(name = "valor")
    private String valor;
}
