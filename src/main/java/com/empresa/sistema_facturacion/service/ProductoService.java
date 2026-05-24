package com.empresa.sistema_facturacion.service;

import com.empresa.sistema_facturacion.dto.request.ProductoCreateDTO;
import com.empresa.sistema_facturacion.dto.response.ProductoResponseDTO;
import com.empresa.sistema_facturacion.entity.Categoria;
import com.empresa.sistema_facturacion.entity.Producto;
import com.empresa.sistema_facturacion.repository.CategoriaRepository;
import com.empresa.sistema_facturacion.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional
    public ProductoResponseDTO crearProducto(ProductoCreateDTO dto) {
        if (productoRepository.findByCodigoPrincipal(dto.getCodigoPrincipal()).isPresent()) {
            throw new RuntimeException("El código principal del producto ya está registrado");
        }

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("La categoría seleccionada no existe"));

        Producto producto = new Producto();
        producto.setCodigoPrincipal(dto.getCodigoPrincipal());
        producto.setNombreGenerico(dto.getNombreGenerico());
        producto.setMarca(dto.getMarca());
        producto.setPresentacion(dto.getPresentacion());
        producto.setPrecioUnitario(dto.getPrecioUnitario());
        producto.setCategoria(categoria);
        producto.setEstado(true);

        Producto productoGuardado = productoRepository.save(producto);
        return mapearAResponseDTO(productoGuardado);
    }

    @Transactional
    public ProductoResponseDTO actualizarProducto(Long id, ProductoCreateDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Validar inmutabilidad del código principal
        if (!producto.getCodigoPrincipal().equals(dto.getCodigoPrincipal())) {
            throw new RuntimeException("El código principal del producto no se puede modificar");
        }

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("La categoría seleccionada no existe"));

        producto.setNombreGenerico(dto.getNombreGenerico());
        producto.setMarca(dto.getMarca());
        producto.setPresentacion(dto.getPresentacion());
        producto.setPrecioUnitario(dto.getPrecioUnitario());
        producto.setCategoria(categoria);

        return mapearAResponseDTO(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponseDTO toggleEstado(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        producto.setEstado(!producto.getEstado());
        return mapearAResponseDTO(productoRepository.save(producto));
    }

    private ProductoResponseDTO mapearAResponseDTO(Producto producto) {
        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(producto.getId());
        response.setCodigoPrincipal(producto.getCodigoPrincipal());

        String nombreCompleto = String.format("%s %s (%s)",
                producto.getNombreGenerico(), producto.getMarca(), producto.getPresentacion());
        response.setNombreCompleto(nombreCompleto);

        response.setPrecioUnitario(producto.getPrecioUnitario());
        response.setCategoriaNombre(producto.getCategoria().getNombre());
        response.setPorcentajeIva(producto.getCategoria().getTarifaIva().getPorcentaje());
        response.setEstado(producto.getEstado());

        return response;
    }
}