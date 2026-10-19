
package co.assip.erp.depositos.retiro_asociado;

import co.assip.erp.depositos.retiro_asociado.dto.RetiroAsociadoEntradaDTO;
import co.assip.erp.depositos.retiro_asociado.dto.RetiroAsociadoPreviewDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestParam;
import co.assip.erp.shared.personas.dto.PersonaBusquedaDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/depositos/retiro-asociado")
public class RetiroAsociadoController {

    private final RetiroAsociadoService retiroAsociadoService;
    private final JdbcTemplate jdbc;

    /** Búsqueda global de asociados; la agencia solo restringe la operación, no la identificación. */
    @GetMapping("/buscar-asociados")
    public List<PersonaBusquedaDTO> buscarAsociados(
            @RequestParam(defaultValue = "") String documento,
            @RequestParam(defaultValue = "") String nombres,
            @RequestParam(defaultValue = "") String primerApellido,
            @RequestParam(defaultValue = "") String segundoApellido) {
        String d = documento.trim(), n = nombres.trim(), p = primerApellido.trim(), s = segundoApellido.trim();
        if (d.isEmpty() && n.isEmpty() && p.isEmpty() && s.isEmpty()) return List.of();
        String sql = """
            SELECT dp.id_datos_personal, dp.documento,
                   TRIM(CONCAT_WS(' ', dp.primer_apellido, dp.segundo_apellido, dp.nombres)) AS nombre_completo
            FROM hoja_vida.datos_personales dp
            WHERE dp.documento ILIKE ? AND dp.nombres ILIKE ?
              AND dp.primer_apellido ILIKE ? AND dp.segundo_apellido ILIKE ?
            ORDER BY dp.primer_apellido, dp.segundo_apellido, dp.nombres
            LIMIT 100
            """;
        return jdbc.query(sql, (rs, i) -> {
            PersonaBusquedaDTO persona = new PersonaBusquedaDTO();
            persona.setIdDatosPersonal(rs.getLong("id_datos_personal"));
            persona.setDocumento(rs.getString("documento"));
            persona.setNombreCompleto(rs.getString("nombre_completo"));
            return persona;
        }, "%" + d + "%", "%" + n + "%", "%" + p + "%", "%" + s + "%");
    }

    /** Consulta informativa de cuentas con saldo positivo. */
    @GetMapping("/cuentas/{idDatosPersonal}")
    public ResponseEntity<List<RetiroAsociadoRepository.CuentaRetiro>> consultarCuentas(
            @PathVariable Integer idDatosPersonal
    ) {
        return ResponseEntity.ok(retiroAsociadoService.consultarCuentas(idDatosPersonal));
    }

    /** Ejecuta el retiro integral en una única transacción. */
    @PostMapping("/aplicar")
    public ResponseEntity<Void> aplicar(
            @RequestBody RetiroAsociadoEntradaDTO entrada
    ) {
        retiroAsociadoService.aplicar(entrada);
        return ResponseEntity.noContent().build();
    }

    /**
     * Consulta las cuentas del asociado y presenta
     * los saldos y la distribución propuesta.
     *
     * No modifica información financiera.
     */
    @PostMapping("/preview")
    public ResponseEntity<RetiroAsociadoPreviewDTO> preview(
            @RequestBody RetiroAsociadoEntradaDTO entrada
    ) {
        return ResponseEntity.ok(
                retiroAsociadoService.preview(entrada)
        );
    }
}
