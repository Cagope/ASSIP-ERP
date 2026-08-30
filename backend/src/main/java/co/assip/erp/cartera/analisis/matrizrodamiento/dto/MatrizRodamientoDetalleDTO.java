package co.assip.erp.cartera.analisis.matrizrodamiento.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MatrizRodamientoDetalleDTO {

    // =========================================================
    // IDENTIFICACIÓN DEL CRÉDITO
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

    // =========================================================
    // DATOS DEL PERÍODO ANTERIOR
    // =========================================================

    private LocalDate fechaComparacion;

    private Integer diasMoraAnterior;

    private String categoriaAnterior;

    private BigDecimal saldoAnterior;

    // =========================================================
    // DATOS DE PARTIDA
    // =========================================================

    /**
     * ACTUAL o CORTE.
     */
    private String tipoPartida;

    private LocalDate fechaPartida;

    private Integer diasMoraPartida;

    private String categoriaPartida;

    /**
     * Saldo que utiliza la matriz POR VALORES.
     *
     * Equivale a SALDOI_T del VB.
     */
    private BigDecimal saldoPartida;
}