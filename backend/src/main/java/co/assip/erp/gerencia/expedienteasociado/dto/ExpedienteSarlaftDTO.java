package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ExpedienteSarlaftDTO {

    // =========================================================
    // Identificación del asociado
    // =========================================================

    private Integer idDatosPersonal;

    private String tipoDocumento;
    private String nombreTipoDocumento;
    private String documento;
    private String tipoPersona;

    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String nombreCompleto;

    private LocalDate fechaNacimiento;

    // =========================================================
    // Estado de actualización
    // =========================================================

    private LocalDate fechaActualizacion;

    private LocalDateTime fechaCreacionDatos;
    private LocalDateTime fechaEdicionDatos;

    // =========================================================
    // Información económica
    // =========================================================

    private String codigoOcupacion;
    private String nombreOcupacion;

    private String codigoSectorEconomico;
    private String nombreSectorEconomico;

    private String codigoActividadSes;
    private String nombreActividadSes;

    private String codigoActividadDian;
    private String nombreActividadDian;

    private String origenFondos;

    // =========================================================
    // Persona Expuesta Políticamente
    // =========================================================

    private Boolean asociadoPeps;

    private String tipoPeps;
    private String nombreTipoPeps;

    private String observacionesPeps;

    private LocalDate fechaInicialPeps;
    private LocalDate fechaFinalPeps;

    // =========================================================
    // Familiar relacionado con PEP
    // =========================================================

    private Boolean familiaPeps;

    private String tipoFamiliaPeps;

    private String cedulaFamiliaPeps;

    private String codigoParentesco;
    private String nombreParentesco;

    private String nombreFamiliaPeps;

    // =========================================================
    // Operaciones en moneda extranjera
    // =========================================================

    private Boolean monedaExtranjera;

    private String observacionMonedaExtranjera;

    // =========================================================
    // Cuenta en el exterior
    // =========================================================

    private Boolean cuentaExtranjero;

    private String tipoMonedaExtranjera;
    private String numeroCuentaExtranjero;
    private String nombreBancoExtranjero;
    private String ciudadCuentaExtranjero;
    private String paisCuentaExtranjero;

    // =========================================================
    // Residencia fiscal / FATCA / CRS
    // =========================================================

    private Long idResidenciaFiscal;

    private Boolean tieneInformacionResidenciaFiscal;

    private Boolean ciudadanoEstadosUnidos;
    private Boolean residenteFiscalEstadosUnidos;
    private Boolean residenteFiscalExterior;

    private String paisResidenciaFiscal;
    private String numeroIdentificacionFiscal;
    private String tipoIdentificacionFiscal;

    private String ciudadResidenciaFiscal;
    private String direccionResidenciaFiscal;

    private String observacionesResidenciaFiscal;

    private LocalDateTime fechaCreacionResidenciaFiscal;
    private LocalDateTime fechaEdicionResidenciaFiscal;

    // =========================================================
    // Condiciones de protección
    // =========================================================

    private Long idCondicionProteccion;

    private Boolean tieneInformacionCondicionesProteccion;

    private Boolean administraRecursosPublicos;

    private Boolean grupoProteccionEspecialConstitucional;

    private Boolean personaMayor60Anos;

    private Boolean discapacidadFisica;

    private Boolean victimaConflictoArmado;

    private Boolean pobrezaExtrema;

    private Boolean poblacionIndigena;

    private Boolean poblacionAfrodescendiente;

    private Boolean poblacionLgbtiqMas;

    private Boolean perteneceGrupoProteccionConstitucional;

    private String observacionesCondicionesProteccion;

    private LocalDateTime fechaCreacionCondicionesProteccion;
    private LocalDateTime fechaEdicionCondicionesProteccion;

    // =========================================================
    // Constructor
    // =========================================================

    public ExpedienteSarlaftDTO() {

        this.asociadoPeps = Boolean.FALSE;
        this.familiaPeps = Boolean.FALSE;

        this.monedaExtranjera = Boolean.FALSE;
        this.cuentaExtranjero = Boolean.FALSE;

        this.tieneInformacionResidenciaFiscal = Boolean.FALSE;

        this.ciudadanoEstadosUnidos = Boolean.FALSE;
        this.residenteFiscalEstadosUnidos = Boolean.FALSE;
        this.residenteFiscalExterior = Boolean.FALSE;

        this.tieneInformacionCondicionesProteccion = Boolean.FALSE;

        this.administraRecursosPublicos = Boolean.FALSE;
        this.grupoProteccionEspecialConstitucional = Boolean.FALSE;
        this.personaMayor60Anos = Boolean.FALSE;
        this.discapacidadFisica = Boolean.FALSE;
        this.victimaConflictoArmado = Boolean.FALSE;
        this.pobrezaExtrema = Boolean.FALSE;
        this.poblacionIndigena = Boolean.FALSE;
        this.poblacionAfrodescendiente = Boolean.FALSE;
        this.poblacionLgbtiqMas = Boolean.FALSE;
        this.perteneceGrupoProteccionConstitucional = Boolean.FALSE;
    }
}