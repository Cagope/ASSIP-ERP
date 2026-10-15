package co.assip.erp.cajas.salida_cheques;

import co.assip.erp.cajas.movimientos.CajasMovimientosRepository;
import co.assip.erp.cajas.salida_cheques.dto.CajasSalidaChequesRequestDTO;
import co.assip.erp.cajas.salida_cheques.dto.CajasSalidaChequesResponseDTO;
import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.cajas.provisiones.CajasUsuariosService;
import co.assip.erp.cajas.provisiones.dto.CajaUsuarioDTO;
import co.assip.erp.cajas.salida_cheques.dto.CajasSalidaChequesChequeDTO;
import co.assip.erp.cajas.salida_cheques.dto.CajasSalidaChequesPreviewDTO;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class CajasSalidaChequesService {

    private static final String MODULO = "CAJAS";
    private static final String PROCESO = "SALIDA_CHEQUES";
    private static final String TIPO_COMPROBANTE = "RC";

    private final CajasSalidaChequesRepository repository;
    private final CajasMovimientosRepository movimientosRepository;
    private final ConsecutivosComprobantesService consecutivosService;
    private final ContabilidadRegistroService contabilidadService;
    private final UsuarioSesionService usuarioSesionService;
    private final CajasUsuariosService cajasUsuariosService;


    @Transactional
    public CajasSalidaChequesResponseDTO ejecutar(
            CajasSalidaChequesRequestDTO request
    ) {

        validarRequest(request);

        Integer idUsuario = usuarioSesionService.idUsuario();
        Integer idDatosPersonalCajero =
                repository.obtenerIdDatosPersonalCajero(idUsuario);

        // Verificar que la caja origen pertenece al usuario autenticado.
        cajasUsuariosService.validarCajaUsuario(
                request.getIdCajaOrigen()
        );

        CajasSalidaChequesRepository.CajaTraslado origen =
                repository.obtenerCaja(request.getIdCajaOrigen());
        if (origen == null) {
            throw new IllegalArgumentException(
                    "La caja origen no existe, está inactiva "
                            + "o no permite cheques."
            );
        }

        if (!"OPERATIVA".equals(origen.tipoCaja())) {
            throw new IllegalArgumentException(
                    "La salida de cheques debe realizarse "
                            + "desde una caja OPERATIVA."
            );
        }

        usuarioSesionService.validarAgencia(
                origen.idAgencia()
        );

        CajasSalidaChequesRepository.CajaTraslado control =
                repository.obtenerCajaControl(
                        origen.idAgencia()
                );

        if (origen.idCaja().equals(control.idCaja())) {
            throw new IllegalStateException(
                    "La caja origen y CONTROL no pueden ser iguales."
            );
        }

        /*
         * Ambas cajas deben tener una cuenta contable configurada.
         *
         * Se permite que utilicen la misma cuenta.
         * El traslado genera el débito y el crédito correspondientes,
         * conservando la trazabilidad del comprobante RC.
         */
        validarCuentas(origen, control);

        LocalDate fecha = request.getFechaContable();

        /*
         * Cada caja debe tener su propia provisión abierta.
         *
         * CONTROL aún no tiene apertura implementada.
         * No creamos una provisión en cero automáticamente.
         */
        Long provisionOrigen =
                movimientosRepository.obtenerProvisionAbierta(
                        origen.idCaja(),
                        fecha
                );

        if (provisionOrigen == null) {
            throw new IllegalStateException(
                    "La caja OPERATIVA no tiene una provisión "
                            + "abierta para la fecha " + fecha
            );
        }

        Long provisionControl =
                movimientosRepository.obtenerProvisionAbierta(
                        control.idCaja(),
                        fecha
                );

        if (provisionControl == null) {
            throw new IllegalStateException(
                    "La caja CONTROL no tiene una provisión "
                            + "abierta para la fecha " + fecha
                            + ". Debe habilitarse su apertura antes "
                            + "de ejecutar el traslado."
            );
        }

        Long idOperacionSalida =
                movimientosRepository.obtenerIdOperacion(
                        "SALIDA_CHEQUE"
                );

        Long idOperacionEntrada =
                movimientosRepository.obtenerIdOperacion(
                        "ENTRADA_CHEQUE"
                );

        if (idOperacionSalida == null
                || idOperacionEntrada == null) {
            throw new IllegalStateException(
                    "No están configuradas las operaciones "
                            + "SALIDA_CHEQUE y ENTRADA_CHEQUE."
            );
        }

        Long idDepartamento =
                movimientosRepository.obtenerIdDepartamento("17");

        if (idDepartamento == null) {
            throw new IllegalStateException(
                    "No existe el departamento operativo "
                            + "17 - SALIDA CHEQUES, o se encuentra inactivo."
            );
        }


        /*
         * El bloqueo FOR UPDATE permanece vigente
         * hasta finalizar esta transacción.
         */
        List<CajasSalidaChequesRepository.ChequeTraslado> cheques =
                repository.bloquearCheques(
                        origen.idCaja(),
                        request.getIdsCheques()
                );

        BigDecimal total = cheques.stream()
                .map(
                        CajasSalidaChequesRepository
                                .ChequeTraslado::valorCheque
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        if (total.signum() <= 0
                || cheques.stream().anyMatch(
                cheque -> cheque.valorCheque() == null
                        || cheque.valorCheque().signum() <= 0
        )) {
            throw new IllegalStateException(
                    "Los cheques seleccionados deben tener "
                            + "valores positivos."
            );
        }

        String numeroRC =
                consecutivosService.generarNumeroDefinitivo(
                        TIPO_COMPROBANTE,
                        origen.idAgencia(),
                        idUsuario
                );

        String concepto = construirConcepto(request, origen, control);

        List<MovimientoContableDTO> auxiliares =
                construirMovimientosContables(
                        cheques,
                        origen,
                        control,
                        fecha,
                        numeroRC,
                        idDatosPersonalCajero
                );

        OrigenComprobanteDTO origenComprobante =
                new OrigenComprobanteDTO();

        origenComprobante.setOrigenTipo("AUTO");
        origenComprobante.setModuloOrigen(MODULO);
        origenComprobante.setProcesoOrigen(PROCESO);
        origenComprobante.setTablaOrigen(
                "cajas.cheques_recibidos"
        );

        contabilidadService.registrarComprobante(
                origen.idAgencia(),
                TIPO_COMPROBANTE,
                numeroRC,
                concepto,
                origenComprobante,
                auxiliares,
                idUsuario
        );

        List<Long> idsMovimientos = new ArrayList<>();

        for (CajasSalidaChequesRepository.ChequeTraslado cheque
                : cheques) {

            String referencia =
                    "CHEQUE " + cheque.numeroCheque();

            Long idSalida =
                    movimientosRepository.insertarMovimientoCaja(
                            provisionOrigen,
                            origen.idCaja(),
                            fecha,
                            idDepartamento,
                            idOperacionSalida,
                            MODULO,
                            PROCESO,
                            "cajas.cheques_recibidos",
                            cheque.idChequeRecibido(),
                            referencia,
                            "EGRESO",
                            "CHEQUE",
                            cheque.valorCheque(),
                            concepto,
                            "REGISTRADO",
                            TIPO_COMPROBANTE,
                            numeroRC,
                            idUsuario
                    );

            Long idEntrada =
                    movimientosRepository.insertarMovimientoCaja(
                            provisionControl,
                            control.idCaja(),
                            fecha,
                            idDepartamento,
                            idOperacionEntrada,
                            MODULO,
                            PROCESO,
                            "cajas.cheques_recibidos",
                            cheque.idChequeRecibido(),
                            referencia,
                            "INGRESO",
                            "CHEQUE",
                            cheque.valorCheque(),
                            concepto,
                            "REGISTRADO",
                            TIPO_COMPROBANTE,
                            numeroRC,
                            idUsuario
                    );

            idsMovimientos.add(idSalida);
            idsMovimientos.add(idEntrada);

            /*
             * Cambia únicamente la custodia actual.
             * El movimiento original del recaudo se conserva.
             */
            repository.trasladarCustodia(
                    cheque.idChequeRecibido(),
                    origen.idCaja(),
                    control.idCaja(),
                    fecha,
                    idUsuario
            );
        }

        return CajasSalidaChequesResponseDTO.builder()
                .idCajaOrigen(origen.idCaja())
                .idCajaControl(control.idCaja())
                .fechaContable(fecha)
                .cantidadCheques(cheques.size())
                .valorTotal(total)
                .tipoComprobante(TIPO_COMPROBANTE)
                .numeroComprobante(numeroRC)
                .idsMovimientosCaja(idsMovimientos)
                .mensaje(
                        "Entrega de cheques registrada correctamente."
                )
                .build();
    }

    /**
     * Vista previa informativa: no bloquea cheques, no reserva
     * consecutivos ni modifica contabilidad o custodia.
     * ejecutar() vuelve a validar y bloquear los cheques.
     */
    @Transactional(readOnly = true)
    public CajasSalidaChequesPreviewDTO preview(
            CajasSalidaChequesRequestDTO request
    ) {
        validarRequest(request);
        Integer idDatosPersonalCajero = repository.obtenerIdDatosPersonalCajero(
                usuarioSesionService.idUsuario()
        );
        cajasUsuariosService.validarCajaUsuario(request.getIdCajaOrigen());

        CajasSalidaChequesRepository.CajaTraslado origen =
                repository.obtenerCaja(request.getIdCajaOrigen());
        if (origen == null || !"OPERATIVA".equals(origen.tipoCaja())) {
            throw new IllegalArgumentException(
                    "La caja origen debe ser OPERATIVA, activa y admitir cheques."
            );
        }
        usuarioSesionService.validarAgencia(origen.idAgencia());

        CajasSalidaChequesRepository.CajaTraslado control =
                repository.obtenerCajaControl(origen.idAgencia());
        if (origen.idCaja().equals(control.idCaja())) {
            throw new IllegalStateException(
                    "La caja origen y CONTROL no pueden ser iguales."
            );
        }
        validarCuentas(origen, control);
        if (origen.codigoCuentaCaja() == null
                || origen.nombreCuentaCaja() == null
                || control.codigoCuentaCaja() == null
                || control.nombreCuentaCaja() == null) {
            throw new IllegalStateException(
                    "Faltan datos del catálogo contable de alguna caja."
            );
        }

        List<Long> ids = request.getIdsCheques();
        if (ids.stream().anyMatch(id -> id == null)
                || new HashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException(
                    "La selección contiene identificadores nulos o repetidos."
            );
        }
        Set<Long> seleccionados = new HashSet<>(ids);
        List<CajasSalidaChequesRepository.ChequeTraslado> cheques =
                repository.listarDisponibles(origen.idCaja()).stream()
                        .filter(c -> seleccionados.contains(c.idChequeRecibido()))
                        .toList();
        if (cheques.size() != ids.size()) {
            throw new IllegalStateException(
                    "Uno o más cheques ya no están disponibles en la caja origen."
            );
        }
        if (cheques.stream().anyMatch(c -> c.valorCheque() == null
                || c.valorCheque().signum() <= 0)) {
            throw new IllegalStateException(
                    "Los cheques seleccionados deben tener valores positivos."
            );
        }

        BigDecimal total = cheques.stream()
                .map(CajasSalidaChequesRepository.ChequeTraslado::valorCheque)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Se reutiliza el mismo constructor del asiento definitivo.
        // El número no se genera ni se reserva durante el preview.
        List<MovimientoContableDTO> auxiliares = construirMovimientosContables(
                cheques, origen, control, request.getFechaContable(), null,
                idDatosPersonalCajero
        );
        List<MovimientoContablePreviewDTO> movimientos = new ArrayList<>();
        BigDecimal totalDebito = BigDecimal.ZERO;
        BigDecimal totalCredito = BigDecimal.ZERO;

        for (MovimientoContableDTO auxiliar : auxiliares) {
            boolean esControl = auxiliar.getValorDebito() != null
                    && auxiliar.getValorDebito().signum() > 0;
            CajasSalidaChequesRepository.CajaTraslado caja =
                    esControl ? control : origen;

            MovimientoContablePreviewDTO fila = new MovimientoContablePreviewDTO();
            fila.setIdAgencia(origen.idAgencia());
            fila.setFechaAuxiliar(request.getFechaContable());
            fila.setIdCatalogoCuenta(caja.idCatalogoCuentaCaja());
            fila.setCodigoCuenta(caja.codigoCuentaCaja());
            fila.setNombreCuenta(caja.nombreCuentaCaja());
            fila.setDetalleMovimiento(auxiliar.getDetalleMovimiento());
            fila.setValorDebito(auxiliar.getValorDebito() == null
                    ? BigDecimal.ZERO : auxiliar.getValorDebito());
            fila.setValorCredito(auxiliar.getValorCredito() == null
                    ? BigDecimal.ZERO : auxiliar.getValorCredito());
            totalDebito = totalDebito.add(fila.getValorDebito());
            totalCredito = totalCredito.add(fila.getValorCredito());
            movimientos.add(fila);
        }

        BigDecimal diferencia = totalDebito.subtract(totalCredito);
        return CajasSalidaChequesPreviewDTO.builder()
                .idAgencia(origen.idAgencia())
                .idCajaOrigen(origen.idCaja())
                .idCajaControl(control.idCaja())
                .fechaContable(request.getFechaContable())
                .cantidadCheques(cheques.size())
                .valorTotal(total)
                .tipoComprobante(TIPO_COMPROBANTE)
                .conceptoComprobante(construirConcepto(request, origen, control))
                .movimientos(movimientos)
                .totalDebito(totalDebito)
                .totalCredito(totalCredito)
                .diferencia(diferencia)
                .cuadrado(diferencia.signum() == 0)
                .build();
    }

    private String construirConcepto(
            CajasSalidaChequesRequestDTO request,
            CajasSalidaChequesRepository.CajaTraslado origen,
            CajasSalidaChequesRepository.CajaTraslado control
    ) {
        String concepto = "ENTREGA DE CHEQUES DE CAJA "
                + origen.idCaja() + " A CAJA CONTROL " + control.idCaja();
        if (request.getNumeroDocumento() != null
                && !request.getNumeroDocumento().isBlank()) {
            concepto += " - SOPORTE " + request.getNumeroDocumento().trim();
        }
        if (request.getObservacion() != null
                && !request.getObservacion().isBlank()) {
            concepto += " - " + request.getObservacion().trim();
        }
        return concepto;
    }

    @Transactional(readOnly = true)
    public List<CajasSalidaChequesChequeDTO> listarDisponibles() {

        // 1. Obtener la caja vinculada al usuario autenticado.
        CajaUsuarioDTO vinculacion =
                cajasUsuariosService.obtenerActivaUsuario();

        // 2. Validar que sea una caja OPERATIVA
        //    activa y habilitada para recibir cheques.
        CajasSalidaChequesRepository.CajaTraslado caja =
                repository.obtenerCaja(vinculacion.getIdCaja());

        if (caja == null
                || !"OPERATIVA".equalsIgnoreCase(caja.tipoCaja())) {
            throw new IllegalStateException(
                    "El usuario no tiene una caja OPERATIVA "
                            + "activa y habilitada para cheques."
            );
        }

        // 3. Validar que el usuario tenga acceso a la agencia.
        usuarioSesionService.validarAgencia(caja.idAgencia());

        // 4. Consultar únicamente los cheques de su caja.
        return repository.listarDisponibles(caja.idCaja())
                .stream()
                .map(cheque -> new CajasSalidaChequesChequeDTO(
                        cheque.idChequeRecibido(),
                        cheque.idCaja(),
                        cheque.codigoBanco(),
                        cheque.numeroCheque(),
                        cheque.valorCheque()
                ))
                .toList();
    }

    private void validarRequest(
            CajasSalidaChequesRequestDTO request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud es obligatoria."
            );
        }

        if (request.getIdCajaOrigen() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar la caja origen."
            );
        }

        if (request.getFechaContable() == null) {
            throw new IllegalArgumentException(
                    "La fecha contable es obligatoria."
            );
        }

        if (request.getIdsCheques() == null
                || request.getIdsCheques().isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un cheque."
            );
        }
    }

    private void validarCuentas(
            CajasSalidaChequesRepository.CajaTraslado origen,
            CajasSalidaChequesRepository.CajaTraslado control
    ) {

        if (origen.idCatalogoCuentaCaja() == null
                || control.idCatalogoCuentaCaja() == null) {

            throw new IllegalStateException(
                    "Las dos cajas deben tener cuentas contables configuradas."
            );
        }
    }

    private List<MovimientoContableDTO> construirMovimientosContables(
            List<CajasSalidaChequesRepository.ChequeTraslado> cheques,
            CajasSalidaChequesRepository.CajaTraslado origen,
            CajasSalidaChequesRepository.CajaTraslado control,
            LocalDate fecha,
            String numeroRC,
            Integer idDatosPersonalCajero
    ) {

        List<MovimientoContableDTO> movimientos =
                new ArrayList<>();

        for (CajasSalidaChequesRepository.ChequeTraslado cheque
                : cheques) {

            String detalle =
                    "TRASLADO CHEQUE "
                            + cheque.numeroCheque()
                            + " DE CAJA "
                            + origen.idCaja()
                            + " A CONTROL "
                            + control.idCaja();

            String soporte =
                    "CHEQUE " + cheque.numeroCheque();

            MovimientoContableDTO debito =
                    nuevoMovimiento(
                            control.idCatalogoCuentaCaja(),
                            origen.idAgencia(),
                            fecha,
                            numeroRC,
                            detalle,
                            soporte,
                            idDatosPersonalCajero
                    );

            debito.setValorDebito(
                    cheque.valorCheque()
            );

            movimientos.add(debito);

            MovimientoContableDTO credito =
                    nuevoMovimiento(
                            origen.idCatalogoCuentaCaja(),
                            origen.idAgencia(),
                            fecha,
                            numeroRC,
                            detalle,
                            soporte,
                            idDatosPersonalCajero
                    );

            credito.setValorCredito(
                    cheque.valorCheque()
            );

            movimientos.add(credito);
        }

        return movimientos;
    }

    private MovimientoContableDTO nuevoMovimiento(
            Integer idCuenta,
            Integer idAgencia,
            LocalDate fecha,
            String numeroRC,
            String detalle,
            String soporte,
            Integer idDatosPersonalCajero
    ) {

        MovimientoContableDTO movimiento =
                new MovimientoContableDTO();

        movimiento.setIdCatalogoCuenta(idCuenta);
        movimiento.setIdDatosPersonal(idDatosPersonalCajero);
        movimiento.setIdAgencia(idAgencia);
        movimiento.setFechaAuxiliar(fecha);
        movimiento.setTipoComprobante(TIPO_COMPROBANTE);
        movimiento.setNumeroComprobante(numeroRC);
        movimiento.setDetalleMovimiento(detalle);
        movimiento.setDocumentoSoporte(soporte);
        movimiento.setOrigenModulo(MODULO);
        movimiento.setDocumentoOrigen(PROCESO);
        movimiento.setValorBase(BigDecimal.ZERO);

        return movimiento;
    }
}
