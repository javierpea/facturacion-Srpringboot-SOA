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

        validarIdentificacion(dto.getTipoIdentificacion(), dto.getIdentificacion());

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
        cliente.setActivo(true);

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente actualizarCliente(Long id, ClienteRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        if (!cliente.getIdentificacion().equals(dto.getIdentificacion())) {
            throw new RuntimeException("La identificación no puede ser modificada");
        }

        cliente.setRazonSocial(dto.getRazonSocial());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente toggleStatus(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        cliente.setActivo(!cliente.getActivo());
        return clienteRepository.save(cliente);
    }

    private void validarIdentificacion(String tipo, String identificacion) {
        if ("CEDULA".equalsIgnoreCase(tipo)) {
            if (identificacion == null || identificacion.length() != 10 || !identificacion.matches("\\d+")) {
                throw new RuntimeException("Cedula must be exactly 10 digits");
            }
        } else if ("RUC".equalsIgnoreCase(tipo)) {
            if (identificacion == null || identificacion.length() != 13 || !identificacion.matches("\\d+")) {
                throw new RuntimeException("RUC must be exactly 13 digits");
            }
        } else if ("PASAPORTE".equalsIgnoreCase(tipo)) {
            if (identificacion == null || identificacion.length() < 3 || identificacion.length() > 13 || !identificacion.matches("[a-zA-Z0-9]+")) {
                throw new RuntimeException("Passport must be alphanumeric (3-13 chars)");
            }
        }
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorIdentificacion(String identificacion) {
        return clienteRepository.findByIdentificacion(identificacion)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }
}