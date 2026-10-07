package co.assip.erp.cartera.evaluacion.centralriesgos.resultados;

import co.assip.erp.cartera.evaluacion.centralriesgos.resultados.dto.CentralRiesgoResultadoDatoDTO;
import co.assip.erp.cartera.evaluacion.centralriesgos.resultados.dto.CentralRiesgoResultadoImportacionDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/centrales-riesgo/resultados")
public class CentralRiesgoResultadoController {

    private static final String PERMISO_VIEW =
            "CARTERA_EVALUACION_VIEW";

    private static final String PERMISO_EDIT =
            "CARTERA_EVALUACION_EDIT";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final CentralRiesgoResultadoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // IMPORTAR RESULTADOS
    // =========================================================

    @PostMapping(
            value = "/importar",
            consumes = "multipart/form-data"
    )
    public CentralRiesgoResultadoImportacionDTO importar(
            @RequestParam Integer idCentralRiesgo,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaCorte,

            @RequestParam(
                    defaultValue = "false"
            )
            boolean reemplazar,

            @RequestPart("archivo")
            MultipartFile archivo
    ) {

        validarEdicion();

        return service.importar(
                idCentralRiesgo,
                fechaCorte,
                archivo,
                reemplazar
        );
    }

    // =========================================================
    // CONSULTAR CABECERA
    // =========================================================

    @GetMapping("/archivo/{idCentralArchivo}")
    public CentralRiesgoResultadoImportacionDTO buscarArchivoPorId(
            @PathVariable Integer idCentralArchivo
    ) {

        validarConsulta();

        return service.buscarArchivoPorId(
                idCentralArchivo
        );
    }

    // =========================================================
    // CONSULTAR DATOS IMPORTADOS
    // =========================================================

    @GetMapping("/archivo/{idCentralArchivo}/datos")
    public List<CentralRiesgoResultadoDatoDTO> listarDatos(
            @PathVariable Integer idCentralArchivo
    ) {

        validarConsulta();

        return service.listarDatos(
                idCentralArchivo
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

    private void validarEdicion() {

        accessValidator.validarAcceso(
                usuarioActual(),
                PERMISO_EDIT
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