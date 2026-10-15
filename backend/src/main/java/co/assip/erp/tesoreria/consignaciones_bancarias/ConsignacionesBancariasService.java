package co.assip.erp.tesoreria.consignaciones_bancarias;

import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoPreviewDTO;
import co.assip.erp.tesoreria.consignaciones_bancarias.ConsignacionesBancariasRepository;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoRequestDTO;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConsignacionesBancariasService {

    private static final String TIPO_COMPROBANTE = "CE";

    private static final String MODULO_ORIGEN = "TESORERIA";
    private static final String PROCESO_ORIGEN = "CONSIGNACION_BANCARIA";

    private static final String MEDIO_CHEQUE = "CHEQUE";
    private static final String MEDIO_EFECTIVO = "EFECTIVO";

    private final ConsignacionesBancariasRepository repository;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final ContabilidadRegistroService contabilidadRegistroService;
    private final UsuarioSesionService usuarioSesionService;

    // =========================================================
    // CONSULTAS
    // =========================================================

    public List<ConsignacionesBancariasRepository.CuentaBancariaConsignacion>
    listarCuentasBancarias(
            Integer idAgencia
    ) {

        validarAgencia(idAgencia);

        return repository.listarCuentasBancarias(
                idAgencia
        );
    }

    public List<ConsignacionesBancariasRepository.ChequeDisponible>
    listarChequesRecibidos(
            Integer idAgencia
    ) {

        validarAgencia(idAgencia);

        return repository.listarChequesRecibidos(
                idAgencia
        );
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

        return repository.listarCajasConProvision(
                idAgencia,
                fechaConsignacion
        );
    }





    // =========================================================
    // PREVIEW CONTABLE
    // =========================================================

    @Transactional(readOnly = true)
    public MovimientoFondoPreviewDTO previewConsignacion(
            MovimientoFondoRequestDTO request
    ) {

        validarRequest(request);

        Integer idAgencia =
                request.getIdAgencia();

        LocalDate fechaConsignacion =
                request.getFechaMovimiento();

        String numeroConsignacion =
                trim(request.getNumeroSoporte());

        // =====================================================
        // CUENTA BANCARIA DESTINO
        // =====================================================

        ConsignacionesBancariasRepository.CuentaBancariaConsignacion
                cuentaBancaria =
                repository.listarCuentasBancarias(idAgencia)
                        .stream()
                        .filter(
                                cuenta ->
                                        cuenta.idCuentaBancaria()
                                                .equals(request.getIdCuentaBancariaDestino())
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

        Map<Long, ConsignacionesBancariasRepository.ChequeDisponible>
                chequesDisponibles =
                new HashMap<>();

        for (ConsignacionesBancariasRepository.ChequeDisponible cheque
                : repository.listarChequesRecibidos(idAgencia)) {

            chequesDisponibles.put(
                    cheque.idChequeRecibido(),
                    cheque
            );
        }

        List<ConsignacionesBancariasRepository.ChequeDisponible>
                cheques =
                new ArrayList<>();

        BigDecimal valorCheques =
                BigDecimal.ZERO;

        for (Long idCheque : idsCheques) {

            ConsignacionesBancariasRepository.ChequeDisponible cheque =
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

        List<MovimientoFondoRequestDTO.EfectivoCajaDTO>
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

        Map<Long, ConsignacionesBancariasRepository.CajaEfectivoDisponible>
                cajasEfectivo =
                new HashMap<>();

        if (!efectivos.isEmpty()) {

            for (ConsignacionesBancariasRepository.CajaEfectivoDisponible caja
                    : repository.listarCajasConProvision(
                    idAgencia,
                    fechaConsignacion
            )) {

                cajasEfectivo.put(
                        caja.idCaja(),
                        caja
                );
            }

            for (MovimientoFondoRequestDTO.EfectivoCajaDTO efectivo
                    : efectivos) {

                ConsignacionesBancariasRepository.CajaEfectivoDisponible caja =
                        cajasEfectivo.get(
                                efectivo.getIdCaja()
                        );

                if (caja == null) {
                    throw new IllegalStateException(
                            "La caja "
                                    + efectivo.getIdCaja()
                                    + " no tiene provisión ABIERTA para la fecha "
                                    + fechaConsignacion
                                    + "."
                    );
                }

                if (!idAgencia.equals(caja.idAgencia())) {
                    throw new IllegalStateException(
                            "No se pueden mezclar cajas de diferentes agencias."
                    );
                }

                validarEfectivoDisponible(
                        caja,
                        efectivo.getValor()
                );

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

        for (ConsignacionesBancariasRepository.ChequeDisponible cheque
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

        for (MovimientoFondoRequestDTO.EfectivoCajaDTO efectivo
                : efectivos) {

            ConsignacionesBancariasRepository.CajaEfectivoDisponible caja =
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

        return MovimientoFondoPreviewDTO.builder()
                .tipoMovimiento(request.getTipoMovimiento())
                .modalidadTrasladoCaja(request.getModalidadTrasladoCaja())
                .idAgencia(request.getIdAgencia())
                .fechaMovimiento(request.getFechaMovimiento())
                .numeroSoporte(request.getNumeroSoporte())
                .idCuentaBancariaOrigen(request.getIdCuentaBancariaOrigen())
                .idCuentaBancariaDestino(request.getIdCuentaBancariaDestino())
                .idCajaOrigen(request.getIdCajaOrigen())
                .idCajaDestino(request.getIdCajaDestino())
                .tipoComprobante(TIPO_COMPROBANTE)
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
    public MovimientoFondoResponseDTO aplicarConsignacion(
            MovimientoFondoRequestDTO request
    ) {

        validarRequest(request);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Integer idDatosPersonal =
                repository.obtenerIdDatosPersonalUsuario(idUsuario);

        Integer idAgencia =
                request.getIdAgencia();

        LocalDate fechaConsignacion =
                request.getFechaMovimiento();

        String numeroConsignacion =
                trim(request.getNumeroSoporte());

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

        ConsignacionesBancariasRepository.CuentaBancariaConsignacion
                cuentaBancaria =
                repository.obtenerCuentaBancariaConLock(
                        request.getIdCuentaBancariaDestino(),
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

        List<ConsignacionesBancariasRepository.ChequeDisponible>
                cheques =
                new ArrayList<>();

        BigDecimal valorCheques =
                BigDecimal.ZERO;

        for (Long idCheque : idsCheques) {

            ConsignacionesBancariasRepository.ChequeDisponible cheque =
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

        List<MovimientoFondoRequestDTO.EfectivoCajaDTO>
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
// CAJAS PARTICIPANTES Y BLOQUEO ORDENADO
// =====================================================

        Map<Long, Long> provisionesCheques = new HashMap<>();

        Map<Long, ConsignacionesBancariasRepository.CajaEfectivoDisponible>
                cajasBloqueadas = new HashMap<>();

        List<Long> idsCajasCheques = cheques.stream()
                .map(ConsignacionesBancariasRepository.ChequeDisponible::idCaja)
                .distinct()
                .toList();

        List<Long> idsCajasEfectivo = efectivos.stream()
                .map(MovimientoFondoRequestDTO.EfectivoCajaDTO::getIdCaja)
                .distinct()
                .toList();

        List<Long> idsCajasParticipantes =
                java.util.stream.Stream.concat(
                                idsCajasCheques.stream(),
                                idsCajasEfectivo.stream()
                        )
                        .distinct()
                        .sorted()
                        .toList();

        for (Long idCaja : idsCajasParticipantes) {

            boolean tieneCheques = idsCajasCheques.contains(idCaja);
            boolean tieneEfectivo = idsCajasEfectivo.contains(idCaja);

            // La caja se bloquea primero por efectivo si participa
            // con ambos medios, reutilizando su provisión.

            if (tieneEfectivo) {

                ConsignacionesBancariasRepository.CajaEfectivoDisponible caja =
                        repository.obtenerCajaConProvisionConLock(
                                idCaja,
                                idAgencia,
                                fechaConsignacion
                        );

                if (caja == null) {
                    throw new IllegalStateException(
                            "La caja " + idCaja
                                    + " no tiene provisión ABIERTA "
                                    + "para consignar efectivo en "
                                    + fechaConsignacion + "."
                    );
                }

                cajasBloqueadas.put(idCaja, caja);

                if (tieneCheques) {
                    provisionesCheques.put(
                            idCaja,
                            caja.idProvision()
                    );
                }

            } else if (tieneCheques) {

                Long idProvision =
                        repository.obtenerProvisionChequeConLock(
                                idCaja,
                                idAgencia,
                                fechaConsignacion
                        );

                if (idProvision == null) {
                    throw new IllegalStateException(
                            "La caja " + idCaja
                                    + " no tiene provisión ABIERTA "
                                    + "para consignar cheques en "
                                    + fechaConsignacion + "."
                    );
                }

                provisionesCheques.put(idCaja, idProvision);
            }
        }

        // =====================================================
        // VALIDAR EFECTIVO DISPONIBLE ANTES DE REGISTRAR
        // =====================================================

        for (MovimientoFondoRequestDTO.EfectivoCajaDTO efectivo : efectivos) {

            ConsignacionesBancariasRepository.CajaEfectivoDisponible caja =
                    cajasBloqueadas.get(efectivo.getIdCaja());

            if (caja == null) {
                throw new IllegalStateException(
                        "No fue posible validar el efectivo de la caja "
                                + efectivo.getIdCaja() + "."
                );
            }

            validarEfectivoDisponible(
                    caja,
                    efectivo.getValor()
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

        for (ConsignacionesBancariasRepository.ChequeDisponible cheque
                : cheques) {

            Long idProvision =
                    provisionesCheques.get(cheque.idCaja());

            if (idProvision == null) {
                throw new IllegalStateException(
                        "No se encontró la provisión de la jornada "
                                + "para la salida del cheque "
                                + cheque.numeroCheque() + "."
                );
            }

            Long idMovimientoCaja =
                    repository.insertarMovimientoCaja(
                            idProvision,
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

        for (MovimientoFondoRequestDTO.EfectivoCajaDTO efectivo
                : efectivos) {

            ConsignacionesBancariasRepository.CajaEfectivoDisponible caja =
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
                        numeroConsignacion,
                        idDatosPersonal
                )
        );

        // -----------------------------------------------------
        // CRÉDITO POR CADA CHEQUE
        // -----------------------------------------------------

        for (ConsignacionesBancariasRepository.ChequeDisponible cheque
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
                            numeroConsignacion,
                            idDatosPersonal
                    )
            );
        }

        // -----------------------------------------------------
        // CRÉDITO POR EFECTIVO DE CADA CAJA
        // -----------------------------------------------------

        for (MovimientoFondoRequestDTO.EfectivoCajaDTO efectivo
                : efectivos) {

            ConsignacionesBancariasRepository.CajaEfectivoDisponible caja =
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
                            numeroConsignacion,
                            idDatosPersonal
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

        for (ConsignacionesBancariasRepository.ChequeDisponible cheque
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

        return new MovimientoFondoResponseDTO(
                null, // No existe todavía ID persistido de movimiento general.
                request.getTipoMovimiento(),
                idAgencia,
                fechaConsignacion,
                numeroConsignacion,
                request.getObservacion(),
                null, null, null, null, null,
                cuentaBancaria.idCuentaBancaria(),
                cuentaBancaria.codigoBanco(),
                cuentaBancaria.nombreBanco(),
                cuentaBancaria.numeroCuenta(),
                cuentaBancaria.nombreCuenta(),
                null, null, null, null, null,
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
    // TRASLADOS DE EFECTIVO ENTRE CONTROL Y OPERATIVA
    // =========================================================



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
            String documentoSoporte,
            Integer idDatosPersonal
    ) {

        MovimientoContableDTO movimiento =
                new MovimientoContableDTO();

        movimiento.setIdDatosPersonal(idDatosPersonal);

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
            MovimientoFondoRequestDTO request,
            String numeroConsignacion,
            ConsignacionesBancariasRepository.CuentaBancariaConsignacion cuentaBancaria
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
    // VALIDAR QUE EL RETIRO NO SE MAYOR QUE EL VALOR EN CAJA
    // =========================================================

    private void validarEfectivoDisponible(
            ConsignacionesBancariasRepository.CajaEfectivoDisponible caja,
            BigDecimal valorSolicitado
    ) {

        BigDecimal disponible = nvl(caja.efectivoDisponible());
        BigDecimal solicitado = nvl(valorSolicitado);

        if (solicitado.compareTo(disponible) > 0) {
            throw new IllegalStateException(
                    "La caja " + caja.codigoCaja()
                            + " tiene $" + disponible.toPlainString()
                            + " disponibles. No puede consignar $"
                            + solicitado.toPlainString() + "."
            );
        }
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarRequest(
            MovimientoFondoRequestDTO request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud de consignación es obligatoria."
            );
        }

        if (request.getTipoMovimiento() != MovimientoFondoRequestDTO.TipoMovimiento.CONSIGNACION) {
            throw new IllegalArgumentException(
                    "Esta operación aún no está implementada. Solo se admite CONSIGNACION."
            );
        }

        validarAgencia(
                request.getIdAgencia()
        );

        if (request.getIdCuentaBancariaDestino() == null) {
            throw new IllegalArgumentException(
                    "La cuenta bancaria destino es obligatoria."
            );
        }

        if (request.getFechaMovimiento() == null) {
            throw new IllegalArgumentException(
                    "La fecha de consignación es obligatoria."
            );
        }

        if (isBlank(
                request.getNumeroSoporte()
        )) {
            throw new IllegalArgumentException(
                    "El número de consignación es obligatorio."
            );
        }

        if (trim(
                request.getNumeroSoporte()
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

    private List<MovimientoFondoRequestDTO.EfectivoCajaDTO>
    normalizarEfectivos(
            List<MovimientoFondoRequestDTO.EfectivoCajaDTO> efectivos
    ) {

        if (efectivos == null
                || efectivos.isEmpty()) {

            return List.of();
        }

        Map<Long, BigDecimal> valoresPorCaja =
                new HashMap<>();

        for (MovimientoFondoRequestDTO.EfectivoCajaDTO item
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

        List<MovimientoFondoRequestDTO.EfectivoCajaDTO>
                resultado =
                new ArrayList<>();

        for (Long idCaja : idsCajas) {

            MovimientoFondoRequestDTO.EfectivoCajaDTO item =
                    new MovimientoFondoRequestDTO.EfectivoCajaDTO();

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
