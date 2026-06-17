package com.empresa.sistema_facturacion.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClienteRequestDTO {
    @NotBlank(message = "El tipo de identificación es obligatorio (RUC, CEDULA, PASAPORTE, CF)")
    private String tipoIdentificacion;

    @NotBlank(message = "La identificación es obligatoria")
    private String identificacion;

    @NotBlank(message = "La razón social o nombre es obligatorio")
    @Size(max = 100, message = "La razón social no puede exceder los 100 caracteres")
    private String razonSocial;

    @Size(max = 150, message = "La dirección no puede exceder los 150 caracteres")
    private String direccion;

    @Pattern(regexp = "^\\+?[0-9]*$", message = "El teléfono debe ser numérico y puede empezar con +")
    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
    private String telefono;

    @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Formato de email inválido")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
    private String email;
}