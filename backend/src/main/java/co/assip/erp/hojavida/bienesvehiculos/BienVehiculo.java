package co.assip.erp.hojavida.bienesvehiculos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class BienVehiculo {

    // =========================================================
    // Relación persona ↔ bien
    // =========================================================
    private Long idBienPersona;
    private Long idDatosPersonal;
    private BigDecimal porcentajePropiedad;

    // =========================================================
    // Bien general: hoja_vida.bienes
    // =========================================================
    private Long idBien;
    private Long idTipoBien;
    private String codigoTipoBien;
    private String nombreTipoBien;
    private String descripcionGeneral;
    private BigDecimal valorComercial;
    private BigDecimal valorGravamen;

    // =========================================================
    // Vehículo: hoja_vida.bienes_vehiculos
    // =========================================================
    private Long idBienVehiculo;
    private Long idTipoVehiculo;
    private String nombreTipoVehiculo;

    private String placa;
    private String marca;
    private String linea;
    private Integer modelo;
    private String color;
    private String numeroMotor;
    private String numeroChasis;
    private String numeroSerie;

    private Long idTipoGravamen;
    private String nombreGravamen;

    private String observaciones;

    // =========================================================
    // Datos del asociado desde vista
    // =========================================================
    private String tipoDocumento;
    private String nombreTipoDocumento;
    private String documento;
    private String nombreCompleto;

    // =========================================================
    // Calculados desde vista
    // =========================================================
    private BigDecimal valorNetoBien;
    private BigDecimal valorPropiedadAsociado;
    private BigDecimal valorGravamenAsociado;
    private BigDecimal valorNetoAsociado;

    // =========================================================
    // Auditoría
    // =========================================================
    private Integer fkSeguridadCreacion;
    private LocalDateTime fechaCreacion;
    private Integer fkSeguridadEdicion;
    private LocalDateTime fechaEdicion;
}