package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ExpedienteAfiliacionDTO {

    // =========================================================
    // Identificación del asociado
    // =========================================================
    private Long idDatosPersonal;

    private String tipoDocumento;
    private String nombreTipoDocumento;
    private String documento;

    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String nombreCompleto;

    // =========================================================
    // Identificación de la afiliación
    // =========================================================
    private Long idAfiliacion;

    private String codigoAsociado;
    private String numeroAfiliacion;

    private LocalDate fechaAfiliacion;
    private LocalDate fechaIngreso;
    private LocalDate fechaAntiguedad;

    // =========================================================
    // Estado actual de la afiliación
    // =========================================================
    private String codigoEstadoAsociado;
    private String nombreEstadoAsociado;

    private String codigoEstadoAfiliacion;
    private String nombreEstadoAfiliacion;

    private Boolean afiliacionActiva;

    private LocalDate fechaEstado;
    private String motivoEstado;
    private String observacionEstado;

    // =========================================================
    // Tipo y clasificación del asociado
    // =========================================================
    private Long idTipoAsociado;

    private String codigoTipoAsociado;
    private String nombreTipoAsociado;

    private Long idCategoriaAsociado;

    private String codigoCategoriaAsociado;
    private String nombreCategoriaAsociado;

    private Long idClaseAsociado;

    private String codigoClaseAsociado;
    private String nombreClaseAsociado;

    // =========================================================
    // Modalidad o vínculo institucional
    // =========================================================
    private String codigoVinculacion;
    private String nombreVinculacion;

    private String codigoOrigenVinculacion;
    private String nombreOrigenVinculacion;

    private Boolean asociadoFundador;
    private Boolean asociadoHabil;
    private Boolean delegado;

    // =========================================================
    // Agencia de afiliación
    // =========================================================
    private Integer idAgenciaAfiliacion;

    private String codigoAgenciaAfiliacion;
    private String nombreAgenciaAfiliacion;

    // =========================================================
    // Agencia actual
    // =========================================================
    private Integer idAgenciaActual;

    private String codigoAgenciaActual;
    private String nombreAgenciaActual;

    // =========================================================
    // Zona o ubicación administrativa
    // =========================================================
    private Long idZona;

    private String codigoZona;
    private String nombreZona;

    private Long idSubZona;

    private String codigoSubZona;
    private String nombreSubZona;

    // =========================================================
    // Aportes sociales
    // =========================================================
    private Long idCuentaAportes;

    private String codigoCuentaAportes;
    private String codigoFormaAportes;
    private String nombreFormaAportes;

    private LocalDate fechaAperturaAportes;

    private BigDecimal cuotaAportes;
    private BigDecimal saldoAportes;
    private BigDecimal saldoDisponibleAportes;

    private String codigoEstadoCuentaAportes;
    private String nombreEstadoCuentaAportes;

    private Boolean cuentaAportesActiva;

    // =========================================================
    // Reciprocidad y obligaciones sociales
    // =========================================================
    private BigDecimal porcentajeReciprocidad;
    private BigDecimal valorReciprocidad;

    private BigDecimal aporteMinimo;
    private BigDecimal aporteOrdinario;
    private BigDecimal aporteExtraordinario;

    private Boolean cumpleAporteMinimo;
    private Boolean cumpleReciprocidad;

    // =========================================================
    // Fechas y antigüedad
    // =========================================================
    private Integer antiguedadDias;
    private Integer antiguedadMeses;
    private Integer antiguedadAnios;

    private LocalDate fechaUltimoAporte;
    private LocalDate fechaUltimoMovimientoAportes;

    // =========================================================
    // Retiro o desvinculación
    // =========================================================
    private Boolean retirado;

    private LocalDate fechaSolicitudRetiro;
    private LocalDate fechaRetiro;
    private LocalDate fechaLiquidacionRetiro;

    private String codigoMotivoRetiro;
    private String nombreMotivoRetiro;

    private String observacionRetiro;

    // =========================================================
    // Reingreso
    // =========================================================
    private Boolean reingreso;

    private Integer numeroReingresos;
    private LocalDate fechaUltimoReingreso;

    // =========================================================
    // Derechos políticos y participación
    // =========================================================
    private Boolean puedeElegir;
    private Boolean puedeSerElegido;
    private Boolean puedeParticiparAsamblea;

    private String restriccionDerechos;
    private LocalDate fechaInicioRestriccion;
    private LocalDate fechaFinRestriccion;

    // =========================================================
    // Información laboral relacionada con la afiliación
    // =========================================================
    private Long idEmpresaVinculada;

    private String documentoEmpresa;
    private String nombreEmpresa;

    private String cargoEmpresa;

    private LocalDate fechaIngresoEmpresa;
    private LocalDate fechaRetiroEmpresa;

    private Boolean libranzaActiva;

    // =========================================================
    // Convenio o grupo empresarial
    // =========================================================
    private Long idConvenio;

    private String codigoConvenio;
    private String nombreConvenio;

    private Long idGrupoAsociado;

    private String codigoGrupoAsociado;
    private String nombreGrupoAsociado;

    // =========================================================
    // Actualización de información
    // =========================================================
    private LocalDate fechaUltimaActualizacion;

    private Integer diasSinActualizar;
    private Boolean informacionActualizada;

    // =========================================================
    // Documentación de afiliación
    // =========================================================
    private Boolean formularioAfiliacionCompleto;
    private Boolean documentosIdentificacionCompletos;
    private Boolean autorizacionTratamientoDatos;
    private Boolean declaracionOrigenFondos;
    private Boolean consultaCentralesAutorizada;

    private Boolean documentacionCompleta;

    // =========================================================
    // Alertas propias del bloque
    // =========================================================
    private Boolean presentaNovedades;
    private Integer cantidadNovedades;

    private String nivelNovedad;
    private String resumenNovedades;

    // =========================================================
    // Observaciones
    // =========================================================
    private String observaciones;

    // =========================================================
    // Auditoría
    // =========================================================
    private Integer fkSeguridadCreacion;
    private LocalDate fechaCreacion;

    private Integer fkSeguridadEdicion;
    private LocalDate fechaEdicion;

    // =========================================================
    // Constructor
    // =========================================================
    public ExpedienteAfiliacionDTO() {

        this.afiliacionActiva = Boolean.FALSE;

        this.asociadoFundador = Boolean.FALSE;
        this.asociadoHabil = Boolean.FALSE;
        this.delegado = Boolean.FALSE;

        this.cuentaAportesActiva = Boolean.FALSE;

        this.cumpleAporteMinimo = Boolean.FALSE;
        this.cumpleReciprocidad = Boolean.FALSE;

        this.retirado = Boolean.FALSE;
        this.reingreso = Boolean.FALSE;

        this.puedeElegir = Boolean.FALSE;
        this.puedeSerElegido = Boolean.FALSE;
        this.puedeParticiparAsamblea = Boolean.FALSE;

        this.libranzaActiva = Boolean.FALSE;

        this.informacionActualizada = Boolean.FALSE;

        this.formularioAfiliacionCompleto = Boolean.FALSE;
        this.documentosIdentificacionCompletos = Boolean.FALSE;
        this.autorizacionTratamientoDatos = Boolean.FALSE;
        this.declaracionOrigenFondos = Boolean.FALSE;
        this.consultaCentralesAutorizada = Boolean.FALSE;
        this.documentacionCompleta = Boolean.FALSE;

        this.presentaNovedades = Boolean.FALSE;
        this.cantidadNovedades = 0;

        this.antiguedadDias = 0;
        this.antiguedadMeses = 0;
        this.antiguedadAnios = 0;

        this.numeroReingresos = 0;
        this.diasSinActualizar = 0;

        this.cuotaAportes = BigDecimal.ZERO;
        this.saldoAportes = BigDecimal.ZERO;
        this.saldoDisponibleAportes = BigDecimal.ZERO;

        this.porcentajeReciprocidad = BigDecimal.ZERO;
        this.valorReciprocidad = BigDecimal.ZERO;

        this.aporteMinimo = BigDecimal.ZERO;
        this.aporteOrdinario = BigDecimal.ZERO;
        this.aporteExtraordinario = BigDecimal.ZERO;
    }

    // =========================================================
    // Métodos auxiliares
    // =========================================================

    /**
     * Calcula el saldo disponible de aportes después de aplicar
     * el valor comprometido por reciprocidad.
     */
    public BigDecimal calcularSaldoDisponibleAportes() {

        BigDecimal saldo =
                saldoAportes == null
                        ? BigDecimal.ZERO
                        : saldoAportes;

        BigDecimal comprometido =
                valorReciprocidad == null
                        ? BigDecimal.ZERO
                        : valorReciprocidad;

        this.saldoDisponibleAportes =
                saldo.subtract(comprometido);

        return this.saldoDisponibleAportes;
    }

    /**
     * Determina si los documentos básicos de afiliación están
     * completos.
     */
    public boolean evaluarDocumentacionCompleta() {

        this.documentacionCompleta =
                Boolean.TRUE.equals(formularioAfiliacionCompleto)
                        && Boolean.TRUE.equals(documentosIdentificacionCompletos)
                        && Boolean.TRUE.equals(autorizacionTratamientoDatos)
                        && Boolean.TRUE.equals(declaracionOrigenFondos);

        return this.documentacionCompleta;
    }

    /**
     * Determina si el asociado conserva las condiciones mínimas
     * para considerarse hábil.
     *
     * La validación definitiva deberá permanecer en el Service,
     * porque puede depender de reglas institucionales adicionales.
     */
    public boolean evaluarCondicionBasicaHabilidad() {

        this.asociadoHabil =
                Boolean.TRUE.equals(afiliacionActiva)
                        && !Boolean.TRUE.equals(retirado)
                        && Boolean.TRUE.equals(cumpleAporteMinimo)
                        && Boolean.TRUE.equals(documentacionCompleta);

        return this.asociadoHabil;
    }

    /**
     * Actualiza el indicador general de novedades del bloque.
     */
    public void actualizarEstadoNovedades() {

        int novedades = 0;

        if (!Boolean.TRUE.equals(afiliacionActiva)) {
            novedades++;
        }

        if (!Boolean.TRUE.equals(documentacionCompleta)) {
            novedades++;
        }

        if (!Boolean.TRUE.equals(cumpleAporteMinimo)) {
            novedades++;
        }

        if (!Boolean.TRUE.equals(informacionActualizada)) {
            novedades++;
        }

        this.cantidadNovedades = novedades;
        this.presentaNovedades = novedades > 0;

        if (novedades == 0) {
            this.nivelNovedad = "NORMAL";
            this.resumenNovedades =
                    "La afiliación no presenta novedades.";
        } else if (novedades == 1) {
            this.nivelNovedad = "ADVERTENCIA";
            this.resumenNovedades =
                    "La afiliación presenta una novedad pendiente de revisión.";
        } else {
            this.nivelNovedad = "CRITICA";
            this.resumenNovedades =
                    "La afiliación presenta varias novedades pendientes de revisión.";
        }
    }
}