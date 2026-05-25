package com.empresa.sistema_facturacion.service;

import com.empresa.sistema_facturacion.dto.request.CategoriaRequestDTO;
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
    public Categoria crearCategoria(CategoriaRequestDTO dto) {
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

    @Transactional(readOnly = true)
    public List<Categoria> listarActivas() {
        return categoriaRepository.findByActivoTrue();
    }

    @Transactional
    public Categoria actualizarCategoria(Long id, CategoriaRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));

        if (categoriaRepository.findByNombreAndIdNot(dto.getNombre(), id).isPresent()) {
            throw new RuntimeException("Ya existe otra categoría con el nombre: " + dto.getNombre());
        }

        TarifaIva tarifaIva = tarifaIvaRepository.findById(dto.getTarifaIvaId())
                .orElseThrow(() -> new RuntimeException("La tarifa de IVA especificada no existe"));

        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setTarifaIva(tarifaIva);

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria toggleEstado(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));

        categoria.setActivo(!categoria.getActivo());
        return categoriaRepository.save(categoria);
    }
}