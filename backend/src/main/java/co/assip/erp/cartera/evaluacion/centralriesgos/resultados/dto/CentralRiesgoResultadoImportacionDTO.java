package co.assip.erp.cartera.evaluacion.centralriesgos.resultados.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CentralRiesgoResultadoImportacionDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCentralArchivo;

    private Integer idCentralRiesgo;

    private String codigoCentral;

    private String nombreCentral;

    private LocalDate fechaCorte;

    // =========================================================
    // Archivo importado
    // =========================================================

    private String nombreArchivo;

    private Long tamanoArchivo;

    private String extensionArchivo;

    private String codificacionArchivo;

    private String separadorArchivo;

    private LocalDateTime fechaImportacion;

    // =========================================================
    // Resumen de la importación
    // =========================================================

    private Integer cantidadRegistrosLeidos;

    private Integer cantidadRegistrosImportados;

    private Integer cantidadRegistrosRechazados;

    private String observaciones;

    private Boolean requiereConfirmacion;

    private String mensajeConfirmacion;

    // =========================================================
    // Auditoría
    // =========================================================

    private Integer fkSeguridadCreacion;

    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;

    private LocalDateTime fechaEdicion;
}