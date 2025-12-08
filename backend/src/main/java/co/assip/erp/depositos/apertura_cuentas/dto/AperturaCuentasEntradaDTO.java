package co.assip.erp.depositos.apertura_cuentas.dto;

import lombok.Data;

@Data
public class AperturaCuentasEntradaDTO {

    private Integer idDatosPersonal;
    private Integer idFormaAhorro;

    // Alias viejo que usabas en el Service
    public String getCodigoForma() {
        return idFormaAhorro != null ? String.valueOf(idFormaAhorro) : null;
    }

    // Valores seleccionados en el formulario (solo para la forma que se está creando)
    private String gmf;              // “S” o “N”
    private Boolean retencion;       // true/false

    // 🔹 Apoderado para la cuenta de APORTES
    private String documentoApoderadoAportes;
    private String nombreApoderadoAportes;
    private String telefonoApoderadoAportes;
    private String celularApoderadoAportes;

    // 🔹 Apoderado para la cuenta de AHORRO opcional
    private String documentoApoderadoAhorro;
    private String nombreApoderadoAhorro;
    private String telefonoApoderadoAhorro;
    private String celularApoderadoAhorro;

    // Auditoría
    private Integer usuarioId;

    // Agencia donde el usuario está creando la cuenta
    private Integer idAgenciaUsuario;
}
