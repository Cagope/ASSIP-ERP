package co.assip.erp.general.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public interface DirectivoDTOs {

    // ===== READ =====
    record DirectivoListItemDTO(
            Integer idDirectivo,
            Integer idDatosPersonal,
            String documento,
            String nombrePersona,
            String codigoTipoDirectivo,
            String calidadDirectivo,
            String estadoDirectivo,
            String actaAsamblea,
            LocalDate fechaAsamblea,
            String resolucionSes,
            LocalDate fechaResolucion,
            LocalDate fechaRetiro,
            Integer periodosVigencia
    ) {}

    record DirectivoDetailDTO(
            Integer idDirectivo,
            Integer idDatosPersonal,
            String documento,
            String nombrePersona,
            String codigoTipoDirectivo,
            String calidadDirectivo,
            String estadoDirectivo,
            String actaAsamblea,
            LocalDate fechaAsamblea,
            String resolucionSes,
            LocalDate fechaResolucion,
            LocalDate fechaRetiro,
            Integer periodosVigencia
    ) {}

    // ===== WRITE =====
    record DirectivoCreateRequest(
            @NotNull @Min(1) Integer idDatosPersonal,
            @NotBlank @Pattern(regexp="^[A-Za-z0-9]{1,2}$") String codigoTipoDirectivo,
            @NotBlank @Pattern(regexp="^[12]{1}$") String calidadDirectivo,
            @NotBlank @Pattern(regexp="^[123]{1}$") String estadoDirectivo,
            @NotBlank @Size(max=10) String actaAsamblea,
            @NotNull LocalDate fechaAsamblea,
            @NotBlank @Size(max=10) String resolucionSes,
            @NotNull LocalDate fechaResolucion,
            LocalDate fechaRetiro,
            @Min(0) Integer periodosVigencia
    ) {}

    record DirectivoUpdateRequest(
            @NotNull @Min(1) Integer idDatosPersonal,
            @NotBlank @Pattern(regexp="^[A-Za-z0-9]{1,2}$") String codigoTipoDirectivo,
            @NotBlank @Pattern(regexp="^[12]{1}$") String calidadDirectivo,
            @NotBlank @Pattern(regexp="^[123]{1}$") String estadoDirectivo,
            @NotBlank @Size(max=10) String actaAsamblea,
            @NotNull LocalDate fechaAsamblea,
            @NotBlank @Size(max=10) String resolucionSes,
            @NotNull LocalDate fechaResolucion,
            LocalDate fechaRetiro,
            @Min(0) Integer periodosVigencia
    ) {}
}
