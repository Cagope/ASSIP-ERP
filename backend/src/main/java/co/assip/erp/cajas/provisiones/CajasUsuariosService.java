package co.assip.erp.cajas.provisiones;

import co.assip.erp.cajas.provisiones.dto.CajaDisponibleDTO;
import co.assip.erp.cajas.provisiones.dto.CajaUsuarioDTO;
import co.assip.erp.cajas.provisiones.dto.CajaUsuarioVincularDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuariosroles.UsuarioRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CajasUsuariosService {

    private static final Integer ROL_CAJERO = 13;
    private static final Integer ROL_TESORERO_GENERAL = 17;
    private static final Integer ROL_TESORERO_AGENCIA = 27;

    private static final String CAJA_OPERATIVA = "OPERATIVA";
    private static final String CAJA_CONTROL = "CONTROL";

    private final CajasUsuariosRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRolService usuarioRolService;

    // =========================================================
    // CAJAS DISPONIBLES
    // =========================================================

    /**
     * Lista las cajas disponibles de las agencias autorizadas,
     * filtradas por el tipo de caja que puede operar el usuario.
     */
    public List<CajaDisponibleDTO> listarDisponibles() {

        Integer idUsuario = usuarioSesionService.idUsuario();

        if (repository.existeJornadaUsuario(idUsuario)) {
            return List.of();
        }

        return consultarCajasDisponibles(idUsuario);
    }

    // =========================================================
    // CAJAS DISPONIBLES PARA CAMBIO
    // =========================================================

    /**
     * Lista las cajas a las cuales puede cambiar el usuario,
     * siempre que su caja actual no tenga movimientos.
     */
    public List<CajaDisponibleDTO> listarDisponiblesCambio() {

        Integer idUsuario = usuarioSesionService.idUsuario();

        CajaUsuarioDTO vinculacionActual = repository
                .obtenerActivaPorUsuario(idUsuario)
                .orElseThrow(() ->
                        new RuntimeException(
                                "El usuario no tiene una caja activa para cambiar."
                        )
                );

        if (repository.tieneMovimientosHoy(
                vinculacionActual.getIdCaja()
        )) {
            return List.of();
        }

        return consultarCajasDisponibles(idUsuario);
    }

    // =========================================================
    // VINCULACIÓN ACTIVA DEL USUARIO
    // =========================================================

    public CajaUsuarioDTO obtenerActivaUsuario() {

        Integer idUsuario = usuarioSesionService.idUsuario();

        return repository.obtenerActivaPorUsuario(idUsuario)
                .orElseThrow(() ->
                        new RuntimeException(
                                "El usuario no tiene una caja vinculada para la jornada actual."
                        )
                );
    }

    public CajaUsuarioDTO obtenerActivaUsuarioSiExiste() {

        Integer idUsuario = usuarioSesionService.idUsuario();

        return repository.obtenerActivaPorUsuario(idUsuario)
                .orElse(null);
    }

    // =========================================================
    // CAJA TRABAJADA POR FECHA
    // =========================================================

    public CajaUsuarioDTO obtenerUsuarioPorFechaSiExiste(
            LocalDate fechaJornada
    ) {

        if (fechaJornada == null) {
            throw new RuntimeException(
                    "Debe indicar la fecha de la jornada."
            );
        }

        Integer idUsuario = usuarioSesionService.idUsuario();

        return repository
                .obtenerPorUsuarioYFecha(
                        idUsuario,
                        fechaJornada
                )
                .orElse(null);
    }

    // =========================================================
    // CONSULTA POR CAJA
    // =========================================================

    public CajaUsuarioDTO obtenerActivaPorCaja(Long idCaja) {

        validarIdCaja(idCaja);

        CajaDisponibleDTO caja = repository
                .obtenerCajaActiva(idCaja)
                .orElseThrow(() ->
                        new RuntimeException(
                                "La caja no existe o se encuentra inactiva."
                        )
                );

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

        // 1. Consultar la caja real.

        CajaDisponibleDTO caja = repository
                .obtenerCajaActiva(dto.getIdCaja())
                .orElseThrow(() ->
                        new RuntimeException(
                                "La caja seleccionada no existe o se encuentra inactiva."
                        )
                );

        // 2. Validar agencia y autorización por tipo de caja.

        usuarioSesionService.validarAgencia(
                caja.getIdAgencia()
        );

        validarTipoCajaAutorizado(
                idUsuario,
                caja.getTipoCaja()
        );

        // 3. Impedir una segunda jornada del usuario.

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

        // 4. Impedir una segunda jornada de la caja.

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

        // 5. Crear vinculación.

        Long idCajaUsuario = repository.crear(
                dto.getIdCaja(),
                idUsuario
        );

        // 6. Consultar vinculación creada.

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

    @Transactional
    public CajaUsuarioDTO cambiarCaja(CajaUsuarioVincularDTO dto) {

        if (dto == null) {
            throw new RuntimeException(
                    "Debe indicar la nueva caja."
            );
        }

        validarIdCaja(dto.getIdCaja());

        Integer idUsuario = usuarioSesionService.idUsuario();

        // 1. Consultar vinculación actual.

        CajaUsuarioDTO vinculacionActual = repository
                .obtenerActivaPorUsuario(idUsuario)
                .orElseThrow(() ->
                        new RuntimeException(
                                "El usuario no tiene una caja activa para cambiar."
                        )
                );

        // 2. Validar que sea otra caja.

        if (dto.getIdCaja().equals(
                vinculacionActual.getIdCaja()
        )) {
            throw new RuntimeException(
                    "La caja seleccionada corresponde a la caja actualmente vinculada."
            );
        }

        // 3. Impedir cambio cuando ya existen movimientos.

        if (repository.tieneMovimientosHoy(
                vinculacionActual.getIdCaja()
        )) {
            throw new RuntimeException(
                    "No es posible cambiar la caja porque ya tiene movimientos registrados durante la jornada actual."
            );
        }

        // 4. Consultar la nueva caja.

        CajaDisponibleDTO cajaNueva = repository
                .obtenerCajaActiva(dto.getIdCaja())
                .orElseThrow(() ->
                        new RuntimeException(
                                "La nueva caja seleccionada no existe o se encuentra inactiva."
                        )
                );

        // 5. Validar agencia y tipo de caja.

        usuarioSesionService.validarAgencia(
                cajaNueva.getIdAgencia()
        );

        validarTipoCajaAutorizado(
                idUsuario,
                cajaNueva.getTipoCaja()
        );

        // 6. Verificar que la nueva caja no haya tenido jornada.

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

        // 7. Cambiar la vinculación.

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

        // 8. Consultar la vinculación actualizada.

        return repository
                .obtenerPorId(vinculacionActual.getIdCajaUsuario())
                .orElseThrow(() ->
                        new RuntimeException(
                                "La caja fue cambiada, pero no fue posible consultar la vinculación."
                        )
                );
    }

    // =========================================================
    // VALIDAR CAJA DEL USUARIO
    // =========================================================

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
    // CONSULTA CENTRALIZADA DE CAJAS DISPONIBLES
    // =========================================================

    /**
     * Aplica las mismas reglas de autorización para
     * la vinculación inicial y el cambio de caja.
     */
    private List<CajaDisponibleDTO> consultarCajasDisponibles(
            Integer idUsuario
    ) {

        List<Integer> agencias = usuarioSesionService.agencias();

        if (agencias == null || agencias.isEmpty()) {
            return List.of();
        }

        List<CajaDisponibleDTO> resultado = new ArrayList<>();

        for (Integer idAgencia : agencias) {

            if (idAgencia == null) {
                continue;
            }

            if (!usuarioSesionService.perteneceA(idAgencia)) {
                continue;
            }

            for (CajaDisponibleDTO caja :
                    repository.listarDisponibles(idAgencia)) {

                if (puedeOperarTipoCaja(
                        idUsuario,
                        caja.getTipoCaja()
                )) {
                    resultado.add(caja);
                }
            }
        }

        return resultado;
    }

    // =========================================================
    // AUTORIZACIÓN POR TIPO DE CAJA
    // =========================================================

    private void validarTipoCajaAutorizado(
            Integer idUsuario,
            String tipoCaja
    ) {

        if (!puedeOperarTipoCaja(idUsuario, tipoCaja)) {
            throw new SecurityException(
                    "El usuario no está autorizado para operar una caja de tipo "
                            + tipoCaja
                            + "."
            );
        }
    }

    private boolean puedeOperarTipoCaja(
            Integer idUsuario,
            String tipoCaja
    ) {

        if (tipoCaja == null || tipoCaja.isBlank()) {
            return false;
        }

        if (!CAJA_OPERATIVA.equalsIgnoreCase(tipoCaja)
                && !CAJA_CONTROL.equalsIgnoreCase(tipoCaja)) {
            return false;
        }

        // ADMIN conserva acceso administrativo total.

        if (usuarioSesionService.tieneAccesoTotal()) {
            return true;
        }

        // Las cajas operativas corresponden a CAJERO.

        if (CAJA_OPERATIVA.equalsIgnoreCase(tipoCaja)) {
            return usuarioRolService.tieneRolVigente(
                    idUsuario,
                    ROL_CAJERO
            );
        }

        // CONTROL corresponde a los tesoreros.

        return usuarioRolService.tieneRolVigente(
                idUsuario,
                ROL_TESORERO_AGENCIA
        ) || usuarioRolService.tieneRolVigente(
                idUsuario,
                ROL_TESORERO_GENERAL
        );
    }

    // =========================================================
    // VALIDACIONES GENERALES
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