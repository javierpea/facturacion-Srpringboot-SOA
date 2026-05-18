package com.empresa.sistema_facturacion.util.sri.modelo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import lombok.Data;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class InfoFactura {

    @XmlElement(name = "fechaEmision")
    private String fechaEmision; // Formato: dd/MM/yyyy

    @XmlElement(name = "dirEstablecimiento")
    private String dirEstablecimiento;

    @XmlElement(name = "obligadoContabilidad")
    private String obligadoContabilidad; // "SI" o "NO"

    @XmlElement(name = "tipoIdentificacionComprador")
    private String tipoIdentificacionComprador; // "04" RUC, "05" Cédula, "07" Consumidor Final

    @XmlElement(name = "razonSocialComprador")
    private String razonSocialComprador;

    @XmlElement(name = "identificacionComprador")
    private String identificacionComprador;

    @XmlElement(name = "totalSinImpuestos")
    private String totalSinImpuestos; // Usamos String para formatear exactamente a 2 decimales (Ej: "10.50")

    @XmlElement(name = "totalDescuento")
    private String totalDescuento;

    @XmlElementWrapper(name = "totalConImpuestos")
    @XmlElement(name = "totalImpuesto")
    private List<TotalImpuesto> totalConImpuestos;

    @XmlElement(name = "propina")
    private String propina = "0.00";

    @XmlElement(name = "importeTotal")
    private String importeTotal;

    @XmlElement(name = "moneda")
    private String moneda = "DOLAR";

    @XmlElementWrapper(name = "pagos")
    @XmlElement(name = "pago")
    private List<Pago> pagos;
}
