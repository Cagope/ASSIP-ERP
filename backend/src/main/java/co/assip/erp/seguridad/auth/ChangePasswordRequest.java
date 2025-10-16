package co.assip.erp.seguridad.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    private String passwordActual;

    @NotBlank
    private String passwordNueva;
}
