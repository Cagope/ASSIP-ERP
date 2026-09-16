package co.assip.erp.cartera.originacion.contexto.dto;

import co.assip.erp.cartera.analisis.vectorcomportamiento.actual.dto.VectorComportamientoResumenDTO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OriginacionContextoDTO {

    // =========================================================
    // ASOCIADO
    // =========================================================

    private Integer idDatosPersonal;
    private Integer idAgencia;


    // =========================================================
    // BLOQUES DE APOYO
    // =========================================================

    private InformacionEconomicaDTO informacionEconomica;

    private ResumenCarteraDTO resumenCartera;

    private ReciprocidadDTO reciprocidad;

    private List<DepositoDTO> depositos =
            new ArrayList<>();

    private List<CarteraDTO> carteraActual =
            new ArrayList<>();

    private List<VectorComportamientoResumenDTO> vectorResumen =
            new ArrayList<>();

    private List<CodeudaDTO> codeudasActuales =
            new ArrayList<>();


    // =========================================================
    // INFORMACIÓN ECONÓMICA
    // =========================================================

    @Getter
    @Setter
    public static class InformacionEconomicaDTO {

        private Integer idFinanciero;
        private Integer idDatosPersonal;

        private String nombreActividadEconomica;
        private String nombreSectorEconomico;
        private String ocupacion;
        private String empresa;

        private BigDecimal valorSalario;
        private BigDecimal valorPension;
        private BigDecimal ingresosArriendo;
        private BigDecimal ingresosComisiones;
        private BigDecimal otrosIngresos;
        private String comentarioOtrosIngresos;

        private BigDecimal totalIngresos;

        private BigDecimal egresosFamiliares;
        private BigDecimal egresosArriendo;
        private BigDecimal egresosCredito;
        private BigDecimal otrosEgresos;
        private String comentarioOtrosEgresos;

        private BigDecimal totalEgresos;

        private BigDecimal totalActivos;
        private BigDecimal totalPasivos;
        private BigDecimal patrimonio;

        private String origenFondos;
        private String relacionFinanciera;
        private BigDecimal deudaRelacionFinanciera;

        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaEdicion;
    }


    // =========================================================
    // RESUMEN DE CARTERA
    // =========================================================

    @Getter
    @Setter
    public static class ResumenCarteraDTO {

        // Cantidad de créditos vigentes con saldo
        private Integer cantidadCreditos;

        // Suma del saldo actual de la cartera vigente
        private BigDecimal saldoCarteraActual;

        // Mayor mora actual entre los créditos vigentes
        private Integer moraActual;

        // Mayor mora observada en el histórico disponible
        private Integer moraMaximaHistorica;

        // Promedio de días de mora observado en los últimos 12 meses
        private BigDecimal promedioMoraUltimos12Meses;

        // Créditos actualmente reclasificados por evaluación
        private Integer cantidadCreditosReclasificados;

        private Boolean tieneCreditosReclasificados;
    }


    // =========================================================
    // RECIPROCIDAD
    // =========================================================

    @Getter
    @Setter
    public static class ReciprocidadDTO {

        // Saldo actual de aportes sociales
        private BigDecimal saldoAportes;

        /*
         * Valor inicial sugerido para la simulación.
         *
         * Ejemplo:
         * $1.000.000 × 50 = $50.000.000
         */
        private BigDecimal reciprocidadInicial;

        // saldoAportes × reciprocidadInicial
        private BigDecimal cupoReciprocidad;

        // Saldo actual total de cartera
        private BigDecimal saldoCarteraActual;

        /*
         * Diferencia matemática:
         *
         * cupoReciprocidad - saldoCarteraActual
         *
         * Puede ser negativa.
         */
        private BigDecimal diferenciaReciprocidad;

        // Disponible mostrado cuando la diferencia es positiva
        private BigDecimal disponibleReciprocidad;

        // Exceso mostrado cuando la diferencia es negativa
        private BigDecimal excesoReciprocidad;
    }


    // =========================================================
    // DEPÓSITOS
    // =========================================================

    @Getter
    @Setter
    public static class DepositoDTO {

        private Integer idCuentaAhorro;

        private Integer idAgencia;
        private Integer idDatosPersonal;

        private Integer idFormaAhorro;
        private String codigoForma;
        private String nombreForma;

        private String codigoCuenta;

        private LocalDate fechaApertura;
        private LocalDate fechaUltimoMovimiento;

        private BigDecimal entradasUltimoAno;
        private BigDecimal salidasUltimoAno;

        private BigDecimal saldo;

        private String codigoEstado;
        private String nombreEstado;
        private Boolean activa;
    }


    // =========================================================
    // CARTERA
    // =========================================================

    @Getter
    @Setter
    public static class CarteraDTO {

        private Integer idCarteraCredito;

        private Integer idAgencia;

        private Integer idLineaCredito;
        private String codigoLineaCredito;
        private String nombreLineaCredito;

        private String pagareCartera;

        private LocalDate fechaDesembolso;

        private BigDecimal saldoActual;

        private Integer diasMora;

        private String codigoEstadoCartera;
        private String nombreEstadoCartera;

        private Boolean vigente;
    }


    // =========================================================
    // CODEUDAS
    // =========================================================

    @Getter
    @Setter
    public static class CodeudaDTO {

        private Long idObligacionFiador;
        private Long idObligacionJuridica;

        private Integer idCarteraCredito;

        private Integer idAgencia;

        private Integer idLineaCredito;
        private String codigoLineaCredito;
        private String nombreLineaCredito;

        private String pagareCartera;

        private Integer idDeudorPrincipal;
        private String documentoDeudorPrincipal;
        private String deudorPrincipal;

        private LocalDate fechaDesembolso;

        private BigDecimal saldoActual;

        private Integer diasMora;

        private String codigoEstadoCartera;
        private String nombreEstadoCartera;

        private Boolean vigente;
    }
}