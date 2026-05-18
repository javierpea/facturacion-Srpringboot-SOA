package com.empresa.sistema_facturacion.service;

import com.empresa.sistema_facturacion.dto.request.ClienteRequestDTO;
import com.empresa.sistema_facturacion.entity.Cliente;
import com.empresa.sistema_facturacion.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public Cliente registrarCliente(ClienteRequestDTO dto) {

        if (clienteRepository.findByIdentificacion(dto.getIdentificacion()).isPresent()) {
            throw new RuntimeException("Un cliente con esa identificación ya se encuentra registrado");
        }

        Cliente cliente = new Cliente();
        cliente.setTipoIdentificacion(dto.getTipoIdentificacion().toUpperCase());
        cliente.setIdentificacion(dto.getIdentificacion());
        cliente.setRazonSocial(dto.getRazonSocial());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());

        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorIdentificacion(String identificacion) {
        return clienteRepository.findByIdentificacion(identificacion)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }
}