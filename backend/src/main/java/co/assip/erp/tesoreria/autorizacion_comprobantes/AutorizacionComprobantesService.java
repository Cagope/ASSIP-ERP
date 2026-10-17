
package co.assip.erp.tesoreria.autorizacion_comprobantes;

import co.assip.erp.cajas.movimientos.CajasMovimientosRepository;
import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import co.assip.erp.tesoreria.autorizacion_comprobantes.dto.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AutorizacionComprobantesService {

    private static final String PERMISO_CONSULTAR =
            "TESORERIA_AUTORIZACION_COMPROBANTES_VIEW";

    private static final String PERMISO_AUTORIZAR =
            "TESORERIA_AUTORIZACION_COMPROBANTES_EXECUTE";

    private final AutorizacionComprobantesRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRepository usuarioRepository;
    private final AccessValidator accessValidator;

    private final ConsecutivosComprobantesService consecutivosService;
    private final ContabilidadRegistroService contabilidadRegistroService;
    private final CajasMovimientosRepository cajasMovimientosRepository;

    // =========================================================
    // CONSULTAR PENDIENTES
    // =========================================================

    @Transactional(readOnly = true)
    public List<ComprobanteResumenDTO> listarPendientes(
            Integer idAgencia
    ) {

        validarPermisoConsulta();
        usuarioSesionService.validarAgencia(idAgencia);

        return repository.listarPendientes(idAgencia)
                .stream()
                .map(this::convertirResumen)
                .toList();
    }

    // =========================================================
    // CONSULTAR DETALLE
    // =========================================================

    @Transactional(readOnly = true)
    public ComprobanteDetalleDTO consultar(
            Long idComprobante
    ) {

        var comprobante = obtenerComprobante(idComprobante);

        validarAccesoConsulta(comprobante);

        return convertirDetalle(comprobante);
    }

    // =========================================================
    // PROCESAR DECISIÓN
    // =========================================================

    @Transactional(rollbackFor = Exception.class)
    public ComprobanteDecisionResponseDTO procesarDecision(
            Long idComprobante,
            ComprobanteDecisionRequestDTO solicitud
    ) {

        if (solicitud == null) {
            throw new IllegalArgumentException(
                    "Debe indicar la decisión del comprobante."
            );
        }

        solicitud.validar();

        Integer idUsuario = usuarioSesionService.idUsuario();

        /*
         * FOR UPDATE:
         * Evita que dos usuarios procesen simultáneamente
         * el mismo comprobante.
         */
        var comprobante = repository.bloquear(idComprobante);

        if (comprobante == null) {
            throw new IllegalArgumentException(
                    "No existe el comprobante " + idComprobante
            );
        }

        usuarioSesionService.validarAgencia(
                comprobante.idAgencia()
        );

        if (!"PENDIENTE".equals(comprobante.estado())) {
            throw new IllegalStateException(
                    "El comprobante ya fue procesado. Estado actual: "
                            + comprobante.estado()
            );
        }

        return switch (solicitud.decision()) {

            case AUTORIZAR -> autorizar(
                    comprobante,
                    idUsuario
            );

            case RECHAZAR -> rechazar(
                    comprobante,
                    idUsuario,
                    solicitud.motivo()
            );

            case ANULAR -> anular(
                    comprobante,
                    idUsuario,
                    solicitud.motivo()
            );
        };
    }

    // =========================================================
    // AUTORIZAR
    // =========================================================

    private ComprobanteDecisionResponseDTO autorizar(
            AutorizacionComprobantesRepository.Comprobante comprobante,
            Integer idUsuario
    ) {

        validarPermisoAutorizacion();

        validarUsuarioDiferente(
                comprobante.usuarioCreacion(),
                idUsuario
        );

        List<AutorizacionComprobantesRepository.Partida> partidas =
                repository.listarPartidas(
                        comprobante.idComprobante()
                );

        List<AutorizacionComprobantesRepository.MovimientoCajaPendiente>
                movimientosCaja =
                repository.listarMovimientosCaja(
                        comprobante.idComprobante()
                );

        validarPartidas(comprobante, partidas);

        /*
         * La fecha contable corresponde al día
         * de autorización, no al día de elaboración.
         */
        LocalDate fechaContable = LocalDate.now();

        /*
         * Validamos las provisiones ANTES de generar
         * el consecutivo.
         */
        for (var movimiento : movimientosCaja) {

            var provision = repository.bloquearProvisionAbierta(
                    movimiento.idCaja(),
                    comprobante.idAgencia(),
                    fechaContable
            );

            if (provision == null) {
                throw new IllegalStateException(
                        "La caja " + movimiento.idCaja()
                                + " no tiene una provisión ABIERTA "
                                + "para la fecha " + fechaContable
                );
            }

            if (movimiento.valor() == null
                    || movimiento.valor().compareTo(BigDecimal.ZERO) <= 0) {

                throw new IllegalStateException(
                        "El movimiento de caja debe tener valor positivo."
                );
            }
        }

        /*
         * El consecutivo se genera solamente
         * cuando el comprobante está listo
         * para contabilizar.
         */
        String numeroComprobante =
                consecutivosService.generarNumeroDefinitivo(
                        comprobante.tipoComprobante(),
                        comprobante.idAgencia(),
                        idUsuario
                );

        // -----------------------------------------------------
        // CONTABILIZACIÓN DEFINITIVA
        // -----------------------------------------------------

        List<MovimientoContableDTO> movimientosContables =
                construirMovimientosContables(
                        partidas,
                        fechaContable
                );

        OrigenComprobanteDTO origen = new OrigenComprobanteDTO();

        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("TESORERIA");
        origen.setProcesoOrigen(comprobante.procesoOrigen());
        origen.setTablaOrigen("tesoreria.comprobantes");
        origen.setIdOrigen(comprobante.idComprobante());

        contabilidadRegistroService.registrarComprobante(
                comprobante.idAgencia(),
                comprobante.tipoComprobante(),
                numeroComprobante,
                comprobante.concepto(),
                origen,
                movimientosContables,
                idUsuario
        );

        // -----------------------------------------------------
        // MOVIMIENTOS FÍSICOS DEFINITIVOS
        // -----------------------------------------------------

        for (var movimiento : movimientosCaja) {

            var provision = repository.bloquearProvisionAbierta(
                    movimiento.idCaja(),
                    comprobante.idAgencia(),
                    fechaContable
            );

            if (provision == null) {
                throw new IllegalStateException(
                        "La provisión de la caja "
                                + movimiento.idCaja()
                                + " dejó de estar disponible."
                );
            }

            Long idMovimiento =
                    cajasMovimientosRepository.insertarMovimientoCaja(
                            provision.idProvision(),
                            movimiento.idCaja(),
                            fechaContable,
                            movimiento.idDepartamentoOperativo(),
                            movimiento.idOperacionCaja(),
                            movimiento.moduloOrigen(),
                            movimiento.procesoOrigen(),
                            "tesoreria.comprobantes",
                            comprobante.idComprobante(),
                            movimiento.codigoReferencia(),
                            movimiento.naturaleza(),
                            movimiento.medioPago(),
                            movimiento.valor(),
                            movimiento.concepto(),
                            "REGISTRADO",
                            comprobante.tipoComprobante(),
                            numeroComprobante,
                            idUsuario
                    );

            if (idMovimiento == null) {
                throw new IllegalStateException(
                        "No se pudo registrar el movimiento físico "
                                + movimiento.numeroLinea()
                );
            }
        }

        // -----------------------------------------------------
        // CAMBIAR ESTADO
        // -----------------------------------------------------

        boolean actualizado = repository.marcarContabilizado(
                comprobante.idComprobante(),
                numeroComprobante,
                fechaContable,
                idUsuario
        );

        if (!actualizado) {
            throw new IllegalStateException(
                    "No fue posible finalizar la autorización."
            );
        }

        var actualizadoComprobante =
                obtenerComprobante(comprobante.idComprobante());

        return ComprobanteDecisionResponseDTO.autorizado(
                comprobante.idComprobante(),
                comprobante.idAgencia(),
                comprobante.tipoComprobante(),
                numeroComprobante,
                comprobante.procesoOrigen(),
                comprobante.fechaDocumento(),
                fechaContable,
                actualizadoComprobante.fechaAutorizacion(),
                idUsuario
        );
    }

    // =========================================================
    // RECHAZAR
    // =========================================================

    private ComprobanteDecisionResponseDTO rechazar(
            AutorizacionComprobantesRepository.Comprobante comprobante,
            Integer idUsuario,
            String motivo
    ) {

        validarPermisoAutorizacion();

        validarUsuarioDiferente(
                comprobante.usuarioCreacion(),
                idUsuario
        );

        boolean actualizado = repository.rechazar(
                comprobante.idComprobante(),
                idUsuario,
                motivo
        );

        if (!actualizado) {
            throw new IllegalStateException(
                    "No fue posible rechazar el comprobante."
            );
        }

        var actualizadoComprobante =
                obtenerComprobante(comprobante.idComprobante());

        return ComprobanteDecisionResponseDTO.rechazado(
                comprobante.idComprobante(),
                comprobante.idAgencia(),
                comprobante.tipoComprobante(),
                comprobante.procesoOrigen(),
                comprobante.fechaDocumento(),
                actualizadoComprobante.fechaRechazo(),
                idUsuario,
                motivo.trim()
        );
    }

    // =========================================================
    // ANULAR
    // =========================================================

    private ComprobanteDecisionResponseDTO anular(
            AutorizacionComprobantesRepository.Comprobante comprobante,
            Integer idUsuario,
            String motivo
    ) {

        /*
         * Puede anular:
         *
         * 1. El usuario que elaboró el comprobante.
         * 2. Otro usuario con permiso de autorización.
         *
         * Solo mientras esté PENDIENTE.
         */
        boolean esElaborador = Objects.equals(
                comprobante.usuarioCreacion(),
                idUsuario
        );

        if (!esElaborador) {
            validarPermisoAutorizacion();
        }

        boolean actualizado = repository.anular(
                comprobante.idComprobante(),
                idUsuario,
                motivo
        );

        if (!actualizado) {
            throw new IllegalStateException(
                    "No fue posible anular el comprobante."
            );
        }

        var actualizadoComprobante =
                obtenerComprobante(comprobante.idComprobante());

        return ComprobanteDecisionResponseDTO.anulado(
                comprobante.idComprobante(),
                comprobante.idAgencia(),
                comprobante.tipoComprobante(),
                comprobante.procesoOrigen(),
                comprobante.fechaDocumento(),
                actualizadoComprobante.fechaAnulacion(),
                idUsuario,
                motivo.trim()
        );
    }

    // =========================================================
    // VALIDACIÓN CONTABLE
    // =========================================================

    private void validarPartidas(
            AutorizacionComprobantesRepository.Comprobante comprobante,
            List<AutorizacionComprobantesRepository.Partida> partidas
    ) {

        if (partidas == null || partidas.isEmpty()) {
            throw new IllegalStateException(
                    "El comprobante no tiene partidas contables."
            );
        }

        BigDecimal debitos = BigDecimal.ZERO;
        BigDecimal creditos = BigDecimal.ZERO;

        for (var partida : partidas) {

            if (!Objects.equals(
                    comprobante.idAgencia(),
                    partida.idAgencia()
            )) {
                throw new IllegalStateException(
                        "La partida " + partida.numeroLinea()
                                + " pertenece a otra agencia."
                );
            }

            BigDecimal debito = partida.valorDebito();
            BigDecimal credito = partida.valorCredito();

            if (debito == null || credito == null
                    || debito.signum() < 0
                    || credito.signum() < 0) {
                throw new IllegalStateException(
                        "La partida " + partida.numeroLinea()
                                + " tiene valores inválidos."
                );
            }

            boolean tieneDebito = debito.signum() > 0;
            boolean tieneCredito = credito.signum() > 0;

            if (tieneDebito == tieneCredito) {
                throw new IllegalStateException(
                        "La partida " + partida.numeroLinea()
                                + " debe tener débito o crédito, "
                                + "pero no ambos."
                );
            }

            if (partida.idCatalogoCuenta() == null) {
                throw new IllegalStateException(
                        "La partida " + partida.numeroLinea()
                                + " no tiene cuenta contable."
                );
            }

            if (partida.idDatosPersonal() == null) {
                throw new IllegalStateException(
                        "La partida " + partida.numeroLinea()
                                + " no tiene tercero contable."
                );
            }

            debitos = debitos.add(debito);
            creditos = creditos.add(credito);
        }

        if (debitos.compareTo(creditos) != 0) {
            throw new IllegalStateException(
                    "Comprobante descuadrado. Débitos: "
                            + debitos + ", créditos: " + creditos
            );
        }

        if (debitos.signum() <= 0) {
            throw new IllegalStateException(
                    "El comprobante no tiene valor contable."
            );
        }

        if (debitos.compareTo(comprobante.totalDebito()) != 0
                || creditos.compareTo(comprobante.totalCredito()) != 0) {
            throw new IllegalStateException(
                    "Los valores de las partidas no coinciden "
                            + "con los totales de la cabecera."
            );
        }
    }

    // =========================================================
    // CONSTRUIR MOVIMIENTOS CONTABLES
    // =========================================================

    private List<MovimientoContableDTO> construirMovimientosContables(
            List<AutorizacionComprobantesRepository.Partida> partidas,
            LocalDate fechaContable
    ) {

        List<MovimientoContableDTO> resultado =
                new ArrayList<>();

        for (var partida : partidas) {

            MovimientoContableDTO movimiento =
                    new MovimientoContableDTO();

            movimiento.setIdCatalogoCuenta(
                    partida.idCatalogoCuenta()
            );

            movimiento.setIdAgencia(
                    partida.idAgencia()
            );

            movimiento.setIdDatosPersonal(
                    partida.idDatosPersonal()
            );

            movimiento.setFechaAuxiliar(
                    fechaContable
            );

            movimiento.setDetalleMovimiento(
                    partida.detalleMovimiento()
            );

            movimiento.setValorDebito(
                    partida.valorDebito()
            );

            movimiento.setValorCredito(
                    partida.valorCredito()
            );

            movimiento.setValorBase(
                    partida.valorBase() == null
                            ? BigDecimal.ZERO
                            : partida.valorBase()
            );

            movimiento.setDocumentoSoporte(
                    partida.documentoSoporte()
            );

            movimiento.setOrigenModulo(
                    partida.origenModulo()
            );

            movimiento.setDocumentoOrigen(
                    partida.documentoOrigen()
            );

            resultado.add(movimiento);
        }

        return resultado;
    }

    // =========================================================
    // CONVERSIONES DTO
    // =========================================================

    private ComprobanteResumenDTO convertirResumen(
            AutorizacionComprobantesRepository.Comprobante c
    ) {

        return new ComprobanteResumenDTO(
                c.idComprobante(),
                c.idAgencia(),
                c.tipoComprobante(),
                c.numeroComprobante(),
                c.procesoOrigen(),
                c.fechaDocumento(),
                c.fechaContable(),
                c.concepto(),
                c.documentoSoporte(),
                c.totalDebito(),
                c.totalCredito(),
                c.estado(),
                c.usuarioCreacion(),
                c.fechaCreacion(),
                c.usuarioAutorizacion(),
                c.fechaAutorizacion()
        );
    }

    private ComprobanteDetalleDTO convertirDetalle(
            AutorizacionComprobantesRepository.Comprobante c
    ) {

        List<ComprobantePartidaDTO> partidas =
                repository.listarPartidas(c.idComprobante())
                        .stream()
                        .map(p -> new ComprobantePartidaDTO(
                                p.idDetalle(),
                                p.idComprobante(),
                                p.numeroLinea(),
                                p.idCatalogoCuenta(),
                                p.idAgencia(),
                                p.idDatosPersonal(),
                                p.detalleMovimiento(),
                                p.valorDebito(),
                                p.valorCredito(),
                                p.valorBase(),
                                p.origenModulo(),
                                p.documentoOrigen(),
                                p.documentoSoporte()
                        ))
                        .toList();

        List<ComprobanteMovimientoCajaDTO> movimientos =
                repository.listarMovimientosCaja(c.idComprobante())
                        .stream()
                        .map(m -> new ComprobanteMovimientoCajaDTO(
                                m.idMovimientoPendiente(),
                                m.idComprobante(),
                                m.numeroLinea(),
                                m.idCaja(),
                                m.idDepartamentoOperativo(),
                                m.idOperacionCaja(),
                                m.moduloOrigen(),
                                m.procesoOrigen(),
                                m.tipoDocumento(),
                                m.numeroDocumento(),
                                m.codigoReferencia(),
                                m.naturaleza(),
                                m.medioPago(),
                                m.valor(),
                                m.concepto()
                        ))
                        .toList();

        return new ComprobanteDetalleDTO(
                c.idComprobante(),
                c.idAgencia(),
                c.tipoComprobante(),
                c.numeroComprobante(),
                c.procesoOrigen(),
                c.fechaDocumento(),
                c.fechaContable(),
                c.concepto(),
                c.documentoSoporte(),
                c.totalDebito(),
                c.totalCredito(),
                c.estado(),
                c.usuarioCreacion(),
                c.fechaCreacion(),
                c.usuarioAutorizacion(),
                c.fechaAutorizacion(),
                c.usuarioRechazo(),
                c.fechaRechazo(),
                c.motivoRechazo(),
                c.usuarioAnulacion(),
                c.fechaAnulacion(),
                c.motivoAnulacion(),
                partidas,
                movimientos
        );
    }

    private void validarPermisoConsulta() {

        Integer idUsuario = usuarioSesionService.idUsuario();

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() ->
                        new SecurityException(
                                "No existe el usuario autenticado."
                        )
                );

        accessValidator.validarAcceso(
                usuario,
                PERMISO_CONSULTAR
        );
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarPermisoAutorizacion() {

        Integer idUsuario = usuarioSesionService.idUsuario();

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() ->
                        new SecurityException(
                                "No existe el usuario autenticado."
                        )
                );

        accessValidator.validarAcceso(
                usuario,
                PERMISO_AUTORIZAR
        );
    }

    private void validarUsuarioDiferente(
            Integer usuarioElaborador,
            Integer usuarioAutorizador
    ) {

        if (Objects.equals(
                usuarioElaborador,
                usuarioAutorizador
        )) {
            throw new SecurityException(
                    "El usuario que elaboró el comprobante "
                            + "no puede autorizarlo ni rechazarlo."
            );
        }
    }

    private void validarAccesoConsulta(
            AutorizacionComprobantesRepository.Comprobante comprobante
    ) {

        usuarioSesionService.validarAgencia(
                comprobante.idAgencia()
        );

        boolean esElaborador = Objects.equals(
                comprobante.usuarioCreacion(),
                usuarioSesionService.idUsuario()
        );

        if (!esElaborador) {
            validarPermisoConsulta();
        }
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private AutorizacionComprobantesRepository.Comprobante
    obtenerComprobante(Long idComprobante) {

        if (idComprobante == null) {
            throw new IllegalArgumentException(
                    "Debe indicar el comprobante."
            );
        }

        var comprobante = repository.buscar(idComprobante);

        if (comprobante == null) {
            throw new IllegalArgumentException(
                    "No existe el comprobante " + idComprobante
            );
        }

        return comprobante;
    }

}
