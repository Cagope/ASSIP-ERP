package co.assip.erp.nomina.desprendible;

import co.assip.erp.nomina.desprendible.dto.DesprendibleEmpleadoDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/nomina/desprendible")
public class DesprendibleController {

    private final DesprendibleService service;
    private final DesprendiblePdfService pdfService;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ============================================================
    // LISTAR EMPLEADOS DEL PERÍODO
    // ============================================================
    @GetMapping("/{idPeriodo}/empleados")
    public ResponseEntity<List<DesprendibleRepository.EmpleadoPeriodoRow>>
    listarEmpleados(
            @PathVariable Integer idPeriodo
    ) {

        validarAcceso("NOMINA_INFORMES_VIEW");

        return ResponseEntity.ok(
                service.listarEmpleadosPeriodo(idPeriodo)
        );
    }

    // ============================================================
    // CONSULTAR DESPRENDIBLE
    // ============================================================
    @GetMapping
    public ResponseEntity<DesprendibleEmpleadoDTO> obtener(
            @RequestParam Integer idPeriodo,
            @RequestParam Integer idContrato
    ) {

        validarAcceso("NOMINA_INFORMES_VIEW");

        return ResponseEntity.ok(
                service.generar(
                        idPeriodo,
                        idContrato
                )
        );
    }

    // ============================================================
    // PDF INDIVIDUAL
    // ============================================================
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(
            @RequestParam Integer idPeriodo,
            @RequestParam Integer idContrato
    ) {

        validarAcceso("NOMINA_INFORMES_VIEW");

        return pdfService.generarPdfResponse(
                service.generar(
                        idPeriodo,
                        idContrato
                )
        );
    }

    // ============================================================
    // PDF TODOS LOS EMPLEADOS
    // ============================================================
    @GetMapping("/pdf/todos")
    public ResponseEntity<byte[]> pdfTodos(
            @RequestParam Integer idPeriodo
    ) {

        validarAcceso("NOMINA_INFORMES_VIEW");

        return pdfService.generarZipPeriodo(
                idPeriodo
        );
    }

    // ============================================================
    // AUTORIZACIÓN
    // ============================================================
    private void validarAcceso(String permiso) {

        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}