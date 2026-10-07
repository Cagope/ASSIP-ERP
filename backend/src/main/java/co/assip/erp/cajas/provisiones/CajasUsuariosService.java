package co.assip.erp.cajas.provisiones;

import co.assip.erp.cajas.provisiones.dto.CajaDisponibleDTO;
import co.assip.erp.cajas.provisiones.dto.CajaUsuarioDTO;
import co.assip.erp.cajas.provisiones.dto.CajaUsuarioVincularDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CajasUsuariosService {

    private final CajasUsuariosRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    // =========================================================
    // CAJAS DISPONIBLES
    // =========================================================

    /**
     * Lista las cajas que el usuario autenticado puede seleccionar
     * para iniciar su jornada.
     *
     * Solo se consultan las agencias autorizadas para el usuario.
     * La disponibilidad de la caja depende exclusivamente de la
     * vinculación diaria, no de provisiones.
     */
    public List<CajaDisponibleDTO> listarDisponibles() {

        Integer idUsuario = usuarioSesionService.idUsuario();

        /*
         * Si el usuario ya tuvo una caja durante la jornada actual,
         * no puede iniciar una segunda jornada.
         */
        if (repository.existeJornadaUsuario(idUsuario)) {
            return List.of();
        }

        List<Integer> agencias = usuarioSesionService.agencias();

        if (agencias == null || agencias.isEmpty()) {
            return List.of();
        }

        List<CajaDisponibleDTO> resultado = new ArrayList<>();

        for (Integer idAgencia : agencias) {
            resultado.addAll(
                    repository.listarDisponibles(idAgencia)
            );
        }

        return resultado;
    }

    // =========================================================
// CAJAS DISPONIBLES PARA CAMBIO
// =========================================================

    /**
     * Lista las cajas disponibles para corregir la vinculación
     * actual del usuario.
     *
     * A diferencia de listarDisponibles(), este método requiere
     * que el usuario ya tenga una vinculación activa.
     */
    public List<CajaDisponibleDTO> listarDisponiblesCambio() {

        Integer idUsuario = usuarioSesionService.idUsuario();

        /*
         * Para cambiar de caja debe existir una vinculación activa.
         */
        CajaUsuarioDTO vinculacionActual = repository
                .obtenerActivaPorUsuario(idUsuario)
                .orElseThrow(() ->
                        new RuntimeException(
                                "El usuario no tiene una caja activa para cambiar."
                        )
                );

        /*
         * Si la caja actual ya tiene movimientos, no existe
         * posibilidad de corregir la vinculación.
         */
        if (repository.tieneMovimientosHoy(
                vinculacionActual.getIdCaja()
        )) {
            return List.of();
        }

        List<Integer> agencias = usuarioSesionService.agencias();

        if (agencias == null || agencias.isEmpty()) {
            return List.of();
        }

        List<CajaDisponibleDTO> resultado = new ArrayList<>();

        for (Integer idAgencia : agencias) {
            resultado.addAll(
                    repository.listarDisponibles(idAgencia)
            );
        }

        return resultado;
    }

    // =========================================================
    // VINCULACIÓN ACTIVA DEL USUARIO
    // =========================================================

    /**
     * Obtiene la caja que el usuario autenticado está operando
     * durante la jornada actual.
     *
     * Este método debe utilizarse cuando una operación requiere
     * obligatoriamente que el usuario tenga una caja vinculada.
     */
    public CajaUsuarioDTO obtenerActivaUsuario() {

        Integer idUsuario = usuarioSesionService.idUsuario();

        return repository.obtenerActivaPorUsuario(idUsuario)
                .orElseThrow(() ->
                        new RuntimeException(
                                "El usuario no tiene una caja vinculada para la jornada actual."
                        )
                );
    }

    /**
     * Consulta la caja activa del usuario sin generar excepción
     * cuando todavía no existe una vinculación.
     *
     * Se utiliza principalmente para consultar el estado desde
     * la pantalla de vinculación.
     */
    public CajaUsuarioDTO obtenerActivaUsuarioSiExiste() {

        Integer idUsuario = usuarioSesionService.idUsuario();

        return repository.obtenerActivaPorUsuario(idUsuario)
                .orElse(null);
    }

    // =========================================================
    // CONSULTA POR CAJA
    // =========================================================

    /**
     * Obtiene el usuario que actualmente está vinculado a una caja.
     */
    public CajaUsuarioDTO obtenerActivaPorCaja(Long idCaja) {

        validarIdCaja(idCaja);

        CajaDisponibleDTO caja = repository.obtenerCajaActiva(idCaja)
                .orElseThrow(() ->
                        new RuntimeException(
                                "La caja no existe o se encuentra inactiva."
                        )
                );

        /*
         * El usuario solamente puede consultar cajas de agencias
         * a las cuales tiene acceso.
         */
        usuarioSesionService.validarAgencia(
                caja.getIdAgencia()
        );

        return repository.obtenerActivaPorCaja(idCaja)
                .orElseThrow(() ->
                        new RuntimeException(
                                "La caja no tiene un usuario vinculado para la jornada actual."
                        )
                );
    }

    // =========================================================
    // VINCULAR CAJA
    // =========================================================

    @Transactional
    public CajaUsuarioDTO vincular(CajaUsuarioVincularDTO dto) {

        if (dto == null) {
            throw new RuntimeException(
                    "Debe indicar la caja que desea vincular."
            );
        }

        validarIdCaja(dto.getIdCaja());

        Integer idUsuario = usuarioSesionService.idUsuario();

        // -----------------------------------------------------
        // 1. La caja debe existir y estar activa
        // -----------------------------------------------------

        CajaDisponibleDTO caja = repository
                .obtenerCajaActiva(dto.getIdCaja())
                .orElseThrow(() ->
                        new RuntimeException(
                                "La caja seleccionada no existe o se encuentra inactiva."
                        )
                );

        // -----------------------------------------------------
        // 2. El usuario debe tener acceso a la agencia
        // -----------------------------------------------------

        usuarioSesionService.validarAgencia(
                caja.getIdAgencia()
        );

        // -----------------------------------------------------
        // 3. El usuario no puede tener otra jornada hoy
        // -----------------------------------------------------

        if (repository.existeJornadaUsuario(idUsuario)) {

            CajaUsuarioDTO activa = repository
                    .obtenerActivaPorUsuario(idUsuario)
                    .orElse(null);

            if (activa != null) {
                throw new RuntimeException(
                        "El usuario ya se encuentra vinculado a la caja "
                                + activa.getCodigoCaja()
                                + " para la jornada actual."
                );
            }

            throw new RuntimeException(
                    "El usuario ya tuvo una caja vinculada durante la jornada actual."
            );
        }

        // -----------------------------------------------------
        // 4. La caja no puede haber tenido otra jornada hoy
        // -----------------------------------------------------

        if (repository.existeJornadaCaja(dto.getIdCaja())) {

            CajaUsuarioDTO activa = repository
                    .obtenerActivaPorCaja(dto.getIdCaja())
                    .orElse(null);

            if (activa != null) {

                String usuarioCaja =
                        activa.getUsername() != null
                                ? activa.getUsername()
                                : String.valueOf(activa.getIdUsuario());

                throw new RuntimeException(
                        "La caja seleccionada ya se encuentra vinculada al usuario "
                                + usuarioCaja
                                + "."
                );
            }

            throw new RuntimeException(
                    "La caja seleccionada ya tuvo una vinculación durante la jornada actual."
            );
        }

        // -----------------------------------------------------
        // 5. Crear vinculación
        // -----------------------------------------------------

        Long idCajaUsuario = repository.crear(
                dto.getIdCaja(),
                idUsuario
        );

        // -----------------------------------------------------
        // 6. Retornar la vinculación creada
        // -----------------------------------------------------

        return repository.obtenerPorId(idCajaUsuario)
                .orElseThrow(() ->
                        new RuntimeException(
                                "La vinculación fue creada, pero no fue posible consultarla."
                        )
                );
    }

    // =========================================================
    // CAMBIAR CAJA VINCULADA
    // =========================================================

    /**
     * Corrige la caja seleccionada por el usuario durante la
     * jornada actual.
     *
     * El cambio solamente se permite mientras la caja originalmente
     * seleccionada no tenga movimientos registrados en la jornada.
     *
     * No se crea una nueva jornada. Se conserva el mismo
     * id_caja_usuario y únicamente se modifica la caja vinculada.
     */
    @Transactional
    public CajaUsuarioDTO cambiarCaja(CajaUsuarioVincularDTO dto) {

        if (dto == null) {
            throw new RuntimeException(
                    "Debe indicar la nueva caja."
            );
        }

        validarIdCaja(dto.getIdCaja());

        Integer idUsuario = usuarioSesionService.idUsuario();

        // -----------------------------------------------------
        // 1. El usuario debe tener una vinculación activa
        // -----------------------------------------------------

        CajaUsuarioDTO vinculacionActual = repository
                .obtenerActivaPorUsuario(idUsuario)
                .orElseThrow(() ->
                        new RuntimeException(
                                "El usuario no tiene una caja activa para cambiar."
                        )
                );

        // -----------------------------------------------------
        // 2. La nueva caja debe ser diferente
        // -----------------------------------------------------

        if (dto.getIdCaja().equals(vinculacionActual.getIdCaja())) {
            throw new RuntimeException(
                    "La caja seleccionada corresponde a la caja actualmente vinculada."
            );
        }

        // -----------------------------------------------------
        // 3. La caja actual no puede tener movimientos
        // -----------------------------------------------------

        if (repository.tieneMovimientosHoy(
                vinculacionActual.getIdCaja()
        )) {
            throw new RuntimeException(
                    "No es posible cambiar la caja porque ya tiene movimientos registrados durante la jornada actual."
            );
        }

        // -----------------------------------------------------
        // 4. La nueva caja debe existir y estar activa
        // -----------------------------------------------------

        CajaDisponibleDTO cajaNueva = repository
                .obtenerCajaActiva(dto.getIdCaja())
                .orElseThrow(() ->
                        new RuntimeException(
                                "La nueva caja seleccionada no existe o se encuentra inactiva."
                        )
                );

        // -----------------------------------------------------
        // 5. El usuario debe tener acceso a la agencia
        // -----------------------------------------------------

        usuarioSesionService.validarAgencia(
                cajaNueva.getIdAgencia()
        );

        // -----------------------------------------------------
        // 6. La nueva caja no puede haber tenido jornada hoy
        // -----------------------------------------------------

        if (repository.existeJornadaCaja(dto.getIdCaja())) {

            CajaUsuarioDTO activa = repository
                    .obtenerActivaPorCaja(dto.getIdCaja())
                    .orElse(null);

            if (activa != null) {

                String usuarioCaja =
                        activa.getUsername() != null
                                ? activa.getUsername()
                                : String.valueOf(activa.getIdUsuario());

                throw new RuntimeException(
                        "La caja seleccionada ya se encuentra vinculada al usuario "
                                + usuarioCaja
                                + "."
                );
            }

            throw new RuntimeException(
                    "La caja seleccionada ya tuvo una vinculación durante la jornada actual."
            );
        }

        // -----------------------------------------------------
        // 7. Cambiar la caja de la vinculación actual
        // -----------------------------------------------------

        int actualizados = repository.cambiarCaja(
                vinculacionActual.getIdCajaUsuario(),
                dto.getIdCaja(),
                idUsuario
        );

        if (actualizados != 1) {
            throw new RuntimeException(
                    "No fue posible cambiar la caja vinculada."
            );
        }

        // -----------------------------------------------------
        // 8. Retornar la misma vinculación con la nueva caja
        // -----------------------------------------------------

        return repository
                .obtenerPorId(vinculacionActual.getIdCajaUsuario())
                .orElseThrow(() ->
                        new RuntimeException(
                                "La caja fue cambiada, pero no fue posible consultar la vinculación."
                        )
                );
    }

    // =========================================================
    // VALIDAR QUE UNA OPERACIÓN PERTENEZCA A LA CAJA DEL USUARIO
    // =========================================================

    /**
     * Valida que la caja destino de una operación corresponda
     * a la caja que está operando el usuario autenticado.
     *
     * Este método podrá ser utilizado posteriormente por CDAT,
     * captura de depósitos, recaudos y demás procesos que sean
     * terminados físicamente en caja.
     */
    public CajaUsuarioDTO validarCajaUsuario(Long idCaja) {

        validarIdCaja(idCaja);

        CajaUsuarioDTO vinculacion = obtenerActivaUsuario();

        if (!idCaja.equals(vinculacion.getIdCaja())) {
            throw new RuntimeException(
                    "La operación está direccionada a una caja diferente "
                            + "de la caja vinculada al usuario."
            );
        }

        return vinculacion;
    }

    // =========================================================
    // FINALIZACIÓN POR CIERRE DEFINITIVO
    // =========================================================

    /**
     * Finaliza la vinculación del usuario autenticado.
     *
     * IMPORTANTE:
     * Este método no debe exponerse como una acción normal de
     * "desvincular caja". Su uso corresponde al cierre definitivo
     * de la jornada.
     */
    @Transactional
    public void finalizarPorCierreDefinitivo() {

        Integer idUsuario = usuarioSesionService.idUsuario();

        CajaUsuarioDTO vinculacion = repository
                .obtenerActivaPorUsuario(idUsuario)
                .orElseThrow(() ->
                        new RuntimeException(
                                "El usuario no tiene una caja vinculada activa."
                        )
                );

        int actualizados = repository.finalizar(
                vinculacion.getIdCajaUsuario(),
                idUsuario,
                "CIERRE DEFINITIVO DE CAJA"
        );

        if (actualizados != 1) {
            throw new RuntimeException(
                    "No fue posible finalizar la vinculación de la caja."
            );
        }
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarIdCaja(Long idCaja) {

        if (idCaja == null) {
            throw new RuntimeException(
                    "Debe indicar la caja."
            );
        }

        if (idCaja <= 0) {
            throw new RuntimeException(
                    "La caja indicada no es válida."
            );
        }
    }
}