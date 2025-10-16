package co.assip.erp.general.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ZonaDTOs {

    public record ZonaListDTO(
            Integer idZona,
            String codigoZona,
            String nombreZona,
            String comentarioZona
    ) {}

    public record ZonaDetailDTO(
            Integer idZona,
            String codigoZona,
            String nombreZona,
            String comentarioZona
    ) {}

    public record ZonaCreateRequest(
            @NotBlank(message = "El código es obligatorio")
            @Pattern(regexp = "\\d{3}", message = "El código debe tener exactamente 3 dígitos (0–9)")
            String codigoZona,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
            String nombreZona,

            @Size(max = 100, message = "El comentario no debe exceder 100 caracteres")
            String comentarioZona
    ) {}

    public record ZonaUpdateRequest(
            @NotBlank(message = "El código es obligatorio")
            @Pattern(regexp = "\\d{3}", message = "El código debe tener exactamente 3 dígitos (0–9)")
            String codigoZona,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
            String nombreZona,

            @Size(max = 100, message = "El comentario no debe exceder 100 caracteres")
            String comentarioZona
    ) {}
}
