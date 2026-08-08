package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio201.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Criterio201DatoDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCierreCarteraCredito;

    private Integer idCarteraCredito;

    private Integer idDatosPersonal;

    private String documento;

    // =========================================================
    // Criterio 201 - Edad cronológica
    // =========================================================

    /**
     * 1 = Persona natural
     * 2 = Persona jurídica
     */
    private Short idTipoPersona;

    /**
     * Para persona natural corresponde a la fecha de nacimiento.
     *
     * Para persona jurídica, según la estructura actual del sistema,
     * también se utiliza este mismo campo como fecha base para
     * calcular la antigüedad.
     */
    private LocalDate fechaNacimiento;
}