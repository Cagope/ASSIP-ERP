package co.assip.erp.cartera.informes.reversiones;

import co.assip.erp.cartera.informes.reversiones.dto.InformeReversionDTO;
import co.assip.erp.cartera.informes.reversiones.dto.InformeReversionFiltroDTO;
import co.assip.erp.cartera.informes.reversiones.dto.InformeReversionAsociadoDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

/** Expone el informe de reversiones con autorización funcional independiente. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/informes/reversiones-pagos")
public class InformeReversionController {
    private static final String PERMISO = "CARTERA_INFORMES_ESPECIALES_VIEW";
    private final InformeReversionService service;
    private final UsuarioSesionService sesion;
    private final AccessValidator accessValidator;

    /** Sugiere asociados con reversiones aplicando el permiso del informe. */
    @GetMapping("/asociados")
    public List<InformeReversionAsociadoDTO> buscarAsociados(@RequestParam String texto) {
        validarPermiso();
        return service.buscarAsociados(texto);
    }

    /** Consulta reversiones según filtros y agencias autorizadas al usuario. */
    @GetMapping
    public List<InformeReversionDTO> consultar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinal,
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) String asociado,
            @RequestParam(required = false) String pagare,
            @RequestParam(required = false) String funcionario) {
        validarPermiso();
        return service.consultar(new InformeReversionFiltroDTO(
                fechaInicial, fechaFinal, idAgencia, asociado, pagare, funcionario));
    }
    /** Comprueba el permiso funcional para ambas consultas del informe. */
    private void validarPermiso() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(sesion.idUsuario());
        accessValidator.validarAcceso(usuario, PERMISO);
    }
}

