package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ExpedienteInformacionFinancieraDTO {

    // =========================================================
    // Identificación
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
    // Ingresos
    // =========================================================

    private BigDecimal ingresosLaborales;
    private BigDecimal ingresosHonorarios;
    private BigDecimal ingresosPensiones;
    private BigDecimal ingresosArrendamientos;
    private BigDecimal ingresosFinancieros;
    private BigDecimal otrosIngresos;

    private BigDecimal totalIngresos;

    // =========================================================
    // Egresos
    // =========================================================

    private BigDecimal gastosPersonales;
    private BigDecimal gastosFamiliares;
    private BigDecimal gastosFinancieros;
    private BigDecimal otrosEgresos;

    private BigDecimal totalEgresos;

    // =========================================================
    // Activos
    // =========================================================

    private BigDecimal efectivo;

    private BigDecimal bancos;

    private BigDecimal inversiones;

    private BigDecimal cartera;

    private BigDecimal bienesRaices;

    private BigDecimal vehiculos;

    private BigDecimal maquinaria;

    private BigDecimal otrosActivos;

    private BigDecimal totalActivos;

    // =========================================================
    // Pasivos
    // =========================================================

    private BigDecimal obligacionesFinancieras;

    private BigDecimal obligacionesHipotecarias;

    private BigDecimal obligacionesVehiculos;

    private BigDecimal obligacionesComerciales;

    private BigDecimal otrosPasivos;

    private BigDecimal totalPasivos;

    // =========================================================
    // Patrimonio
    // =========================================================

    private BigDecimal patrimonioDeclarado;

    private BigDecimal patrimonioCalculado;

    // =========================================================
    // Capacidad de pago
    // =========================================================

    private BigDecimal capacidadPago;

    private BigDecimal porcentajeEndeudamiento;

    private BigDecimal porcentajeGastos;

    private BigDecimal flujoCajaLibre;

    // =========================================================
    // Productos ASSIP
    // =========================================================

    private BigDecimal saldoAportes;

    private BigDecimal saldoAhorros;

    private BigDecimal saldoCdats;

    private BigDecimal saldoCapitalCreditos;

    private BigDecimal saldoInteresesCreditos;

    // =========================================================
    // Consolidado ASSIP
    // =========================================================

    private BigDecimal exposicionTotalEntidad;

    private BigDecimal patrimonioConBienes;

    // =========================================================
    // Evaluación financiera
    // =========================================================

    private String nivelIngresos;

    private String nivelEndeudamiento;

    private String calificacionFinanciera;

    private Boolean capacidadPagoSuficiente;

    // =========================================================
    // Información de la declaración
    // =========================================================

    private LocalDate fechaInformacionFinanciera;

    private Integer diasSinActualizar;

    private Boolean informacionActualizada;

    // =========================================================
    // Indicadores
    // =========================================================

    private Integer scoreFinanciero;

    private String semaforoFinanciero;

    // =========================================================
    // Alertas
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

    public ExpedienteInformacionFinancieraDTO() {

        ingresosLaborales = BigDecimal.ZERO;
        ingresosHonorarios = BigDecimal.ZERO;
        ingresosPensiones = BigDecimal.ZERO;
        ingresosArrendamientos = BigDecimal.ZERO;
        ingresosFinancieros = BigDecimal.ZERO;
        otrosIngresos = BigDecimal.ZERO;

        totalIngresos = BigDecimal.ZERO;

        gastosPersonales = BigDecimal.ZERO;
        gastosFamiliares = BigDecimal.ZERO;
        gastosFinancieros = BigDecimal.ZERO;
        otrosEgresos = BigDecimal.ZERO;

        totalEgresos = BigDecimal.ZERO;

        efectivo = BigDecimal.ZERO;
        bancos = BigDecimal.ZERO;
        inversiones = BigDecimal.ZERO;
        cartera = BigDecimal.ZERO;
        bienesRaices = BigDecimal.ZERO;
        vehiculos = BigDecimal.ZERO;
        maquinaria = BigDecimal.ZERO;
        otrosActivos = BigDecimal.ZERO;

        totalActivos = BigDecimal.ZERO;

        obligacionesFinancieras = BigDecimal.ZERO;
        obligacionesHipotecarias = BigDecimal.ZERO;
        obligacionesVehiculos = BigDecimal.ZERO;
        obligacionesComerciales = BigDecimal.ZERO;
        otrosPasivos = BigDecimal.ZERO;

        totalPasivos = BigDecimal.ZERO;

        patrimonioDeclarado = BigDecimal.ZERO;
        patrimonioCalculado = BigDecimal.ZERO;

        capacidadPago = BigDecimal.ZERO;
        porcentajeEndeudamiento = BigDecimal.ZERO;
        porcentajeGastos = BigDecimal.ZERO;
        flujoCajaLibre = BigDecimal.ZERO;

        saldoAportes = BigDecimal.ZERO;
        saldoAhorros = BigDecimal.ZERO;
        saldoCdats = BigDecimal.ZERO;
        saldoCapitalCreditos = BigDecimal.ZERO;
        saldoInteresesCreditos = BigDecimal.ZERO;

        exposicionTotalEntidad = BigDecimal.ZERO;
        patrimonioConBienes = BigDecimal.ZERO;

        capacidadPagoSuficiente = Boolean.FALSE;

        informacionActualizada = Boolean.FALSE;

        presentaNovedades = Boolean.FALSE;
        cantidadNovedades = 0;
    }

    // =========================================================
    // Métodos auxiliares
    // =========================================================

    public BigDecimal calcularTotalIngresos() {

        totalIngresos =
                ingresosLaborales
                        .add(ingresosHonorarios)
                        .add(ingresosPensiones)
                        .add(ingresosArrendamientos)
                        .add(ingresosFinancieros)
                        .add(otrosIngresos);

        return totalIngresos;
    }

    public BigDecimal calcularTotalEgresos() {

        totalEgresos =
                gastosPersonales
                        .add(gastosFamiliares)
                        .add(gastosFinancieros)
                        .add(otrosEgresos);

        return totalEgresos;
    }

    public BigDecimal calcularPatrimonio() {

        patrimonioCalculado =
                totalActivos.subtract(totalPasivos);

        return patrimonioCalculado;
    }

    public BigDecimal calcularCapacidadPago() {

        capacidadPago =
                totalIngresos.subtract(totalEgresos);

        return capacidadPago;
    }

    public BigDecimal calcularExposicionEntidad() {

        exposicionTotalEntidad =
                saldoAportes
                        .add(saldoAhorros)
                        .add(saldoCdats)
                        .add(saldoCapitalCreditos);

        return exposicionTotalEntidad;
    }

    public void evaluarEstadoFinanciero() {

        calcularTotalIngresos();
        calcularTotalEgresos();
        calcularPatrimonio();
        calcularCapacidadPago();

        cantidadNovedades = 0;

        if (capacidadPago.compareTo(BigDecimal.ZERO) <= 0) {
            cantidadNovedades++;
        }

        if (!Boolean.TRUE.equals(informacionActualizada)) {
            cantidadNovedades++;
        }

        presentaNovedades = cantidadNovedades > 0;

        if (cantidadNovedades == 0) {

            nivelNovedad = "NORMAL";
            resumenNovedades =
                    "Información financiera consistente.";

        } else if (cantidadNovedades == 1) {

            nivelNovedad = "ADVERTENCIA";
            resumenNovedades =
                    "La información financiera requiere revisión.";

        } else {

            nivelNovedad = "CRITICA";
            resumenNovedades =
                    "La información financiera presenta novedades importantes.";
        }
    }

}