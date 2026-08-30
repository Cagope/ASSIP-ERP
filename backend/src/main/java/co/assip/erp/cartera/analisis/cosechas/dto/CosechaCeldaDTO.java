package co.assip.erp.cartera.analisis.cosechas.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CosechaCeldaDTO {

    // =========================================================
    // IDENTIFICACIÓN
    // =========================================================

    private LocalDate cosecha;

    private LocalDate fechaCorte;

    private Integer mob;


    // =========================================================
    // ORIGINACIÓN
    // =========================================================

    private Integer cantidadOriginada;

    private BigDecimal valorInicialOriginal;

    private BigDecimal valorDesembolsadoOriginal;


    // =========================================================
    // PRESENCIA EN EL CORTE
    // =========================================================

    private Integer cantidadPresentesCorte;

    private Integer cantidadSinPresenciaCorte;

    private BigDecimal porcentajePresentesCorte;

    private BigDecimal porcentajeSinPresenciaCorte;


    // =========================================================
    // SALDO
    // =========================================================

    private Integer cantidadConSaldo;

    private BigDecimal saldoCapital;

    /**
     * saldoCapital / valorDesembolsadoOriginal * 100
     */
    private BigDecimal porcentajeSaldoRemanente;


    // =========================================================
    // MORA 30+
    // =========================================================

    private Integer cantidadMora30;

    private BigDecimal saldoMora30;

    /**
     * cantidadMora30 / cantidadOriginada * 100
     */
    private BigDecimal porcentajeCantidadMora30;

    /**
     * saldoMora30 / saldoCapital * 100
     */
    private BigDecimal porcentajeSaldoMora30SobreSaldo;

    /**
     * saldoMora30 / valorDesembolsadoOriginal * 100
     */
    private BigDecimal porcentajeSaldoMora30SobreOriginacion;


    // =========================================================
    // MORA 60+
    // =========================================================

    private Integer cantidadMora60;

    private BigDecimal saldoMora60;

    private BigDecimal porcentajeCantidadMora60;

    private BigDecimal porcentajeSaldoMora60SobreSaldo;

    private BigDecimal porcentajeSaldoMora60SobreOriginacion;


    // =========================================================
    // MORA 90+
    // =========================================================

    private Integer cantidadMora90;

    private BigDecimal saldoMora90;

    private BigDecimal porcentajeCantidadMora90;

    private BigDecimal porcentajeSaldoMora90SobreSaldo;

    private BigDecimal porcentajeSaldoMora90SobreOriginacion;


    // =========================================================
    // MORA 180+
    // =========================================================

    private Integer cantidadMora180;

    private BigDecimal saldoMora180;

    private BigDecimal porcentajeCantidadMora180;

    private BigDecimal porcentajeSaldoMora180SobreSaldo;

    private BigDecimal porcentajeSaldoMora180SobreOriginacion;
}