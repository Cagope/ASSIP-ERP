package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio102.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Criterio102DatoDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCierreCarteraCredito;

    private Integer idCarteraCredito;

    private Integer idDatosPersonal;

    private String documento;

    // =========================================================
    // Criterio 102 - Actualización de datos
    // =========================================================

    private LocalDate fechaActualizacionDatos;
}