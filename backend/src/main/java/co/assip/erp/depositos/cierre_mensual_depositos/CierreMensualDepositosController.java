package co.assip.erp.depositos.cierre_mensual_depositos;

import co.assip.erp.depositos.cierre_mensual_depositos.dto.CierreMensualDepositosApplyResponseDTO;
import co.assip.erp.depositos.cierre_mensual_depositos.dto.CierreMensualDepositosPreviewDTO;
import co.assip.erp.depositos.cierre_mensual_depositos.dto.CierreMensualDepositosRequestDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos/cierre-mensual-depositos")
@RequiredArgsConstructor
public class CierreMensualDepositosController {

    private static final String PERMISO_EXECUTE =
            "DEPOSITOS_CIERRE_MENSUAL_EXECUTE";

    private final CierreMensualDepositosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;


    // =========================================================
    // PREVIEW
    //
    // No persiste información.
    // Calcula el cierre para toda la entidad.
    // =========================================================

    @PostMapping("/preview")
    public CierreMensualDepositosPreviewDTO preview(
            @RequestBody CierreMensualDepositosRequestDTO request
    ) {

        validarAcceso();

        return service.preview(
                request
        );
    }


    // =========================================================
    // GENERAR FOTOGRAFÍA
    //
    // Genera una sola fotografía mensual para toda la entidad.
    //
    // Estado resultante:
    // P = En proceso / fotografía abierta
    // =========================================================

    @PostMapping("/generar")
    public CierreMensualDepositosApplyResponseDTO generar(
            @RequestBody CierreMensualDepositosRequestDTO request
    ) {

        validarAcceso();

        return service.generar(
                request
        );
    }


    // =========================================================
    // REGENERAR FOTOGRAFÍA
    //
    // Solamente estado P.
    // Conserva id_cierre_mensual.
    // Incluye nuevamente todas las agencias.
    // =========================================================

    @PostMapping("/{idCierre}/regenerar")
    public CierreMensualDepositosApplyResponseDTO regenerar(
            @PathVariable Long idCierre
    ) {

        validarAcceso();

        return service.regenerar(
                idCierre
        );
    }


    // =========================================================
    // CERRAR FOTOGRAFÍA EN FIRME
    //
    // P -> C
    //
    // No recalcula.
    // =========================================================

    @PostMapping("/{idCierre}/cerrar")
    public CierreMensualDepositosApplyResponseDTO cerrarFotografia(
            @PathVariable Long idCierre
    ) {

        validarAcceso();

        return service.cerrarFotografia(
                idCierre
        );
    }


    // =========================================================
    // LISTAR CIERRES
    // =========================================================

    @GetMapping
    public List<CierreMensualDepositosPreviewDTO> listar() {

        validarAcceso();

        return service.listar();
    }


    // =========================================================
    // OBTENER CIERRE POR ID
    // =========================================================

    @GetMapping("/{idCierre}")
    public CierreMensualDepositosPreviewDTO obtenerPorId(
            @PathVariable Long idCierre
    ) {

        validarAcceso();

        return service.obtenerPorId(
                idCierre
        );
    }


    // =========================================================
    // ELIMINAR PRECIERRE
    //
    // Solamente estado P.
    // =========================================================

    @DeleteMapping("/{idCierre}")
    public void eliminar(
            @PathVariable Long idCierre
    ) {

        validarAcceso();

        service.eliminar(
                idCierre
        );
    }


    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarAcceso() {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO_EXECUTE
        );
    }
}