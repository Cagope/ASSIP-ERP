package co.assip.erp.nomina.novedades_nomina;

import co.assip.erp.nomina.novedades_nomina.dto.*;
import co.assip.erp.nomina.periodos_nomina.PeriodoNominaActivoService;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/nomina/novedades")
public class NovedadesNominaController {

    private final NovedadesNominaService service;
    private final NovedadesNominaMasivoService masivoService;
    private final NovedadCalculoService calculoService;
    private final PeriodoNominaActivoService periodoActivoService;
    private final UsuarioSesionService usuarioSesionService;

    public NovedadesNominaController(
            NovedadesNominaService service,
            NovedadesNominaMasivoService masivoService,
            NovedadCalculoService calculoService,
            PeriodoNominaActivoService periodoActivoService,
            UsuarioSesionService usuarioSesionService
    ) {
        this.service = service;
        this.masivoService = masivoService;
        this.calculoService = calculoService;
        this.periodoActivoService = periodoActivoService;
        this.usuarioSesionService = usuarioSesionService;
    }

    // =========================================================
    // LISTAR (PERÍODO ACTIVO IMPLÍCITO)
    // =========================================================
    @GetMapping
    public List<NovedadNominaListDTO> listar(
            @RequestParam Integer agencia,
            @RequestParam(required = false) Integer empleado
    ) {
        return service.listar(agencia, empleado);
    }

    // =========================================================
    // OBTENER
    // =========================================================
    @GetMapping("/{id}")
    public NovedadNominaFormDTO obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    // =========================================================
    // CREAR INDIVIDUAL
    // =========================================================
    @PostMapping
    public void crear(@RequestBody NovedadNominaFormDTO dto) {
        Integer idUsuario =
                usuarioSesionService.idUsuario();
        service.crear(dto, idUsuario);
    }

    @GetMapping("/periodo-activo")
    public Object obtenerPeriodoActivo() {
        return periodoActivoService.obtenerPeriodoActivoInfo();
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================
    @PutMapping("/{id}")
    public void actualizar(
            @PathVariable Integer id,
            @RequestBody NovedadNominaFormDTO dto
    ) {
        Integer idUsuario =
                usuarioSesionService.idUsuario();
        service.actualizar(id, dto, idUsuario);
    }

    // =========================================================
    // ELIMINAR
    // =========================================================
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }

    // =========================================================
    // MASIVO (GRABA)
    // =========================================================
    @PostMapping("/masivo")
    public NovedadMasivaResultDTO generarMasivo(
            @RequestBody NovedadMasivaRequestDTO request
    ) {
        Integer idUsuario =
                usuarioSesionService.idUsuario();
        return masivoService.generarMasivo(request, idUsuario);
    }

    // =========================================================
    // MASIVO PREVIEW
    // =========================================================
    @PostMapping("/masivo/preview")
    public List<Map<String, Object>> previewMasivo(
            @RequestBody NovedadMasivaRequestDTO request
    ) {
        return masivoService.previewMasivo(request);
    }

    // =========================================================
    // CALCULAR INDIVIDUAL (PREVIEW)
    // =========================================================
    @PostMapping("/calcular")
    public NovedadCalculoResponseDTO calcular(
            @RequestBody NovedadCalculoRequestDTO request
    ) {
        BigDecimal valor = calculoService.calcularValor(
                request.getIdContrato(),
                request.getCodigoConcepto(),
                request.getCantidad()
        );

        return NovedadCalculoResponseDTO.builder()
                .valorCalculado(valor)
                .build();
    }
}