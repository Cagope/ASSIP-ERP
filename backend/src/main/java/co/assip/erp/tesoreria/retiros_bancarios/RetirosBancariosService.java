
package co.assip.erp.tesoreria.retiros_bancarios;

import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoPreviewDTO;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoRequestDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RetirosBancariosService {

    // =========================================================
    // CONSTANTES DEL PROCESO
    // =========================================================

    private static final String PROCESO = "RETIRO_BANCARIO";
    private static final String MODULO = "TESORERIA";
    private static final String MEDIO_PAGO = "EFECTIVO";

    // =========================================================
    // CONFIGURACIÓN OPERATIVA - RETIROS BANCARIOS
    // =========================================================

    /** Tipo de comprobante de Tesorería. */
    private static final String TIPO_COMPROBANTE = "TS";

    /** Departamento operativo de ingresos varios. */
    private static final String CODIGO_DEPARTAMENTO = "14";

    /** Operación de ingreso de efectivo a caja. */
    private static final String CODIGO_OPERACION = "INGRESO_VARIOS";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final RetirosBancariosRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    // =========================================================
    // DTO DE RESPUESTA
    // =========================================================

    /**
     * Resultado de la preparación de un retiro bancario.
     *
     * El comprobante queda pendiente de autorización,
     * sin consecutivo contable y sin afectar la caja.
     */
    public record RetiroPendienteResponse(
            Long idComprobante,
            Integer idAgencia,
            String procesoOrigen,
            String estado,
            LocalDate fechaDocumento,
            String numeroSoporte,
            BigDecimal valor,
            String mensaje
    ) {}

    // =========================================================
    // CONSULTAS
    // =========================================================

    /**
     * Consulta las cuentas bancarias habilitadas para
     * realizar retiros en la agencia seleccionada.
     *
     * @param idAgencia Agencia de operación.
     * @return Cuentas bancarias habilitadas.
     */
    @Transactional(readOnly = true)
    public List<RetirosBancariosRepository.CuentaBancariaRetiro>
    listarCuentasRetiro(Integer idAgencia) {

        validarAgencia(idAgencia);

        return repository.listarCuentasRetiro(idAgencia);
    }

    /**
     * Consulta las cajas CONTROL habilitadas para
     * recibir efectivo en una agencia.
     *
     * @param idAgencia Agencia de operación.
     * @return Cajas CONTROL disponibles.
     */
    @Transactional(readOnly = true)
    public List<RetirosBancariosRepository.CajaControl>
    listarCajasControl(Integer idAgencia) {

        validarAgencia(idAgencia);

        return repository.listarCajasControl(idAgencia);
    }

    // =========================================================
    // VISTA PREVIA CONTABLE
    // =========================================================

    /**
     * Construye la vista previa contable del retiro bancario.
     *
     * Débito: cuenta contable de caja CONTROL.
     * Crédito: cuenta contable bancaria.
     *
     * No genera consecutivo, comprobante ni movimientos
     * físicos definitivos.
     *
     * @param request Datos del retiro bancario.
     * @return Vista previa contable.
     */
    @Transactional(readOnly = true)
    public MovimientoFondoPreviewDTO previewRetiro(
            MovimientoFondoRequestDTO request
    ) {

        validarSolicitud(request);

        var cuenta = obtenerCuenta(request);
        var caja = obtenerCaja(request);

        BigDecimal valor = request.getValor();
        LocalDate fecha = request.getFechaMovimiento();

        String concepto = construirConcepto(request, cuenta);

        Integer idUsuario = usuarioSesionService.idUsuario();

        Integer idTercero =
                repository.obtenerTerceroUsuario(idUsuario);

        if (idTercero == null) {
            throw new IllegalStateException(
                    "No se encontró el tercero contable del usuario."
            );
        }

        MovimientoContablePreviewDTO debito =
                crearMovimientoPreview(
                        request.getIdAgencia(),
                        fecha,
                        caja.idCatalogoCuentaCaja(),
                        caja.codigoCuentaCaja(),
                        caja.nombreCuentaCaja(),
                        idTercero,
                        concepto,
                        valor,
                        BigDecimal.ZERO
                );

        MovimientoContablePreviewDTO credito =
                crearMovimientoPreview(
                        request.getIdAgencia(),
                        fecha,
                        cuenta.idCuentaContable(),
                        cuenta.codigoCuentaContable(),
                        cuenta.nombreCuentaContable(),
                        idTercero,
                        concepto,
                        BigDecimal.ZERO,
                        valor
                );

        return MovimientoFondoPreviewDTO.builder()
                .tipoMovimiento(
                        MovimientoFondoRequestDTO.TipoMovimiento.RETIRO
                )
                .idAgencia(request.getIdAgencia())
                .fechaMovimiento(fecha)
                .numeroSoporte(limpiar(request.getNumeroSoporte()))
                .idCuentaBancariaOrigen(
                        request.getIdCuentaBancariaOrigen()
                )
                .idCajaDestino(request.getIdCajaDestino())
                .tipoComprobante(TIPO_COMPROBANTE)
                .conceptoComprobante(concepto)
                .movimientos(List.of(debito, credito))
                .totalDebito(valor)
                .totalCredito(valor)
                .diferencia(BigDecimal.ZERO)
                .cuadrado(true)
                .valorCheques(BigDecimal.ZERO)
                .valorEfectivo(valor)
                .valorTotal(valor)
                .build();
    }

    // =========================================================
    // PREPARACIÓN DEL RETIRO
    // =========================================================

    /**
     * Prepara un retiro bancario pendiente de autorización.
     *
     * Guarda en una única transacción:
     *
     * 1. Cabecera del comprobante pendiente.
     * 2. Débito contable a caja CONTROL.
     * 3. Crédito contable a la cuenta bancaria.
     * 4. Movimiento físico proyectado de ingreso.
     *
     * No contabiliza, no genera consecutivo y
     * no modifica los saldos físicos de caja.
     *
     * @param request Datos del retiro bancario.
     * @return Información del comprobante pendiente.
     */
    @Transactional(rollbackFor = Exception.class)
    public RetiroPendienteResponse prepararRetiro(
            MovimientoFondoRequestDTO request
    ) {

        validarSolicitud(request);

        Integer idUsuario = usuarioSesionService.idUsuario();

        // -----------------------------------------------------
        // 1. Validar cuenta bancaria con bloqueo
        // -----------------------------------------------------

        var cuenta = repository.obtenerCuentaRetiroConLock(
                request.getIdCuentaBancariaOrigen(),
                request.getIdAgencia()
        );

        if (cuenta == null) {
            throw new IllegalStateException(
                    "La cuenta bancaria no está habilitada "
                            + "para retiros en esta agencia."
            );
        }

        // -----------------------------------------------------
        // 2. Validar caja CONTROL
        // -----------------------------------------------------

        var caja = obtenerCaja(request);

        // -----------------------------------------------------
        // 3. Obtener tercero contable
        // -----------------------------------------------------

        Integer idTercero =
                repository.obtenerTerceroUsuario(idUsuario);

        if (idTercero == null) {
            throw new IllegalStateException(
                    "No se encontró el tercero contable del usuario."
            );
        }

        // -----------------------------------------------------
        // 4. Obtener departamento operativo
        // -----------------------------------------------------

        Long idDepartamento =
                repository.obtenerDepartamentoOperativo(
                        CODIGO_DEPARTAMENTO
                );

        if (idDepartamento == null) {
            throw new IllegalStateException(
                    "No se encontró el departamento operativo "
                            + CODIGO_DEPARTAMENTO
            );
        }

        // -----------------------------------------------------
        // 5. Obtener operación de caja
        // -----------------------------------------------------

        Long idOperacion =
                repository.obtenerOperacionCaja(
                        CODIGO_OPERACION
                );

        if (idOperacion == null) {
            throw new IllegalStateException(
                    "No se encontró la operación de caja "
                            + CODIGO_OPERACION
            );
        }

        // -----------------------------------------------------
        // 6. Preparar información del comprobante
        // -----------------------------------------------------

        BigDecimal valor = request.getValor();

        String soporte = limpiar(request.getNumeroSoporte());

        String concepto = construirConcepto(request, cuenta);

        // -----------------------------------------------------
        // 7. Crear cabecera pendiente
        // -----------------------------------------------------

        Long idComprobante =
                repository.crearComprobantePendiente(
                        request.getIdAgencia(),
                        TIPO_COMPROBANTE,
                        request.getFechaMovimiento(),
                        concepto,
                        soporte,
                        valor,
                        idUsuario
                );

        // -----------------------------------------------------
        // 8. Débito contable a caja CONTROL
        // -----------------------------------------------------

        repository.insertarPartida(
                idComprobante,
                new RetirosBancariosRepository.PartidaPendiente(
                        1,
                        caja.idCatalogoCuentaCaja(),
                        request.getIdAgencia(),
                        idTercero,
                        concepto,
                        valor,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        MODULO,
                        PROCESO,
                        soporte
                )
        );

        // -----------------------------------------------------
        // 9. Crédito contable a cuenta bancaria
        // -----------------------------------------------------

        repository.insertarPartida(
                idComprobante,
                new RetirosBancariosRepository.PartidaPendiente(
                        2,
                        cuenta.idCuentaContable(),
                        request.getIdAgencia(),
                        idTercero,
                        concepto,
                        BigDecimal.ZERO,
                        valor,
                        BigDecimal.ZERO,
                        MODULO,
                        PROCESO,
                        soporte
                )
        );

        // -----------------------------------------------------
        // 10. Movimiento físico proyectado
        // -----------------------------------------------------

        /*
         * El movimiento se registra únicamente como pendiente.
         *
         * La autorización posterior deberá resolver la
         * provisión abierta de la caja CONTROL.
         */

        repository.insertarMovimientoCajaPendiente(
                idComprobante,
                new RetirosBancariosRepository.MovimientoCajaPendiente(
                        1,
                        caja.idCaja(),
                        idDepartamento,
                        idOperacion,
                        MODULO,
                        PROCESO,
                        "RETIRO_BANCARIO",
                        soporte,
                        "COMP_" + idComprobante,
                        "INGRESO",
                        MEDIO_PAGO,
                        valor,
                        concepto
                )
        );

        // -----------------------------------------------------
        // 11. Respuesta
        // -----------------------------------------------------

        return new RetiroPendienteResponse(
                idComprobante,
                request.getIdAgencia(),
                PROCESO,
                "PENDIENTE",
                request.getFechaMovimiento(),
                soporte,
                valor,
                "Retiro bancario preparado. "
                        + "Pendiente de autorización."
        );
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    /**
     * Valida los datos obligatorios del retiro bancario.
     *
     * La cuenta bancaria y la caja CONTROL se validan
     * nuevamente contra PostgreSQL antes de guardar.
     *
     * @param request Solicitud del retiro.
     */
    private void validarSolicitud(
            MovimientoFondoRequestDTO request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud es obligatoria."
            );
        }

        if (request.getTipoMovimiento()
                != MovimientoFondoRequestDTO.TipoMovimiento.RETIRO) {

            throw new IllegalArgumentException(
                    "La operación debe ser RETIRO."
            );
        }

        validarAgencia(request.getIdAgencia());

        if (request.getFechaMovimiento() == null) {
            throw new IllegalArgumentException(
                    "La fecha del documento es obligatoria."
            );
        }

        if (request.getFechaMovimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha del documento no puede ser futura."
            );
        }

        if (request.getIdCuentaBancariaOrigen() == null) {
            throw new IllegalArgumentException(
                    "Seleccione la cuenta bancaria de origen."
            );
        }

        if (request.getIdCajaDestino() == null) {
            throw new IllegalArgumentException(
                    "Seleccione la caja CONTROL de destino."
            );
        }

        if (request.getValor() == null
                || request.getValor().signum() <= 0) {

            throw new IllegalArgumentException(
                    "El valor del retiro debe ser mayor que cero."
            );
        }

        if (request.getValor().scale() > 2) {
            throw new IllegalArgumentException(
                    "El valor del retiro admite máximo dos decimales."
            );
        }

        if (limpiar(request.getNumeroSoporte()).isEmpty()) {
            throw new IllegalArgumentException(
                    "El documento soporte del retiro es obligatorio."
            );
        }

        if (limpiar(request.getNumeroSoporte()).length() > 100) {
            throw new IllegalArgumentException(
                    "El documento soporte supera 100 caracteres."
            );
        }

        if (limpiar(request.getObservacion()).length() > 500) {
            throw new IllegalArgumentException(
                    "La observación supera 500 caracteres."
            );
        }
    }

    // =========================================================
    // CONSULTAS Y VALIDACIONES DE ENTIDADES
    // =========================================================

    /**
     * Consulta y valida que la cuenta bancaria de origen
     * esté habilitada para retiros en la agencia.
     *
     * @param request Solicitud del retiro.
     * @return Cuenta bancaria habilitada.
     */
    private RetirosBancariosRepository.CuentaBancariaRetiro
    obtenerCuenta(MovimientoFondoRequestDTO request) {

        var cuenta = repository.listarCuentasRetiro(
                        request.getIdAgencia()
                )
                .stream()
                .filter(c -> c.idCuentaBancaria().equals(
                        request.getIdCuentaBancariaOrigen()
                ))
                .findFirst()
                .orElse(null);

        if (cuenta == null) {
            throw new IllegalArgumentException(
                    "La cuenta bancaria no está habilitada "
                            + "para retiros."
            );
        }

        return cuenta;
    }

    /**
     * Valida que la caja destino sea de tipo CONTROL,
     * esté activa y pertenezca a la agencia seleccionada.
     *
     * @param request Solicitud del retiro.
     * @return Caja CONTROL habilitada.
     */
    private RetirosBancariosRepository.CajaControl
    obtenerCaja(MovimientoFondoRequestDTO request) {

        var caja = repository.obtenerCajaControl(
                request.getIdCajaDestino(),
                request.getIdAgencia()
        );

        if (caja == null) {
            throw new IllegalArgumentException(
                    "La caja seleccionada no es una caja CONTROL "
                            + "habilitada para recibir efectivo."
            );
        }

        return caja;
    }

    // =========================================================
    // CONSTRUCCIÓN DEL CONCEPTO
    // =========================================================

    /**
     * Construye el concepto contable y físico del retiro,
     * identificando banco, cuenta y documento soporte.
     *
     * @param request Solicitud del retiro.
     * @param cuenta Cuenta bancaria de origen.
     * @return Concepto del comprobante.
     */
    private String construirConcepto(
            MovimientoFondoRequestDTO request,
            RetirosBancariosRepository.CuentaBancariaRetiro cuenta
    ) {

        String concepto =
                "RETIRO BANCARIO - "
                        + cuenta.nombreBanco()
                        + " - CUENTA "
                        + cuenta.numeroCuenta()
                        + " - SOPORTE "
                        + limpiar(request.getNumeroSoporte());

        String observacion = limpiar(request.getObservacion());

        if (!observacion.isEmpty()) {
            concepto += " - " + observacion;
        }

        return concepto.length() > 500
                ? concepto.substring(0, 500)
                : concepto;
    }

    // =========================================================
    // CONSTRUCCIÓN DE MOVIMIENTOS CONTABLES
    // =========================================================

    /**
     * Construye una partida contable para la vista previa.
     *
     * No persiste información en PostgreSQL.
     *
     * @param idAgencia Agencia contable.
     * @param fecha Fecha del documento.
     * @param idCuenta Identificador de cuenta contable.
     * @param codigoCuenta Código contable.
     * @param nombreCuenta Nombre de la cuenta.
     * @param idTercero Tercero contable.
     * @param concepto Descripción del movimiento.
     * @param debito Valor débito.
     * @param credito Valor crédito.
     * @return Movimiento para vista previa.
     */
    private MovimientoContablePreviewDTO crearMovimientoPreview(
            Integer idAgencia,
            LocalDate fecha,
            Integer idCuenta,
            String codigoCuenta,
            String nombreCuenta,
            Integer idTercero,
            String concepto,
            BigDecimal debito,
            BigDecimal credito
    ) {

        MovimientoContablePreviewDTO movimiento =
                new MovimientoContablePreviewDTO();

        movimiento.setIdAgencia(idAgencia);
        movimiento.setFechaAuxiliar(fecha);

        movimiento.setIdCatalogoCuenta(idCuenta);
        movimiento.setCodigoCuenta(codigoCuenta);
        movimiento.setNombreCuenta(nombreCuenta);

        movimiento.setIdDatosPersonal(idTercero);
        movimiento.setDetalleMovimiento(concepto);

        movimiento.setValorDebito(debito);
        movimiento.setValorCredito(credito);

        return movimiento;
    }

    // =========================================================
    // VALIDACIONES GENERALES
    // =========================================================

    /**
     * Valida que se haya seleccionado una agencia válida.
     *
     * @param idAgencia Identificador de agencia.
     */
    private void validarAgencia(Integer idAgencia) {

        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException(
                    "La agencia es obligatoria."
            );
        }
    }

    /**
     * Normaliza los valores de texto opcionales.
     *
     * @param texto Texto a normalizar.
     * @return Texto sin espacios exteriores.
     */
    private String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
