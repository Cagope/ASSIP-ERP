package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
public class ExpedienteGarantiaDTO {

    // =========================================================
    // Identificación de la relación garantía
    // =========================================================
    private Long idGarantia;
    private Long idCredito;

    private Long idDatosPersonal;
    private Long idBien;
    private Long idBienPersona;

    // =========================================================
    // Identificación del asociado
    // =========================================================
    private String tipoDocumento;
    private String nombreTipoDocumento;
    private String documento;
    private String nombreCompleto;

    // =========================================================
    // Identificación del crédito
    // =========================================================
    private String numeroCredito;

    private Long idLineaCredito;
    private String codigoLineaCredito;
    private String nombreLineaCredito;

    private String codigoEstadoCredito;
    private String nombreEstadoCredito;

    private BigDecimal valorDesembolsado;
    private BigDecimal saldoCapitalCredito;
    private BigDecimal saldoTotalCredito;

    // =========================================================
    // Tipo de garantía de cartera
    // =========================================================
    private Long idTipoGarantiaCredito;

    private String codigoTipoGarantiaCredito;
    private String nombreTipoGarantiaCredito;

    /*
     * Ejemplos conocidos del catálogo:
     * 1 = No idónea
     * 2 = Hipotecaria
     * 3 = Prendaria
     * 6 = Fiducia
     */
    private Boolean garantiaIdonea;
    private Boolean garantiaReal;
    private Boolean garantiaPersonal;

    // =========================================================
    // Tipo de bien relacionado
    // =========================================================
    private Long idTipoBien;

    private String codigoTipoBien;
    private String nombreTipoBien;

    /*
     * Valores esperados:
     * INMUEBLE
     * VEHICULO
     * MAQUINARIA
     * INVERSION
     * OTRA
     */
    private String categoriaBien;

    private String descripcionBien;

    // =========================================================
    // Identificación específica del bien
    // =========================================================
    private Long idBienInmueble;
    private Long idBienVehiculo;
    private Long idBienMaquinaria;
    private Long idBienInversion;

    private String numeroMatriculaInmobiliaria;
    private String cedulaCatastral;

    private String placaVehiculo;

    private String serieMaquinaria;

    private String numeroTituloInversion;

    // =========================================================
    // Titularidad
    // =========================================================
    private BigDecimal porcentajePropiedad;
    private BigDecimal porcentajeAfectacionGarantia;

    private Integer cantidadPropietarios;

    private Boolean propiedadCompartida;
    private Boolean titularGarantiaEsDeudor;

    private String documentoTitularGarantia;
    private String nombreTitularGarantia;

    // =========================================================
    // Valores del bien
    // =========================================================
    private BigDecimal valorComercialBien;
    private BigDecimal valorGravamenBien;
    private BigDecimal valorNetoBien;

    private BigDecimal valorParticipacionAsociado;
    private BigDecimal valorAfectadoGarantia;

    // =========================================================
    // Valoración de la garantía
    // =========================================================
    private BigDecimal valorAvaluo;
    private LocalDate fechaAvaluo;

    private String entidadAvaluadora;
    private String numeroAvaluo;

    private Integer antiguedadAvaluoMeses;

    private Boolean tieneAvaluo;
    private Boolean avaluoVigente;
    private Boolean avaluoProximoVencer;
    private Boolean avaluoVencido;

    // =========================================================
    // Porcentaje admisible
    // =========================================================
    private BigDecimal porcentajeAdmisible;

    private BigDecimal valorAdmisibleGarantia;

    /*
     * Valor utilizado por la institución después de aplicar:
     * porcentaje de propiedad,
     * porcentaje de afectación,
     * porcentaje admisible,
     * gravámenes y demás reglas institucionales.
     */
    private BigDecimal valorCoberturaReconocido;

    // =========================================================
    // Seguro
    // =========================================================
    private Boolean requiereSeguro;
    private Boolean tieneSeguro;

    private String aseguradora;
    private String numeroPoliza;

    private BigDecimal valorAsegurado;

    private LocalDate fechaInicioSeguro;
    private LocalDate fechaVencimientoSeguro;

    private Integer diasParaVencimientoSeguro;

    private Boolean seguroVigente;
    private Boolean seguroProximoVencer;
    private Boolean seguroVencido;

    private BigDecimal porcentajeCoberturaSeguro;
    private Boolean seguroSuficiente;

    // =========================================================
    // Constitución y registro
    // =========================================================
    private LocalDate fechaConstitucionGarantia;
    private LocalDate fechaRegistroGarantia;

    private String numeroDocumentoGarantia;
    private String entidadRegistroGarantia;

    private Boolean garantiaConstituida;
    private Boolean garantiaRegistrada;

    private Boolean documentacionCompleta;

    // =========================================================
    // Gravamen
    // =========================================================
    private Long idTipoGravamen;
    private String codigoTipoGravamen;
    private String nombreTipoGravamen;

    private Boolean tieneGravamen;

    private Boolean gravamenCompatibleGarantia;
    private Boolean gravamenSuperaValorBien;

    // =========================================================
    // Cobertura del crédito
    // =========================================================
    private BigDecimal saldoCreditoCubierto;

    private BigDecimal porcentajeCoberturaCredito;

    private BigDecimal excesoCobertura;
    private BigDecimal faltanteCobertura;

    private Boolean coberturaSuficiente;
    private Boolean coberturaParcial;
    private Boolean coberturaInsuficiente;

    private Boolean garantiaSobreutilizada;

    // =========================================================
    // Utilización compartida
    // =========================================================
    private Integer cantidadCreditosRespaldados;

    private BigDecimal saldoTotalCreditosRespaldados;

    private BigDecimal valorDisponibleGarantia;

    private Boolean garantiaCompartidaEntreCreditos;

    // =========================================================
    // Estado de la garantía
    // =========================================================
    private String codigoEstadoGarantia;
    private String nombreEstadoGarantia;

    /*
     * Estados gerenciales esperados:
     * VIGENTE
     * PENDIENTE
     * INCOMPLETA
     * VENCIDA
     * LIBERADA
     * CANCELADA
     */
    private Boolean vigente;
    private Boolean pendienteConstitucion;
    private Boolean liberada;
    private Boolean cancelada;

    private LocalDate fechaLiberacion;
    private LocalDate fechaCancelacion;

    // =========================================================
    // Calidad de información
    // =========================================================
    private Boolean informacionBienCompleta;
    private Boolean informacionValoracionCompleta;
    private Boolean informacionSeguroCompleta;
    private Boolean informacionRegistroCompleta;
    private Boolean informacionCoberturaCompleta;

    private Boolean informacionCompleta;
    private Integer porcentajeCompletitud;

    // =========================================================
    // Revisión
    // =========================================================
    private Boolean requiereActualizacion;
    private Boolean requiereRevision;

    private String motivoRevision;

    // =========================================================
    // Alertas gerenciales
    // =========================================================
    private Integer cantidadAlertas;

    private Integer alertasCriticas;
    private Integer alertasAdvertencia;
    private Integer alertasInformativas;

    private String nivelAlerta;
    private String resumenAlertas;

    // =========================================================
    // Observaciones
    // =========================================================
    private String observaciones;

    // =========================================================
    // Auditoría
    // =========================================================
    private Integer fkSeguridadCreacion;
    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;
    private LocalDateTime fechaEdicion;

    // =========================================================
    // Constructor
    // =========================================================
    public ExpedienteGarantiaDTO() {

        this.valorDesembolsado = BigDecimal.ZERO;
        this.saldoCapitalCredito = BigDecimal.ZERO;
        this.saldoTotalCredito = BigDecimal.ZERO;

        this.garantiaIdonea = Boolean.FALSE;
        this.garantiaReal = Boolean.FALSE;
        this.garantiaPersonal = Boolean.FALSE;

        this.porcentajePropiedad = BigDecimal.valueOf(100);
        this.porcentajeAfectacionGarantia = BigDecimal.valueOf(100);

        this.cantidadPropietarios = 1;
        this.propiedadCompartida = Boolean.FALSE;
        this.titularGarantiaEsDeudor = Boolean.TRUE;

        this.valorComercialBien = BigDecimal.ZERO;
        this.valorGravamenBien = BigDecimal.ZERO;
        this.valorNetoBien = BigDecimal.ZERO;

        this.valorParticipacionAsociado = BigDecimal.ZERO;
        this.valorAfectadoGarantia = BigDecimal.ZERO;

        this.valorAvaluo = BigDecimal.ZERO;
        this.antiguedadAvaluoMeses = 0;

        this.tieneAvaluo = Boolean.FALSE;
        this.avaluoVigente = Boolean.FALSE;
        this.avaluoProximoVencer = Boolean.FALSE;
        this.avaluoVencido = Boolean.FALSE;

        this.porcentajeAdmisible = BigDecimal.valueOf(100);
        this.valorAdmisibleGarantia = BigDecimal.ZERO;
        this.valorCoberturaReconocido = BigDecimal.ZERO;

        this.requiereSeguro = Boolean.FALSE;
        this.tieneSeguro = Boolean.FALSE;

        this.valorAsegurado = BigDecimal.ZERO;
        this.diasParaVencimientoSeguro = 0;

        this.seguroVigente = Boolean.FALSE;
        this.seguroProximoVencer = Boolean.FALSE;
        this.seguroVencido = Boolean.FALSE;

        this.porcentajeCoberturaSeguro = BigDecimal.ZERO;
        this.seguroSuficiente = Boolean.FALSE;

        this.garantiaConstituida = Boolean.FALSE;
        this.garantiaRegistrada = Boolean.FALSE;
        this.documentacionCompleta = Boolean.FALSE;

        this.tieneGravamen = Boolean.FALSE;
        this.gravamenCompatibleGarantia = Boolean.TRUE;
        this.gravamenSuperaValorBien = Boolean.FALSE;

        this.saldoCreditoCubierto = BigDecimal.ZERO;
        this.porcentajeCoberturaCredito = BigDecimal.ZERO;

        this.excesoCobertura = BigDecimal.ZERO;
        this.faltanteCobertura = BigDecimal.ZERO;

        this.coberturaSuficiente = Boolean.FALSE;
        this.coberturaParcial = Boolean.FALSE;
        this.coberturaInsuficiente = Boolean.FALSE;

        this.garantiaSobreutilizada = Boolean.FALSE;

        this.cantidadCreditosRespaldados = 0;
        this.saldoTotalCreditosRespaldados = BigDecimal.ZERO;
        this.valorDisponibleGarantia = BigDecimal.ZERO;

        this.garantiaCompartidaEntreCreditos = Boolean.FALSE;

        this.vigente = Boolean.FALSE;
        this.pendienteConstitucion = Boolean.FALSE;
        this.liberada = Boolean.FALSE;
        this.cancelada = Boolean.FALSE;

        this.informacionBienCompleta = Boolean.FALSE;
        this.informacionValoracionCompleta = Boolean.FALSE;
        this.informacionSeguroCompleta = Boolean.FALSE;
        this.informacionRegistroCompleta = Boolean.FALSE;
        this.informacionCoberturaCompleta = Boolean.FALSE;

        this.informacionCompleta = Boolean.FALSE;
        this.porcentajeCompletitud = 0;

        this.requiereActualizacion = Boolean.FALSE;
        this.requiereRevision = Boolean.FALSE;

        this.cantidadAlertas = 0;
        this.alertasCriticas = 0;
        this.alertasAdvertencia = 0;
        this.alertasInformativas = 0;
    }

    // =========================================================
    // Cálculos del bien
    // =========================================================

    /**
     * Calcula el valor neto del bien después de descontar
     * el gravamen registrado.
     */
    public BigDecimal calcularValorNetoBien() {

        this.valorNetoBien =
                valorSeguro(valorComercialBien)
                        .subtract(valorSeguro(valorGravamenBien));

        this.gravamenSuperaValorBien =
                valorSeguro(valorGravamenBien)
                        .compareTo(valorSeguro(valorComercialBien)) > 0;

        return this.valorNetoBien;
    }

    /**
     * Calcula el valor patrimonial que corresponde al asociado.
     */
    public BigDecimal calcularValorParticipacionAsociado() {

        this.valorParticipacionAsociado =
                valorSeguro(valorNetoBien)
                        .max(BigDecimal.ZERO)
                        .multiply(
                                normalizarPorcentaje(
                                        porcentajePropiedad
                                )
                        )
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );

        return this.valorParticipacionAsociado;
    }

    /**
     * Calcula cuánto del valor del asociado está afectado
     * específicamente a la garantía.
     */
    public BigDecimal calcularValorAfectadoGarantia() {

        this.valorAfectadoGarantia =
                valorSeguro(valorParticipacionAsociado)
                        .multiply(
                                normalizarPorcentaje(
                                        porcentajeAfectacionGarantia
                                )
                        )
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );

        return this.valorAfectadoGarantia;
    }

    /**
     * Calcula el valor admisible según el porcentaje institucional.
     */
    public BigDecimal calcularValorAdmisibleGarantia() {

        this.valorAdmisibleGarantia =
                valorSeguro(valorAfectadoGarantia)
                        .multiply(
                                normalizarPorcentaje(
                                        porcentajeAdmisible
                                )
                        )
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );

        this.valorCoberturaReconocido =
                this.valorAdmisibleGarantia;

        return this.valorAdmisibleGarantia;
    }

    // =========================================================
    // Avalúo
    // =========================================================

    /**
     * Evalúa la vigencia del avalúo sin fijar una regla institucional
     * dentro del DTO.
     */
    public void evaluarAvaluo(
            int vigenciaMeses,
            int mesesAdvertencia
    ) {

        this.tieneAvaluo =
                fechaAvaluo != null
                        || valorSeguro(valorAvaluo)
                        .compareTo(BigDecimal.ZERO) > 0;

        this.avaluoVigente = Boolean.FALSE;
        this.avaluoProximoVencer = Boolean.FALSE;
        this.avaluoVencido = Boolean.FALSE;
        this.antiguedadAvaluoMeses = 0;

        if (fechaAvaluo == null) {
            return;
        }

        LocalDate hoy = LocalDate.now();

        long meses =
                ChronoUnit.MONTHS.between(
                        fechaAvaluo.withDayOfMonth(1),
                        hoy.withDayOfMonth(1)
                );

        this.antiguedadAvaluoMeses =
                convertirEnteroSeguro(meses);

        int vigencia =
                Math.max(vigenciaMeses, 1);

        int advertencia =
                Math.max(mesesAdvertencia, 0);

        int inicioAdvertencia =
                Math.max(vigencia - advertencia, 0);

        if (meses >= vigencia) {

            this.avaluoVencido = Boolean.TRUE;

        } else if (meses >= inicioAdvertencia) {

            this.avaluoVigente = Boolean.TRUE;
            this.avaluoProximoVencer = Boolean.TRUE;

        } else {

            this.avaluoVigente = Boolean.TRUE;
        }
    }

    // =========================================================
    // Seguro
    // =========================================================

    /**
     * Evalúa la existencia, vigencia y cobertura del seguro.
     */
    public void evaluarSeguro(
            int diasAdvertencia
    ) {

        this.tieneSeguro =
                tieneTexto(aseguradora)
                        || tieneTexto(numeroPoliza)
                        || fechaInicioSeguro != null
                        || fechaVencimientoSeguro != null
                        || valorSeguro(valorAsegurado)
                        .compareTo(BigDecimal.ZERO) > 0;

        this.seguroVigente = Boolean.FALSE;
        this.seguroProximoVencer = Boolean.FALSE;
        this.seguroVencido = Boolean.FALSE;
        this.diasParaVencimientoSeguro = 0;

        if (fechaVencimientoSeguro != null) {

            long dias =
                    ChronoUnit.DAYS.between(
                            LocalDate.now(),
                            fechaVencimientoSeguro
                    );

            this.diasParaVencimientoSeguro =
                    convertirEnteroSeguro(dias);

            int advertencia =
                    Math.max(diasAdvertencia, 0);

            if (dias < 0) {

                this.seguroVencido = Boolean.TRUE;

            } else if (dias <= advertencia) {

                this.seguroVigente = Boolean.TRUE;
                this.seguroProximoVencer = Boolean.TRUE;

            } else {

                this.seguroVigente = Boolean.TRUE;
            }
        }

        calcularCoberturaSeguro();
    }

    /**
     * Calcula el porcentaje asegurado frente al valor afectado
     * a la garantía.
     */
    public BigDecimal calcularCoberturaSeguro() {

        BigDecimal base =
                valorSeguro(valorAfectadoGarantia);

        if (base.compareTo(BigDecimal.ZERO) <= 0) {

            this.porcentajeCoberturaSeguro = BigDecimal.ZERO;
            this.seguroSuficiente = Boolean.FALSE;

            return this.porcentajeCoberturaSeguro;
        }

        this.porcentajeCoberturaSeguro =
                valorSeguro(valorAsegurado)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                base,
                                2,
                                RoundingMode.HALF_UP
                        );

        this.seguroSuficiente =
                valorSeguro(valorAsegurado)
                        .compareTo(base) >= 0;

        return this.porcentajeCoberturaSeguro;
    }

    // =========================================================
    // Cobertura del crédito
    // =========================================================

    /**
     * Calcula la cobertura individual de la garantía frente
     * al saldo del crédito relacionado.
     */
    public BigDecimal calcularCoberturaCredito() {

        BigDecimal saldoCredito =
                valorSeguro(saldoTotalCredito);

        BigDecimal cobertura =
                valorSeguro(valorCoberturaReconocido)
                        .max(BigDecimal.ZERO);

        this.saldoCreditoCubierto =
                cobertura.min(saldoCredito);

        this.excesoCobertura =
                cobertura.subtract(saldoCredito)
                        .max(BigDecimal.ZERO);

        this.faltanteCobertura =
                saldoCredito.subtract(cobertura)
                        .max(BigDecimal.ZERO);

        this.coberturaSuficiente = Boolean.FALSE;
        this.coberturaParcial = Boolean.FALSE;
        this.coberturaInsuficiente = Boolean.FALSE;

        if (saldoCredito.compareTo(BigDecimal.ZERO) <= 0) {

            this.porcentajeCoberturaCredito =
                    BigDecimal.ZERO;

            this.coberturaSuficiente =
                    Boolean.TRUE;

            return this.porcentajeCoberturaCredito;
        }

        this.porcentajeCoberturaCredito =
                cobertura
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                saldoCredito,
                                2,
                                RoundingMode.HALF_UP
                        );

        if (cobertura.compareTo(saldoCredito) >= 0) {

            this.coberturaSuficiente =
                    Boolean.TRUE;

        } else if (cobertura.compareTo(BigDecimal.ZERO) > 0) {

            this.coberturaParcial =
                    Boolean.TRUE;

        } else {

            this.coberturaInsuficiente =
                    Boolean.TRUE;
        }

        if (cobertura.compareTo(saldoCredito) < 0) {
            this.coberturaInsuficiente =
                    Boolean.TRUE;
        }

        return this.porcentajeCoberturaCredito;
    }

    /**
     * Calcula la disponibilidad del bien cuando respalda
     * varios créditos.
     */
    public BigDecimal calcularUtilizacionCompartida() {

        BigDecimal cobertura =
                valorSeguro(valorCoberturaReconocido)
                        .max(BigDecimal.ZERO);

        BigDecimal saldos =
                valorSeguro(saldoTotalCreditosRespaldados);

        this.valorDisponibleGarantia =
                cobertura.subtract(saldos);

        this.garantiaSobreutilizada =
                this.valorDisponibleGarantia
                        .compareTo(BigDecimal.ZERO) < 0;

        this.garantiaCompartidaEntreCreditos =
                cantidadCreditosRespaldados != null
                        && cantidadCreditosRespaldados > 1;

        return this.valorDisponibleGarantia;
    }

    // =========================================================
    // Clasificación
    // =========================================================

    /**
     * Clasifica la garantía según el catálogo de cartera.
     */
    public void clasificarGarantia() {

        this.garantiaIdonea = Boolean.FALSE;
        this.garantiaReal = Boolean.FALSE;
        this.garantiaPersonal = Boolean.FALSE;

        if (idTipoGarantiaCredito == null) {
            return;
        }

        if (idTipoGarantiaCredito.equals(2L)
                || idTipoGarantiaCredito.equals(3L)
                || idTipoGarantiaCredito.equals(6L)) {

            this.garantiaIdonea = Boolean.TRUE;
        }

        if (idTipoGarantiaCredito.equals(2L)
                || idTipoGarantiaCredito.equals(3L)) {

            this.garantiaReal = Boolean.TRUE;
        }

        if (idTipoGarantiaCredito.equals(1L)) {

            this.garantiaPersonal = Boolean.TRUE;
        }
    }

    /**
     * Evalúa la titularidad y propiedad compartida.
     */
    public void evaluarTitularidad() {

        BigDecimal porcentaje =
                normalizarPorcentaje(
                        porcentajePropiedad
                );

        this.propiedadCompartida =
                porcentaje.compareTo(
                        BigDecimal.valueOf(100)
                ) < 0
                        || (
                        cantidadPropietarios != null
                                && cantidadPropietarios > 1
                );
    }

    // =========================================================
    // Calidad de información
    // =========================================================

    /**
     * Evalúa la completitud de la información de la garantía.
     */
    public int evaluarCompletitud() {

        this.informacionBienCompleta =
                idBien != null
                        && tieneTexto(nombreTipoBien)
                        && tieneTexto(descripcionBien);

        this.informacionValoracionCompleta =
                valorSeguro(valorComercialBien)
                        .compareTo(BigDecimal.ZERO) > 0
                        && (
                        !Boolean.TRUE.equals(garantiaReal)
                                || Boolean.TRUE.equals(tieneAvaluo)
                );

        this.informacionSeguroCompleta =
                !Boolean.TRUE.equals(requiereSeguro)
                        || (
                        Boolean.TRUE.equals(tieneSeguro)
                                && tieneTexto(aseguradora)
                                && tieneTexto(numeroPoliza)
                                && fechaVencimientoSeguro != null
                                && valorSeguro(valorAsegurado)
                                .compareTo(BigDecimal.ZERO) > 0
                );

        this.informacionRegistroCompleta =
                Boolean.TRUE.equals(garantiaConstituida)
                        && (
                        !Boolean.TRUE.equals(garantiaReal)
                                || Boolean.TRUE.equals(
                                garantiaRegistrada
                        )
                );

        this.informacionCoberturaCompleta =
                valorSeguro(valorCoberturaReconocido)
                        .compareTo(BigDecimal.ZERO) > 0
                        && saldoTotalCredito != null;

        int totalValidaciones = 5;
        int validacionesCompletas = 0;

        if (Boolean.TRUE.equals(
                informacionBienCompleta)) {

            validacionesCompletas++;
        }

        if (Boolean.TRUE.equals(
                informacionValoracionCompleta)) {

            validacionesCompletas++;
        }

        if (Boolean.TRUE.equals(
                informacionSeguroCompleta)) {

            validacionesCompletas++;
        }

        if (Boolean.TRUE.equals(
                informacionRegistroCompleta)) {

            validacionesCompletas++;
        }

        if (Boolean.TRUE.equals(
                informacionCoberturaCompleta)) {

            validacionesCompletas++;
        }

        this.porcentajeCompletitud =
                (validacionesCompletas * 100)
                        / totalValidaciones;

        this.informacionCompleta =
                validacionesCompletas
                        == totalValidaciones;

        this.documentacionCompleta =
                Boolean.TRUE.equals(
                        informacionRegistroCompleta
                )
                        && Boolean.TRUE.equals(
                        informacionValoracionCompleta
                )
                        && Boolean.TRUE.equals(
                        informacionSeguroCompleta
                );

        return this.porcentajeCompletitud;
    }

    // =========================================================
    // Estado general
    // =========================================================

    /**
     * Consolida cálculos, validaciones y alertas gerenciales.
     */
    public void evaluarEstadoGeneral(
            int vigenciaAvaluoMeses,
            int mesesAdvertenciaAvaluo,
            int diasAdvertenciaSeguro
    ) {

        clasificarGarantia();
        evaluarTitularidad();

        calcularValorNetoBien();
        calcularValorParticipacionAsociado();
        calcularValorAfectadoGarantia();
        calcularValorAdmisibleGarantia();

        evaluarAvaluo(
                vigenciaAvaluoMeses,
                mesesAdvertenciaAvaluo
        );

        evaluarSeguro(
                diasAdvertenciaSeguro
        );

        calcularCoberturaCredito();
        calcularUtilizacionCompartida();
        evaluarCompletitud();

        this.tieneGravamen =
                idTipoGravamen != null
                        && valorSeguro(valorGravamenBien)
                        .compareTo(BigDecimal.ZERO) > 0;

        int criticas = 0;
        int advertencias = 0;
        int informativas = 0;

        StringBuilder motivos =
                new StringBuilder();

        // -----------------------------------------------------
        // Alertas críticas
        // -----------------------------------------------------
        if (Boolean.TRUE.equals(
                coberturaInsuficiente)) {

            criticas++;
            agregarMotivo(
                    motivos,
                    "Cobertura insuficiente"
            );
        }

        if (Boolean.TRUE.equals(
                garantiaSobreutilizada)) {

            criticas++;
            agregarMotivo(
                    motivos,
                    "Garantía sobreutilizada"
            );
        }

        if (Boolean.TRUE.equals(
                gravamenSuperaValorBien)) {

            criticas++;
            agregarMotivo(
                    motivos,
                    "El gravamen supera el valor comercial"
            );
        }

        if (Boolean.TRUE.equals(
                seguroVencido)
                && Boolean.TRUE.equals(
                requiereSeguro)) {

            criticas++;
            agregarMotivo(
                    motivos,
                    "Seguro vencido"
            );
        }

        if (Boolean.TRUE.equals(
                avaluoVencido)
                && Boolean.TRUE.equals(
                garantiaReal)) {

            criticas++;
            agregarMotivo(
                    motivos,
                    "Avalúo vencido"
            );
        }

        // -----------------------------------------------------
        // Advertencias
        // -----------------------------------------------------
        if (Boolean.TRUE.equals(
                coberturaParcial)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Cobertura parcial"
            );
        }

        if (!Boolean.TRUE.equals(
                garantiaConstituida)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Garantía pendiente de constitución"
            );
        }

        if (Boolean.TRUE.equals(
                garantiaReal)
                && !Boolean.TRUE.equals(
                garantiaRegistrada)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Garantía pendiente de registro"
            );
        }

        if (Boolean.TRUE.equals(
                requiereSeguro)
                && !Boolean.TRUE.equals(
                tieneSeguro)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Garantía sin seguro"
            );
        }

        if (Boolean.TRUE.equals(
                seguroProximoVencer)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Seguro próximo a vencer"
            );
        }

        if (Boolean.TRUE.equals(
                requiereSeguro)
                && Boolean.TRUE.equals(
                tieneSeguro)
                && !Boolean.TRUE.equals(
                seguroSuficiente)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Valor asegurado insuficiente"
            );
        }

        if (Boolean.TRUE.equals(
                avaluoProximoVencer)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Avalúo próximo a vencer"
            );
        }

        if (Boolean.TRUE.equals(
                garantiaReal)
                && !Boolean.TRUE.equals(
                tieneAvaluo)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Garantía sin avalúo"
            );
        }

        if (!Boolean.TRUE.equals(
                informacionCompleta)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Información incompleta"
            );
        }

        if (!Boolean.TRUE.equals(
                titularGarantiaEsDeudor)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Garantía de un tercero"
            );
        }

        if (!Boolean.TRUE.equals(
                gravamenCompatibleGarantia)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Gravamen incompatible con la garantía"
            );
        }

        // -----------------------------------------------------
        // Alertas informativas
        // -----------------------------------------------------
        if (Boolean.TRUE.equals(
                propiedadCompartida)) {

            informativas++;
        }

        if (Boolean.TRUE.equals(
                garantiaCompartidaEntreCreditos)) {

            informativas++;
        }

        if (Boolean.TRUE.equals(
                tieneGravamen)) {

            informativas++;
        }

        if (Boolean.TRUE.equals(
                garantiaIdonea)) {

            informativas++;
        }

        this.alertasCriticas = criticas;
        this.alertasAdvertencia = advertencias;
        this.alertasInformativas = informativas;

        this.cantidadAlertas =
                criticas
                        + advertencias
                        + informativas;

        this.requiereActualizacion =
                Boolean.TRUE.equals(
                        avaluoVencido)
                        || Boolean.TRUE.equals(
                        seguroVencido)
                        || !Boolean.TRUE.equals(
                        informacionCompleta);

        this.requiereRevision =
                criticas > 0
                        || advertencias > 0;

        this.motivoRevision =
                motivos.toString();

        if (Boolean.TRUE.equals(liberada)) {

            this.vigente = Boolean.FALSE;
            this.codigoEstadoGarantia = "LIBERADA";
            this.nombreEstadoGarantia = "Liberada";

        } else if (Boolean.TRUE.equals(cancelada)) {

            this.vigente = Boolean.FALSE;
            this.codigoEstadoGarantia = "CANCELADA";
            this.nombreEstadoGarantia = "Cancelada";

        } else if (!Boolean.TRUE.equals(
                garantiaConstituida)) {

            this.vigente = Boolean.FALSE;
            this.pendienteConstitucion = Boolean.TRUE;
            this.codigoEstadoGarantia = "PENDIENTE";
            this.nombreEstadoGarantia =
                    "Pendiente de constitución";

        } else if (!Boolean.TRUE.equals(
                informacionCompleta)) {

            this.vigente = Boolean.FALSE;
            this.codigoEstadoGarantia = "INCOMPLETA";
            this.nombreEstadoGarantia =
                    "Información incompleta";

        } else {

            this.vigente = Boolean.TRUE;
            this.pendienteConstitucion = Boolean.FALSE;
            this.codigoEstadoGarantia = "VIGENTE";
            this.nombreEstadoGarantia = "Vigente";
        }

        if (criticas > 0) {

            this.nivelAlerta = "CRITICA";
            this.resumenAlertas =
                    "La garantía presenta situaciones de atención prioritaria.";

        } else if (advertencias > 0) {

            this.nivelAlerta = "ADVERTENCIA";
            this.resumenAlertas =
                    "La garantía requiere revisión o actualización.";

        } else if (informativas > 0) {

            this.nivelAlerta = "INFORMATIVA";
            this.resumenAlertas =
                    "La garantía presenta condiciones relevantes para consulta.";

        } else {

            this.nivelAlerta = "NORMAL";
            this.resumenAlertas =
                    "La garantía no presenta novedades gerenciales.";
        }
    }

    // =========================================================
    // Métodos privados
    // =========================================================

    private BigDecimal valorSeguro(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private BigDecimal normalizarPorcentaje(
            BigDecimal porcentaje
    ) {

        if (porcentaje == null) {
            return BigDecimal.ZERO;
        }

        if (porcentaje.compareTo(
                BigDecimal.ZERO) < 0) {

            return BigDecimal.ZERO;
        }

        if (porcentaje.compareTo(
                BigDecimal.valueOf(100)) > 0) {

            return BigDecimal.valueOf(100);
        }

        return porcentaje;
    }

    private boolean tieneTexto(
            String valor
    ) {

        return valor != null
                && !valor.isBlank();
    }

    private int convertirEnteroSeguro(
            long valor
    ) {

        if (valor > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        if (valor < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        return (int) valor;
    }

    private void agregarMotivo(
            StringBuilder resultado,
            String motivo
    ) {

        if (!tieneTexto(motivo)) {
            return;
        }

        if (!resultado.isEmpty()) {
            resultado.append("; ");
        }

        resultado.append(motivo);
    }
}