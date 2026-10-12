package co.assip.erp.tesoreria.consignaciones_bancarias.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ConsignacionBancariaRequest {

    // =========================================================
    // DATOS GENERALES
    // =========================================================

    /**
     * Agencia que realiza la consignación.
     *
     * Todos los recursos involucrados deben pertenecer a esta agencia:
     * - cuenta bancaria habilitada;
     * - cajas origen;
     * - provisiones;
     * - cheques.
     */
    private Integer idAgencia;

    /**
     * Cuenta bancaria física de destino.
     *
     * La cuenta contable no llega desde el frontend.
     * El backend la obtiene de:
     *
     * tesoreria.cuentas_bancarias_agencias
     *
     * usando idCuentaBancaria + idAgencia.
     */
    private Long idCuentaBancaria;

    /**
     * Fecha efectiva de la consignación bancaria.
     */
    private LocalDate fechaConsignacion;

    /**
     * Número del soporte externo entregado por el banco
     * o utilizado para identificar la consignación.
     *
     * No corresponde al número del CE.
     */
    private String numeroConsignacion;

    /**
     * Observación general de la operación.
     */
    private String observacion;

    // =========================================================
    // CHEQUES
    // =========================================================

    /**
     * Cheques físicos seleccionados para consignar.
     *
     * El frontend solamente envía los IDs.
     *
     * Valor, caja, banco del cheque, cuenta contable y demás
     * información se vuelve a consultar y validar en backend
     * bajo bloqueo.
     */
    private List<Long> idsCheques = new ArrayList<>();

    // =========================================================
    // EFECTIVO
    // =========================================================

    /**
     * Efectivo opcional incluido en la consignación.
     *
     * Puede estar vacío.
     *
     * Cada registro identifica la caja física desde la cual
     * sale el efectivo y el valor entregado.
     */
    private List<EfectivoCajaRequest> efectivos = new ArrayList<>();

    @Getter
    @Setter
    public static class EfectivoCajaRequest {

        /**
         * Caja desde la cual sale físicamente el efectivo.
         */
        private Long idCaja;

        /**
         * Valor de efectivo que sale de esa caja.
         */
        private BigDecimal valor;
    }
}