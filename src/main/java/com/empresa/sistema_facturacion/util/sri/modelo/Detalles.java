package com.empresa.sistema_facturacion.util.sri.modelo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Detalles {

    // Lista de cada producto en la factura
    @XmlElement(name = "detalle")
    private List<Detalle> detalle;
}
