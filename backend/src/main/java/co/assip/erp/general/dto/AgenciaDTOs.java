package co.assip.erp.general.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public interface AgenciaDTOs {

    // ====== READ ======
    public record AgenciaListItemDTO(
            Integer idAgencia,
            String codigoAgencia,
            String nombreAgencia,
            String siglaAgencia
    ) {}

    public record AgenciaDetailDTO(
            Integer idAgencia,
            String codigoAgencia,
            String nombreAgencia,
            String siglaAgencia,
            String direccionAgencia,
            Integer idDepartamento,
            Integer idCiudad,
            String correoAgencia,
            String celularAgencia,
            String telefonoAgencia
    ) {}

    // ====== WRITE ======
    public record AgenciaCreateRequest(
            @NotBlank
            @Size(max = 2)
            @Pattern(regexp = "^[A-Za-z0-9]+$")
            String codigoAgencia,

            @NotBlank @Size(max = 100)
            String nombreAgencia,

            @NotBlank @Size(max = 100)
            String siglaAgencia,

            @NotBlank @Size(max = 100)
            String direccionAgencia,

            Integer idDepartamento,
            Integer idCiudad,

            @Size(max = 100)
            String correoAgencia,

            @Pattern(regexp = "^[0-9]{10}$")
            String celularAgencia,

            @Pattern(regexp = "^[0-9]{7}$")
            String telefonoAgencia
    ) {}

    public record AgenciaUpdateRequest(
            @NotBlank
            @Size(max = 2)
            @Pattern(regexp = "^[A-Za-z0-9]+$")
            String codigoAgencia,

            @NotBlank @Size(max = 100)
            String nombreAgencia,

            @NotBlank @Size(max = 100)
            String siglaAgencia,

            @NotBlank @Size(max = 100)
            String direccionAgencia,

            Integer idDepartamento,
            Integer idCiudad,

            @Size(max = 100)
            String correoAgencia,

            @Pattern(regexp = "^[0-9]{10}$")
            String celularAgencia,

            @Pattern(regexp = "^[0-9]{7}$")
            String telefonoAgencia
    ) {}
}
