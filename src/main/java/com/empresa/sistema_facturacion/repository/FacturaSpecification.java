package com.empresa.sistema_facturacion.repository;

import com.empresa.sistema_facturacion.entity.Factura;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FacturaSpecification {

    public static Specification<Factura> conFiltros(LocalDateTime inicio, LocalDateTime fin, Long sucursalId, 
                                                    Long clienteId, Long usuarioId, String estadoSri) {
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
            if (clienteId != null) {
                predicates.add(cb.equal(root.get("venta").get("cliente").get("id"), clienteId));
            }
            if (usuarioId != null) {
                predicates.add(cb.equal(root.get("venta").get("usuario").get("id"), usuarioId));
            }
            if (estadoSri != null && !estadoSri.isEmpty()) {
                predicates.add(cb.equal(root.get("estadoSri"), estadoSri));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
