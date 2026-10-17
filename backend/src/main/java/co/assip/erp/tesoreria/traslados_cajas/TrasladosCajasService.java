package co.assip.erp.tesoreria.traslados_cajas;

import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoPreviewDTO;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoRequestDTO;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoResponseDTO;

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
public class TrasladosCajasService {

    private static final String TIPO_COMPROBANTE = "TS";
    private static final String MODULO_ORIGEN = "TESORERIA";
    private static final String PROCESO_ORIGEN = "TRASLADO_CAJAS";

    private static final String MEDIO_EFECTIVO = "EFECTIVO";
    private static final String TIPO_DOCUMENTO = "TF";

    private static final String NATURALEZA_INGRESO = "INGRESO";
    private static final String NATURALEZA_EGRESO = "EGRESO";

    private final TrasladosCajasRepository repository;

    private final ConsecutivosComprobantesService consecutivosComprobantesService;

    private final ContabilidadRegistroService contabilidadRegistroService;

    private final UsuarioSesionService usuarioSesionService;

    // =========================================================
    // VALIDACIONES GENERALES
    // =========================================================

    private void validarTrasladoCajas(
            MovimientoFondoRequestDTO request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud del traslado es obligatoria."
            );
        }

        validarAgencia(request.getIdAgencia());

        if (request.getFechaMovimiento() == null) {
            throw new IllegalArgumentException(
                    "La fecha del traslado es obligatoria."
            );
        }

        if (request.getIdCajaOrigen() == null
                || request.getIdCajaDestino() == null
                || request.getIdCajaOrigen().equals(request.getIdCajaDestino())) {

            throw new IllegalArgumentException(
                    "Seleccione dos cajas diferentes de la misma agencia."
            );
        }

        if (request.getValor() == null
                || request.getValor().compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El valor del traslado debe ser mayor a cero."
            );
        }

        var modalidad = request.getModalidadTrasladoCaja();

        if (modalidad != MovimientoFondoRequestDTO.ModalidadTrasladoCaja.PROVISION
                && modalidad != MovimientoFondoRequestDTO.ModalidadTrasladoCaja.REEMBOLSO) {

            throw new IllegalArgumentException(
                    "Solo se permiten PROVISION y REEMBOLSO entre cajas CONTROL y OPERATIVA."
            );
        }

        if ((request.getIdsCheques() != null
                && !request.getIdsCheques().isEmpty())
                || (request.getEfectivos() != null
                && !request.getEfectivos().isEmpty())) {

            throw new IllegalArgumentException(
                    "El traslado entre cajas utiliza únicamente el valor en efectivo."
            );
        }
    }

    // =========================================================
    // CAJAS ORIGEN Y DESTINO
    // =========================================================

    private record CajasTraslado(
            TrasladosCajasRepository.CajaMovimientoDisponible origen,
            TrasladosCajasRepository.CajaMovimientoDisponible destino
    ) {
    }

    private CajasTraslado obtenerCajasTraslado(
            MovimientoFondoRequestDTO request,
            boolean bloquear
    ) {

        List<TrasladosCajasRepository.CajaMovimientoDisponible> cajas =
                new ArrayList<>();

        if (bloquear) {

            List<Long> ids = new ArrayList<>(
                    List.of(
                            request.getIdCajaOrigen(),
                            request.getIdCajaDestino()
                    )
            );

            ids.sort(Comparator.naturalOrder());

            for (Long idCaja : ids) {

                var caja = repository.obtenerCajaMovimientoConLock(
                        idCaja,
                        request.getIdAgencia()
                );

                if (caja == null) {
                    throw new IllegalStateException(
                            "La caja " + idCaja + " no está habilitada para traslados."
                    );
                }

                cajas.add(caja);
            }

        } else {

            cajas.addAll(
                    repository.listarCajasMovimiento(
                            request.getIdAgencia()
                    )
            );
        }

        var origen = cajas.stream()
                .filter(c -> c.idCaja().equals(request.getIdCajaOrigen()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "La caja origen no está disponible."
                ));

        var destino = cajas.stream()
                .filter(c -> c.idCaja().equals(request.getIdCajaDestino()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "La caja destino no está disponible."
                ));

        boolean provision =
                request.getModalidadTrasladoCaja()
                        == MovimientoFondoRequestDTO.ModalidadTrasladoCaja.PROVISION;

        if (provision) {

            if (!"CONTROL".equalsIgnoreCase(origen.tipoCaja())
                    || !"OPERATIVA".equalsIgnoreCase(destino.tipoCaja())) {

                throw new IllegalArgumentException(
                        "PROVISION requiere una caja CONTROL como origen "
                                + "y una caja OPERATIVA como destino."
                );
            }

        } else {

            if (!"OPERATIVA".equalsIgnoreCase(origen.tipoCaja())
                    || !"CONTROL".equalsIgnoreCase(destino.tipoCaja())) {

                throw new IllegalArgumentException(
                        "REEMBOLSO requiere una caja OPERATIVA como origen "
                                + "y una caja CONTROL como destino."
                );
            }
        }

        return new CajasTraslado(origen, destino);
    }

    // =========================================================
    // CONCEPTO DEL TRASLADO
    // =========================================================

    private String conceptoTraslado(
            MovimientoFondoRequestDTO request
    ) {

        String concepto = request.getModalidadTrasladoCaja() + " de efectivo entre cajas";

        if (!isBlank(request.getObservacion())) {
            concepto += " - " + trim(request.getObservacion());
        }

        return limitar(concepto, 250);
    }

    // =========================================================
    // VALIDACIÓN DEL EFECTIVO DISPONIBLE
    // =========================================================

    private void validarEfectivoDisponible(
            Long idProvisionOrigen,
            BigDecimal valorTraslado
    ) {

        BigDecimal disponible =
                repository.obtenerEfectivoDisponibleJornada(
                        idProvisionOrigen
                );

        if (disponible == null) {
            throw new IllegalStateException(
                    "No fue posible determinar el efectivo disponible de la caja origen."
            );
        }

        if (disponible.compareTo(valorTraslado) < 0) {

            throw new IllegalStateException(
                    "Efectivo insuficiente en la caja origen. "
                            + "Disponible: " + disponible.toPlainString()
                            + ". Valor solicitado: " + valorTraslado.toPlainString()
                            + "."
            );
        }
    }

    // =========================================================
    // VISTA PREVIA
    // =========================================================

    @Transactional(readOnly = true)
    public MovimientoFondoPreviewDTO previewTrasladoCajas(
            MovimientoFondoRequestDTO request
    ) {

        validarTrasladoCajas(request);

        CajasTraslado cajas =
                obtenerCajasTraslado(request, false);

        Integer agencia = request.getIdAgencia();
        LocalDate fecha = request.getFechaMovimiento();
        BigDecimal valor = request.getValor();

        if (!repository.tieneProvisionAbierta(
                cajas.origen().idCaja(),
                agencia,
                fecha
        )) {

            throw new IllegalStateException(
                    "La caja origen no tiene una provisión ABIERTA para la fecha del traslado."
            );
        }

        if (!repository.tieneProvisionAbierta(
                cajas.destino().idCaja(),
                agencia,
                fecha
        )) {

            throw new IllegalStateException(
                    "La caja destino no tiene una provisión ABIERTA para la fecha del traslado."
            );
        }

        String concepto = conceptoTraslado(request);

        List<MovimientoContablePreviewDTO> movimientos = List.of(

                crearMovimientoPreview(
                        cajas.destino().idCatalogoCuentaCaja(),
                        cajas.destino().codigoCuentaCaja(),
                        cajas.destino().nombreCuentaCaja(),
                        agencia,
                        fecha,
                        valor,
                        BigDecimal.ZERO,
                        concepto
                ),

                crearMovimientoPreview(
                        cajas.origen().idCatalogoCuentaCaja(),
                        cajas.origen().codigoCuentaCaja(),
                        cajas.origen().nombreCuentaCaja(),
                        agencia,
                        fecha,
                        BigDecimal.ZERO,
                        valor,
                        concepto
                )
        );

        return MovimientoFondoPreviewDTO.builder()
                .tipoMovimiento(request.getTipoMovimiento())
                .modalidadTrasladoCaja(request.getModalidadTrasladoCaja())
                .idAgencia(agencia)
                .fechaMovimiento(fecha)
                .numeroSoporte(null)
                .idCajaOrigen(cajas.origen().idCaja())
                .idCajaDestino(cajas.destino().idCaja())
                .tipoComprobante(TIPO_COMPROBANTE)
                .conceptoComprobante(concepto)
                .movimientos(movimientos)
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
    // APLICACIÓN DEFINITIVA DEL TRASLADO
    // =========================================================

    @Transactional(rollbackFor = Exception.class)
    public MovimientoFondoResponseDTO aplicarTrasladoCajas(
            MovimientoFondoRequestDTO request
    ) {

        validarTrasladoCajas(request);

        // -----------------------------------------------------
        // 1. Usuario autenticado y datos generales
        // -----------------------------------------------------

        Integer usuario = usuarioSesionService.idUsuario();

        Integer idDatosPersonal =
                repository.obtenerIdDatosPersonalUsuario(usuario);

        Integer agencia = request.getIdAgencia();

        LocalDate fecha = request.getFechaMovimiento();

        BigDecimal valor = request.getValor();

        String concepto = conceptoTraslado(request);

        // -----------------------------------------------------
        // 2. Validar y bloquear ambas cajas
        // -----------------------------------------------------

        CajasTraslado cajas =
                obtenerCajasTraslado(request, true);

        // -----------------------------------------------------
        // 3. Bloquear las dos provisiones en orden fijo
        // -----------------------------------------------------

        List<Long> idsCajas = new ArrayList<>(
                List.of(
                        cajas.origen().idCaja(),
                        cajas.destino().idCaja()
                )
        );

        idsCajas.sort(Comparator.naturalOrder());

        Map<Long, TrasladosCajasRepository.CajaEfectivoDisponible> jornadas =
                new HashMap<>();

        for (Long idCaja : idsCajas) {

            var jornada =
                    repository.obtenerCajaConProvisionAbiertaConLock(
                            idCaja,
                            agencia,
                            fecha
                    );

            if (jornada == null) {

                throw new IllegalStateException(
                        "La caja " + idCaja
                                + " no tiene provisión ABIERTA para "
                                + fecha + "."
                );
            }

            jornadas.put(idCaja, jornada);
        }

        var jornadaOrigen =
                jornadas.get(cajas.origen().idCaja());

        var jornadaDestino =
                jornadas.get(cajas.destino().idCaja());

        // -----------------------------------------------------
        // 4. Validar retiro <= efectivo disponible
        // -----------------------------------------------------

        validarEfectivoDisponible(
                jornadaOrigen.idProvision(),
                valor
        );

        // -----------------------------------------------------
        // 5. Obtener departamento y operación de cajas
        // -----------------------------------------------------

        boolean esProvision = request.getModalidadTrasladoCaja()
                == MovimientoFondoRequestDTO.ModalidadTrasladoCaja.PROVISION;

        String codigoDepartamento = esProvision ? "22" : "27";
        String codigoOperacion = esProvision ? "PROVISION_CAJA" : "REEMBOLSO_CAJA";

        Long departamento = repository.obtenerDepartamentoOperativo(codigoDepartamento);
        Long operacion = repository.obtenerOperacionCaja(codigoOperacion);

        if (departamento == null || operacion == null) {

            throw new IllegalStateException(
                    "El departamento o la operación de traslado "
                            + "no están activos en el catálogo de Cajas."
            );
        }

        // -----------------------------------------------------
        // 6. Generar comprobante contable
        // -----------------------------------------------------

        String comprobante =
                consecutivosComprobantesService.generarNumeroDefinitivo(
                        TIPO_COMPROBANTE,
                        agencia,
                        usuario
                );

        if (isBlank(comprobante)) {

            throw new IllegalStateException(
                    "No fue posible generar el comprobante contable."
            );
        }

        // El consecutivo TS es el soporte de ambos movimientos físicos.
        String numeroSoporte = comprobante;

        String referencia =
                limitar(
                        cajas.origen().codigoCaja()
                                + "-"
                                + cajas.destino().codigoCaja(),
                        20
                );

        // -----------------------------------------------------
        // 7. EGRESO de la caja origen
        // -----------------------------------------------------

        Long idMovimientoOrigen =
                repository.insertarMovimientoCaja(
                        jornadaOrigen.idProvision(),
                        cajas.origen().idCaja(),
                        departamento,
                        operacion,
                        PROCESO_ORIGEN,
                        TIPO_DOCUMENTO,
                        numeroSoporte,
                        referencia,
                        NATURALEZA_EGRESO,
                        MEDIO_EFECTIVO,
                        valor,
                        concepto,
                        usuario
                );

        // -----------------------------------------------------
        // 8. INGRESO en la caja destino
        // -----------------------------------------------------

        Long idMovimientoDestino =
                repository.insertarMovimientoCaja(
                        jornadaDestino.idProvision(),
                        cajas.destino().idCaja(),
                        departamento,
                        operacion,
                        PROCESO_ORIGEN,
                        TIPO_DOCUMENTO,
                        numeroSoporte,
                        referencia,
                        NATURALEZA_INGRESO,
                        MEDIO_EFECTIVO,
                        valor,
                        concepto,
                        usuario
                );

        // -----------------------------------------------------
        // 9. Construir partidas contables
        //
        // Débito: caja destino
        // Crédito: caja origen
        // -----------------------------------------------------

        List<MovimientoContableDTO> movimientosContables = List.of(

                crearMovimientoContable(
                        cajas.destino().idCatalogoCuentaCaja(),
                        agencia,
                        fecha,
                        comprobante,
                        valor,
                        BigDecimal.ZERO,
                        concepto,
                        numeroSoporte,
                        idDatosPersonal
                ),

                crearMovimientoContable(
                        cajas.origen().idCatalogoCuentaCaja(),
                        agencia,
                        fecha,
                        comprobante,
                        BigDecimal.ZERO,
                        valor,
                        concepto,
                        numeroSoporte,
                        idDatosPersonal
                )
        );

        validarCuadre(movimientosContables);

        // -----------------------------------------------------
        // 10. Registrar comprobante contable
        // -----------------------------------------------------

        OrigenComprobanteDTO origenContable =
                new OrigenComprobanteDTO();

        origenContable.setOrigenTipo("AUTO");
        origenContable.setModuloOrigen(MODULO_ORIGEN);
        origenContable.setProcesoOrigen(PROCESO_ORIGEN);
        origenContable.setTablaOrigen("cajas.movimientos_caja");
        origenContable.setIdOrigen(idMovimientoOrigen);

        contabilidadRegistroService.registrarComprobante(
                agencia,
                TIPO_COMPROBANTE,
                comprobante,
                concepto,
                origenContable,
                movimientosContables,
                usuario
        );

        // -----------------------------------------------------
        // 11. Asociar el mismo TS a ambos movimientos físicos
        // -----------------------------------------------------

        repository.asociarComprobanteContable(
                idMovimientoOrigen,
                TIPO_COMPROBANTE,
                comprobante,
                usuario
        );

        repository.asociarComprobanteContable(
                idMovimientoDestino,
                TIPO_COMPROBANTE,
                comprobante,
                usuario
        );

        // -----------------------------------------------------
        // 12. Respuesta
        // -----------------------------------------------------

        return new MovimientoFondoResponseDTO(
                null,
                request.getTipoMovimiento(),
                agencia,
                fecha,
                numeroSoporte,
                request.getObservacion(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                cajas.origen().idCaja(),
                cajas.origen().nombreCaja(),
                cajas.destino().idCaja(),
                cajas.destino().nombreCaja(),
                request.getModalidadTrasladoCaja(),
                0,
                BigDecimal.ZERO,
                valor,
                valor,
                TIPO_COMPROBANTE,
                comprobante,
                concepto
        );
    }

    // =========================================================
    // CONSTRUCCIÓN DE MOVIMIENTOS CONTABLES
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
        movimiento.setIdCatalogoCuenta(idCatalogoCuenta);
        movimiento.setIdAgencia(idAgencia);
        movimiento.setFechaAuxiliar(fecha);
        movimiento.setTipoComprobante(TIPO_COMPROBANTE);
        movimiento.setNumeroComprobante(numeroComprobante);

        movimiento.setDetalleMovimiento(
                limitar(detalle, 100)
        );

        movimiento.setValorDebito(nvl(debito));
        movimiento.setValorCredito(nvl(credito));
        movimiento.setValorBase(BigDecimal.ZERO);

        movimiento.setDocumentoSoporte(
                limitar(documentoSoporte, 20)
        );

        movimiento.setOrigenModulo(MODULO_ORIGEN);
        movimiento.setDocumentoOrigen(PROCESO_ORIGEN);

        return movimiento;
    }

    // =========================================================
    // CONSTRUCCIÓN DE VISTA PREVIA CONTABLE
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

        movimiento.setIdAgencia(idAgencia);
        movimiento.setFechaAuxiliar(fecha);
        movimiento.setIdCatalogoCuenta(idCatalogoCuenta);
        movimiento.setCodigoCuenta(codigoCuenta);
        movimiento.setNombreCuenta(nombreCuenta);

        movimiento.setDetalleMovimiento(
                limitar(detalle, 100)
        );

        movimiento.setValorDebito(nvl(debito));
        movimiento.setValorCredito(nvl(credito));

        return movimiento;
    }

    // =========================================================
    // VALIDACIÓN DEL CUADRE CONTABLE
    // =========================================================

    private void validarCuadre(
            List<MovimientoContableDTO> movimientos
    ) {

        BigDecimal debitos = BigDecimal.ZERO;
        BigDecimal creditos = BigDecimal.ZERO;

        for (MovimientoContableDTO movimiento : movimientos) {

            debitos = debitos.add(
                    nvl(movimiento.getValorDebito())
            );

            creditos = creditos.add(
                    nvl(movimiento.getValorCredito())
            );
        }

        if (debitos.compareTo(creditos) != 0) {

            throw new IllegalStateException(
                    "El traslado entre cajas no cuadra contablemente. "
                            + "Débitos: " + debitos
                            + " - Créditos: " + creditos + "."
            );
        }
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private void validarAgencia(
            Integer idAgencia
    ) {

        if (idAgencia == null || idAgencia <= 0) {

            throw new IllegalArgumentException(
                    "La agencia es obligatoria."
            );
        }
    }

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

        String limpio = valor.trim();

        if (limpio.length() <= longitud) {
            return limpio;
        }

        return limpio.substring(0, longitud);
    }
}