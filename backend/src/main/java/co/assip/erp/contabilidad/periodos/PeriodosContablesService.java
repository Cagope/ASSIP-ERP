
package co.assip.erp.contabilidad.periodos;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import co.assip.erp.seguridad.usuariosroles.UsuarioRolService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Administra los períodos contables por agencia.
 *
 * Reutiliza la seguridad central del ERP y controla
 * las operaciones de apertura, cierre y contabilización.
 */
@Service
@RequiredArgsConstructor
public class PeriodosContablesService {

    private static final String ABIERTO = "ABIERTO";
    private static final String CERRADO = "CERRADO";
    private static final String NO_CREADO = "NO_CREADO";

    private static final String PERMISO_VIEW =
            "CONTABILIDAD_PERIODOS_VIEW";

    private static final String PERMISO_ADMIN =
            "CONTABILIDAD_PERIODOS_ADMIN";

    private static final String PERMISO_REOPEN =
            "CONTABILIDAD_PERIODOS_REOPEN";

    private static final int ID_ROL_CONTADOR = 15;

    private final PeriodosContablesRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolService usuarioRolService;
    private final AccessValidator accessValidator;

    /**
     * Consulta el estado de un período contable.
     * Verifica el permiso funcional y el alcance de agencia.
     */
    @Transactional(readOnly = true)
    public String consultarEstado(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        validarPeriodo(ano, mes);
        autorizar(PERMISO_VIEW, idAgencia);

        return repository
                .consultarEstado(idAgencia, ano, mes)
                .orElse(NO_CREADO);
    }

    /**
     * Garantiza que el período contable esté abierto.
     *
     * Si no existe, lo crea automáticamente con el usuario
     * que registra el primer movimiento contable.
     *
     * Bloquea el período durante la transacción y registra
     * el historial cuando ocurre una apertura automática.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void validarPeriodoAbierto(
            Integer idAgencia,
            LocalDate fechaContable
    ) {
        if (fechaContable == null) {
            throw new IllegalArgumentException(
                    "La fecha contable es obligatoria."
            );
        }

        int ano = fechaContable.getYear();
        int mes = fechaContable.getMonthValue();

        validarPeriodo(ano, mes);
        validarAlcanceAgencia(idAgencia);

        Integer idUsuario = usuarioSesionService.idUsuario();

        // 1. Crea el período si no existe.
        boolean periodoCreado = repository.crearPeriodoSiNoExiste(
                idAgencia,
                ano,
                mes,
                idUsuario
        );

        // 2. Bloquea el período durante la contabilización.
        var periodo = repository.bloquearPeriodo(
                idAgencia,
                ano,
                mes
        ).orElseThrow(() -> new IllegalStateException(
                "No fue posible obtener el período contable."
        ));

        // 3. Impide contabilizar en períodos cerrados.
        if (!ABIERTO.equals(periodo.estado())) {
            throw new IllegalStateException(
                    "El período contable está cerrado. "
                            + "No se permite contabilizar."
            );
        }

        // 4. Registra el historial solo si el período fue creado.
        // Registra la apertura del período creado por el primer movimiento.
        if (periodoCreado) {
            repository.registrarHistorial(
                    periodo.idPeriodoContable(),
                    null,
                    ABIERTO,
                    "APERTURA",
                    "Apertura automática por primera contabilización",
                    idUsuario
            );
        }
    }

    /**
     * Crea un período abierto por primera vez.
     * Exige permiso administrativo y registra auditoría.
     */
    @Transactional
    public Long abrirPeriodo(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        validarPeriodo(ano, mes);
        autorizar(PERMISO_ADMIN, idAgencia);

        if (repository.consultarEstado(idAgencia, ano, mes)
                .isPresent()) {
            throw new IllegalStateException(
                    "El período ya existe. "
                            + "No puede abrirse nuevamente."
            );
        }

        Integer idUsuario = usuarioSesionService.idUsuario();

        Long idPeriodo = repository.crearPeriodo(
                idAgencia,
                ano,
                mes,
                idUsuario
        );

        repository.registrarHistorial(
                idPeriodo,
                null,
                ABIERTO,
                "APERTURA",
                null,
                idUsuario
        );

        return idPeriodo;
    }

    /**
     * Cierra un período abierto de la agencia indicada.
     *
     * Bloquea la fila y registra el cambio de estado
     * y su historial en una misma transacción.
     */
    @Transactional
    public void cerrarPeriodo(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        validarPeriodo(ano, mes);
        autorizar(PERMISO_ADMIN, idAgencia);

        var periodo = repository.bloquearPeriodo(
                idAgencia,
                ano,
                mes
        ).orElseThrow(() -> new IllegalStateException(
                "El período contable no existe."
        ));

        if (!ABIERTO.equals(periodo.estado())) {
            throw new IllegalStateException(
                    "El período contable ya está cerrado."
            );
        }

        Integer idUsuario = usuarioSesionService.idUsuario();

        boolean actualizado = repository.cambiarEstado(
                periodo.idPeriodoContable(),
                ABIERTO,
                CERRADO,
                idUsuario
        );

        if (!actualizado) {
            throw new IllegalStateException(
                    "No fue posible cerrar el período contable."
            );
        }

        repository.registrarHistorial(
                periodo.idPeriodoContable(),
                ABIERTO,
                CERRADO,
                "CIERRE",
                null,
                idUsuario
        );
    }

    /**
     * Reserva la reapertura excepcional a usuarios
     * con permiso funcional y alcance autorizado.
     *
     * La ejecución permanece deshabilitada hasta
     * integrar el control de cierre anual definitivo.
     */
    @Transactional
    public void reabrirPeriodo(
            Integer idAgencia,
            int ano,
            int mes,
            String justificacion
    ) {
        validarPeriodo(ano, mes);
        autorizar(PERMISO_REOPEN, idAgencia);

        if (justificacion == null
                || justificacion.isBlank()) {
            throw new IllegalArgumentException(
                    "La justificación es obligatoria."
            );
        }

        throw new UnsupportedOperationException(
                "La reapertura no está habilitada hasta "
                        + "integrar la validación del "
                        + "cierre anual definitivo."
        );
    }

    /**
     * Valida un permiso mediante AccessValidator
     * y aplica el alcance de agencia de Contabilidad.
     */
    private void autorizar(
            String codigoPermiso,
            Integer idAgencia
    ) {
        Usuario usuario = obtenerUsuarioActual();

        accessValidator.validarAcceso(
                usuario,
                codigoPermiso
        );

        validarAlcanceAgencia(idAgencia);
    }

    /**
     * Obtiene el usuario autenticado desde el repositorio
     * existente, sin consultar roles desde el JWT.
     */
    private Usuario obtenerUsuarioActual() {
        Integer idUsuario = usuarioSesionService.idUsuario();

        return usuarioRepository.findById(idUsuario)
                .filter(usuario ->
                        Boolean.TRUE.equals(usuario.getActivo())
                )
                .orElseThrow(() -> new SecurityException(
                        "El usuario autenticado no está activo."
                ));
    }

    /**
     * Verifica el alcance de agencia.
     *
     * ADMIN conserva acceso global.
     * CONTADOR tiene alcance global solo en Contabilidad.
     * Los demás usuarios respetan sus agencias autorizadas.
     */
    private void validarAlcanceAgencia(
            Integer idAgencia
    ) {
        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException(
                    "La agencia es obligatoria."
            );
        }

        if (usuarioSesionService.tieneAccesoTotal()) {
            return;
        }

        Integer idUsuario = usuarioSesionService.idUsuario();

        boolean esContador = usuarioRolService
                .listarVigentes(idUsuario)
                .stream()
                .anyMatch(asignacion ->
                        asignacion.getRol() != null
                                && Boolean.TRUE.equals(
                                asignacion.getRol().getActivo()
                        )
                                && Integer.valueOf(ID_ROL_CONTADOR)
                                .equals(
                                        asignacion.getRol().getIdRol()
                                )
                );

        if (esContador) {
            return;
        }

        usuarioSesionService.validarAgencia(idAgencia);
    }

    /**
     * Valida el año y mes conforme a las restricciones
     * definidas en contabilidad.periodos_contables.
     */
    private void validarPeriodo(
            int ano,
            int mes
    ) {
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
}
