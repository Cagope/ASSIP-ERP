package co.assip.erp.cartera.evaluacion.centralriesgos.resultados.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CentralRiesgoResultadoDatoDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCentralDato;

    private Integer idCentralArchivo;

    private Integer numeroFila;

    // =========================================================
    // Información importada
    // =========================================================

    private String documento;

    private String clasificacionCartera;

    private String calificacionGuiaFinal;

    private Integer alertasTotales;

    // =========================================================
    // Auditoría
    // =========================================================

    private Integer fkSeguridadCreacion;

    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;

    private LocalDateTime fechaEdicion;
}