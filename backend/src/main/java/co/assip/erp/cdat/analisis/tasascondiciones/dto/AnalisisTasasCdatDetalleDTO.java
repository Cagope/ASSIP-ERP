package co.assip.erp.cdat.analisis.tasascondiciones.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AnalisisTasasCdatDetalleDTO {

    // =========================================================
    // IDENTIFICACIÓN DEL CDAT
    // =========================================================

    private Long idCuentaCdat;

    private String codigoCdat;

    // =========================================================
    // AGENCIA
    // =========================================================

    private Integer idAgencia;

    private String codigoAgencia;

    private String nombreAgencia;

    // =========================================================
    // DEPOSITANTE
    // =========================================================

    private Integer idDatosPersonal;

    private String tipoDocumento;

    private String documento;

    private String nombreCompleto;

    // =========================================================
    // CONDICIONES DEL CDAT
    // =========================================================

    private LocalDate fechaAperturaCdat;

    private LocalDate fechaVencimientoCdat;

    private Integer plazoMeses;

    private Integer plazoDias;

    private String amortizacionDeposito;

    private String nombreAmortizacion;

    // =========================================================
    // VALORES
    // =========================================================

    private BigDecimal valorAperturaCdat;

    private BigDecimal saldoActualCdat;

    // =========================================================
    // TASAS
    // =========================================================

    private BigDecimal tasaNominalAnual;

    private BigDecimal tasaEfectivaAnual;
}