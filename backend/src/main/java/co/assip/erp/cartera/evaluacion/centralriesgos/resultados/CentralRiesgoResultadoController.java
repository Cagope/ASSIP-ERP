package co.assip.erp.cartera.evaluacion.centralriesgos.resultados;

import co.assip.erp.cartera.evaluacion.centralriesgos.resultados.dto.CentralRiesgoResultadoDatoDTO;
import co.assip.erp.cartera.evaluacion.centralriesgos.resultados.dto.CentralRiesgoResultadoImportacionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/centrales-riesgo/resultados")
public class CentralRiesgoResultadoController {

    private final CentralRiesgoResultadoService service;

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

        return service.listarDatos(
                idCentralArchivo
        );
    }
}