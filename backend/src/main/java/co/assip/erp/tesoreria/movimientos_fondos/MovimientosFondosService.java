
package co.assip.erp.tesoreria.movimientos_fondos;

import co.assip.erp.tesoreria.traslados_cajas.TrasladosCajasService;
import co.assip.erp.tesoreria.traslados_cajas.TrasladosCajasRepository;

import co.assip.erp.tesoreria.consignaciones_bancarias.ConsignacionesBancariasRepository;
import co.assip.erp.tesoreria.consignaciones_bancarias.ConsignacionesBancariasService;

import co.assip.erp.tesoreria.retiros_bancarios.RetirosBancariosService;
import co.assip.erp.tesoreria.retiros_bancarios.RetirosBancariosRepository;

import co.assip.erp.tesoreria.traslados_bancarios.TrasladosBancariosService;
import co.assip.erp.tesoreria.traslados_bancarios.TrasladosBancariosRepository;

import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoPreviewDTO;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoRequestDTO;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoResponseDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio coordinador de los movimientos de fondos de Tesorería.
 *
 * Centraliza el acceso a los procesos especializados:
 *
 * - Consignaciones bancarias.
 * - Traslados entre cajas.
 * - Retiros bancarios.
 * - Traslados entre cuentas bancarias.
 *
 * Cada proceso conserva sus propias validaciones,
 * consultas y reglas de negocio.
 *
 * Reglas de autorización:
 *
 * - CONSIGNACION: contabilización directa (implementación actual).
 * - TRASLADO_CAJAS: contabilización directa.
 * - RETIRO: requiere preparación y autorización.
 * - TRASLADO_BANCARIO: requiere preparación y autorización.
 */
@Service
@RequiredArgsConstructor
public class MovimientosFondosService {

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final TrasladosCajasRepository trasladosCajasRepository;

    private final ConsignacionesBancariasService consignacionesBancariasService;

    private final TrasladosCajasService trasladosCajasService;

    private final RetirosBancariosService retirosBancariosService;

    private final TrasladosBancariosService trasladosBancariosService;

    // =========================================================
    // CATÁLOGOS - CONSIGNACIONES BANCARIAS
    // =========================================================

    public List<ConsignacionesBancariasRepository.CuentaBancariaConsignacion>
    listarCuentasBancarias(Integer idAgencia) {

        validarAgencia(idAgencia);

        return consignacionesBancariasService
                .listarCuentasBancarias(idAgencia);
    }

    public List<ConsignacionesBancariasRepository.ChequeDisponible>
    listarChequesRecibidos(Integer idAgencia) {

        validarAgencia(idAgencia);

        return consignacionesBancariasService
                .listarChequesRecibidos(idAgencia);
    }

    public List<ConsignacionesBancariasRepository.CajaEfectivoDisponible>
    listarCajasConProvision(
            Integer idAgencia,
            LocalDate fechaConsignacion
    ) {

        validarAgencia(idAgencia);

        if (fechaConsignacion == null) {
            throw new IllegalArgumentException(
                    "La fecha de consignación es obligatoria."
            );
        }

        return consignacionesBancariasService
                .listarCajasConProvision(
                        idAgencia,
                        fechaConsignacion
                );
    }

    // =========================================================
    // CATÁLOGOS - TRASLADOS ENTRE CAJAS
    // =========================================================

    public List<TrasladosCajasRepository.CajaMovimientoDisponible>
    listarCajasMovimiento(Integer idAgencia) {

        validarAgencia(idAgencia);

        return trasladosCajasRepository
                .listarCajasMovimiento(idAgencia);
    }

    public List<TrasladosCajasRepository.CajaTrasladoAbierta>
    listarCajasTrasladoAbiertas(
            Integer idAgencia,
            LocalDate fechaContable
    ) {

        validarAgencia(idAgencia);

        if (fechaContable == null) {
            throw new IllegalArgumentException(
                    "La fecha contable es obligatoria."
            );
        }

        return trasladosCajasRepository
                .listarCajasTrasladoAbiertas(
                        idAgencia,
                        fechaContable
                );
    }

    // =========================================================
    // CATÁLOGOS - RETIROS BANCARIOS
    // =========================================================

    public List<RetirosBancariosRepository.CuentaBancariaRetiro>
    listarCuentasRetiro(Integer idAgencia) {

        validarAgencia(idAgencia);

        return retirosBancariosService
                .listarCuentasRetiro(idAgencia);
    }

    public List<RetirosBancariosRepository.CajaControl>
    listarCajasControlRetiro(Integer idAgencia) {

        validarAgencia(idAgencia);

        return retirosBancariosService
                .listarCajasControl(idAgencia);
    }

    // =========================================================
    // CATÁLOGOS - TRASLADOS BANCARIOS
    // =========================================================

    /**
     * Cuentas bancarias habilitadas para retirar fondos.
     */
    public List<TrasladosBancariosRepository.CuentaBancaria>
    listarCuentasOrigenTrasladoBancario(Integer idAgencia) {

        validarAgencia(idAgencia);

        return trasladosBancariosService
                .listarCuentasOrigen(idAgencia);
    }

    /**
     * Cuentas bancarias habilitadas para recibir fondos.
     */
    public List<TrasladosBancariosRepository.CuentaBancaria>
    listarCuentasDestinoTrasladoBancario(Integer idAgencia) {

        validarAgencia(idAgencia);

        return trasladosBancariosService
                .listarCuentasDestino(idAgencia);
    }

    // =========================================================
    // VISTA PREVIA DE MOVIMIENTOS
    // =========================================================

    /**
     * Construye la vista previa contable del movimiento.
     *
     * No genera comprobantes, consecutivos ni movimientos
     * contables o físicos definitivos.
     */
    public MovimientoFondoPreviewDTO preview(
            MovimientoFondoRequestDTO request
    ) {

        validarTipoMovimiento(request);

        return switch (request.getTipoMovimiento()) {

            case CONSIGNACION ->
                    consignacionesBancariasService
                            .previewConsignacion(request);

            case TRASLADO_CAJAS ->
                    trasladosCajasService
                            .previewTrasladoCajas(request);

            case RETIRO ->
                    retirosBancariosService
                            .previewRetiro(request);

            case TRASLADO_BANCARIO ->
                    trasladosBancariosService
                            .previewTraslado(request);
        };
    }

    // =========================================================
    // PREPARACIÓN - RETIROS BANCARIOS
    // =========================================================

    public RetirosBancariosService.RetiroPendienteResponse
    prepararRetiro(
            MovimientoFondoRequestDTO request
    ) {

        validarTipoMovimiento(request);

        if (request.getTipoMovimiento()
                != MovimientoFondoRequestDTO.TipoMovimiento.RETIRO) {

            throw new IllegalArgumentException(
                    "Esta operación únicamente permite "
                            + "preparar retiros bancarios."
            );
        }

        return retirosBancariosService
                .prepararRetiro(request);
    }

    // =========================================================
    // PREPARACIÓN - TRASLADOS BANCARIOS
    // =========================================================

    /**
     * Prepara un traslado bancario pendiente de autorización.
     *
     * No genera consecutivo ni contabiliza.
     *
     * No registra movimientos físicos de caja.
     */
    public TrasladosBancariosService.TrasladoPendienteResponse
    prepararTrasladoBancario(
            MovimientoFondoRequestDTO request
    ) {

        validarTipoMovimiento(request);

        if (request.getTipoMovimiento()
                != MovimientoFondoRequestDTO.TipoMovimiento
                .TRASLADO_BANCARIO) {

            throw new IllegalArgumentException(
                    "Esta operación únicamente permite "
                            + "preparar traslados bancarios."
            );
        }

        return trasladosBancariosService
                .prepararTraslado(request);
    }

    // =========================================================
    // APLICACIÓN DE MOVIMIENTOS
    // =========================================================

    /**
     * Ejecuta exclusivamente los procesos que actualmente
     * permiten contabilización directa.
     *
     * Los retiros y traslados bancarios deben pasar por
     * preparación y autorización.
     */
    public MovimientoFondoResponseDTO aplicar(
            MovimientoFondoRequestDTO request
    ) {

        validarTipoMovimiento(request);

        return switch (request.getTipoMovimiento()) {

            case CONSIGNACION ->
                    consignacionesBancariasService
                            .aplicarConsignacion(request);

            case TRASLADO_CAJAS ->
                    trasladosCajasService
                            .aplicarTrasladoCajas(request);

            case RETIRO ->
                    throw new IllegalArgumentException(
                            "El retiro bancario requiere "
                                    + "preparación y autorización previa. "
                                    + "Utilice la operación prepararRetiro."
                    );

            case TRASLADO_BANCARIO ->
                    throw new IllegalArgumentException(
                            "El traslado bancario requiere "
                                    + "preparación y autorización previa. "
                                    + "Utilice la operación "
                                    + "prepararTrasladoBancario."
                    );
        };
    }

    // =========================================================
    // VALIDACIONES INTERNAS
    // =========================================================

    private void validarTipoMovimiento(
            MovimientoFondoRequestDTO request
    ) {

        if (request == null
                || request.getTipoMovimiento() == null) {

            throw new IllegalArgumentException(
                    "Seleccione un tipo de movimiento válido."
            );
        }
    }

    private void validarAgencia(Integer idAgencia) {

        if (idAgencia == null || idAgencia <= 0) {

            throw new IllegalArgumentException(
                    "La agencia es obligatoria."
            );
        }
    }
}
