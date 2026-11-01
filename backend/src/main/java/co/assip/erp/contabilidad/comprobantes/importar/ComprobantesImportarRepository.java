
package co.assip.erp.contabilidad.comprobantes.importar;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Consultas de apoyo para validar terceros y tipos de control
 * durante la importación de comprobantes contables.
 * No inserta ni modifica información contable.
 */
@Repository
@RequiredArgsConstructor
public class ComprobantesImportarRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Busca los identificadores internos asociados al documento del tercero.
     * Limita los resultados a dos para detectar documentos ambiguos.
     */
    public List<Integer> buscarIdsTerceroPorDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            return List.of();
        }

        return jdbcTemplate.query(
                """
                SELECT id_datos_personal
                FROM hoja_vida.datos_personales
                WHERE documento = ?
                ORDER BY id_datos_personal
                LIMIT 2
                """,
                (rs, fila) -> rs.getInt("id_datos_personal"),
                documento.trim()
        );
    }

    /**
     * Busca tipos de control por descripción, sin distinguir mayúsculas.
     * Devuelve hasta dos coincidencias para detectar ambigüedades.
     */
    public List<TipoControl> buscarTiposControlPorDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return List.of();
        }

        return jdbcTemplate.query(
                """
                SELECT
                    id_tipos_control,
                    descripcion,
                    pide_documento
                FROM contabilidad.tipos_control_ent_sal
                WHERE UPPER(TRIM(descripcion)) = UPPER(TRIM(?))
                ORDER BY id_tipos_control
                LIMIT 2
                """,
                (rs, fila) -> new TipoControl(
                        rs.getInt("id_tipos_control"),
                        rs.getString("descripcion"),
                        rs.getObject("pide_documento", Boolean.class)
                ),
                descripcion.trim()
        );
    }

    /**
     * Resuelve un tipo de control únicamente cuando existe una coincidencia.
     * Si no existe o hay duplicados, devuelve Optional.empty().
     */
    public Optional<TipoControl> resolverTipoControl(String descripcion) {
        List<TipoControl> resultados =
                buscarTiposControlPorDescripcion(descripcion);

        if (resultados.size() != 1) {
            return Optional.empty();
        }

        return Optional.of(resultados.get(0));
    }

    /**
     * Datos de parametrización necesarios para validar cada movimiento.
     */
    public record TipoControl(
            Integer idTiposControl,
            String descripcion,
            Boolean pideDocumento
    ) {
    }
}
