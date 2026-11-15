package co.assip.erp.contabilidad.cierres.mensualcontable;

import co.assip.erp.contabilidad.cierres.mensualcontable.dto.CierreMensualContableDetalleCuentaDTO;
import co.assip.erp.contabilidad.cierres.mensualcontable.dto.CierreMensualContableDiaPendienteDTO;
import co.assip.erp.contabilidad.cierres.mensualcontable.dto.CierreMensualContableResumenDTO;
import co.assip.erp.contabilidad.periodos.PeriodosContablesRepository;
import co.assip.erp.contabilidad.periodos.PeriodosDiariosRepository;
import co.assip.erp.general.agencia.Agencia;
import co.assip.erp.general.agencia.AgenciaRepository;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import co.assip.erp.seguridad.usuariosroles.UsuarioRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Administra la vista previa y confirmación del cierre mensual.
 * Consolida saldos y registra el cierre en una sola transacción.
 */
@Service
@RequiredArgsConstructor
public class CierreMensualContableService {

    private static final String ABIERTO = "ABIERTO";
    private static final String CERRADO = "CERRADO";
    private static final String NO_CREADO = "NO_CREADO";

    private static final String PERMISO_VIEW =
            "CONTABILIDAD_PERIODOS_VIEW";
    private static final String PERMISO_ADMIN =
            "CONTABILIDAD_PERIODOS_ADMIN";

    private static final String PERMISO_REOPEN = "CONTABILIDAD_PERIODOS_REOPEN";

    private static final int ID_ROL_CONTADOR = 15;

    private final CierreMensualContableRepository cierreRepository;
    private final PeriodosContablesRepository periodosRepository;
    private final PeriodosDiariosRepository diariosRepository;
    private final AgenciaRepository agencias;
    private final UsuarioSesionService sesion;
    private final UsuarioRepository usuarios;
    private final UsuarioRolService usuariosRoles;
    private final AccessValidator accessValidator;

    /**
     * Consulta el estado, los días pendientes y los saldos del mes.
     * No modifica registros contables.
     */
    @Transactional(readOnly = true)
    public CierreMensualContableResumenDTO consultar(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        validarParametros(idAgencia, ano, mes);
        autorizar(PERMISO_VIEW, idAgencia);

        String estado = periodosRepository
                .consultarEstado(idAgencia, ano, mes)
                .orElse(NO_CREADO);

        return construirResumen(idAgencia, ano, mes, estado);
    }

    /**
     * Consulta el detalle contable según el estado del período.
     * Para períodos cerrados recupera los saldos históricos guardados.
     */
    @Transactional(readOnly = true)
    public List<CierreMensualContableDetalleCuentaDTO> consultarDetalle(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        validarParametros(idAgencia, ano, mes);
        autorizar(PERMISO_VIEW, idAgencia);

        String estado = periodosRepository
                .consultarEstado(idAgencia, ano, mes)
                .orElse(NO_CREADO);

        return consultarCuentasSegunEstado(
                idAgencia, ano, mes, estado
        );
    }

    /**
     * Confirma el cierre mensual.
     * Bloquea el período, valida los requisitos y las cuentas contables,
     * guarda los acumulados y registra estado e historial atómicamente.
     */
    @Transactional
    public CierreMensualContableResumenDTO cerrar(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        validarParametros(idAgencia, ano, mes);
        autorizar(PERMISO_ADMIN, idAgencia);

        var periodo = periodosRepository.bloquearPeriodo(
                idAgencia, ano, mes
        ).orElseThrow(() -> new IllegalStateException(
                "El período contable no existe."
        ));

        if (!ABIERTO.equals(periodo.estado())) {
            throw new IllegalStateException(
                    "El período mensual no está abierto."
            );
        }

        // Impide consolidar movimientos registrados en cuentas inválidas.
        List<String> cuentasInvalidas =
                cierreRepository.consultarMovimientosCuentasInvalidas(
                        idAgencia, ano, mes
                );

        if (!cuentasInvalidas.isEmpty()) {
            throw new IllegalStateException(
                    "No se permite cerrar el mes porque existen "
                            + "movimientos en cuentas inexistentes "
                            + "o no operables: "
                            + String.join(", ", cuentasInvalidas)
            );
        }

        // Verifica los días cerrados y las condiciones del período.
        CierreMensualContableResumenDTO resumen = construirResumen(
                idAgencia, ano, mes, periodo.estado()
        );

        if (!resumen.puedeCerrar()) {
            throw new IllegalStateException(
                    resumen.mensajeValidacion()
            );
        }

        // Impide registrar nuevamente un cierre mensual existente.
        if (cierreRepository.contarRegistrosCierre(
                idAgencia, ano, mes
        ) > 0) {
            throw new IllegalStateException(
                    "El período ya tiene registros de cierre mensual."
            );
        }

        // Calcula los acumulados definitivos por cuenta contable.
        List<CierreMensualContableDetalleCuentaDTO> cuentas =
                cierreRepository.consultarConsolidacion(
                        idAgencia, ano, mes
                );

        validarEquilibrio(cuentas);

        Integer idUsuario = sesion.idUsuario();

        // Guarda la consolidación dentro de la transacción de cierre.
        cierreRepository.guardarConsolidacion(
                idAgencia,
                ano,
                mes,
                idUsuario,
                cuentas
        );

        // Cambia el estado del período únicamente después de consolidar.
        boolean actualizado = periodosRepository.cambiarEstado(
                periodo.idPeriodoContable(),
                ABIERTO,
                CERRADO,
                idUsuario
        );

        if (!actualizado) {
            throw new IllegalStateException(
                    "No fue posible actualizar el período mensual."
            );
        }

        // Registra el usuario responsable y la operación de cierre.
        periodosRepository.registrarHistorial(
                periodo.idPeriodoContable(),
                ABIERTO,
                CERRADO,
                "CIERRE",
                "Cierre mensual con consolidación de saldos",
                idUsuario
        );

        return construirResumen(
                idAgencia, ano, mes, CERRADO
        );
    }

    /**
     * Reabre un cierre mensual de manera excepcional y auditable.
     * Valida permisos, ejercicio anual y secuencia; retira la consolidación
     * vigente sin alterar auxiliares ni cierres diarios.
     */
    @Transactional
    public CierreMensualContableResumenDTO reabrir(
            Integer idAgencia, int ano, int mes, String justificacion) {
        validarParametros(idAgencia, ano, mes);
        autorizar(PERMISO_REOPEN, idAgencia);
        String motivo = justificacion == null ? "" : justificacion.trim();
        if (motivo.isEmpty()) {
            throw new IllegalArgumentException("La observación de reapertura es obligatoria.");
        }
        var periodo = periodosRepository.bloquearPeriodo(idAgencia, ano, mes)
                .orElseThrow(() -> new IllegalStateException("El período contable no existe."));
        if (!CERRADO.equals(periodo.estado())) {
            throw new IllegalStateException("Solo se puede reabrir un período mensual cerrado.");
        }
        if (periodosRepository.existeMesPosteriorCerrado(idAgencia, ano, mes)) {
            throw new IllegalStateException(
                    "No se puede reabrir el mes porque existe un cierre mensual posterior.");
        }
        if (periodosRepository.existeCierreAnual(idAgencia, ano)) {
            throw new IllegalStateException(
                    "No se puede reabrir un mes de un ejercicio con cierre anual registrado.");
        }
        long cantidad = cierreRepository.contarRegistrosCierre(idAgencia, ano, mes);
        if (cantidad == 0) {
            throw new IllegalStateException(
                    "El período está cerrado, pero no tiene consolidación. Requiere revisión.");
        }
        int retirados = cierreRepository.retirarConsolidacion(idAgencia, ano, mes);
        if (retirados != cantidad) {
            throw new IllegalStateException("No se retiró completamente la consolidación anterior.");
        }
        Integer idUsuario = sesion.idUsuario();
        if (!periodosRepository.cambiarEstado(
                periodo.idPeriodoContable(), CERRADO, ABIERTO, idUsuario)) {
            throw new IllegalStateException("No fue posible reabrir el período mensual.");
        }
        periodosRepository.registrarHistorial(
                periodo.idPeriodoContable(), CERRADO, ABIERTO,
                "REAPERTURA", motivo, idUsuario);
        return construirResumen(idAgencia, ano, mes, ABIERTO);
    }

    /**
     * Construye el resumen financiero y las validaciones.
     * No bloquea cuentas por ausencia individual de registro previo;
     * exige la consolidación del período anterior como control general.
     * Los totales se calculan sobre cuentas operables para
     * evitar duplicar valores de cuentas superiores.
     */
    private CierreMensualContableResumenDTO construirResumen(
            Integer idAgencia,
            int ano,
            int mes,
            String estado
    ) {
        Agencia agencia = agencias.findById(idAgencia)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La agencia no existe."
                ));

        List<PeriodosDiariosRepository.DiaPendienteCierre> pendientes =
                diariosRepository.consultarDiasPendientesCierre(
                        idAgencia, ano, mes
                );

        List<CierreMensualContableDiaPendienteDTO> diasPendientes =
                pendientes.stream()
                        .map(dia -> new CierreMensualContableDiaPendienteDTO(
                                dia.fecha(),
                                dia.estado()
                        ))
                        .toList();

        List<CierreMensualContableDetalleCuentaDTO> cuentas =
                consultarCuentasSegunEstado(
                        idAgencia, ano, mes, estado
                );

        BigDecimal debitoAnterior = BigDecimal.ZERO;
        BigDecimal creditoAnterior = BigDecimal.ZERO;
        BigDecimal debitoMes = BigDecimal.ZERO;
        BigDecimal creditoMes = BigDecimal.ZERO;
        BigDecimal debitoAcumulado = BigDecimal.ZERO;
        BigDecimal creditoAcumulado = BigDecimal.ZERO;

        for (CierreMensualContableDetalleCuentaDTO cuenta : cuentas) {
            if (!Boolean.TRUE.equals(cuenta.cuentaOperable())) {
                continue;
            }

            debitoAnterior = debitoAnterior.add(
                    cuenta.debitoAnterior()
            );
            creditoAnterior = creditoAnterior.add(
                    cuenta.creditoAnterior()
            );
            debitoMes = debitoMes.add(cuenta.debitoMes());
            creditoMes = creditoMes.add(cuenta.creditoMes());
            debitoAcumulado = debitoAcumulado.add(
                    cuenta.debitoAcumulado()
            );
            creditoAcumulado = creditoAcumulado.add(
                    cuenta.creditoAcumulado()
            );
        }

        BigDecimal diferencia = debitoMes.subtract(creditoMes);

        LocalDate inicio = LocalDate.of(ano, mes, 1);
        int cantidadDias = inicio.lengthOfMonth();
        int cantidadPendientes = diasPendientes.size();
        // Solo cuenta controles diarios realmente cerrados; días sin actividad no suman.
        int cantidadCerrados = diariosRepository.contarDiasCerrados(idAgencia, ano, mes);

        List<String> observaciones = new ArrayList<>();

        // Valida que los movimientos pertenezcan a cuentas operables
        // del catálogo contable de la agencia.
        if (ABIERTO.equals(estado)) {
            List<String> cuentasInvalidas =
                    cierreRepository.consultarMovimientosCuentasInvalidas(
                            idAgencia, ano, mes
                    );

            if (!cuentasInvalidas.isEmpty()) {
                observaciones.add(
                        "Existen movimientos registrados en cuentas "
                                + "inexistentes o no operables: "
                                + String.join(", ", cuentasInvalidas)
                );
            }
        }

        if (!ABIERTO.equals(estado)) {
            observaciones.add(
                    CERRADO.equals(estado)
                            ? "El período ya está cerrado."
                            : "El período mensual no está creado."
            );
        }

        if (ABIERTO.equals(estado) && cantidadPendientes > 0) {
            observaciones.add(
                    "Existen " + cantidadPendientes
                            + " días pendientes de cierre."
            );
        }

        if ((ABIERTO.equals(estado) || NO_CREADO.equals(estado)) && mes == 1) {
            if (!CERRADO.equals(periodosRepository.consultarEstado(
                    idAgencia, ano - 1, 12).orElse(NO_CREADO))) {
                observaciones.add("Diciembre del año anterior no está cerrado.");
            }
            if (cierreRepository.contarRegistrosCierreAnual(
                    idAgencia, ano - 1) == 0) {
                observaciones.add("No existe consolidación anual del año anterior.");
            }
        } else if (ABIERTO.equals(estado) || NO_CREADO.equals(estado)) {
            boolean anteriorCerrado =
                    periodosRepository.mesAnteriorCerrado(
                            idAgencia, ano, mes
                    );

            if (!anteriorCerrado) {
                observaciones.add(
                        "No se puede cerrar " + nombreMes(mes) + " de " + ano
                                + " porque " + nombreMes(mes - 1) + " de " + ano
                                + " no está cerrado."
                );
            }

            if (anteriorCerrado
                    && cierreRepository.contarRegistrosCierre(
                    idAgencia, ano, mes - 1
            ) == 0) {
                observaciones.add(
                        "No existen saldos consolidados "
                                + "del mes anterior."
                );
            }
        }

        if (cuentas.isEmpty()) {
            observaciones.add(
                    "No existen cuentas contables para consolidar."
            );
        }

        if (ABIERTO.equals(estado)
                && diferencia.compareTo(BigDecimal.ZERO) != 0) {
            observaciones.add(
                    "Los movimientos del mes no están equilibrados."
            );
        }

        if (ABIERTO.equals(estado)
                && cierreRepository.contarRegistrosCierre(
                idAgencia, ano, mes
        ) > 0) {
            observaciones.add(
                    "Ya existen registros de consolidación "
                            + "para este período."
            );
        }

        boolean puedeCerrar = observaciones.isEmpty();

        String mensaje = puedeCerrar
                ? "El período cumple las condiciones para el cierre."
                : String.join(" ", observaciones);

        long movimientos = cierreRepository.contarMovimientos(
                idAgencia, ano, mes
        );

        return new CierreMensualContableResumenDTO(
                idAgencia,
                agencia.getNombreAgencia(),
                ano,
                mes,
                estado,
                puedeCerrar,
                mensaje,
                cantidadDias,
                cantidadCerrados,
                cantidadPendientes,
                diasPendientes,
                cuentas.size(),
                Math.toIntExact(movimientos),
                debitoAnterior,
                creditoAnterior,
                debitoMes,
                creditoMes,
                debitoAcumulado,
                creditoAcumulado,
                diferencia
        );
    }

    /** Nombre legible del mes para informar restricciones de cierre. */
    private String nombreMes(int mes) {
        return java.time.Month.of(mes).getDisplayName(
                java.time.format.TextStyle.FULL, new java.util.Locale("es", "CO"));
    }

    /**
     * Selecciona la fuente de saldos según el estado del período.
     * Los períodos cerrados utilizan los registros históricos;
     * los demás calculan una vista previa sin modificar datos.
     */
    private List<CierreMensualContableDetalleCuentaDTO> consultarCuentasSegunEstado(
            Integer idAgencia,
            int ano,
            int mes,
            String estado
    ) {
        if (CERRADO.equals(estado)) {
            if (cierreRepository.contarRegistrosCierre(
                    idAgencia, ano, mes
            ) == 0) {
                throw new IllegalStateException(
                        "El período está cerrado, pero no existen "
                                + "saldos consolidados del cierre mensual."
                );
            }

            return cierreRepository.consultarCierreHistorico(
                    idAgencia, ano, mes
            );
        }

        return cierreRepository.consultarConsolidacion(
                idAgencia, ano, mes
        );
    }

    /**
     * Verifica el equilibrio de los movimientos consolidados.
     * Solo suma cuentas operables para evitar duplicaciones.
     */
    private void validarEquilibrio(
            List<CierreMensualContableDetalleCuentaDTO> cuentas
    ) {
        BigDecimal debitos = BigDecimal.ZERO;
        BigDecimal creditos = BigDecimal.ZERO;

        for (CierreMensualContableDetalleCuentaDTO cuenta : cuentas) {
            if (!Boolean.TRUE.equals(cuenta.cuentaOperable())) {
                continue;
            }

            debitos = debitos.add(cuenta.debitoMes());
            creditos = creditos.add(cuenta.creditoMes());
        }

        if (debitos.compareTo(creditos) != 0) {
            throw new IllegalStateException(
                    "El cierre mensual presenta diferencias "
                            + "entre débitos y créditos."
            );
        }
    }

    /** Valida agencia, año y mes antes de procesar. */
    private void validarParametros(
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
     * Valida permiso funcional y alcance de agencia.
     * Conserva el criterio existente del Cierre Diario.
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

