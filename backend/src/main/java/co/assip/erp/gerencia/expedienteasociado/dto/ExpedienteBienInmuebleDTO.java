package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
public class ExpedienteBienInmuebleDTO {

    // =========================================================
    // Relación persona ↔ bien
    // =========================================================
    private Long idBienPersona;
    private Long idDatosPersonal;

    private BigDecimal porcentajePropiedad;
    private BigDecimal valorParticipacion;

    private Boolean titularPrincipal;
    private Boolean propiedadCompartida;
    private Integer cantidadPropietarios;

    // =========================================================
    // Identificación del asociado
    // =========================================================
    private String tipoDocumento;
    private String nombreTipoDocumento;
    private String documento;
    private String nombreCompleto;

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
    private BigDecimal valorNeto;

    // =========================================================
    // Inmueble: hoja_vida.bienes_inmuebles
    // =========================================================
    private Long idBienInmueble;

    private Long idTipoInmueble;
    private String codigoTipoInmueble;
    private String nombreTipoInmueble;

    private Long idTipoZonaInmueble;
    private String codigoTipoZonaInmueble;
    private String nombreTipoZonaInmueble;

    // =========================================================
    // Identificación registral
    // =========================================================
    private String numeroMatriculaInmobiliaria;
    private String cedulaCatastral;

    private String numeroEscritura;
    private LocalDate fechaEscritura;
    private String notaria;

    // =========================================================
    // Ubicación
    // =========================================================
    private Long idPais;
    private String codigoPais;
    private String nombrePais;

    private Long idDepartamento;
    private String codigoDepartamento;
    private String nombreDepartamento;

    private Long idCiudad;
    private String codigoCiudad;
    private String nombreCiudad;

    private String direccion;
    private String barrioVereda;

    private String ubicacionCompleta;

    // =========================================================
    // Características físicas
    // =========================================================
    private BigDecimal areaTerreno;
    private BigDecimal areaConstruida;

    private String unidadAreaTerreno;
    private String unidadAreaConstruida;

    private Integer numeroHabitaciones;
    private Integer numeroBanos;
    private Integer numeroPisos;

    private Boolean tieneGaraje;
    private Integer numeroGarajes;

    private Boolean propiedadHorizontal;
    private String nombreConjuntoEdificio;

    private Integer estrato;

    // =========================================================
    // Gravamen principal
    // Regla del proyecto: un gravamen principal por bien.
    // =========================================================
    private Long idTipoGravamen;
    private String codigoTipoGravamen;
    private String nombreTipoGravamen;

    private Boolean tieneGravamen;
    private BigDecimal porcentajeGravamen;

    private Boolean gravamenSuperaValorComercial;
    private Boolean valorNetoNegativo;

    // =========================================================
    // Último avalúo
    // =========================================================
    private Long idBienInmuebleAvaluo;

    private LocalDate fechaAvaluo;
    private BigDecimal valorAvaluoComercial;
    private BigDecimal valorAvaluoCatastral;

    private String entidadAvaluadora;
    private String numeroAvaluo;
    private LocalDate fechaRegistroAvaluo;

    private String observacionesAvaluo;

    private Integer diasDesdeAvaluo;
    private Integer antiguedadAvaluoMeses;

    private Boolean tieneAvaluo;
    private Boolean avaluoVigente;
    private Boolean avaluoProximoVencer;
    private Boolean avaluoVencido;

    // =========================================================
    // Último seguro
    // =========================================================
    private Long idBienInmuebleSeguro;

    private String aseguradora;
    private String numeroPoliza;

    private BigDecimal valorAsegurado;

    private LocalDate fechaInicioSeguro;
    private LocalDate fechaVencimientoSeguro;

    private String estadoSeguro;
    private String observacionesSeguro;

    private Integer diasParaVencimientoSeguro;

    private Boolean tieneSeguro;
    private Boolean seguroVigente;
    private Boolean seguroProximoVencer;
    private Boolean seguroVencido;

    private BigDecimal porcentajeCoberturaSeguro;
    private Boolean seguroCubreValorComercial;

    // =========================================================
    // Relación como garantía de cartera
    // Se completará al integrar el módulo de Cartera.
    // =========================================================
    private Boolean vinculadoComoGarantia;
    private Integer cantidadCreditosGarantizados;

    private BigDecimal saldoCreditosGarantizados;
    private BigDecimal valorDisponibleGarantia;
    private BigDecimal porcentajeCoberturaGarantia;

    private Boolean garantiaSuficiente;
    private Boolean garantiaSobreutilizada;

    // =========================================================
    // Calidad de la información
    // =========================================================
    private Boolean informacionRegistralCompleta;
    private Boolean informacionUbicacionCompleta;
    private Boolean informacionValoracionCompleta;
    private Boolean informacionSeguroCompleta;

    private Boolean informacionCompleta;
    private Integer porcentajeCompletitud;

    // =========================================================
    // Estado general del bien
    // =========================================================
    private String estadoBien;
    private Boolean activo;

    private Boolean tieneValorComercial;
    private Boolean requiereActualizacion;
    private Boolean requiereRevision;

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
    private LocalDate fechaCreacion;

    private Integer fkSeguridadEdicion;
    private LocalDate fechaEdicion;

    // =========================================================
    // Constructor
    // =========================================================
    public ExpedienteBienInmuebleDTO() {

        this.porcentajePropiedad = BigDecimal.valueOf(100);
        this.valorParticipacion = BigDecimal.ZERO;

        this.titularPrincipal = Boolean.FALSE;
        this.propiedadCompartida = Boolean.FALSE;
        this.cantidadPropietarios = 1;

        this.valorComercial = BigDecimal.ZERO;
        this.valorGravamen = BigDecimal.ZERO;
        this.valorNeto = BigDecimal.ZERO;

        this.areaTerreno = BigDecimal.ZERO;
        this.areaConstruida = BigDecimal.ZERO;

        this.numeroHabitaciones = 0;
        this.numeroBanos = 0;
        this.numeroPisos = 0;
        this.numeroGarajes = 0;

        this.tieneGaraje = Boolean.FALSE;
        this.propiedadHorizontal = Boolean.FALSE;

        this.tieneGravamen = Boolean.FALSE;
        this.porcentajeGravamen = BigDecimal.ZERO;
        this.gravamenSuperaValorComercial = Boolean.FALSE;
        this.valorNetoNegativo = Boolean.FALSE;

        this.valorAvaluoComercial = BigDecimal.ZERO;
        this.valorAvaluoCatastral = BigDecimal.ZERO;

        this.diasDesdeAvaluo = 0;
        this.antiguedadAvaluoMeses = 0;

        this.tieneAvaluo = Boolean.FALSE;
        this.avaluoVigente = Boolean.FALSE;
        this.avaluoProximoVencer = Boolean.FALSE;
        this.avaluoVencido = Boolean.FALSE;

        this.valorAsegurado = BigDecimal.ZERO;
        this.diasParaVencimientoSeguro = 0;

        this.tieneSeguro = Boolean.FALSE;
        this.seguroVigente = Boolean.FALSE;
        this.seguroProximoVencer = Boolean.FALSE;
        this.seguroVencido = Boolean.FALSE;

        this.porcentajeCoberturaSeguro = BigDecimal.ZERO;
        this.seguroCubreValorComercial = Boolean.FALSE;

        this.vinculadoComoGarantia = Boolean.FALSE;
        this.cantidadCreditosGarantizados = 0;

        this.saldoCreditosGarantizados = BigDecimal.ZERO;
        this.valorDisponibleGarantia = BigDecimal.ZERO;
        this.porcentajeCoberturaGarantia = BigDecimal.ZERO;

        this.garantiaSuficiente = Boolean.FALSE;
        this.garantiaSobreutilizada = Boolean.FALSE;

        this.informacionRegistralCompleta = Boolean.FALSE;
        this.informacionUbicacionCompleta = Boolean.FALSE;
        this.informacionValoracionCompleta = Boolean.FALSE;
        this.informacionSeguroCompleta = Boolean.FALSE;
        this.informacionCompleta = Boolean.FALSE;

        this.porcentajeCompletitud = 0;

        this.activo = Boolean.TRUE;
        this.tieneValorComercial = Boolean.FALSE;
        this.requiereActualizacion = Boolean.FALSE;
        this.requiereRevision = Boolean.FALSE;

        this.cantidadAlertas = 0;
        this.alertasCriticas = 0;
        this.alertasAdvertencia = 0;
        this.alertasInformativas = 0;
    }

    // =========================================================
    // Métodos auxiliares
    // =========================================================

    /**
     * Calcula el valor correspondiente a la participación del
     * asociado sobre el valor comercial total del inmueble.
     */
    public BigDecimal calcularValorParticipacion() {

        BigDecimal porcentaje =
                valorSeguro(porcentajePropiedad);

        this.valorParticipacion =
                valorSeguro(valorComercial)
                        .multiply(porcentaje)
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );

        return this.valorParticipacion;
    }

    /**
     * Calcula el valor neto del inmueble después del gravamen.
     */
    public BigDecimal calcularValorNeto() {

        this.valorNeto =
                valorSeguro(valorComercial)
                        .subtract(valorSeguro(valorGravamen));

        this.valorNetoNegativo =
                this.valorNeto.compareTo(BigDecimal.ZERO) < 0;

        this.gravamenSuperaValorComercial =
                valorSeguro(valorGravamen)
                        .compareTo(valorSeguro(valorComercial)) > 0;

        return this.valorNeto;
    }

    /**
     * Calcula el porcentaje que representa el gravamen frente al
     * valor comercial registrado.
     */
    public BigDecimal calcularPorcentajeGravamen() {

        BigDecimal comercial =
                valorSeguro(valorComercial);

        if (comercial.compareTo(BigDecimal.ZERO) <= 0) {
            this.porcentajeGravamen = BigDecimal.ZERO;
            return this.porcentajeGravamen;
        }

        this.porcentajeGravamen =
                valorSeguro(valorGravamen)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(comercial, 2, RoundingMode.HALF_UP);

        return this.porcentajeGravamen;
    }

    /**
     * Evalúa la propiedad compartida.
     */
    public void evaluarTitularidad() {

        BigDecimal porcentaje =
                valorSeguro(porcentajePropiedad);

        this.propiedadCompartida =
                porcentaje.compareTo(BigDecimal.valueOf(100)) < 0
                        || (cantidadPropietarios != null
                        && cantidadPropietarios > 1);

        this.titularPrincipal =
                porcentaje.compareTo(BigDecimal.valueOf(50)) >= 0;
    }

    /**
     * Evalúa la vigencia del último avalúo.
     *
     * La vigencia se recibe como parámetro para no fijar una regla
     * institucional dentro del DTO.
     */
    public void evaluarAvaluo(int vigenciaMeses, int mesesAdvertencia) {

        this.tieneAvaluo =
                fechaAvaluo != null
                        || valorSeguro(valorAvaluoComercial)
                        .compareTo(BigDecimal.ZERO) > 0;

        this.avaluoVigente = Boolean.FALSE;
        this.avaluoProximoVencer = Boolean.FALSE;
        this.avaluoVencido = Boolean.FALSE;

        if (fechaAvaluo == null) {
            this.diasDesdeAvaluo = 0;
            this.antiguedadAvaluoMeses = 0;
            return;
        }

        LocalDate hoy = LocalDate.now();

        long dias =
                ChronoUnit.DAYS.between(fechaAvaluo, hoy);

        long meses =
                ChronoUnit.MONTHS.between(
                        fechaAvaluo.withDayOfMonth(1),
                        hoy.withDayOfMonth(1)
                );

        this.diasDesdeAvaluo =
                convertirEnteroSeguro(dias);

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

            this.avaluoProximoVencer = Boolean.TRUE;
            this.avaluoVigente = Boolean.TRUE;

        } else {

            this.avaluoVigente = Boolean.TRUE;
        }
    }

    /**
     * Evalúa la vigencia del último seguro.
     */
    public void evaluarSeguro(int diasAdvertencia) {

        this.tieneSeguro =
                tieneTexto(numeroPoliza)
                        || tieneTexto(aseguradora)
                        || fechaVencimientoSeguro != null
                        || valorSeguro(valorAsegurado)
                        .compareTo(BigDecimal.ZERO) > 0;

        this.seguroVigente = Boolean.FALSE;
        this.seguroProximoVencer = Boolean.FALSE;
        this.seguroVencido = Boolean.FALSE;

        if (fechaVencimientoSeguro == null) {
            this.diasParaVencimientoSeguro = 0;
            calcularCoberturaSeguro();
            return;
        }

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

            this.seguroProximoVencer = Boolean.TRUE;
            this.seguroVigente = Boolean.TRUE;

        } else {

            this.seguroVigente = Boolean.TRUE;
        }

        calcularCoberturaSeguro();
    }

    /**
     * Calcula el porcentaje asegurado frente al valor comercial.
     */
    public BigDecimal calcularCoberturaSeguro() {

        BigDecimal comercial =
                valorSeguro(valorComercial);

        if (comercial.compareTo(BigDecimal.ZERO) <= 0) {
            this.porcentajeCoberturaSeguro = BigDecimal.ZERO;
            this.seguroCubreValorComercial = Boolean.FALSE;
            return this.porcentajeCoberturaSeguro;
        }

        this.porcentajeCoberturaSeguro =
                valorSeguro(valorAsegurado)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(comercial, 2, RoundingMode.HALF_UP);

        this.seguroCubreValorComercial =
                valorSeguro(valorAsegurado)
                        .compareTo(comercial) >= 0;

        return this.porcentajeCoberturaSeguro;
    }

    /**
     * Calcula la cobertura del inmueble frente a los créditos que
     * garantiza.
     */
    public BigDecimal calcularCoberturaGarantia() {

        BigDecimal saldo =
                valorSeguro(saldoCreditosGarantizados);

        BigDecimal disponible =
                valorSeguro(valorNeto)
                        .max(BigDecimal.ZERO);

        this.valorDisponibleGarantia =
                disponible.subtract(saldo);

        this.garantiaSobreutilizada =
                this.valorDisponibleGarantia
                        .compareTo(BigDecimal.ZERO) < 0;

        if (saldo.compareTo(BigDecimal.ZERO) <= 0) {

            this.porcentajeCoberturaGarantia = BigDecimal.ZERO;
            this.garantiaSuficiente = Boolean.TRUE;

            return this.porcentajeCoberturaGarantia;
        }

        this.porcentajeCoberturaGarantia =
                disponible
                        .multiply(BigDecimal.valueOf(100))
                        .divide(saldo, 2, RoundingMode.HALF_UP);

        this.garantiaSuficiente =
                disponible.compareTo(saldo) >= 0;

        return this.porcentajeCoberturaGarantia;
    }

    /**
     * Construye una descripción compacta de la ubicación.
     */
    public String construirUbicacionCompleta() {

        StringBuilder ubicacion =
                new StringBuilder();

        agregarParte(ubicacion, direccion);
        agregarParte(ubicacion, barrioVereda);
        agregarParte(ubicacion, nombreCiudad);
        agregarParte(ubicacion, nombreDepartamento);
        agregarParte(ubicacion, nombrePais);

        this.ubicacionCompleta =
                ubicacion.toString();

        return this.ubicacionCompleta;
    }

    /**
     * Evalúa la completitud de los datos relevantes del inmueble.
     */
    public int evaluarCompletitud() {

        this.informacionRegistralCompleta =
                tieneTexto(numeroMatriculaInmobiliaria)
                        || tieneTexto(cedulaCatastral);

        this.informacionUbicacionCompleta =
                tieneTexto(direccion)
                        && idCiudad != null;

        this.informacionValoracionCompleta =
                valorSeguro(valorComercial)
                        .compareTo(BigDecimal.ZERO) > 0;

        this.informacionSeguroCompleta =
                !Boolean.TRUE.equals(tieneSeguro)
                        || (
                        tieneTexto(aseguradora)
                                && tieneTexto(numeroPoliza)
                                && fechaVencimientoSeguro != null
                                && valorSeguro(valorAsegurado)
                                .compareTo(BigDecimal.ZERO) > 0
                );

        int total = 4;
        int completos = 0;

        if (Boolean.TRUE.equals(informacionRegistralCompleta)) {
            completos++;
        }

        if (Boolean.TRUE.equals(informacionUbicacionCompleta)) {
            completos++;
        }

        if (Boolean.TRUE.equals(informacionValoracionCompleta)) {
            completos++;
        }

        if (Boolean.TRUE.equals(informacionSeguroCompleta)) {
            completos++;
        }

        this.porcentajeCompletitud =
                (completos * 100) / total;

        this.informacionCompleta =
                completos == total;

        return this.porcentajeCompletitud;
    }

    /**
     * Consolida los indicadores y alertas gerenciales del inmueble.
     */
    public void evaluarEstadoGeneral(
            int vigenciaAvaluoMeses,
            int mesesAdvertenciaAvaluo,
            int diasAdvertenciaSeguro
    ) {

        calcularValorParticipacion();
        calcularValorNeto();
        calcularPorcentajeGravamen();

        evaluarTitularidad();
        evaluarAvaluo(
                vigenciaAvaluoMeses,
                mesesAdvertenciaAvaluo
        );
        evaluarSeguro(diasAdvertenciaSeguro);
        calcularCoberturaGarantia();
        construirUbicacionCompleta();
        evaluarCompletitud();

        this.tieneValorComercial =
                valorSeguro(valorComercial)
                        .compareTo(BigDecimal.ZERO) > 0;

        this.tieneGravamen =
                idTipoGravamen != null
                        && valorSeguro(valorGravamen)
                        .compareTo(BigDecimal.ZERO) > 0;

        int criticas = 0;
        int advertencias = 0;
        int informativas = 0;

        // -----------------------------------------------------
        // Alertas críticas
        // -----------------------------------------------------
        if (Boolean.TRUE.equals(seguroVencido)
                && Boolean.TRUE.equals(vinculadoComoGarantia)) {
            criticas++;
        }

        if (Boolean.TRUE.equals(avaluoVencido)
                && Boolean.TRUE.equals(vinculadoComoGarantia)) {
            criticas++;
        }

        if (Boolean.TRUE.equals(gravamenSuperaValorComercial)) {
            criticas++;
        }

        if (Boolean.TRUE.equals(garantiaSobreutilizada)) {
            criticas++;
        }

        // -----------------------------------------------------
        // Advertencias
        // -----------------------------------------------------
        if (!Boolean.TRUE.equals(tieneValorComercial)) {
            advertencias++;
        }

        if (Boolean.TRUE.equals(seguroVencido)
                && !Boolean.TRUE.equals(vinculadoComoGarantia)) {
            advertencias++;
        }

        if (Boolean.TRUE.equals(seguroProximoVencer)) {
            advertencias++;
        }

        if (Boolean.TRUE.equals(avaluoVencido)
                && !Boolean.TRUE.equals(vinculadoComoGarantia)) {
            advertencias++;
        }

        if (Boolean.TRUE.equals(avaluoProximoVencer)) {
            advertencias++;
        }

        if (!Boolean.TRUE.equals(informacionCompleta)) {
            advertencias++;
        }

        if (Boolean.TRUE.equals(vinculadoComoGarantia)
                && !Boolean.TRUE.equals(tieneSeguro)) {
            advertencias++;
        }

        if (Boolean.TRUE.equals(vinculadoComoGarantia)
                && !Boolean.TRUE.equals(tieneAvaluo)) {
            advertencias++;
        }

        if (Boolean.TRUE.equals(vinculadoComoGarantia)
                && !Boolean.TRUE.equals(garantiaSuficiente)) {
            advertencias++;
        }

        // -----------------------------------------------------
        // Informativas
        // -----------------------------------------------------
        if (Boolean.TRUE.equals(propiedadCompartida)) {
            informativas++;
        }

        if (Boolean.TRUE.equals(tieneGravamen)) {
            informativas++;
        }

        if (Boolean.TRUE.equals(vinculadoComoGarantia)) {
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
                Boolean.TRUE.equals(avaluoVencido)
                        || Boolean.TRUE.equals(seguroVencido)
                        || !Boolean.TRUE.equals(informacionCompleta);

        this.requiereRevision =
                criticas > 0
                        || advertencias > 0;

        if (criticas > 0) {

            this.nivelAlerta = "CRITICA";
            this.resumenAlertas =
                    "El inmueble presenta situaciones de atención prioritaria.";

        } else if (advertencias > 0) {

            this.nivelAlerta = "ADVERTENCIA";
            this.resumenAlertas =
                    "El inmueble requiere revisión o actualización.";

        } else if (informativas > 0) {

            this.nivelAlerta = "INFORMATIVA";
            this.resumenAlertas =
                    "El inmueble presenta condiciones relevantes para consulta.";

        } else {

            this.nivelAlerta = "NORMAL";
            this.resumenAlertas =
                    "El inmueble no presenta novedades gerenciales.";
        }
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private boolean tieneTexto(String valor) {
        return valor != null
                && !valor.isBlank();
    }

    private int convertirEnteroSeguro(long valor) {

        if (valor > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        if (valor < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        return (int) valor;
    }

    private void agregarParte(
            StringBuilder resultado,
            String valor
    ) {

        if (!tieneTexto(valor)) {
            return;
        }

        if (!resultado.isEmpty()) {
            resultado.append(", ");
        }

        resultado.append(valor.trim());
    }
}