package co.assip.erp.tesoreria.consignaciones_bancarias;

import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.tesoreria.consignaciones_bancarias.dto.ConsignacionBancariaPreviewResponse;
import co.assip.erp.tesoreria.consignaciones_bancarias.dto.ConsignacionBancariaRequest;
import co.assip.erp.tesoreria.consignaciones_bancarias.dto.ConsignacionBancariaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConsignacionBancariaService {

    private static final String TIPO_COMPROBANTE = "CE";

    private static final String MODULO_ORIGEN = "TESORERIA";
    private static final String PROCESO_ORIGEN = "CONSIGNACION_BANCARIA";

    private static final String MEDIO_CHEQUE = "CHEQUE";
    private static final String MEDIO_EFECTIVO = "EFECTIVO";

    private final ConsignacionBancariaRepository repository;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final ContabilidadRegistroService contabilidadRegistroService;
    private final UsuarioSesionService usuarioSesionService;

    // =========================================================
    // CONSULTAS
    // =========================================================

    public List<ConsignacionBancariaRepository.CuentaBancariaConsignacion>
    listarCuentasBancarias(
            Integer idAgencia
    ) {

        validarAgencia(idAgencia);

        return repository.listarCuentasBancarias(
                idAgencia
        );
    }

    public List<ConsignacionBancariaRepository.ChequeDisponible>
    listarChequesRecibidos(
            Integer idAgencia
    ) {

        validarAgencia(idAgencia);

        return repository.listarChequesRecibidos(
                idAgencia
        );
    }

    public List<ConsignacionBancariaRepository.CajaEfectivoDisponible>
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

        return repository.listarCajasConProvision(
                idAgencia,
                fechaConsignacion
        );
    }

    // =========================================================
    // PREVIEW CONTABLE
    // =========================================================

    @Transactional(readOnly = true)
    public ConsignacionBancariaPreviewResponse preview(
            ConsignacionBancariaRequest request
    ) {

        validarRequest(request);

        Integer idAgencia =
                request.getIdAgencia();

        LocalDate fechaConsignacion =
                request.getFechaConsignacion();

        String numeroConsignacion =
                trim(request.getNumeroConsignacion());

        // =====================================================
        // CUENTA BANCARIA DESTINO
        // =====================================================

        ConsignacionBancariaRepository.CuentaBancariaConsignacion
                cuentaBancaria =
                repository.listarCuentasBancarias(idAgencia)
                        .stream()
                        .filter(
                                cuenta ->
                                        cuenta.idCuentaBancaria()
                                                .equals(request.getIdCuentaBancaria())
                        )
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "La cuenta bancaria seleccionada no está habilitada "
                                                + "para consignaciones de esta agencia."
                                )
                        );

        // =====================================================
        // CHEQUES SELECCIONADOS
        // =====================================================

        List<Long> idsCheques =
                normalizarIdsCheques(
                        request.getIdsCheques()
                );

        Map<Long, ConsignacionBancariaRepository.ChequeDisponible>
                chequesDisponibles =
                new HashMap<>();

        for (ConsignacionBancariaRepository.ChequeDisponible cheque
                : repository.listarChequesRecibidos(idAgencia)) {

            chequesDisponibles.put(
                    cheque.idChequeRecibido(),
                    cheque
            );
        }

        List<ConsignacionBancariaRepository.ChequeDisponible>
                cheques =
                new ArrayList<>();

        BigDecimal valorCheques =
                BigDecimal.ZERO;

        for (Long idCheque : idsCheques) {

            ConsignacionBancariaRepository.ChequeDisponible cheque =
                    chequesDisponibles.get(idCheque);

            if (cheque == null) {
                throw new IllegalStateException(
                        "El cheque "
                                + idCheque
                                + " ya no está disponible para consignación."
                );
            }

            if (!idAgencia.equals(cheque.idAgencia())) {
                throw new IllegalStateException(
                        "No se pueden mezclar cheques de diferentes agencias."
                );
            }

            if (nvl(cheque.valorCheque())
                    .compareTo(BigDecimal.ZERO) <= 0) {

                throw new IllegalStateException(
                        "El cheque "
                                + cheque.numeroCheque()
                                + " tiene un valor inválido."
                );
            }

            cheques.add(cheque);

            valorCheques =
                    valorCheques.add(
                            cheque.valorCheque()
                    );
        }

        // =====================================================
        // EFECTIVO
        // =====================================================

        List<ConsignacionBancariaRequest.EfectivoCajaRequest>
                efectivos =
                normalizarEfectivos(
                        request.getEfectivos()
                );

        BigDecimal valorEfectivo =
                efectivos.stream()
                        .map(
                                item -> nvl(item.getValor())
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal valorTotal =
                valorCheques.add(valorEfectivo);

        if (valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "La consignación debe tener un valor mayor a cero."
            );
        }

        // =====================================================
        // CAJAS PARA EFECTIVO
        // =====================================================

        Map<Long, ConsignacionBancariaRepository.CajaEfectivoDisponible>
                cajasEfectivo =
                new HashMap<>();

        if (!efectivos.isEmpty()) {

            for (ConsignacionBancariaRepository.CajaEfectivoDisponible caja
                    : repository.listarCajasConProvision(
                    idAgencia,
                    fechaConsignacion
            )) {

                cajasEfectivo.put(
                        caja.idCaja(),
                        caja
                );
            }

            for (ConsignacionBancariaRequest.EfectivoCajaRequest efectivo
                    : efectivos) {

                ConsignacionBancariaRepository.CajaEfectivoDisponible caja =
                        cajasEfectivo.get(
                                efectivo.getIdCaja()
                        );

                if (caja == null) {
                    throw new IllegalStateException(
                            "La caja "
                                    + efectivo.getIdCaja()
                                    + " no tiene una provisión creada para la fecha "
                                    + fechaConsignacion
                                    + "."
                    );
                }

                if (!idAgencia.equals(caja.idAgencia())) {
                    throw new IllegalStateException(
                            "No se pueden mezclar cajas de diferentes agencias."
                    );
                }
            }
        }

        // =====================================================
        // CONCEPTO
        // =====================================================

        String conceptoComprobante =
                construirConceptoComprobante(
                        request,
                        numeroConsignacion,
                        cuentaBancaria
                );

        // =====================================================
        // MOVIMIENTOS DEL PREVIEW
        // =====================================================

        List<MovimientoContablePreviewDTO> movimientos =
                new ArrayList<>();

        // Débito banco destino

        movimientos.add(
                crearMovimientoPreview(
                        cuentaBancaria.idCuentaContable(),
                        cuentaBancaria.codigoCuentaContable(),
                        cuentaBancaria.nombreCuentaContable(),
                        idAgencia,
                        fechaConsignacion,
                        valorTotal,
                        BigDecimal.ZERO,
                        "Consignacion bancaria "
                                + numeroConsignacion
                                + " "
                                + valorSeguro(
                                cuentaBancaria.nombreBanco()
                        )
                )
        );

        // Crédito por cada cheque

        for (ConsignacionBancariaRepository.ChequeDisponible cheque
                : cheques) {

            movimientos.add(
                    crearMovimientoPreview(
                            cheque.idCuentaContableCaja(),
                            cheque.codigoCuentaContableCaja(),
                            cheque.nombreCuentaContableCaja(),
                            idAgencia,
                            fechaConsignacion,
                            BigDecimal.ZERO,
                            cheque.valorCheque(),
                            "Consignacion cheque "
                                    + valorSeguro(
                                    cheque.numeroCheque()
                            )
                                    + " caja "
                                    + valorSeguro(
                                    cheque.codigoCaja()
                            )
                    )
            );
        }

        // Crédito por efectivo

        for (ConsignacionBancariaRequest.EfectivoCajaRequest efectivo
                : efectivos) {

            ConsignacionBancariaRepository.CajaEfectivoDisponible caja =
                    cajasEfectivo.get(
                            efectivo.getIdCaja()
                    );

            movimientos.add(
                    crearMovimientoPreview(
                            caja.idCuentaContableCaja(),
                            caja.codigoCuentaContableCaja(),
                            caja.nombreCuentaContableCaja(),
                            idAgencia,
                            fechaConsignacion,
                            BigDecimal.ZERO,
                            efectivo.getValor(),
                            "Consignacion efectivo caja "
                                    + valorSeguro(
                                    caja.codigoCaja()
                            )
                    )
            );
        }

        BigDecimal totalDebito =
                movimientos.stream()
                        .map(
                                movimiento ->
                                        nvl(movimiento.getValorDebito())
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalCredito =
                movimientos.stream()
                        .map(
                                movimiento ->
                                        nvl(movimiento.getValorCredito())
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal diferencia =
                totalDebito.subtract(totalCredito);

        return ConsignacionBancariaPreviewResponse.builder()
                .movimientos(movimientos)
                .totalDebito(totalDebito)
                .totalCredito(totalCredito)
                .diferencia(diferencia)
                .cuadrado(
                        diferencia.compareTo(BigDecimal.ZERO) == 0
                )
                .valorCheques(valorCheques)
                .valorEfectivo(valorEfectivo)
                .valorTotal(valorTotal)
                .conceptoComprobante(conceptoComprobante)
                .build();
    }

    // =========================================================
    // APLICAR
    // =========================================================

    @Transactional
    public ConsignacionBancariaResponse aplicar(
            ConsignacionBancariaRequest request
    ) {

        validarRequest(request);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Integer idAgencia =
                request.getIdAgencia();

        LocalDate fechaConsignacion =
                request.getFechaConsignacion();

        String numeroConsignacion =
                trim(request.getNumeroConsignacion());

        // =====================================================
        // PARÁMETROS DE CAJA
        // =====================================================

        Long idDepartamento =
                repository.obtenerDepartamentoConsignacionesBancarias();

        if (idDepartamento == null) {
            throw new IllegalStateException(
                    "No se encuentra configurado el departamento "
                            + "19 - CONSIGNACIONES BANCARIAS."
            );
        }

        Long idOperacion =
                repository.obtenerOperacionConsignacionBanco();

        if (idOperacion == null) {
            throw new IllegalStateException(
                    "No se encuentra configurada la operación "
                            + "CONSIGNACION_BANCO."
            );
        }

        // =====================================================
        // CUENTA BANCARIA DESTINO
        // =====================================================

        ConsignacionBancariaRepository.CuentaBancariaConsignacion
                cuentaBancaria =
                repository.obtenerCuentaBancariaConLock(
                        request.getIdCuentaBancaria(),
                        idAgencia
                );

        if (cuentaBancaria == null) {
            throw new IllegalStateException(
                    "La cuenta bancaria seleccionada no está habilitada "
                            + "para consignaciones de esta agencia."
            );
        }

        // =====================================================
        // CHEQUES
        //
        // Orden determinístico antes de FOR UPDATE.
        // =====================================================

        List<Long> idsCheques =
                normalizarIdsCheques(
                        request.getIdsCheques()
                );

        List<ConsignacionBancariaRepository.ChequeDisponible>
                cheques =
                new ArrayList<>();

        BigDecimal valorCheques =
                BigDecimal.ZERO;

        for (Long idCheque : idsCheques) {

            ConsignacionBancariaRepository.ChequeDisponible cheque =
                    repository.obtenerChequeConLock(
                            idCheque,
                            idAgencia
                    );

            if (cheque == null) {
                throw new IllegalStateException(
                        "El cheque "
                                + idCheque
                                + " ya no está disponible para consignación."
                );
            }

            if (!idAgencia.equals(
                    cheque.idAgencia()
            )) {
                throw new IllegalStateException(
                        "No se pueden mezclar cheques de diferentes agencias."
                );
            }

            if (nvl(cheque.valorCheque())
                    .compareTo(BigDecimal.ZERO) <= 0) {

                throw new IllegalStateException(
                        "El cheque "
                                + cheque.numeroCheque()
                                + " tiene un valor inválido."
                );
            }

            cheques.add(
                    cheque
            );

            valorCheques =
                    valorCheques.add(
                            cheque.valorCheque()
                    );
        }

        // =====================================================
        // EFECTIVO
        //
        // El DTO permite varias cajas.
        // =====================================================

        List<ConsignacionBancariaRequest.EfectivoCajaRequest>
                efectivos =
                normalizarEfectivos(
                        request.getEfectivos()
                );

        BigDecimal valorEfectivo =
                efectivos.stream()
                        .map(
                                item -> nvl(
                                        item.getValor()
                                )
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        if (valorCheques
                .add(valorEfectivo)
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "La consignación debe tener un valor mayor a cero."
            );
        }

        // =====================================================
        // CAJAS QUE PARTICIPAN
        //
        // Se bloquean en orden de idCaja para mantener
        // un orden determinístico de concurrencia.
        // =====================================================

        List<Long> idsCajas =
                new ArrayList<>();

        for (ConsignacionBancariaRepository.ChequeDisponible cheque
                : cheques) {

            if (!idsCajas.contains(
                    cheque.idCaja()
            )) {
                idsCajas.add(
                        cheque.idCaja()
                );
            }
        }

        for (ConsignacionBancariaRequest.EfectivoCajaRequest efectivo
                : efectivos) {

            if (!idsCajas.contains(
                    efectivo.getIdCaja()
            )) {
                idsCajas.add(
                        efectivo.getIdCaja()
                );
            }
        }

        idsCajas.sort(
                Comparator.naturalOrder()
        );

        Map<Long, ConsignacionBancariaRepository.CajaEfectivoDisponible>
                cajasBloqueadas =
                new HashMap<>();

        for (Long idCaja : idsCajas) {

            ConsignacionBancariaRepository.CajaEfectivoDisponible caja =
                    repository.obtenerCajaConProvisionConLock(
                            idCaja,
                            idAgencia,
                            fechaConsignacion
                    );

            if (caja == null) {
                throw new IllegalStateException(
                        "La caja "
                                + idCaja
                                + " no tiene una provisión creada para la fecha "
                                + fechaConsignacion
                                + "."
                );
            }

            if (!idAgencia.equals(
                    caja.idAgencia()
            )) {
                throw new IllegalStateException(
                        "No se pueden mezclar cajas de diferentes agencias."
                );
            }

            cajasBloqueadas.put(
                    idCaja,
                    caja
            );
        }

        // =====================================================
        // CONSECUTIVO CONTABLE CE
        //
        // ConsecutivosComprobantesService realiza FOR UPDATE.
        // =====================================================

        String numeroComprobante =
                consecutivosComprobantesService.generarNumeroDefinitivo(
                        TIPO_COMPROBANTE,
                        idAgencia,
                        idUsuario
                );

        if (isBlank(numeroComprobante)) {
            throw new IllegalStateException(
                    "No fue posible generar el consecutivo CE."
            );
        }

        // =====================================================
        // MOVIMIENTOS FÍSICOS DE CAJA
        // =====================================================

        List<Long> idsMovimientosCaja =
                new ArrayList<>();

        // -----------------------------------------------------
        // SALIDA INDIVIDUAL DE CADA CHEQUE
        // -----------------------------------------------------

        for (ConsignacionBancariaRepository.ChequeDisponible cheque
                : cheques) {

            ConsignacionBancariaRepository.CajaEfectivoDisponible caja =
                    cajasBloqueadas.get(
                            cheque.idCaja()
                    );

            if (caja == null) {
                throw new IllegalStateException(
                        "No fue posible determinar la caja del cheque "
                                + cheque.numeroCheque()
                                + "."
                );
            }

            Long idMovimientoCaja =
                    repository.insertarMovimientoCaja(
                            caja.idProvision(),
                            cheque.idCaja(),
                            idDepartamento,
                            idOperacion,
                            numeroConsignacion,
                            limitar(
                                    cheque.numeroCheque(),
                                    20
                            ),
                            MEDIO_CHEQUE,
                            cheque.valorCheque(),
                            limitar(
                                    "Salida cheque "
                                            + valorSeguro(
                                            cheque.numeroCheque()
                                    )
                                            + " caja "
                                            + valorSeguro(
                                            cheque.codigoCaja()
                                    )
                                            + " consignacion "
                                            + numeroConsignacion,
                                    250
                            ),
                            idUsuario
                    );

            idsMovimientosCaja.add(
                    idMovimientoCaja
            );
        }

        // -----------------------------------------------------
        // SALIDA DE EFECTIVO
        // -----------------------------------------------------

        for (ConsignacionBancariaRequest.EfectivoCajaRequest efectivo
                : efectivos) {

            ConsignacionBancariaRepository.CajaEfectivoDisponible caja =
                    cajasBloqueadas.get(
                            efectivo.getIdCaja()
                    );

            if (caja == null) {
                throw new IllegalStateException(
                        "No fue posible determinar la caja "
                                + efectivo.getIdCaja()
                                + " para la salida de efectivo."
                );
            }

            Long idMovimientoCaja =
                    repository.insertarMovimientoCaja(
                            caja.idProvision(),
                            caja.idCaja(),
                            idDepartamento,
                            idOperacion,
                            numeroConsignacion,
                            limitar(
                                    caja.codigoCaja(),
                                    20
                            ),
                            MEDIO_EFECTIVO,
                            efectivo.getValor(),
                            limitar(
                                    "Salida efectivo caja "
                                            + valorSeguro(
                                            caja.codigoCaja()
                                    )
                                            + " consignacion "
                                            + numeroConsignacion,
                                    250
                            ),
                            idUsuario
                    );

            idsMovimientosCaja.add(
                    idMovimientoCaja
            );
        }

        // =====================================================
        // CONTABILIDAD
        // =====================================================

        BigDecimal valorTotal =
                valorCheques.add(
                        valorEfectivo
                );

        List<MovimientoContableDTO> movimientosContables =
                new ArrayList<>();

        // -----------------------------------------------------
        // DÉBITO BANCO DESTINO
        // -----------------------------------------------------

        movimientosContables.add(
                crearMovimientoContable(
                        cuentaBancaria.idCuentaContable(),
                        idAgencia,
                        fechaConsignacion,
                        numeroComprobante,
                        valorTotal,
                        BigDecimal.ZERO,
                        "Consignacion bancaria "
                                + numeroConsignacion
                                + " "
                                + valorSeguro(
                                cuentaBancaria.nombreBanco()
                        ),
                        numeroConsignacion
                )
        );

        // -----------------------------------------------------
        // CRÉDITO POR CADA CHEQUE
        // -----------------------------------------------------

        for (ConsignacionBancariaRepository.ChequeDisponible cheque
                : cheques) {

            movimientosContables.add(
                    crearMovimientoContable(
                            cheque.idCuentaContableCaja(),
                            idAgencia,
                            fechaConsignacion,
                            numeroComprobante,
                            BigDecimal.ZERO,
                            cheque.valorCheque(),
                            "Consignacion cheque "
                                    + valorSeguro(
                                    cheque.numeroCheque()
                            )
                                    + " caja "
                                    + valorSeguro(
                                    cheque.codigoCaja()
                            ),
                            numeroConsignacion
                    )
            );
        }

        // -----------------------------------------------------
        // CRÉDITO POR EFECTIVO DE CADA CAJA
        // -----------------------------------------------------

        for (ConsignacionBancariaRequest.EfectivoCajaRequest efectivo
                : efectivos) {

            ConsignacionBancariaRepository.CajaEfectivoDisponible caja =
                    cajasBloqueadas.get(
                            efectivo.getIdCaja()
                    );

            movimientosContables.add(
                    crearMovimientoContable(
                            caja.idCuentaContableCaja(),
                            idAgencia,
                            fechaConsignacion,
                            numeroComprobante,
                            BigDecimal.ZERO,
                            efectivo.getValor(),
                            "Consignacion efectivo caja "
                                    + valorSeguro(
                                    caja.codigoCaja()
                            ),
                            numeroConsignacion
                    )
            );
        }

        validarCuadre(
                movimientosContables
        );

        // =====================================================
        // ORIGEN CONTABLE
        // =====================================================

        OrigenComprobanteDTO origen =
                new OrigenComprobanteDTO();

        origen.setOrigenTipo(
                "AUTO"
        );

        origen.setModuloOrigen(
                MODULO_ORIGEN
        );

        origen.setProcesoOrigen(
                PROCESO_ORIGEN
        );

        origen.setTablaOrigen(
                null
        );

        origen.setIdOrigen(
                null
        );

        String conceptoComprobante =
                construirConceptoComprobante(
                        request,
                        numeroConsignacion,
                        cuentaBancaria
                );

        contabilidadRegistroService.registrarComprobante(
                idAgencia,
                TIPO_COMPROBANTE,
                numeroComprobante,
                conceptoComprobante,
                origen,
                movimientosContables,
                idUsuario
        );

        // =====================================================
        // ASOCIAR MOVIMIENTOS DE CAJA AL CE
        // =====================================================

        for (Long idMovimientoCaja : idsMovimientosCaja) {

            repository.asociarComprobanteContable(
                    idMovimientoCaja,
                    numeroComprobante,
                    idUsuario
            );
        }

        // =====================================================
        // CHEQUES: RECIBIDO -> CONSIGNADO
        // =====================================================

        for (ConsignacionBancariaRepository.ChequeDisponible cheque
                : cheques) {

            repository.marcarChequeConsignado(
                    cheque.idChequeRecibido(),
                    fechaConsignacion,
                    numeroConsignacion,
                    idUsuario
            );
        }

        // =====================================================
        // RESPUESTA
        // =====================================================

        return new ConsignacionBancariaResponse(
                idAgencia,
                fechaConsignacion,
                numeroConsignacion,
                cuentaBancaria.idCuentaBancaria(),
                cuentaBancaria.codigoBanco(),
                cuentaBancaria.nombreBanco(),
                cuentaBancaria.numeroCuenta(),
                cuentaBancaria.nombreCuenta(),
                cheques.size(),
                valorCheques,
                valorEfectivo,
                valorTotal,
                TIPO_COMPROBANTE,
                numeroComprobante,
                conceptoComprobante
        );
    }

    // =========================================================
    // MOVIMIENTO CONTABLE
    // =========================================================

    private MovimientoContableDTO crearMovimientoContable(
            Integer idCatalogoCuenta,
            Integer idAgencia,
            LocalDate fecha,
            String numeroComprobante,
            BigDecimal debito,
            BigDecimal credito,
            String detalle,
            String documentoSoporte
    ) {

        MovimientoContableDTO movimiento =
                new MovimientoContableDTO();

        movimiento.setIdCatalogoCuenta(
                idCatalogoCuenta
        );

        movimiento.setIdAgencia(
                idAgencia
        );

        movimiento.setFechaAuxiliar(
                fecha
        );

        movimiento.setTipoComprobante(
                TIPO_COMPROBANTE
        );

        movimiento.setNumeroComprobante(
                numeroComprobante
        );

        movimiento.setDetalleMovimiento(
                limitar(
                        detalle,
                        100
                )
        );

        movimiento.setValorDebito(
                nvl(debito)
        );

        movimiento.setValorCredito(
                nvl(credito)
        );

        movimiento.setValorBase(
                BigDecimal.ZERO
        );

        movimiento.setDocumentoSoporte(
                limitar(
                        documentoSoporte,
                        20
                )
        );

        movimiento.setOrigenModulo(
                MODULO_ORIGEN
        );

        movimiento.setDocumentoOrigen(
                PROCESO_ORIGEN
        );

        return movimiento;
    }

    // =========================================================
    // MOVIMIENTO CONTABLE PREVIEW
    // =========================================================

    private MovimientoContablePreviewDTO crearMovimientoPreview(
            Integer idCatalogoCuenta,
            String codigoCuenta,
            String nombreCuenta,
            Integer idAgencia,
            LocalDate fecha,
            BigDecimal debito,
            BigDecimal credito,
            String detalle
    ) {

        MovimientoContablePreviewDTO movimiento =
                new MovimientoContablePreviewDTO();

        movimiento.setIdAgencia(
                idAgencia
        );

        movimiento.setFechaAuxiliar(
                fecha
        );

        movimiento.setIdCatalogoCuenta(
                idCatalogoCuenta
        );

        movimiento.setCodigoCuenta(
                codigoCuenta
        );

        movimiento.setNombreCuenta(
                nombreCuenta
        );

        movimiento.setDetalleMovimiento(
                limitar(
                        detalle,
                        100
                )
        );

        movimiento.setValorDebito(
                nvl(debito)
        );

        movimiento.setValorCredito(
                nvl(credito)
        );

        return movimiento;
    }

    // =========================================================
    // CONCEPTO DEL COMPROBANTE
    // =========================================================

    private String construirConceptoComprobante(
            ConsignacionBancariaRequest request,
            String numeroConsignacion,
            ConsignacionBancariaRepository.CuentaBancariaConsignacion cuentaBancaria
    ) {

        return limitar(
                isBlank(request.getObservacion())
                        ? "Consignacion bancaria No. "
                        + numeroConsignacion
                        + " - "
                        + valorSeguro(
                        cuentaBancaria.nombreBanco()
                )
                        + " - Cuenta "
                        + valorSeguro(
                        cuentaBancaria.numeroCuenta()
                )
                        : trim(
                        request.getObservacion()
                ),
                250
        );
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarRequest(
            ConsignacionBancariaRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud de consignación es obligatoria."
            );
        }

        validarAgencia(
                request.getIdAgencia()
        );

        if (request.getIdCuentaBancaria() == null) {
            throw new IllegalArgumentException(
                    "La cuenta bancaria destino es obligatoria."
            );
        }

        if (request.getFechaConsignacion() == null) {
            throw new IllegalArgumentException(
                    "La fecha de consignación es obligatoria."
            );
        }

        if (isBlank(
                request.getNumeroConsignacion()
        )) {
            throw new IllegalArgumentException(
                    "El número de consignación es obligatorio."
            );
        }

        if (trim(
                request.getNumeroConsignacion()
        ).length() > 20) {

            throw new IllegalArgumentException(
                    "El número de consignación no puede superar 20 caracteres."
            );
        }

        boolean tieneCheques =
                request.getIdsCheques() != null
                        && !request.getIdsCheques().isEmpty();

        boolean tieneEfectivo =
                request.getEfectivos() != null
                        && request.getEfectivos()
                        .stream()
                        .anyMatch(
                                item ->
                                        item != null
                                                && nvl(
                                                item.getValor()
                                        ).compareTo(
                                                BigDecimal.ZERO
                                        ) > 0
                        );

        if (!tieneCheques && !tieneEfectivo) {
            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un cheque "
                            + "o registrar efectivo."
            );
        }
    }

    private void validarAgencia(
            Integer idAgencia
    ) {

        if (idAgencia == null
                || idAgencia <= 0) {

            throw new IllegalArgumentException(
                    "La agencia es obligatoria."
            );
        }
    }

    // =========================================================
    // NORMALIZAR CHEQUES
    // =========================================================

    private List<Long> normalizarIdsCheques(
            List<Long> idsCheques
    ) {

        if (idsCheques == null
                || idsCheques.isEmpty()) {

            return List.of();
        }

        List<Long> resultado =
                idsCheques.stream()
                        .filter(
                                id ->
                                        id != null
                                                && id > 0
                        )
                        .distinct()
                        .sorted()
                        .toList();

        if (resultado.size()
                != idsCheques.size()) {

            throw new IllegalArgumentException(
                    "La selección de cheques contiene registros "
                            + "nulos, inválidos o repetidos."
            );
        }

        return resultado;
    }

    // =========================================================
    // NORMALIZAR EFECTIVO
    // =========================================================

    private List<ConsignacionBancariaRequest.EfectivoCajaRequest>
    normalizarEfectivos(
            List<ConsignacionBancariaRequest.EfectivoCajaRequest> efectivos
    ) {

        if (efectivos == null
                || efectivos.isEmpty()) {

            return List.of();
        }

        Map<Long, BigDecimal> valoresPorCaja =
                new HashMap<>();

        for (ConsignacionBancariaRequest.EfectivoCajaRequest item
                : efectivos) {

            if (item == null) {
                throw new IllegalArgumentException(
                        "Existe un registro de efectivo inválido."
                );
            }

            if (item.getIdCaja() == null
                    || item.getIdCaja() <= 0) {

                throw new IllegalArgumentException(
                        "La caja del efectivo es obligatoria."
                );
            }

            BigDecimal valor =
                    nvl(
                            item.getValor()
                    );

            if (valor.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "El valor en efectivo no puede ser negativo."
                );
            }

            if (valor.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            valoresPorCaja.merge(
                    item.getIdCaja(),
                    valor,
                    BigDecimal::add
            );
        }

        List<Long> idsCajas =
                valoresPorCaja.keySet()
                        .stream()
                        .sorted()
                        .toList();

        List<ConsignacionBancariaRequest.EfectivoCajaRequest>
                resultado =
                new ArrayList<>();

        for (Long idCaja : idsCajas) {

            ConsignacionBancariaRequest.EfectivoCajaRequest item =
                    new ConsignacionBancariaRequest.EfectivoCajaRequest();

            item.setIdCaja(
                    idCaja
            );

            item.setValor(
                    valoresPorCaja.get(
                            idCaja
                    )
            );

            resultado.add(
                    item
            );
        }

        return resultado;
    }

    // =========================================================
    // CUADRE CONTABLE
    // =========================================================

    private void validarCuadre(
            List<MovimientoContableDTO> movimientos
    ) {

        BigDecimal debitos =
                BigDecimal.ZERO;

        BigDecimal creditos =
                BigDecimal.ZERO;

        for (MovimientoContableDTO movimiento : movimientos) {

            debitos =
                    debitos.add(
                            nvl(
                                    movimiento.getValorDebito()
                            )
                    );

            creditos =
                    creditos.add(
                            nvl(
                                    movimiento.getValorCredito()
                            )
                    );
        }

        if (debitos.compareTo(
                creditos
        ) != 0) {

            throw new IllegalStateException(
                    "La consignación bancaria no cuadra contablemente. "
                            + "Débitos: "
                            + debitos
                            + " - Créditos: "
                            + creditos
                            + "."
            );
        }
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private BigDecimal nvl(
            BigDecimal valor
    ) {

        return valor != null
                ? valor
                : BigDecimal.ZERO;
    }

    private boolean isBlank(
            String valor
    ) {

        return valor == null
                || valor.trim().isEmpty();
    }

    private String trim(
            String valor
    ) {

        return valor == null
                ? null
                : valor.trim();
    }

    private String limitar(
            String valor,
            int longitud
    ) {

        if (valor == null) {
            return null;
        }

        String limpio =
                valor.trim();

        if (limpio.length()
                <= longitud) {

            return limpio;
        }

        return limpio.substring(
                0,
                longitud
        );
    }

    private String valorSeguro(
            Object valor
    ) {

        return valor == null
                ? ""
                : valor.toString().trim();
    }
}