package co.assip.erp.cajas.provisiones;

import co.assip.erp.cajas.provisiones.dto.CajaDisponibleDTO;
import co.assip.erp.cajas.provisiones.dto.CajaUsuarioDTO;
import co.assip.erp.cajas.provisiones.dto.CajaUsuarioVincularDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cajas/vinculacion")
@RequiredArgsConstructor
public class CajasUsuariosController {

    private final CajasUsuariosService service;

    // =========================================================
    // CAJAS DISPONIBLES
    // =========================================================

    /**
     * Lista las cajas disponibles para el usuario autenticado.
     *
     * Las agencias autorizadas se obtienen de la sesión.
     * No se recibe fecha: la jornada corresponde siempre
     * a la fecha del servidor.
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<CajaDisponibleDTO>> listarDisponibles() {

        return ResponseEntity.ok(
                service.listarDisponibles()
        );
    }

    // =========================================================
    // CAJAS DISPONIBLES PARA CAMBIO
    // =========================================================

    /**
     * Lista las cajas que pueden utilizarse para corregir
     * la vinculación actual del usuario.
     */
    @GetMapping("/disponibles-cambio")
    public ResponseEntity<List<CajaDisponibleDTO>> listarDisponiblesCambio() {

        return ResponseEntity.ok(
                service.listarDisponiblesCambio()
        );
    }

    // =========================================================
    // VINCULACIÓN ACTIVA DEL USUARIO
    // =========================================================

    /**
     * Consulta la caja activa del usuario autenticado.
     *
     * No tener una caja vinculada es un estado normal.
     * En ese caso se retorna 200 con cuerpo vacío.
     */
    @GetMapping("/activa")
    public ResponseEntity<CajaUsuarioDTO> obtenerActivaUsuario() {

        return ResponseEntity.ok(
                service.obtenerActivaUsuarioSiExiste()
        );
    }

    // =========================================================
    // CONSULTAR VINCULACIÓN DE UNA CAJA
    // =========================================================

    /**
     * Consulta quién está operando actualmente una caja.
     */
    @GetMapping("/caja/{idCaja}")
    public ResponseEntity<CajaUsuarioDTO> obtenerActivaPorCaja(
            @PathVariable Long idCaja
    ) {

        return ResponseEntity.ok(
                service.obtenerActivaPorCaja(idCaja)
        );
    }

    // =========================================================
    // VINCULAR
    // =========================================================

    /**
     * Vincula el usuario autenticado a la caja seleccionada
     * para la jornada actual.
     *
     * El usuario, la fecha y la agencia no son recibidos
     * desde el frontend.
     */
    @PostMapping
    public ResponseEntity<CajaUsuarioDTO> vincular(
            @RequestBody CajaUsuarioVincularDTO dto
    ) {

        return ResponseEntity.ok(
                service.vincular(dto)
        );
    }

    // =========================================================
    // CAMBIAR CAJA
    // =========================================================

    /**
     * Corrige la caja vinculada al usuario autenticado.
     *
     * El cambio solamente será permitido por el servicio
     * mientras la caja actual no tenga movimientos durante
     * la jornada.
     *
     * Se conserva la misma vinculación y la misma jornada.
     */
    @PutMapping("/cambiar")
    public ResponseEntity<CajaUsuarioDTO> cambiarCaja(
            @RequestBody CajaUsuarioVincularDTO dto
    ) {

        return ResponseEntity.ok(
                service.cambiarCaja(dto)
        );
    }
}