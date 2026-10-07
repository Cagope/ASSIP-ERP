package co.assip.erp.cartera.originacion.contexto;

import co.assip.erp.cartera.originacion.contexto.dto.OriginacionContextoDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/originacion/contexto")
public class OriginacionContextoController {

    private static final String PERMISO_VIEW =
            "CARTERA_ORIGINACION_VIEW";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final OriginacionContextoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // CONTEXTO COMPLETO
    // =========================================================

    @GetMapping("/{idDatosPersonal}")
    public ResponseEntity<OriginacionContextoDTO> consultar(
            @PathVariable Integer idDatosPersonal,
            @RequestParam Integer idAgencia
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.consultar(
                        idDatosPersonal,
                        idAgencia
                )
        );
    }

    // =========================================================
    // INFORMACIÓN ECONÓMICA
    // =========================================================

    @GetMapping("/{idDatosPersonal}/informacion-economica")
    public ResponseEntity<OriginacionContextoDTO.InformacionEconomicaDTO>
    consultarInformacionEconomica(
            @PathVariable Integer idDatosPersonal,
            @RequestParam Integer idAgencia
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.consultarInformacionEconomica(
                        idDatosPersonal,
                        idAgencia
                )
        );
    }

    // =========================================================
    // DEPÓSITOS
    // =========================================================

    @GetMapping("/{idDatosPersonal}/depositos")
    public ResponseEntity<List<OriginacionContextoDTO.DepositoDTO>>
    listarDepositos(
            @PathVariable Integer idDatosPersonal,
            @RequestParam Integer idAgencia
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.listarDepositos(
                        idDatosPersonal,
                        idAgencia
                )
        );
    }

    // =========================================================
    // CARTERA ACTUAL
    // =========================================================

    @GetMapping("/{idDatosPersonal}/cartera")
    public ResponseEntity<List<OriginacionContextoDTO.CarteraDTO>>
    listarCarteraActual(
            @PathVariable Integer idDatosPersonal,
            @RequestParam Integer idAgencia
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.listarCarteraActual(
                        idDatosPersonal,
                        idAgencia
                )
        );
    }

    // =========================================================
    // CARTERA HISTÓRICA
    // =========================================================

    @GetMapping("/{idDatosPersonal}/cartera/historico")
    public ResponseEntity<List<OriginacionContextoDTO.CarteraDTO>>
    listarCarteraHistorica(
            @PathVariable Integer idDatosPersonal,
            @RequestParam Integer idAgencia
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.listarCarteraHistorica(
                        idDatosPersonal,
                        idAgencia
                )
        );
    }

    // =========================================================
    // CODEUDAS ACTUALES
    // =========================================================

    @GetMapping("/{idDatosPersonal}/codeudas")
    public ResponseEntity<List<OriginacionContextoDTO.CodeudaDTO>>
    listarCodeudasActuales(
            @PathVariable Integer idDatosPersonal,
            @RequestParam Integer idAgencia
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.listarCodeudasActuales(
                        idDatosPersonal,
                        idAgencia
                )
        );
    }

    // =========================================================
    // CODEUDAS HISTÓRICAS
    // =========================================================

    @GetMapping("/{idDatosPersonal}/codeudas/historico")
    public ResponseEntity<List<OriginacionContextoDTO.CodeudaDTO>>
    listarCodeudasHistoricas(
            @PathVariable Integer idDatosPersonal,
            @RequestParam Integer idAgencia
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.listarCodeudasHistoricas(
                        idDatosPersonal,
                        idAgencia
                )
        );
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarConsulta() {

        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO_VIEW
        );
    }
}