package co.assip.erp.cdat.cdats.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import co.assip.erp.cajas.medios_pago.dto.MediosPagoDTO;

@Getter
@Setter
public class CdatSaveDTO {

    private Long idCuentaCdat;

    // Agencia y código
    private Integer idAgencia;
    private String codigoCdat;
    private BigDecimal tasaNominalMensual;

    // Titular
    private Integer idDatosPersonal;
    private Integer idDatosPersonalCotitular;

    // Cuentas obligatorias
    private Integer idCuentaAportes;
    private Integer idCuentaAhorro;

    // Datos CDAT
    private Integer idProductoCdat;
    private LocalDate fechaAperturaCdat;
    private Integer plazoMeses;
    private Integer plazoDias;

    private BigDecimal valorAperturaCdat;

    private BigDecimal tasaNominalAnual;
    private BigDecimal tasaEfectivaAnual;
    private BigDecimal tasaEfectivaMensual;

    private Boolean retencionFuenteCdat;

    // Catálogo
    private String amortizacionDeposito;

    // Modalidad
    private String modalidadCdat;

    // Cuenta conjunta
    private String cuentaConjunta;
    private String accionConjunta;

    // Renovación
    private String origenCdat;
    private Long idCuentaCdatOrigen;

    // Comprobante
    private String tipoComprobante;
    private String numeroComprobante;
    private LocalDate fechaComprobante;

    // Valores de apertura
    private BigDecimal valorEfectivo;
    private BigDecimal valorCheque;
    private BigDecimal valorDepositos;
    private BigDecimal valorBanco;
    private BigDecimal valorTrasladosAgencias;

    // Detalle completo de medios de pago
    private MediosPagoDTO mediosPago;

    // Otros
    private String observacion;
    private List<CdatBeneficiarioDTO> beneficiarios;

}