package com.empresa.sistema_facturacion.util.sri.modelo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Pago {

    @XmlElement(name = "formaPago")
    private String formaPago; // "01" Efectivo, "20" Otros...

    @XmlElement(name = "total")
    private String total;
}
