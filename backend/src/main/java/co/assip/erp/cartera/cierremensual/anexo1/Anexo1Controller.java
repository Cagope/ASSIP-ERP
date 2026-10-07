package co.assip.erp.cartera.cierremensual.anexo1;

import co.assip.erp.cartera.cierremensual.anexo1.dto.DetalleAnexo1DTO;
import co.assip.erp.cartera.cierremensual.anexo1.dto.ResumenAnexo1DTO;
import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cartera/anexo1")
@RequiredArgsConstructor
public class Anexo1Controller {

    private static final String PERMISO_VIEW =
            "CARTERA_CIERRE_VIEW";

    private static final String PERMISO_EXECUTE =
            "CARTERA_CIERRE_EXECUTE";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final Anexo1Service service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // CALCULAR EDAD CONTABLE
    // ALINEAMIENTO / LEY DE ARRASTRE
    //
    // POST
    // /cartera/anexo1/{idCierreCartera}/edad-contable
    // =========================================================

    @PostMapping("/{idCierreCartera}/edad-contable")
    public ResponseEntity<Integer> calcularEdadContable(
            @PathVariable Integer idCierreCartera
    ) {

        validarEjecucion();

        int cantidad =
                service.calcularEdadContable(
                        idCierreCartera
                );

        return ResponseEntity.ok(
                cantidad
        );
    }

    // =========================================================
    // PROCESAR ANEXO 1 COMPLETO
    //
    // POST
    // /cartera/anexo1/{idCierreCartera}/procesar
    // =========================================================

    @PostMapping("/{idCierreCartera}/procesar")
    public ResponseEntity<Anexo1Service.ResultadoAnexo1> procesarAnexo1(
            @PathVariable Integer idCierreCartera
    ) {

        validarEjecucion();

        return ResponseEntity.ok(
                service.procesarAnexo1(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // CERRAR ANEXO 1 EN FIRME
    //
    // POST
    // /cartera/anexo1/{idCierreCartera}/cerrar
    // =========================================================

    @PostMapping("/{idCierreCartera}/cerrar")
    public ResponseEntity<CierreMensualDTO> cerrarAnexo1(
            @PathVariable Integer idCierreCartera
    ) {

        validarEjecucion();

        return ResponseEntity.ok(
                service.cerrarAnexo1(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // CONSULTAR DETALLE ANEXO 1
    //
    // GET
    // /cartera/anexo1/{idCierreCartera}/detalle
    //
    // Consulta exclusivamente de lectura.
    // No ejecuta ni recalcula el Anexo 1.
    // =========================================================

    @GetMapping("/{idCierreCartera}/detalle")
    public ResponseEntity<List<DetalleAnexo1DTO>> obtenerDetalleAnexo1(
            @PathVariable Integer idCierreCartera
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.obtenerDetalleAnexo1(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // CONSULTAR RESUMEN ANEXO 1
    //
    // GET
    // /cartera/anexo1/{idCierreCartera}/resumen
    //
    // Consulta exclusivamente de lectura.
    // No ejecuta ni recalcula el Anexo 1.
    //
    // Presenta:
    //
    // - población procesada
    // - saldos
    // - distribución por edad contable
    // - coberturas aplicadas
    // - deterioro de capital
    // - deterioro de intereses
    // - deterioro total
    // =========================================================

    @GetMapping("/{idCierreCartera}/resumen")
    public ResponseEntity<ResumenAnexo1DTO> obtenerResumenAnexo1(
            @PathVariable Integer idCierreCartera
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.obtenerResumenAnexo1(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarConsulta() {

        Usuario usuarioActual = usuarioActual();

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO_VIEW
        );
    }

    private void validarEjecucion() {

        Usuario usuarioActual = usuarioActual();

        accessValidator.validarAcceso(
                usuarioActual,
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