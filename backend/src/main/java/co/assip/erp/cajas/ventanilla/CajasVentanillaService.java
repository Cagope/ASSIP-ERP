package co.assip.erp.cajas.ventanilla;

import co.assip.erp.cajas.provisiones.CajasProvisionService;
import co.assip.erp.cajas.provisiones.dto.CajaProvisionActivaDTO;
import co.assip.erp.cajas.ventanilla.dto.CajaVentanillaEstadoDTO;
import co.assip.erp.cajas.ventanilla.dto.CajaVentanillaMovimientoDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CajasVentanillaService {

    private static final String TIPO_CAJA_OPERATIVA =
            "OPERATIVA";

    private static final String ESTADO_PROVISION_ABIERTA =
            "ABIERTA";

    private static final String CAJA_A_VENTANILLA =
            "CAJA_A_VENTANILLA";

    private static final String VENTANILLA_A_CAJA =
            "VENTANILLA_A_CAJA";


    private final CajasVentanillaRepository repository;
    private final CajasProvisionService cajasProvisionService;
    private final UsuarioSesionService usuarioSesionService;


    // =========================================================
    // ESTADO ACTUAL
    // =========================================================

    @Transactional(readOnly = true)
    public CajaVentanillaEstadoDTO obtenerEstadoActual() {

        CajaProvisionActivaDTO provision =
                obtenerProvisionActualUsuario();

        CajaVentanillaEstadoDTO estado =
                repository.obtenerEstado(
                                provision.getIdProvision()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No fue posible consultar "
                                                + "el estado de la caja."
                                )
                        );

        validarEstado(
                estado,
                provision
        );

        return estado;
    }


    // =========================================================
    // REALIZAR TRASLADO
    // =========================================================

    @Transactional
    public CajaVentanillaEstadoDTO trasladar(
            CajaVentanillaMovimientoDTO dto
    ) {

        validarSolicitud(dto);

        /*
         * La provisión NO llega desde Angular.
         *
         * Se obtiene exclusivamente desde:
         *
         * usuario autenticado
         *     -> vinculación actual
         *     -> caja actual
         *     -> provisión abierta actual
         */
        CajaProvisionActivaDTO provision =
                obtenerProvisionActualUsuario();

        Long idProvision =
                provision.getIdProvision();

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        /*
         * CONCURRENCIA
         *
         * Bloqueamos la provisión antes de calcular
         * los saldos utilizados para autorizar
         * el traslado.
         */
        repository.bloquearProvision(
                idProvision
        );

        /*
         * Recalculamos después del FOR UPDATE.
         */
        CajaVentanillaEstadoDTO estado =
                repository.obtenerEstado(
                                idProvision
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No fue posible consultar "
                                                + "el estado de la caja."
                                )
                        );

        /*
         * Revalidamos contra la provisión que fue
         * obtenida desde la sesión del usuario.
         */
        validarEstado(
                estado,
                provision
        );

        BigDecimal valor =
                normalizarValor(
                        dto.getValor()
                );

        String tipoMovimiento =
                normalizarTipoMovimiento(
                        dto.getTipoMovimiento()
                );

        String observacion =
                normalizarObservacion(
                        dto.getObservacion()
                );

        switch (tipoMovimiento) {

            case CAJA_A_VENTANILLA ->
                    trasladarCajaAVentanilla(
                            estado,
                            valor,
                            observacion,
                            idUsuario
                    );

            case VENTANILLA_A_CAJA ->
                    trasladarVentanillaACaja(
                            estado,
                            valor,
                            observacion,
                            idUsuario
                    );

            default ->
                    throw new RuntimeException(
                            "El tipo de traslado no es válido."
                    );
        }

        /*
         * Devolvemos los saldos recalculados después
         * del movimiento.
         *
         * El bloqueo continúa vigente hasta terminar
         * esta transacción.
         */
        return repository.obtenerEstado(
                        idProvision
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "No fue posible consultar "
                                        + "el estado final de la caja."
                        )
                );
    }


    // =========================================================
    // PROVISIÓN ACTUAL DEL USUARIO
    // =========================================================

    private CajaProvisionActivaDTO
    obtenerProvisionActualUsuario() {

        CajaProvisionActivaDTO provision =
                cajasProvisionService
                        .obtenerProvisionActivaUsuario();

        if (provision == null) {
            throw new RuntimeException(
                    "El usuario no tiene una provisión "
                            + "activa para la jornada actual."
            );
        }

        if (provision.getIdProvision() == null) {
            throw new RuntimeException(
                    "La provisión activa del usuario "
                            + "no es válida."
            );
        }

        if (provision.getIdCaja() == null) {
            throw new RuntimeException(
                    "La provisión activa no tiene "
                            + "una caja válida."
            );
        }

        if (provision.getFechaContable() == null) {
            throw new RuntimeException(
                    "La provisión activa no tiene "
                            + "fecha contable válida."
            );
        }

        /*
         * Ventanilla trabaja exclusivamente sobre
         * la jornada actual.
         */
        if (
                !LocalDate.now().equals(
                        provision.getFechaContable()
                )
        ) {
            throw new RuntimeException(
                    "La provisión de la caja no corresponde "
                            + "a la jornada actual."
            );
        }

        if (
                !ESTADO_PROVISION_ABIERTA.equalsIgnoreCase(
                        provision.getEstado()
                )
        ) {
            throw new RuntimeException(
                    "La caja no tiene una provisión abierta "
                            + "para la jornada actual."
            );
        }

        usuarioSesionService.validarAgencia(
                provision.getIdAgencia()
        );

        return provision;
    }


    // =========================================================
    // VALIDACIÓN DEL ESTADO
    // =========================================================

    private void validarEstado(
            CajaVentanillaEstadoDTO estado,
            CajaProvisionActivaDTO provision
    ) {

        if (
                !ESTADO_PROVISION_ABIERTA.equalsIgnoreCase(
                        estado.getEstadoProvision()
                )
        ) {
            throw new RuntimeException(
                    "La caja no se encuentra abierta."
            );
        }

        if (
                !TIPO_CAJA_OPERATIVA.equalsIgnoreCase(
                        estado.getTipoCaja()
                )
        ) {
            throw new RuntimeException(
                    "Los movimientos de ventanilla "
                            + "solo están permitidos "
                            + "para cajas operativas."
            );
        }

        if (
                !provision.getIdProvision().equals(
                        estado.getIdProvision()
                )
        ) {
            throw new RuntimeException(
                    "La provisión consultada no corresponde "
                            + "a la provisión actual del usuario."
            );
        }

        if (
                !provision.getIdCaja().equals(
                        estado.getIdCaja()
                )
        ) {
            throw new RuntimeException(
                    "La provisión no corresponde "
                            + "a la caja vinculada al usuario."
            );
        }

        if (
                !provision.getFechaContable().equals(
                        estado.getFechaContable()
                )
        ) {
            throw new RuntimeException(
                    "La fecha contable de la provisión "
                            + "no corresponde a la jornada actual."
            );
        }

        usuarioSesionService.validarAgencia(
                estado.getIdAgencia()
        );
    }


    // =========================================================
    // CAJA → VENTANILLA
    // =========================================================

    private void trasladarCajaAVentanilla(
            CajaVentanillaEstadoDTO estado,
            BigDecimal valor,
            String observacion,
            Integer idUsuario
    ) {

        BigDecimal saldoCaja =
                nvl(
                        estado.getSaldoCajaInterna()
                );

        if (
                saldoCaja.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new RuntimeException(
                    "El saldo de la caja interna "
                            + "presenta una inconsistencia."
            );
        }

        if (
                valor.compareTo(
                        saldoCaja
                ) > 0
        ) {
            throw new RuntimeException(
                    "El valor a trasladar supera "
                            + "el efectivo disponible "
                            + "en la caja interna. "
                            + "Disponible: "
                            + saldoCaja.toPlainString()
            );
        }

        BigDecimal saldoVentanilla =
                nvl(
                        estado.getSaldoVentanilla()
                );

        if (
                saldoVentanilla.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new RuntimeException(
                    "El saldo de ventanilla "
                            + "presenta una inconsistencia."
            );
        }

        BigDecimal saldoResultante =
                saldoVentanilla.add(
                        valor
                );

        BigDecimal limiteMaximo =
                nvl(
                        estado.getLimiteMaximoVentanilla()
                );

        if (
                limiteMaximo.compareTo(
                        BigDecimal.ZERO
                ) > 0
                        && saldoResultante.compareTo(
                        limiteMaximo
                ) > 0
        ) {
            throw new RuntimeException(
                    "El traslado supera el límite máximo "
                            + "permitido en ventanilla. "
                            + "Límite: "
                            + limiteMaximo.toPlainString()
                            + ". Saldo resultante: "
                            + saldoResultante.toPlainString()
            );
        }

        repository.registrarCajaAVentanilla(
                estado.getIdProvision(),
                valor,
                observacion,
                idUsuario
        );
    }


    // =========================================================
    // VENTANILLA → CAJA
    // =========================================================

    private void trasladarVentanillaACaja(
            CajaVentanillaEstadoDTO estado,
            BigDecimal valor,
            String observacion,
            Integer idUsuario
    ) {

        BigDecimal saldoVentanilla =
                nvl(
                        estado.getSaldoVentanilla()
                );

        if (
                saldoVentanilla.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new RuntimeException(
                    "El saldo de ventanilla "
                            + "presenta una inconsistencia."
            );
        }

        if (
                valor.compareTo(
                        saldoVentanilla
                ) > 0
        ) {
            throw new RuntimeException(
                    "El valor a trasladar supera "
                            + "el efectivo disponible "
                            + "en ventanilla. "
                            + "Disponible: "
                            + saldoVentanilla.toPlainString()
            );
        }

        repository.registrarVentanillaACaja(
                estado.getIdProvision(),
                valor,
                observacion,
                idUsuario
        );
    }


    // =========================================================
    // VALIDACIÓN DE SOLICITUD
    // =========================================================

    private void validarSolicitud(
            CajaVentanillaMovimientoDTO dto
    ) {

        if (dto == null) {
            throw new RuntimeException(
                    "Debe indicar los datos del traslado."
            );
        }

        normalizarTipoMovimiento(
                dto.getTipoMovimiento()
        );

        normalizarValor(
                dto.getValor()
        );
    }


    // =========================================================
    // NORMALIZACIONES
    // =========================================================

    private String normalizarTipoMovimiento(
            String tipoMovimiento
    ) {

        if (
                tipoMovimiento == null
                        || tipoMovimiento.isBlank()
        ) {
            throw new RuntimeException(
                    "Debe indicar el tipo de traslado."
            );
        }

        String tipo =
                tipoMovimiento
                        .trim()
                        .toUpperCase();

        if (
                !CAJA_A_VENTANILLA.equals(tipo)
                        && !VENTANILLA_A_CAJA.equals(tipo)
        ) {
            throw new RuntimeException(
                    "El tipo de traslado no es válido."
            );
        }

        return tipo;
    }


    private BigDecimal normalizarValor(
            BigDecimal valor
    ) {

        if (valor == null) {
            throw new RuntimeException(
                    "Debe indicar el valor del traslado."
            );
        }

        if (
                valor.compareTo(
                        BigDecimal.ZERO
                ) <= 0
        ) {
            throw new RuntimeException(
                    "El valor del traslado "
                            + "debe ser mayor que cero."
            );
        }

        return valor;
    }


    private String normalizarObservacion(
            String observacion
    ) {

        if (observacion == null) {
            return null;
        }

        String valor =
                observacion.trim();

        return valor.isEmpty()
                ? null
                : valor;
    }


    private BigDecimal nvl(
            BigDecimal valor
    ) {

        return valor != null
                ? valor
                : BigDecimal.ZERO;
    }
}