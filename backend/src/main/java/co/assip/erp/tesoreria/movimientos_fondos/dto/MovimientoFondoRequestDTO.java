
package co.assip.erp.tesoreria.movimientos_fondos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Solicitud para registrar movimientos de fondos.
 *
 * Operaciones:
 * - CONSIGNACION
 * - RETIRO
 * - TRASLADO_CAJAS
 * - TRASLADO_BANCARIO
 *
 * El frontend no determina cuentas contables,
 * consecutivos ni comprobantes.
 */
@Getter
@Setter
public class MovimientoFondoRequestDTO {

    // =========================================================
    // TIPO DE OPERACIÓN
    // =========================================================

    private TipoMovimiento tipoMovimiento;

    public enum TipoMovimiento {
        CONSIGNACION,
        RETIRO,
        TRASLADO_CAJAS,
        TRASLADO_BANCARIO
    }

    // =========================================================
    // DATOS GENERALES
    // =========================================================

    /** Agencia responsable del movimiento. */
    private Integer idAgencia;

    /** Fecha del documento o movimiento. */
    private LocalDate fechaMovimiento;

    /**
     * Número del soporte externo.
     * No corresponde al consecutivo contable.
     */
    private String numeroSoporte;

    /** Descripción u observaciones. */
    private String observacion;

    /**
     * Valor del retiro o traslado.
     *
     * Para consignaciones, el valor se calcula
     * a partir de los cheques y el efectivo.
     */
    private BigDecimal valor;

    // =========================================================
    // CUENTAS BANCARIAS
    // =========================================================

    /**
     * Cuenta bancaria desde la cual salen los fondos.
     *
     * Utilizada en:
     * - RETIRO
     * - TRASLADO_BANCARIO
     */
    private Long idCuentaBancariaOrigen;

    /**
     * Cuenta bancaria que recibe los fondos.
     *
     * Utilizada en:
     * - CONSIGNACION
     * - TRASLADO_BANCARIO
     */
    private Long idCuentaBancariaDestino;

    // =========================================================
    // MEDIO DE TRASLADO BANCARIO
    // =========================================================

    /**
     * Medio utilizado para trasladar recursos entre bancos.
     *
     * EFECTIVO:
     *   Transporte físico de efectivo entre entidades bancarias.
     *
     * BRE_B:
     *   Transferencia electrónica mediante Bre-B.
     *
     * TRANSFERENCIA_NORMAL:
     *   Transferencia bancaria electrónica tradicional.
     *
     * Este campo aplica exclusivamente a TRASLADO_BANCARIO.
     */
    private MedioTrasladoBancario medioTrasladoBancario;

    public enum MedioTrasladoBancario {
        EFECTIVO,
        BRE_B,
        TRANSFERENCIA_NORMAL
    }

    // =========================================================
    // CAJAS
    // =========================================================

    /**
     * Caja física desde la cual salen los fondos.
     * Utilizada en traslados entre cajas.
     */
    private Long idCajaOrigen;

    /**
     * Caja física que recibe los fondos.
     *
     * Utilizada en:
     * - RETIRO
     * - TRASLADO_CAJAS
     */
    private Long idCajaDestino;

    /**
     * Modalidad del traslado entre cajas.
     *
     * PROVISION:
     *   Débito  Caja OPERATIVA.
     *   Crédito Caja CONTROL.
     *
     * REEMBOLSO:
     *   Débito  Caja CONTROL.
     *   Crédito Caja OPERATIVA.
     *
     * TRASLADO:
     *   Entre cajas autorizadas.
     */
    private ModalidadTrasladoCaja modalidadTrasladoCaja;

    public enum ModalidadTrasladoCaja {
        PROVISION,
        REEMBOLSO,
        TRASLADO
    }

    // =========================================================
    // CHEQUES
    // =========================================================

    /**
     * Cheques seleccionados para consignación.
     *
     * El backend consulta sus valores y estados
     * y valida nuevamente bajo bloqueo.
     */
    private List<Long> idsCheques = new ArrayList<>();

    // =========================================================
    // EFECTIVO PARA CONSIGNACIÓN
    // =========================================================

    /**
     * Detalle del efectivo consignado por caja.
     *
     * Se conserva el modelo utilizado por
     * ConsignacionBancariaRequest.
     */
    private List<EfectivoCajaDTO> efectivos = new ArrayList<>();

    @Getter
    @Setter
    public static class EfectivoCajaDTO {

        /** Caja física de origen. */
        private Long idCaja;

        /** Valor de efectivo entregado. */
        private BigDecimal valor;
    }
}
