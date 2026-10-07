package co.assip.erp.cajas.provisiones;

import co.assip.erp.cajas.provisiones.dto.CajaAperturaDTO;
import co.assip.erp.cajas.provisiones.dto.CajaDisponibleDTO;
import co.assip.erp.cajas.provisiones.dto.CajaEstadoDTO;
import co.assip.erp.cajas.provisiones.dto.CajaProvisionActivaDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionCerrarDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionListDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionSaveDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cajas/provisiones")
@RequiredArgsConstructor
public class CajasProvisionController {

    private static final String PERMISO_VINCULAR_CAJA =
            "CAJAS_VINCULAR_CAJA";

    private final CajasProvisionService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @GetMapping("/preparar-apertura")
    public CajaAperturaDTO prepararAperturaUsuario() {

        validarVincularCaja();

        return service.prepararAperturaUsuario();
    }

    @PostMapping("/abrir")
    public Long abrirCaja(
            @RequestBody CajasProvisionSaveDTO dto
    ) {

        validarVincularCaja();

        return service.abrirCaja(dto);
    }

    @GetMapping("/activa-usuario")
    public CajaProvisionActivaDTO
    obtenerProvisionActivaUsuario() {

        return service.obtenerProvisionActivaUsuario();
    }

    /*
     * Endpoints existentes de administración/mantenimiento.
     */

    @PostMapping
    public Long guardar(
            @RequestBody CajasProvisionSaveDTO dto
    ) {

        return service.guardar(dto);
    }

    @PostMapping("/cerrar")
    public void cerrar(
            @RequestBody CajasProvisionCerrarDTO dto
    ) {

        service.cerrar(dto);
    }

    @GetMapping("/abiertas")
    public List<CajasProvisionListDTO>
    listarCajasAbiertas(
            @RequestParam Integer idAgencia,
            @RequestParam String fecha
    ) {

        return service.listarCajasAbiertas(
                idAgencia,
                java.time.LocalDate.parse(fecha)
        );
    }

    @GetMapping("/disponibles")
    public List<CajaDisponibleDTO>
    listarCajasDisponibles(
            @RequestParam Integer idAgencia
    ) {

        return service.listarCajasDisponibles(
                idAgencia
        );
    }

    @GetMapping("/estado-cajas")
    public List<CajaEstadoDTO> listarEstadoCajas(
            @RequestParam Integer idAgencia,
            @RequestParam String fechaContable
    ) {

        validarVincularCaja();

        return service.listarEstadoCajas(
                idAgencia,
                java.time.LocalDate.parse(
                        fechaContable
                )
        );
    }

    private void validarVincularCaja() {

        accessValidator.validarAcceso(
                usuarioActual(),
                PERMISO_VINCULAR_CAJA
        );
    }

    private Usuario usuarioActual() {

        Usuario usuarioActual =
                new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        return usuarioActual;
    }
}