package com.empresa.sistema_facturacion.util.sri.modelo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import lombok.Data;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Detalle {

    @XmlElement(name = "codigoPrincipal")
    private String codigoPrincipal;

    @XmlElement(name = "descripcion")
    private String descripcion;

    @XmlElement(name = "cantidad")
    private String cantidad;

    @XmlElement(name = "precioUnitario")
    private String precioUnitario;

    @XmlElement(name = "descuento")
    private String descuento;

    @XmlElement(name = "precioTotalSinImpuesto")
    private String precioTotalSinImpuesto;

    // Cada producto debe declarar qué impuesto se le aplicó
    @XmlElementWrapper(name = "impuestos")
    @XmlElement(name = "impuesto")
    private List<ImpuestoDetalle> impuestos;
}
