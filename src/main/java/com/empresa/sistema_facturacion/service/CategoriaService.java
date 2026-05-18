package com.empresa.sistema_facturacion.service;

import com.empresa.sistema_facturacion.dto.request.CategoriaCreateDTO;
import com.empresa.sistema_facturacion.entity.Categoria;
import com.empresa.sistema_facturacion.entity.TarifaIva;
import com.empresa.sistema_facturacion.repository.CategoriaRepository;
import com.empresa.sistema_facturacion.repository.TarifaIvaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final TarifaIvaRepository tarifaIvaRepository;

    @Transactional
    public Categoria crearCategoria(CategoriaCreateDTO dto) {
        if (categoriaRepository.findByNombre(dto.getNombre()).isPresent()) {
            throw new RuntimeException("Ya existe una categoría con el nombre: " + dto.getNombre());
        }

        TarifaIva tarifaIva = tarifaIvaRepository.findById(dto.getTarifaIvaId())
                .orElseThrow(() -> new RuntimeException("La tarifa de IVA especificada no existe en el catálogo del SRI"));

        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setTarifaIva(tarifaIva);

        return categoriaRepository.save(categoria);
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }
}