package co.assip.erp.cartera.analisis.cosechas.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CosechaDetalleDTO {

    // =========================================================
    // CRÉDITO
    // =========================================================

    private Integer idCarteraCredito;

    private Integer idAgencia;

    private Integer idLineaCredito;

    private String codigoLineaCredito;

    private String nombreLineaCredito;

    private String pagareCartera;


    // =========================================================
    // ASOCIADO
    // =========================================================

    private Integer idDatosPersonal;

    private String tipoDocumento;

    private String documento;

    private String nombreCompleto;


    // =========================================================
    // CONTACTO
    // =========================================================

    private String telefono;

    private String celular;

    private String correo;


    // =========================================================
    // ORIGINACIÓN
    // =========================================================

    private LocalDate fechaDesembolso;

    private LocalDate cosecha;

    private BigDecimal valorInicialCredito;

    private BigDecimal valorDesembolsado;


    // =========================================================
    // OBSERVACIÓN
    // =========================================================

    private LocalDate fechaCorte;

    private Integer mob;

    private Boolean presenteCorte;

    private BigDecimal saldoCapital;

    private Integer diasMora;

    private String categoriaMora;

    private String codigoEstadoCartera;

    private String descripcionEstadoCartera;


    // =========================================================
    // EDADES DE RIESGO
    // =========================================================

    private String edadRiesgoInicial;

    private String edadMora;

    private String edadRiesgo;

    private String edadPe;

    private String edadHomologacion;

    private String edadContable;


    // =========================================================
    // PE / DETERIORO
    // =========================================================

    private BigDecimal vea;

    private BigDecimal pi;

    private BigDecimal pdi;

    private BigDecimal perdidaEsperada;

    private BigDecimal deterioroCapital;

    private BigDecimal deterioroIntereses;

    private BigDecimal deterioroOtros;

    private BigDecimal deterioroTotal;


    // =========================================================
    // INDICADORES
    // =========================================================

    private Boolean conSaldo;

    private Boolean mora30;

    private Boolean mora60;

    private Boolean mora90;

    private Boolean mora180;
}