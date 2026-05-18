package com.empresa.sistema_facturacion.util.sri.modelo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class InfoTributaria {

    @XmlElement(name = "ambiente")
    private String ambiente; // 1 Pruebas, 2 Produccion

    @XmlElement(name = "tipoEmision")
    private String tipoEmision; // 1 Normal

    @XmlElement(name = "razonSocial")
    private String razonSocial;

    @XmlElement(name = "nombreComercial")
    private String nombreComercial;

    @XmlElement(name = "ruc")
    private String ruc;

    @XmlElement(name = "claveAcceso")
    private String claveAcceso;

    @XmlElement(name = "codDoc")
    private String codDoc; // "01" para Factura

    @XmlElement(name = "estab")
    private String estab;

    @XmlElement(name = "ptoEmi")
    private String ptoEmi;

    @XmlElement(name = "secuencial")
    private String secuencial;

    @XmlElement(name = "dirMatriz")
    private String dirMatriz;
}
