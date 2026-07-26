package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
public class ExpedienteAlertaDTO {

    // =========================================================
    // Identificación de la alerta
    // =========================================================
    private Long idAlerta;

    /*
     * Código funcional único de la alerta.
     *
     * Ejemplos:
     * CREDITO_MORA_GRAVE
     * GARANTIA_SEGURO_VENCIDO
     * SARLAFT_ACTUALIZACION_VENCIDA
     * CONTACTO_INCOMPLETO
     */
    private String codigoAlerta;

    private String titulo;
    private String descripcion;

    // =========================================================
    // Asociado
    // =========================================================
    private Long idDatosPersonal;

    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    // =========================================================
    // Módulo y origen
    // =========================================================

    /*
     * Módulos esperados:
     *
     * HOJA_VIDA
     * AFILIACION
     * CONTACTO
     * INFORMACION_FINANCIERA
     * SARLAFT
     * DEPOSITOS
     * CDAT
     * CARTERA
     * BIENES
     * GARANTIAS
     */
    private String modulo;

    /*
     * Submódulo o proceso específico.
     *
     * Ejemplos:
     * CUENTAS_AHORRO
     * CREDITO
     * INMUEBLE
     * VEHICULO
     * SEGURO
     * AVALUO
     */
    private String submodulo;

    private String procesoOrigen;
    private String tablaOrigen;
    private String vistaOrigen;

    // =========================================================
    // Registro relacionado
    // =========================================================
    private Long idRegistroOrigen;

    private Long idCredito;
    private Long idCuentaAhorro;
    private Long idCdat;
    private Long idBien;
    private Long idGarantia;

    private String referenciaRegistro;
    private String descripcionRegistro;

    // =========================================================
    // Clasificación
    // =========================================================

    /*
     * Niveles gerenciales:
     *
     * CRITICA
     * ADVERTENCIA
     * INFORMATIVA
     * NORMAL
     */
    private String nivel;

    /*
     * Prioridades:
     *
     * ALTA
     * MEDIA
     * BAJA
     */
    private String prioridad;

    /*
     * Tipos:
     *
     * RIESGO
     * VENCIMIENTO
     * DOCUMENTACION
     * CUMPLIMIENTO
     * FINANCIERA
     * OPERATIVA
     * COBRANZA
     * GARANTIA
     * ACTUALIZACION
     * INFORMATIVA
     */
    private String tipoAlerta;

    private String categoria;

    // =========================================================
    // Estado
    // =========================================================

    /*
     * Estados esperados:
     *
     * ABIERTA
     * EN_GESTION
     * ATENDIDA
     * DESCARTADA
     * VENCIDA
     */
    private String codigoEstado;
    private String nombreEstado;

    private Boolean activa;
    private Boolean abierta;
    private Boolean enGestion;
    private Boolean atendida;
    private Boolean descartada;
    private Boolean vencida;

    // =========================================================
    // Fechas
    // =========================================================
    private LocalDate fechaEvento;
    private LocalDate fechaVencimiento;

    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaPrimeraDeteccion;
    private LocalDateTime fechaUltimaDeteccion;

    private LocalDateTime fechaInicioGestion;
    private LocalDateTime fechaAtencion;
    private LocalDateTime fechaCierre;

    private Integer diasDesdeDeteccion;
    private Integer diasParaVencimiento;
    private Integer diasVencida;

    // =========================================================
    // Gestión
    // =========================================================
    private Integer idUsuarioResponsable;
    private String nombreUsuarioResponsable;

    private Long idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    private String accionRequerida;
    private String gestionRealizada;
    private String resultadoGestion;

    private String observacionesGestion;

    private Boolean requiereGestion;
    private Boolean requiereSeguimiento;
    private Boolean requiereEscalamiento;

    // =========================================================
    // Indicadores
    // =========================================================
    private Boolean bloqueante;
    private Boolean reincidente;

    private Integer cantidadReincidencias;
    private Integer cantidadDiasAbierta;

    private Integer ordenVisual;

    private String icono;
    private String color;

    // =========================================================
    // Navegación en el frontend
    // =========================================================
    private String rutaFrontend;
    private String parametroRuta;
    private String etiquetaAccion;

    private Boolean permiteGestionar;
    private Boolean permiteDescartar;
    private Boolean permiteCerrar;

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
    public ExpedienteAlertaDTO() {

        this.nivel = "INFORMATIVA";
        this.prioridad = "BAJA";
        this.tipoAlerta = "INFORMATIVA";

        this.codigoEstado = "ABIERTA";
        this.nombreEstado = "Abierta";

        this.activa = Boolean.TRUE;
        this.abierta = Boolean.TRUE;
        this.enGestion = Boolean.FALSE;
        this.atendida = Boolean.FALSE;
        this.descartada = Boolean.FALSE;
        this.vencida = Boolean.FALSE;

        this.fechaGeneracion = LocalDateTime.now();
        this.fechaPrimeraDeteccion = LocalDateTime.now();
        this.fechaUltimaDeteccion = LocalDateTime.now();

        this.diasDesdeDeteccion = 0;
        this.diasParaVencimiento = 0;
        this.diasVencida = 0;

        this.requiereGestion = Boolean.FALSE;
        this.requiereSeguimiento = Boolean.FALSE;
        this.requiereEscalamiento = Boolean.FALSE;

        this.bloqueante = Boolean.FALSE;
        this.reincidente = Boolean.FALSE;

        this.cantidadReincidencias = 0;
        this.cantidadDiasAbierta = 0;

        this.ordenVisual = 30;

        this.icono = "info";
        this.color = "AZUL";

        this.permiteGestionar = Boolean.TRUE;
        this.permiteDescartar = Boolean.TRUE;
        this.permiteCerrar = Boolean.TRUE;
    }

    // =========================================================
    // Clasificación
    // =========================================================

    /**
     * Clasifica automáticamente prioridad, color, icono y orden
     * de acuerdo con el nivel gerencial.
     */
    public void clasificarNivel() {

        String nivelNormalizado =
                normalizarCodigo(nivel);

        switch (nivelNormalizado) {

            case "CRITICA" -> {

                this.nivel = "CRITICA";
                this.prioridad = "ALTA";

                this.color = "ROJO";
                this.icono = "error";

                this.ordenVisual = 10;

                this.requiereGestion = Boolean.TRUE;
                this.requiereSeguimiento = Boolean.TRUE;
                this.requiereEscalamiento = Boolean.TRUE;
            }

            case "ADVERTENCIA" -> {

                this.nivel = "ADVERTENCIA";
                this.prioridad = "MEDIA";

                this.color = "AMARILLO";
                this.icono = "warning";

                this.ordenVisual = 20;

                this.requiereGestion = Boolean.TRUE;
                this.requiereSeguimiento = Boolean.TRUE;
                this.requiereEscalamiento = Boolean.FALSE;
            }

            case "NORMAL" -> {

                this.nivel = "NORMAL";
                this.prioridad = "BAJA";

                this.color = "VERDE";
                this.icono = "check_circle";

                this.ordenVisual = 40;

                this.requiereGestion = Boolean.FALSE;
                this.requiereSeguimiento = Boolean.FALSE;
                this.requiereEscalamiento = Boolean.FALSE;
            }

            default -> {

                this.nivel = "INFORMATIVA";
                this.prioridad = "BAJA";

                this.color = "AZUL";
                this.icono = "info";

                this.ordenVisual = 30;

                this.requiereGestion = Boolean.FALSE;
                this.requiereSeguimiento = Boolean.FALSE;
                this.requiereEscalamiento = Boolean.FALSE;
            }
        }
    }

    // =========================================================
    // Estado
    // =========================================================

    /**
     * Actualiza los indicadores booleanos según el estado.
     */
    public void evaluarEstado() {

        String estado =
                normalizarCodigo(codigoEstado);

        this.abierta = Boolean.FALSE;
        this.enGestion = Boolean.FALSE;
        this.atendida = Boolean.FALSE;
        this.descartada = Boolean.FALSE;

        switch (estado) {

            case "EN_GESTION" -> {

                this.codigoEstado = "EN_GESTION";
                this.nombreEstado = "En gestión";

                this.enGestion = Boolean.TRUE;
                this.activa = Boolean.TRUE;
            }

            case "ATENDIDA" -> {

                this.codigoEstado = "ATENDIDA";
                this.nombreEstado = "Atendida";

                this.atendida = Boolean.TRUE;
                this.activa = Boolean.FALSE;
            }

            case "DESCARTADA" -> {

                this.codigoEstado = "DESCARTADA";
                this.nombreEstado = "Descartada";

                this.descartada = Boolean.TRUE;
                this.activa = Boolean.FALSE;
            }

            case "VENCIDA" -> {

                this.codigoEstado = "VENCIDA";
                this.nombreEstado = "Vencida";

                this.vencida = Boolean.TRUE;
                this.activa = Boolean.TRUE;
            }

            default -> {

                this.codigoEstado = "ABIERTA";
                this.nombreEstado = "Abierta";

                this.abierta = Boolean.TRUE;
                this.activa = Boolean.TRUE;
            }
        }

        actualizarPermisosGestion();
    }

    /**
     * Marca la alerta como iniciada en gestión.
     */
    public void iniciarGestion(
            Integer idUsuario,
            String nombreUsuario
    ) {

        this.idUsuarioResponsable = idUsuario;
        this.nombreUsuarioResponsable = nombreUsuario;

        this.codigoEstado = "EN_GESTION";
        this.nombreEstado = "En gestión";

        this.fechaInicioGestion = LocalDateTime.now();

        this.activa = Boolean.TRUE;
        this.abierta = Boolean.FALSE;
        this.enGestion = Boolean.TRUE;
        this.atendida = Boolean.FALSE;
        this.descartada = Boolean.FALSE;

        actualizarPermisosGestion();
    }

    /**
     * Marca la alerta como atendida.
     */
    public void marcarAtendida(
            String gestion,
            String resultado
    ) {

        this.gestionRealizada = gestion;
        this.resultadoGestion = resultado;

        this.codigoEstado = "ATENDIDA";
        this.nombreEstado = "Atendida";

        this.fechaAtencion = LocalDateTime.now();
        this.fechaCierre = LocalDateTime.now();

        this.activa = Boolean.FALSE;
        this.abierta = Boolean.FALSE;
        this.enGestion = Boolean.FALSE;
        this.atendida = Boolean.TRUE;
        this.descartada = Boolean.FALSE;
        this.vencida = Boolean.FALSE;

        actualizarPermisosGestion();
    }

    /**
     * Descarta una alerta cuando se determina que no aplica.
     */
    public void marcarDescartada(
            String observacion
    ) {

        this.observacionesGestion = observacion;

        this.codigoEstado = "DESCARTADA";
        this.nombreEstado = "Descartada";

        this.fechaCierre = LocalDateTime.now();

        this.activa = Boolean.FALSE;
        this.abierta = Boolean.FALSE;
        this.enGestion = Boolean.FALSE;
        this.atendida = Boolean.FALSE;
        this.descartada = Boolean.TRUE;
        this.vencida = Boolean.FALSE;

        actualizarPermisosGestion();
    }

    // =========================================================
    // Fechas
    // =========================================================

    /**
     * Calcula antigüedad, vencimiento y días abiertos.
     */
    public void calcularIndicadoresFecha() {

        LocalDate hoy =
                LocalDate.now();

        LocalDate fechaBaseDeteccion =
                obtenerFechaDeteccion();

        if (fechaBaseDeteccion != null) {

            long dias =
                    ChronoUnit.DAYS.between(
                            fechaBaseDeteccion,
                            hoy
                    );

            this.diasDesdeDeteccion =
                    convertirEnteroSeguro(
                            Math.max(dias, 0)
                    );
        }

        if (fechaVencimiento != null) {

            long dias =
                    ChronoUnit.DAYS.between(
                            hoy,
                            fechaVencimiento
                    );

            this.diasParaVencimiento =
                    convertirEnteroSeguro(dias);

            if (dias < 0
                    && Boolean.TRUE.equals(activa)) {

                this.vencida = Boolean.TRUE;

                this.diasVencida =
                        convertirEnteroSeguro(
                                Math.abs(dias)
                        );

            } else {

                this.vencida = Boolean.FALSE;
                this.diasVencida = 0;
            }
        }

        if (Boolean.TRUE.equals(activa)
                && fechaBaseDeteccion != null) {

            long diasAbierta =
                    ChronoUnit.DAYS.between(
                            fechaBaseDeteccion,
                            hoy
                    );

            this.cantidadDiasAbierta =
                    convertirEnteroSeguro(
                            Math.max(diasAbierta, 0)
                    );

        } else if (fechaBaseDeteccion != null
                && fechaCierre != null) {

            long diasAbierta =
                    ChronoUnit.DAYS.between(
                            fechaBaseDeteccion,
                            fechaCierre.toLocalDate()
                    );

            this.cantidadDiasAbierta =
                    convertirEnteroSeguro(
                            Math.max(diasAbierta, 0)
                    );
        }

        if (Boolean.TRUE.equals(vencida)
                && !Boolean.TRUE.equals(atendida)
                && !Boolean.TRUE.equals(descartada)) {

            this.codigoEstado = "VENCIDA";
            this.nombreEstado = "Vencida";

            this.abierta = Boolean.FALSE;
            this.enGestion = Boolean.FALSE;
            this.activa = Boolean.TRUE;
        }
    }

    // =========================================================
    // Reincidencia
    // =========================================================

    /**
     * Evalúa si la alerta es reincidente.
     */
    public void evaluarReincidencia() {

        int reincidencias =
                cantidadReincidencias == null
                        ? 0
                        : Math.max(
                        cantidadReincidencias,
                        0
                );

        this.cantidadReincidencias =
                reincidencias;

        this.reincidente =
                reincidencias > 0;

        if (reincidencias >= 3
                && "ADVERTENCIA".equals(
                normalizarCodigo(nivel))) {

            this.nivel = "CRITICA";
        }
    }

    // =========================================================
    // Navegación
    // =========================================================

    /**
     * Genera la ruta base del frontend de acuerdo con el módulo.
     */
    public void construirRutaFrontend() {

        String moduloNormalizado =
                normalizarCodigo(modulo);

        switch (moduloNormalizado) {

            case "HOJA_VIDA",
                 "AFILIACION",
                 "CONTACTO",
                 "INFORMACION_FINANCIERA",
                 "SARLAFT" -> {

                this.rutaFrontend =
                        "/hoja-vida/datos-personales/"
                                + valorRuta(idDatosPersonal);

                this.etiquetaAccion =
                        "Consultar hoja de vida";
            }

            case "DEPOSITOS" -> {

                this.rutaFrontend =
                        "/depositos/cuentas-ahorro";

                this.parametroRuta =
                        idCuentaAhorro == null
                                ? null
                                : idCuentaAhorro.toString();

                this.etiquetaAccion =
                        "Consultar cuenta";
            }

            case "CDAT" -> {

                this.rutaFrontend =
                        "/cdat";

                this.parametroRuta =
                        idCdat == null
                                ? null
                                : idCdat.toString();

                this.etiquetaAccion =
                        "Consultar CDAT";
            }

            case "CARTERA" -> {

                this.rutaFrontend =
                        "/cartera/consulta-creditos";

                this.parametroRuta =
                        idCredito == null
                                ? null
                                : idCredito.toString();

                this.etiquetaAccion =
                        "Consultar crédito";
            }

            case "BIENES" -> {

                this.rutaFrontend =
                        "/hoja-vida/bienes";

                this.parametroRuta =
                        idBien == null
                                ? null
                                : idBien.toString();

                this.etiquetaAccion =
                        "Consultar bien";
            }

            case "GARANTIAS" -> {

                this.rutaFrontend =
                        "/cartera/garantias";

                this.parametroRuta =
                        idGarantia == null
                                ? null
                                : idGarantia.toString();

                this.etiquetaAccion =
                        "Consultar garantía";
            }

            default -> {

                this.rutaFrontend =
                        "/gerencia/expediente-asociado/"
                                + valorRuta(idDatosPersonal);

                this.etiquetaAccion =
                        "Consultar expediente";
            }
        }
    }

    // =========================================================
    // Evaluación general
    // =========================================================

    /**
     * Consolida nivel, estado, fechas, reincidencia y navegación.
     */
    public void evaluarEstadoGeneral() {

        evaluarReincidencia();
        clasificarNivel();
        evaluarEstado();
        calcularIndicadoresFecha();

        if (Boolean.TRUE.equals(vencida)
                && Boolean.TRUE.equals(activa)) {

            this.codigoEstado = "VENCIDA";
            this.nombreEstado = "Vencida";

            if ("INFORMATIVA".equals(
                    normalizarCodigo(nivel))) {

                this.nivel = "ADVERTENCIA";
            }
        }

        if (Boolean.TRUE.equals(bloqueante)) {

            this.nivel = "CRITICA";
            this.prioridad = "ALTA";

            this.requiereGestion = Boolean.TRUE;
            this.requiereSeguimiento = Boolean.TRUE;
            this.requiereEscalamiento = Boolean.TRUE;
        }

        clasificarNivel();
        construirRutaFrontend();
        actualizarPermisosGestion();
    }

    // =========================================================
    // Métodos de utilidad
    // =========================================================

    public boolean esCritica() {

        return "CRITICA".equals(
                normalizarCodigo(nivel)
        );
    }

    public boolean esAdvertencia() {

        return "ADVERTENCIA".equals(
                normalizarCodigo(nivel)
        );
    }

    public boolean esInformativa() {

        return "INFORMATIVA".equals(
                normalizarCodigo(nivel)
        );
    }

    public boolean estaPendiente() {

        return Boolean.TRUE.equals(activa)
                && !Boolean.TRUE.equals(atendida)
                && !Boolean.TRUE.equals(descartada);
    }

    // =========================================================
    // Métodos privados
    // =========================================================

    private void actualizarPermisosGestion() {

        boolean cerrada =
                Boolean.TRUE.equals(atendida)
                        || Boolean.TRUE.equals(descartada);

        this.permiteGestionar =
                !cerrada;

        this.permiteDescartar =
                !cerrada
                        && !Boolean.TRUE.equals(bloqueante);

        this.permiteCerrar =
                !cerrada;
    }

    private LocalDate obtenerFechaDeteccion() {

        if (fechaPrimeraDeteccion != null) {
            return fechaPrimeraDeteccion.toLocalDate();
        }

        if (fechaGeneracion != null) {
            return fechaGeneracion.toLocalDate();
        }

        return fechaEvento;
    }

    private String normalizarCodigo(
            String valor
    ) {

        if (valor == null
                || valor.isBlank()) {

            return "";
        }

        return valor
                .trim()
                .toUpperCase()
                .replace(' ', '_')
                .replace('-', '_');
    }

    private String valorRuta(
            Long valor
    ) {

        return valor == null
                ? ""
                : valor.toString();
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
}