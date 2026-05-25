package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.Factura;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FacturaSpecification {

    public static Specification<Factura> conFiltros(LocalDateTime inicio, LocalDateTime fin, Long sucursalId, String estadoSri, Long usuarioId, String clienteIdentificacion) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (inicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("venta").get("fechaEmision"), inicio));
            }

            if (fin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("venta").get("fechaEmision"), fin));
            }

            if (sucursalId != null) {
                predicates.add(cb.equal(root.get("venta").get("sucursal").get("id"), sucursalId));
            }

            if (estadoSri != null && !estadoSri.isEmpty()) {
                predicates.add(cb.equal(root.get("estadoSri"), estadoSri));
            }

            if (usuarioId != null) {
                predicates.add(cb.equal(root.get("venta").get("usuario").get("id"), usuarioId));
            }

            if (clienteIdentificacion != null && !clienteIdentificacion.isEmpty()) {
                predicates.add(cb.equal(root.get("venta").get("cliente").get("identificacion"), clienteIdentificacion));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
