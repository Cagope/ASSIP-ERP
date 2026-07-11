package co.assip.erp.hojavida.bienesinmuebles;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class BienInmueble {

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
    // Inmueble: hoja_vida.bienes_inmuebles
    // =========================================================
    private Long idBienInmueble;
    private Long idTipoInmueble;
    private String codigoTipoInmueble;
    private String nombreTipoInmueble;

    private String numeroMatriculaInmobiliaria;
    private String cedulaCatastral;

    private Long idPais;
    private String nombrePais;

    private Long idDepartamento;
    private String nombreDepartamento;

    private Long idCiudad;
    private String nombreCiudad;

    private String direccion;
    private String barrioVereda;

    private BigDecimal areaTerreno;
    private BigDecimal areaConstruida;

    private String numeroEscritura;
    private LocalDate fechaEscritura;
    private String notaria;

    private Long idPaisNotaria;
    private String nombrePaisNotaria;

    private Long idDepartamentoNotaria;
    private String nombreDepartamentoNotaria;

    private Long idCiudadNotaria;
    private String nombreCiudadNotaria;

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
    // Último avalúo desde vista
    // =========================================================
    private Long idBienInmuebleAvaluo;
    private LocalDate fechaAvaluo;
    private BigDecimal valorAvaluoComercial;
    private BigDecimal valorAvaluoCatastral;
    private String entidadAvaluadora;
    private String numeroInforme;
    private String observacionesAvaluo;

    // =========================================================
    // Seguro vigente o más reciente desde vista
    // =========================================================
    private Long idBienInmuebleSeguro;
    private String aseguradora;
    private String numeroPoliza;
    private BigDecimal valorAsegurado;
    private LocalDate fechaInicioSeguro;
    private LocalDate fechaVencimientoSeguro;
    private String estadoSeguro;
    private String nombreEstadoSeguro;
    private String observacionesSeguro;

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