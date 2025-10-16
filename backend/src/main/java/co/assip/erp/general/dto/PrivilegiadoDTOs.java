package co.assip.erp.general.dto;

import jakarta.validation.constraints.*;

public interface PrivilegiadoDTOs {

    // ===== READ =====
    // Agregamos: documentoDirectivo, nombreDirectivo (opcionales en la proyección)
    record PrivilegiadoListItemDTO(
            Integer idPrivilegiado,
            Integer idDirectivo,
            Integer idDatosPersonal,
            String documento,
            String nombrePersona,
            String codigoParentesco,
            String nombreParentesco,
            String documentoDirectivo,
            String nombreDirectivo
    ) {}

    record PrivilegiadoDetailDTO(
            Integer idPrivilegiado,
            Integer idDirectivo,
            Integer idDatosPersonal,
            String documento,
            String nombrePersona,
            String codigoParentesco,
            String nombreParentesco
    ) {}

    // ===== WRITE =====
    record PrivilegiadoCreateRequest(
            @NotNull @Min(1) Integer idDirectivo,
            @NotNull @Min(1) Integer idDatosPersonal,
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9]{1,2}$") String codigoParentesco
    ) {}

    record PrivilegiadoUpdateRequest(
            @NotNull @Min(1) Integer idDirectivo,
            @NotNull @Min(1) Integer idDatosPersonal,
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9]{1,2}$") String codigoParentesco
    ) {}
}
