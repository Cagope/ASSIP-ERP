package co.assip.erp.nomina.empleado_contratos;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoListViewDTO;


@RestController
@RequiredArgsConstructor
@RequestMapping("/nomina/empleado-contratos")
public class EmpleadoContratoController {

    private final EmpleadoContratoService service;
    private final UsuarioSesionService usuarioSesionService;

    // ============================================================
    // LISTAR
    // ============================================================
    @GetMapping
    public List<EmpleadoContratoListViewDTO> listar() {
        return service.listar();
    }

    // ============================================================
    // LISTAR POR EMPLEADO
    // ============================================================
    @GetMapping("/por-empleado/{idEmpleado}")
    public List<EmpleadoContratoListViewDTO> listarPorEmpleado(
            @PathVariable Integer idEmpleado) {

        return service.listarPorEmpleado(idEmpleado);
    }

    // ============================================================
    // OBTENER
    // ============================================================
    @GetMapping("/{id}")
    public EmpleadoContratoDTO obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    // ============================================================
    // CREAR
    // ============================================================
    @PostMapping
    public Integer crear(@RequestBody EmpleadoContratoDTO dto) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        return service.crear(dto, idUsuario);
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    @PutMapping("/{id}")
    public void actualizar(@PathVariable Integer id,
                           @RequestBody EmpleadoContratoDTO dto) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.actualizar(id, dto, idUsuario);
    }

    // ============================================================
    // ELIMINAR
    // ============================================================
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}
