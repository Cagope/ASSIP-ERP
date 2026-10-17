package co.assip.erp.tesoreria.traslados_bancarios;

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
public class TrasladosBancariosService {

    // =========================================================
    // CONSTANTES
    // =========================================================

    private static final String PROCESO = "TRASLADO_BANCARIO";
    private static final String TIPO_COMPROBANTE = "TS";
    private static final int MAX_DETALLE_CONTABLE = 100;

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final TrasladosBancariosRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    // =========================================================
    // RESPUESTA DE PREPARACIÓN
    // =========================================================

    public record TrasladoPendienteResponse(
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
    // CATÁLOGOS BANCARIOS
    // =========================================================

    @Transactional(readOnly = true)
    public List<TrasladosBancariosRepository.CuentaBancaria>
    listarCuentasOrigen(Integer idAgencia) {

        validarAgencia(idAgencia);

        return repository.listarCuentasOrigen(idAgencia);
    }

    @Transactional(readOnly = true)
    public List<TrasladosBancariosRepository.CuentaBancaria>
    listarCuentasDestino(Integer idAgencia) {

        validarAgencia(idAgencia);

        return repository.listarCuentasDestino(idAgencia);
    }

    // =========================================================
    // VISTA PREVIA
    // =========================================================

    @Transactional(readOnly = true)
    public MovimientoFondoPreviewDTO previewTraslado(
            MovimientoFondoRequestDTO request
    ) {

        validarSolicitud(request);

        var origen = obtenerOrigen(request);
        var destino = obtenerDestino(request);

        Integer idUsuario = usuarioSesionService.idUsuario();

        Integer idTercero =
                repository.obtenerTerceroUsuario(idUsuario);

        if (idTercero == null) {
            throw new IllegalStateException(
                    "No se encontró el tercero contable del usuario."
            );
        }

        String concepto = construirConcepto(
                request,
                origen,
                destino
        );

        String detalleContable = construirDetalleContable(request, origen, destino);

        BigDecimal valor = request.getValor();

        MovimientoContablePreviewDTO debito =
                crearMovimientoPreview(
                        request.getIdAgencia(),
                        request.getFechaMovimiento(),
                        destino.idCuentaContable(),
                        destino.codigoCuentaContable(),
                        destino.nombreCuentaContable(),
                        idTercero,
                        detalleContable,
                        valor,
                        BigDecimal.ZERO
                );

        MovimientoContablePreviewDTO credito =
                crearMovimientoPreview(
                        request.getIdAgencia(),
                        request.getFechaMovimiento(),
                        origen.idCuentaContable(),
                        origen.codigoCuentaContable(),
                        origen.nombreCuentaContable(),
                        idTercero,
                        detalleContable,
                        BigDecimal.ZERO,
                        valor
                );

        return MovimientoFondoPreviewDTO.builder()
                .tipoMovimiento(
                        MovimientoFondoRequestDTO.TipoMovimiento
                                .TRASLADO_BANCARIO
                )
                .idAgencia(request.getIdAgencia())
                .fechaMovimiento(request.getFechaMovimiento())
                .numeroSoporte(limpiar(request.getNumeroSoporte()))
                .idCuentaBancariaOrigen(
                        request.getIdCuentaBancariaOrigen()
                )
                .idCuentaBancariaDestino(
                        request.getIdCuentaBancariaDestino()
                )
                .tipoComprobante(TIPO_COMPROBANTE)
                .conceptoComprobante(concepto)
                .movimientos(List.of(debito, credito))
                .totalDebito(valor)
                .totalCredito(valor)
                .diferencia(BigDecimal.ZERO)
                .cuadrado(true)
                .valorCheques(BigDecimal.ZERO)
                .valorEfectivo(BigDecimal.ZERO)
                .valorTotal(valor)
                .build();
    }

    // =========================================================
    // PREPARACIÓN PARA AUTORIZACIÓN
    // =========================================================

    @Transactional(rollbackFor = Exception.class)
    public TrasladoPendienteResponse prepararTraslado(
            MovimientoFondoRequestDTO request
    ) {

        validarSolicitud(request);

        Integer idUsuario = usuarioSesionService.idUsuario();

        // -----------------------------------------------------
        // 1. Validar y bloquear cuenta bancaria origen
        // -----------------------------------------------------

        var origen = repository.obtenerOrigenConLock(
                request.getIdCuentaBancariaOrigen(),
                request.getIdAgencia()
        );

        if (origen == null) {
            throw new IllegalStateException(
                    "La cuenta bancaria de origen no está "
                            + "habilitada para retirar fondos."
            );
        }

        // -----------------------------------------------------
        // 2. Validar y bloquear cuenta bancaria destino
        // -----------------------------------------------------

        var destino = repository.obtenerDestinoConLock(
                request.getIdCuentaBancariaDestino(),
                request.getIdAgencia()
        );

        if (destino == null) {
            throw new IllegalStateException(
                    "La cuenta bancaria de destino no está "
                            + "habilitada para recibir fondos."
            );
        }

        // -----------------------------------------------------
        // 3. Validar cuentas contables
        // -----------------------------------------------------

        if (origen.idCuentaContable()
                .equals(destino.idCuentaContable())) {

            throw new IllegalArgumentException(
                    "Las cuentas contables de origen y destino "
                            + "deben ser diferentes."
            );
        }

        // -----------------------------------------------------
        // 4. Obtener tercero contable
        // -----------------------------------------------------

        Integer idTercero =
                repository.obtenerTerceroUsuario(idUsuario);

        if (idTercero == null) {
            throw new IllegalStateException(
                    "No se encontró el tercero contable del usuario."
            );
        }

        // -----------------------------------------------------
        // 5. Preparar información
        // -----------------------------------------------------

        BigDecimal valor = request.getValor();

        String soporte = limpiar(request.getNumeroSoporte());

        String concepto = construirConcepto(
                request,
                origen,
                destino
        );

        String detalleContable = construirDetalleContable(request, origen, destino);

        // -----------------------------------------------------
        // 6. Crear comprobante pendiente
        // -----------------------------------------------------

        Long idComprobante =
                repository.crearComprobantePendiente(
                        request.getIdAgencia(),
                        request.getFechaMovimiento(),
                        concepto,
                        soporte,
                        valor,
                        idUsuario
                );

        // -----------------------------------------------------
        // 7. Débito contable - banco destino
        // -----------------------------------------------------

        repository.insertarPartida(
                idComprobante,
                new TrasladosBancariosRepository.PartidaPendiente(
                        1,
                        destino.idCuentaContable(),
                        request.getIdAgencia(),
                        idTercero,
                        detalleContable,
                        valor,
                        BigDecimal.ZERO,
                        soporte
                )
        );

        // -----------------------------------------------------
        // 8. Crédito contable - banco origen
        // -----------------------------------------------------

        repository.insertarPartida(
                idComprobante,
                new TrasladosBancariosRepository.PartidaPendiente(
                        2,
                        origen.idCuentaContable(),
                        request.getIdAgencia(),
                        idTercero,
                        detalleContable,
                        BigDecimal.ZERO,
                        valor,
                        soporte
                )
        );

        // -----------------------------------------------------
        // 9. Respuesta
        // -----------------------------------------------------

        return new TrasladoPendienteResponse(
                idComprobante,
                request.getIdAgencia(),
                PROCESO,
                "PENDIENTE",
                request.getFechaMovimiento(),
                soporte,
                valor,
                "Traslado bancario preparado correctamente. "
                        + "Pendiente de autorización."
        );
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarSolicitud(
            MovimientoFondoRequestDTO request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud es obligatoria."
            );
        }

        if (request.getTipoMovimiento()
                != MovimientoFondoRequestDTO.TipoMovimiento
                .TRASLADO_BANCARIO) {

            throw new IllegalArgumentException(
                    "La operación debe ser TRASLADO_BANCARIO."
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

        if (request.getIdCuentaBancariaDestino() == null) {
            throw new IllegalArgumentException(
                    "Seleccione la cuenta bancaria de destino."
            );
        }

        if (request.getIdCuentaBancariaOrigen()
                .equals(request.getIdCuentaBancariaDestino())) {

            throw new IllegalArgumentException(
                    "La cuenta bancaria de origen y destino "
                            + "deben ser diferentes."
            );
        }

        if (request.getValor() == null
                || request.getValor().signum() <= 0) {

            throw new IllegalArgumentException(
                    "El valor del traslado debe ser mayor que cero."
            );
        }

        if (request.getValor().scale() > 2) {
            throw new IllegalArgumentException(
                    "El valor del traslado admite máximo dos decimales."
            );
        }

        if (limpiar(request.getNumeroSoporte()).isEmpty()) {
            throw new IllegalArgumentException(
                    "El documento soporte es obligatorio."
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

        if (request.getMedioTrasladoBancario() == null) {
            throw new IllegalArgumentException(
                    "Seleccione el medio de traslado bancario."
            );
        }
    }

    // =========================================================
    // OBTENCIÓN DE CUENTAS
    // =========================================================

    private TrasladosBancariosRepository.CuentaBancaria
    obtenerOrigen(MovimientoFondoRequestDTO request) {

        return repository.listarCuentasOrigen(request.getIdAgencia())
                .stream()
                .filter(c -> c.idCuentaBancaria().equals(
                        request.getIdCuentaBancariaOrigen()
                ))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "La cuenta bancaria de origen no está habilitada."
                ));
    }

    private TrasladosBancariosRepository.CuentaBancaria
    obtenerDestino(MovimientoFondoRequestDTO request) {

        return repository.listarCuentasDestino(request.getIdAgencia())
                .stream()
                .filter(c -> c.idCuentaBancaria().equals(
                        request.getIdCuentaBancariaDestino()
                ))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "La cuenta bancaria de destino no está habilitada."
                ));
    }

    // =========================================================
    // CONCEPTO CONTABLE
    // =========================================================

    private String construirConcepto(
            MovimientoFondoRequestDTO request,
            TrasladosBancariosRepository.CuentaBancaria origen,
            TrasladosBancariosRepository.CuentaBancaria destino
    ) {

        String medio = switch (request.getMedioTrasladoBancario()) {
            case EFECTIVO -> "TRASLADO FISICO";
            case BRE_B -> "TRANSFERENCIA BRE-B";
            case TRANSFERENCIA_NORMAL -> "TRANSFERENCIA BANCARIA";
        };

        String concepto =
                medio
                        + " - ORIGEN "
                        + origen.nombreBanco()
                        + " - CUENTA "
                        + origen.numeroCuenta()
                        + " - DESTINO "
                        + destino.nombreBanco()
                        + " - CUENTA "
                        + destino.numeroCuenta()
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
    // DETALLE PARA AUXILIAR CONTABLE (MAXIMO 100 CARACTERES)
    // =========================================================

    private String construirDetalleContable(
            MovimientoFondoRequestDTO request,
            TrasladosBancariosRepository.CuentaBancaria origen,
            TrasladosBancariosRepository.CuentaBancaria destino
    ) {
        String medio = switch (request.getMedioTrasladoBancario()) {
            case EFECTIVO -> "EFECTIVO";
            case BRE_B -> "BRE-B";
            case TRANSFERENCIA_NORMAL -> "TRANSFERENCIA";
        };

        String detalle = "TRASLADO " + medio
                + " - " + limpiar(origen.nombreBanco())
                + " A " + limpiar(destino.nombreBanco())
                + " - SOP " + limpiar(request.getNumeroSoporte());

        return detalle.length() > MAX_DETALLE_CONTABLE
                ? detalle.substring(0, MAX_DETALLE_CONTABLE)
                : detalle;
    }

    // =========================================================
    // MOVIMIENTO CONTABLE PARA PREVIEW
    // =========================================================

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

    private void validarAgencia(Integer idAgencia) {

        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException(
                    "La agencia es obligatoria."
            );
        }
    }

    private String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
