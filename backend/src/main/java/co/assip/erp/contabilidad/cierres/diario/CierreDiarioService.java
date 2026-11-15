package co.assip.erp.contabilidad.cierres.diario;

import co.assip.erp.contabilidad.cierres.diario.dto.CierreDiarioDetalleCuentaDTO;
import co.assip.erp.contabilidad.cierres.diario.dto.CierreDiarioDiaDTO;
import co.assip.erp.contabilidad.cierres.diario.dto.CierreDiarioInformeDTO;
import co.assip.erp.contabilidad.cierres.diario.dto.CierreDiarioMensualDTO;
import co.assip.erp.contabilidad.cierres.diario.dto.CierreDiarioResumenDTO;
import co.assip.erp.contabilidad.periodos.PeriodosContablesRepository;
import co.assip.erp.general.agencia.Agencia;
import co.assip.erp.general.agencia.AgenciaRepository;
import co.assip.erp.seguridad.auditoria.LogEventoService;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import co.assip.erp.seguridad.usuariosroles.UsuarioRolService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Administra la consulta, cierre y reapertura de fechas contables.
 * Conserva los resultados del último cierre y su historial.
 */
@Service
@RequiredArgsConstructor
public class CierreDiarioService {

    private static final String PERMISO_VIEW =
            "CONTABILIDAD_PERIODOS_VIEW";
    private static final String PERMISO_ADMIN =
            "CONTABILIDAD_PERIODOS_ADMIN";
    private static final String PERMISO_REOPEN =
            "CONTABILIDAD_PERIODOS_REOPEN";

    private static final int ID_ROL_CONTADOR = 15;

    private static final String ABIERTO = "A";
    private static final String CERRADO = "C";
    private static final String MES_ABIERTO = "ABIERTO";
    private static final String MES_NO_CREADO = "NO_CREADO";

    private final CierreDiarioRepository cierreRepository;
    private final AgenciaRepository agencias;
    private final PeriodosContablesRepository mesesRepository;
    private final UsuarioSesionService sesion;
    private final UsuarioRepository usuarios;
    private final UsuarioRolService usuariosRoles;
    private final AccessValidator accessValidator;
    private final LogEventoService logEventoService;

    /**
     * Consulta los días del mes y consolida sus resultados.
     * No modifica períodos ni movimientos contables.
     */
    @Transactional(readOnly = true)
    public CierreDiarioMensualDTO consultarMes(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        validarMes(idAgencia, ano, mes);
        autorizar(PERMISO_VIEW, idAgencia);

        String estadoMes = mesesRepository.consultarEstado(
                idAgencia,
                ano,
                mes
        ).orElse(MES_NO_CREADO);

        List<CierreDiarioDiaDTO> dias =
                cierreRepository.consultarDiasMes(idAgencia, ano, mes);

        int diasAbiertos = 0;
        int diasCerrados = 0;
        int diasConDiferencias = 0;
        int diasSinMovimientos = 0;

        BigDecimal totalDebitos = BigDecimal.ZERO;
        BigDecimal totalCreditos = BigDecimal.ZERO;

        for (CierreDiarioDiaDTO dia : dias) {
            if (CERRADO.equals(dia.estado())) {
                diasCerrados++;
            } else {
                diasAbiertos++;
            }

            if (!dia.cuadrado()) {
                diasConDiferencias++;
            }

            if (!dia.tieneMovimientos()) {
                diasSinMovimientos++;
            }

            totalDebitos = totalDebitos.add(dia.totalDebitos());
            totalCreditos = totalCreditos.add(dia.totalCreditos());
        }

        return new CierreDiarioMensualDTO(
                idAgencia,
                ano,
                mes,
                estadoMes,
                diasAbiertos,
                diasCerrados,
                diasConDiferencias,
                diasSinMovimientos,
                totalDebitos,
                totalCreditos,
                totalDebitos.subtract(totalCreditos),
                dias
        );
    }

    /**
     * Consulta el estado y los movimientos actuales de una fecha.
     * Permite consultar incluso años y meses cerrados.
     */
    @Transactional(readOnly = true)
    public CierreDiarioResumenDTO consultar(
            Integer idAgencia,
            LocalDate fecha
    ) {
        validarParametros(idAgencia, fecha);
        autorizar(PERMISO_VIEW, idAgencia);

        String estadoMes = mesesRepository.consultarEstado(
                idAgencia,
                fecha.getYear(),
                fecha.getMonthValue()
        ).orElse(MES_NO_CREADO);

        String estadoDia = cierreRepository.consultarEstadoDiario(
                idAgencia,
                fecha
        );

        if (estadoDia == null) {
            estadoDia = ABIERTO;
        }

        return construirRespuesta(
                idAgencia,
                fecha,
                estadoMes,
                estadoDia
        );
    }

    /**
     * Construye el informe completo de una fecha para impresión y Excel.
     * Verifica que el detalle coincida con los totales del cierre diario.
     */
    @Transactional(readOnly = true)
    public CierreDiarioInformeDTO consultarInforme(
            Integer idAgencia,
            LocalDate fecha
    ) {
        validarParametros(idAgencia, fecha);
        autorizar(PERMISO_VIEW, idAgencia);

        Agencia agencia = agencias.findById(idAgencia)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La agencia consultada no existe."
                ));

        String estadoMes = mesesRepository.consultarEstado(
                idAgencia,
                fecha.getYear(),
                fecha.getMonthValue()
        ).orElse(MES_NO_CREADO);

        String estadoDia = cierreRepository.consultarEstadoDiario(idAgencia, fecha);
        if (estadoDia == null) {
            estadoDia = ABIERTO;
        }

        CierreDiarioResumenDTO resumen = cierreRepository.consultarResumen(idAgencia, fecha);
        List<CierreDiarioDetalleCuentaDTO> cuentas =
                cierreRepository.consultarDetallePorCuenta(idAgencia, fecha);
        List<CierreDiarioInformeDTO.ResumenMovimientoDTO> movimientos =
                cierreRepository.consultarMovimientosInforme(idAgencia, fecha);

        BigDecimal debitosDetalle = BigDecimal.ZERO;
        BigDecimal creditosDetalle = BigDecimal.ZERO;
        for (CierreDiarioInformeDTO.ResumenMovimientoDTO movimiento : movimientos) {
            debitosDetalle = debitosDetalle.add(movimiento.valorDebito());
            creditosDetalle = creditosDetalle.add(movimiento.valorCredito());
        }

        if (movimientos.size() != resumen.cantidadMovimientos()
                || debitosDetalle.compareTo(resumen.totalDebitos()) != 0
                || creditosDetalle.compareTo(resumen.totalCreditos()) != 0) {
            throw new IllegalStateException(
                    "El detalle contable no coincide con el resumen del día. "
                            + "No se puede generar un informe inconsistente."
            );
        }

        return new CierreDiarioInformeDTO(
                idAgencia,
                agencia.getCodigoAgencia(),
                agencia.getNombreAgencia(),
                fecha,
                estadoDia,
                estadoMes,
                LocalDateTime.now(),
                resumen.cantidadComprobantes(),
                resumen.cantidadMovimientos(),
                resumen.totalDebitos(),
                resumen.totalCreditos(),
                resumen.diferencia(),
                cuentas,
                movimientos
        );
    }

    /**
     * Cierra una fecha contable, guarda sus resultados y registra
     * el historial. Toda la operación se ejecuta en una transacción.
     */
    @Transactional
    public CierreDiarioResumenDTO cerrar(
            Integer idAgencia,
            LocalDate fecha,
            HttpServletRequest request
    ) {
        validarParametros(idAgencia, fecha);
        autorizar(PERMISO_ADMIN, idAgencia);

        Integer idUsuario = sesion.idUsuario();

        validarAnoNoCerrado(idAgencia, fecha.getYear());
        bloquearMesAbierto(idAgencia, fecha, idUsuario);
        validarAnoNoCerrado(idAgencia, fecha.getYear());

        String estadoDia = cierreRepository.bloquearEstadoDiario(
                idAgencia,
                fecha
        );

        if (CERRADO.equals(estadoDia)) {
            throw new IllegalStateException(
                    "La fecha contable ya está cerrada."
            );
        }

        if (estadoDia != null && !ABIERTO.equals(estadoDia)) {
            throw new IllegalStateException(
                    "El estado de la fecha contable no es válido."
            );
        }

        CierreDiarioResumenDTO resumen = construirRespuesta(
                idAgencia,
                fecha,
                MES_ABIERTO,
                ABIERTO
        );

        if (resumen.diferencia().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                    "No es posible cerrar el día: "
                            + "los débitos y créditos no coinciden."
            );
        }

        int registros;

        if (estadoDia == null) {
            registros = cierreRepository.insertarCierre(
                    idAgencia,
                    fecha,
                    resumen,
                    idUsuario
            );
        } else {
            registros = cierreRepository.actualizarCierre(
                    idAgencia,
                    fecha,
                    resumen,
                    idUsuario
            );
        }

        if (registros != 1) {
            throw new IllegalStateException(
                    "No fue posible registrar el cierre diario."
            );
        }

        int registrosHistorial = cierreRepository.registrarHistorial(
                idAgencia,
                fecha,
                "CIERRE",
                null,
                resumen,
                idUsuario
        );

        if (registrosHistorial != 1) {
            throw new IllegalStateException(
                    "No fue posible registrar el historial del cierre diario."
            );
        }

        logEventoService.registrarEvento(
                idUsuario,
                "CONTABILIDAD",
                "CIERRE_DIARIO",
                "Agencia=" + idAgencia
                        + "; fecha=" + fecha
                        + "; estado=A->C"
                        + "; comprobantes=" + resumen.cantidadComprobantes()
                        + "; movimientos=" + resumen.cantidadMovimientos()
                        + "; debitos=" + resumen.totalDebitos()
                        + "; creditos=" + resumen.totalCreditos()
                        + "; diferencia=" + resumen.diferencia(),
                request
        );

        return new CierreDiarioResumenDTO(
                idAgencia,
                fecha,
                MES_ABIERTO,
                CERRADO,
                resumen.totalDebitos(),
                resumen.totalCreditos(),
                resumen.diferencia(),
                resumen.cantidadComprobantes(),
                resumen.cantidadMovimientos(),
                resumen.detalleCuentas()
        );
    }

    /**
     * Reabre un día cerrado con justificación obligatoria.
     * Conserva el último resumen y registra la reapertura.
     */
    @Transactional
    public CierreDiarioResumenDTO reabrir(
            Integer idAgencia,
            LocalDate fecha,
            String justificacion,
            HttpServletRequest request
    ) {
        validarParametros(idAgencia, fecha);
        autorizar(PERMISO_REOPEN, idAgencia);

        if (justificacion == null || justificacion.isBlank()) {
            throw new IllegalArgumentException(
                    "La justificación de reapertura es obligatoria."
            );
        }

        String motivo = justificacion.trim();
        Integer idUsuario = sesion.idUsuario();

        validarAnoNoCerrado(idAgencia, fecha.getYear());
        bloquearMesAbierto(idAgencia, fecha, idUsuario);
        validarAnoNoCerrado(idAgencia, fecha.getYear());

        String estadoDia = cierreRepository.bloquearEstadoDiario(
                idAgencia,
                fecha
        );

        if (estadoDia == null) {
            throw new IllegalStateException(
                    "No existe un cierre diario para esta fecha."
            );
        }

        if (!CERRADO.equals(estadoDia)) {
            throw new IllegalStateException(
                    "La fecha contable no está cerrada."
            );
        }

        /*
         * El historial de reapertura conserva la fotografía del último
         * cierre. Los valores actuales se consultarán después de reabrir.
         */
        CierreDiarioResumenDTO ultimoCierre =
                cierreRepository.consultarUltimoCierre(
                        idAgencia,
                        fecha
                );

        int registros = cierreRepository.reabrirDia(
                idAgencia,
                fecha,
                idUsuario
        );

        if (registros != 1) {
            throw new IllegalStateException(
                    "No fue posible reabrir el día contable."
            );
        }

        int registrosHistorial = cierreRepository.registrarHistorial(
                idAgencia,
                fecha,
                "REAPERTURA",
                motivo,
                ultimoCierre,
                idUsuario
        );

        if (registrosHistorial != 1) {
            throw new IllegalStateException(
                    "No fue posible registrar el historial de reapertura."
            );
        }

        logEventoService.registrarEvento(
                idUsuario,
                "CONTABILIDAD",
                "REAPERTURA_DIARIA",
                "Agencia=" + idAgencia
                        + "; fecha=" + fecha
                        + "; estado=C->A"
                        + "; justificacion=" + motivo,
                request
        );

        return construirRespuesta(
                idAgencia,
                fecha,
                MES_ABIERTO,
                ABIERTO
        );
    }

    /**
     * Construye el resumen con los movimientos contables actuales
     * y su detalle por cuenta.
     */
    private CierreDiarioResumenDTO construirRespuesta(
            Integer idAgencia,
            LocalDate fecha,
            String estadoMes,
            String estadoDia
    ) {
        CierreDiarioResumenDTO resumen =
                cierreRepository.consultarResumen(idAgencia, fecha);

        List<CierreDiarioDetalleCuentaDTO> detalle =
                cierreRepository.consultarDetallePorCuenta(
                        idAgencia,
                        fecha
                );

        return new CierreDiarioResumenDTO(
                idAgencia,
                fecha,
                estadoMes,
                estadoDia,
                resumen.totalDebitos(),
                resumen.totalCreditos(),
                resumen.diferencia(),
                resumen.cantidadComprobantes(),
                resumen.cantidadMovimientos(),
                detalle
        );
    }

    /**
     * Garantiza que el mes exista y esté abierto.
     * Bloquea el período para impedir operaciones simultáneas.
     */
    private void bloquearMesAbierto(
            Integer idAgencia,
            LocalDate fecha,
            Integer idUsuario
    ) {
        int ano = fecha.getYear();
        int mes = fecha.getMonthValue();

        boolean creado = mesesRepository.crearPeriodoSiNoExiste(
                idAgencia,
                ano,
                mes,
                idUsuario
        );

        var periodo = mesesRepository.bloquearPeriodo(
                idAgencia,
                ano,
                mes
        ).orElseThrow(() -> new IllegalStateException(
                "No fue posible bloquear el período mensual."
        ));

        if (!MES_ABIERTO.equals(periodo.estado())) {
            throw new IllegalStateException(
                    "El período mensual está cerrado. "
                            + "Solo se permite consultar sus días."
            );
        }

        if (creado) {
            mesesRepository.registrarHistorial(
                    periodo.idPeriodoContable(),
                    null,
                    MES_ABIERTO,
                    "APERTURA",
                    "Apertura automática al realizar el cierre diario",
                    idUsuario
            );
        }
    }

    /**
     * Impide operaciones sobre años contables cerrados.
     * Los años cerrados permanecen disponibles para consulta.
     */
    private void validarAnoNoCerrado(
            Integer idAgencia,
            int ano
    ) {
        if (cierreRepository.estaAnoCerrado(idAgencia, ano)) {
            throw new IllegalStateException(
                    "El año contable está cerrado. "
                            + "Solo se permite consultar sus días."
            );
        }
    }

    /**
     * Valida los parámetros de una consulta mensual.
     */
    private void validarMes(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException(
                    "La agencia es obligatoria."
            );
        }

        if (ano < 1900 || ano > 9999) {
            throw new IllegalArgumentException(
                    "El año contable no es válido."
            );
        }

        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException(
                    "El mes contable debe estar entre 1 y 12."
            );
        }
    }

    /**
     * Valida agencia y fecha antes de cualquier operación.
     */
    private void validarParametros(
            Integer idAgencia,
            LocalDate fecha
    ) {
        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException(
                    "La agencia es obligatoria."
            );
        }

        if (fecha == null
                || fecha.getYear() < 1900
                || fecha.getYear() > 9999) {
            throw new IllegalArgumentException(
                    "La fecha contable no es válida."
            );
        }
    }

    /**
     * Valida el permiso funcional y el alcance de agencia.
     * Conserva el criterio de autorización existente.
     */
    private void autorizar(
            String permiso,
            Integer idAgencia
    ) {
        Usuario usuario = usuarios.findById(sesion.idUsuario())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new SecurityException(
                        "El usuario autenticado no está activo."
                ));

        accessValidator.validarAcceso(usuario, permiso);

        if (sesion.tieneAccesoTotal()) {
            return;
        }

        boolean esContador = usuariosRoles
                .listarVigentes(sesion.idUsuario())
                .stream()
                .anyMatch(asignacion ->
                        asignacion.getRol() != null
                                && Boolean.TRUE.equals(
                                asignacion.getRol().getActivo()
                        )
                                && Integer.valueOf(ID_ROL_CONTADOR).equals(
                                asignacion.getRol().getIdRol()
                        )
                );

        if (!esContador) {
            sesion.validarAgencia(idAgencia);
        }
    }
}
