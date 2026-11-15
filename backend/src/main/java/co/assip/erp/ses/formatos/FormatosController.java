
package co.assip.erp.ses.formatos;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ses/formatos")
@RequiredArgsConstructor
public class FormatosController {

    private final FormatosService formatosService;

    /**
     * Consulta el código oficial de un formato SES activo.
     *
     * Parámetros:
     * - modulo: módulo de origen del reporte.
     * - nombre: nombre funcional del formato.
     *
     * Ejemplo:
     * GET /api/v1/ses/formatos
     *     ?modulo=HOJA_VIDA&nombre=ASOCIADOS
     *
     * La información proviene de supersolidaria.formatos.
     *
     * No modifica registros ni configuraciones.
     */
    @PreAuthorize("hasAuthority('SES_FORMATOS_VIEW')")
    @GetMapping
    public ResponseEntity<Map<String, String>> consultarFormato(
            @RequestParam("modulo") String modulo,
            @RequestParam("nombre") String nombre
    ) {

        String codigoFormato =
                formatosService.obtenerCodigoFormato(
                        modulo,
                        nombre
                );

        return ResponseEntity.ok(
                Map.of(
                        "codigoFormato",
                        codigoFormato
                )
        );
    }
}
