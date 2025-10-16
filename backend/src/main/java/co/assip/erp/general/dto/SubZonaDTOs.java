package co.assip.erp.general.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SubZonaDTOs {

    public record SubZonaListDTO(
            Integer idSubZona,
            Integer idZona,
            String codigoSubZona,
            String nombreSubZona,
            String comentarioSubZona,
            String nombreZona  // útil para listados
    ) {}

    public record SubZonaDetailDTO(
            Integer idSubZona,
            Integer idZona,
            String codigoSubZona,
            String nombreSubZona,
            String comentarioSubZona,
            String nombreZona
    ) {}

    public record SubZonaCreateRequest(
            @NotNull(message = "La zona es obligatoria")
            Integer idZona,

            @NotBlank(message = "El código es obligatorio")
            @Pattern(regexp = "\\d{3}", message = "El código debe tener exactamente 3 dígitos (0–9)")
            String codigoSubZona,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
            String nombreSubZona,

            @NotBlank(message = "El comentario es obligatorio")
            @Size(max = 100, message = "El comentario no debe exceder 100 caracteres")
            String comentarioSubZona
    ) {}

    public record SubZonaUpdateRequest(
            @NotNull(message = "La zona es obligatoria")
            Integer idZona,

            @NotBlank(message = "El código es obligatorio")
            @Pattern(regexp = "\\d{3}", message = "El código debe tener exactamente 3 dígitos (0–9)")
            String codigoSubZona,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
            String nombreSubZona,

            @NotBlank(message = "El comentario es obligatorio")
            @Size(max = 100, message = "El comentario no debe exceder 100 caracteres")
            String comentarioSubZona
    ) {}
}
