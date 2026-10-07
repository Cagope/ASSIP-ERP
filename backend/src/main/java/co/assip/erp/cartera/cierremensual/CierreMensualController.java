package co.assip.erp.cartera.cierremensual;

import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cartera/cierre-mensual")
@RequiredArgsConstructor
public class CierreMensualController {

    private static final String PERMISO_VIEW =
            "CARTERA_CIERRE_VIEW";

    private static final String PERMISO_EXECUTE =
            "CARTERA_CIERRE_EXECUTE";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final CierreMensualService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // LISTAR CIERRES
    // =========================================================

    @GetMapping
    public ResponseEntity<List<CierreMensualDTO>> listar() {

        validarConsulta();

        return ResponseEntity.ok(
                service.listar()
        );
    }

    // =========================================================
    // CONSULTAR CIERRE POR ID
    // =========================================================

    @GetMapping("/{idCierreCartera}")
    public ResponseEntity<CierreMensualDTO> buscarPorId(
            @PathVariable Integer idCierreCartera
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.buscarPorId(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // EJECUTAR CIERRE MENSUAL
    //
    // POST
    // /api/v1/cartera/cierre-mensual/ejecutar
    //      ?fechaCorte=2026-08-31
    //
    // Este proceso:
    //
    // 1. crea / recupera la cabecera
    // 2. genera la fotografía mensual
    // 3. genera el precierre de Hoja de Vida
    // 4. crea la base de cálculos comunes
    // 5. valida cantidades
    // 6. calcula saldo maestro de la foto
    // 7. actualiza la cabecera
    //
    // Al terminar:
    //
    // estado_fotografia = E
    //
    // NO deja la fotografía en firme.
    // NO cierra el cierre mensual general.
    // =========================================================

    @PostMapping("/ejecutar")
    public ResponseEntity<CierreMensualDTO> ejecutar(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte
    ) {

        validarEjecucion();

        return ResponseEntity.ok(
                service.ejecutar(
                        fechaCorte
                )
        );
    }

    // =========================================================
    // REGENERAR FOTOGRAFÍA
    //
    // POST
    // /api/v1/cartera/cierre-mensual/{idCierreCartera}/regenerar
    //
    // REGLAS:
    //
    // - permitida mientras estado_fotografia <> C
    // - conserva la cabecera
    // - conserva id_cierre_cartera
    // - elimina resultados base actuales
    // - elimina fotografía actual
    // - genera nuevamente la fotografía
    // - genera nuevamente resultados base
    // - recalcula cantidad de créditos
    // - recalcula saldo maestro
    //
    // Al terminar:
    //
    // estado_fotografia = E
    //
    // Si el proceso falla, la transacción del Service
    // revierte la regeneración.
    // =========================================================

    @PostMapping("/{idCierreCartera}/regenerar")
    public ResponseEntity<CierreMensualDTO> regenerar(
            @PathVariable Integer idCierreCartera
    ) {

        validarEjecucion();

        return ResponseEntity.ok(
                service.regenerar(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // CERRAR FOTOGRAFÍA EN FIRME
    //
    // POST
    // /api/v1/cartera/cierre-mensual/{idCierreCartera}/cerrar-fotografia
    //
    // REGLAS:
    //
    // - estado_fotografia debe ser E
    // - la fotografía debe existir
    // - la base de resultados debe existir
    // - créditos foto con saldo = resultados base
    //
    // ACTUALIZA:
    //
    // estado_fotografia = C
    // fecha_fotografia_firme = CURRENT_TIMESTAMP
    //
    // NO ACTUALIZA:
    //
    // estado_cierre
    // fecha_finalizacion
    //
    // Después de quedar en firme:
    //
    // - ya no se puede regenerar la fotografía
    // - se habilita el proceso de Cálculos
    // =========================================================

    @PostMapping("/{idCierreCartera}/cerrar-fotografia")
    public ResponseEntity<CierreMensualDTO> cerrarFotografia(
            @PathVariable Integer idCierreCartera
    ) {

        validarEjecucion();

        return ResponseEntity.ok(
                service.cerrarFotografia(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // CONSULTAR SI EL CIERRE TIENE FOTOGRAFÍA
    // =========================================================

    @GetMapping("/{idCierreCartera}/foto/existe")
    public ResponseEntity<Boolean> existeFoto(
            @PathVariable Integer idCierreCartera
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.existeFoto(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // PREPARAR BASE DE CÁLCULOS DE CIERRE HISTÓRICO
    //
    // POST
    // /api/v1/cartera/cierre-mensual/{idCierreCartera}/preparar-base-historica
    //
    // REGLAS:
    //
    // - la cabecera ya debe existir
    // - la fotografía ya debe existir
    // - NO modifica la fotografía
    // - NO modifica la cabecera
    // - crea únicamente la base de cálculos comunes
    //   en cierres_cartera_resultados
    // - se usa para cierres históricos migrados
    // =========================================================

    @PostMapping("/{idCierreCartera}/preparar-base-historica")
    public ResponseEntity<Integer> prepararBaseHistorica(
            @PathVariable Integer idCierreCartera
    ) {

        validarEjecucion();

        return ResponseEntity.ok(
                service.prepararBaseCalculosHistorico(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarConsulta() {

        accessValidator.validarAcceso(
                usuarioActual(),
                PERMISO_VIEW
        );
    }

    private void validarEjecucion() {

        accessValidator.validarAcceso(
                usuarioActual(),
                PERMISO_EXECUTE
        );
    }

    private Usuario usuarioActual() {

        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        return usuarioActual;
    }
}