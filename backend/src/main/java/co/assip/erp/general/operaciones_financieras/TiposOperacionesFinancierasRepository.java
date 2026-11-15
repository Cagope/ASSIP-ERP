
package co.assip.erp.general.operaciones_financieras;

import co.assip.erp.general.operaciones_financieras.dto.TipoOperacionFinancieraDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TiposOperacionesFinancierasRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Busca la configuración activa de un proceso financiero.
     * Conserva compatibilidad con los servicios actuales y rechaza
     * resultados ambiguos para evitar ejecutar una operación incorrecta.
     */
    public Optional<TipoOperacionFinancieraDTO> buscarActivo(
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen
    ) {
        String sql = """
                SELECT
                    id_tipo_operacion,
                    modulo_origen,
                    proceso_origen,
                    tabla_origen,
                    codigo_operacion_financiera,
                    nombre_operacion,
                    naturaleza,
                    permite_caja,
                    permite_tesoreria,
                    permite_parciales,
                    perfil_ejecucion,
                    activo
                FROM general.tipos_operaciones_financieras
                WHERE modulo_origen = ?
                  AND proceso_origen = ?
                  AND tabla_origen = ?
                  AND activo = TRUE
                """;

        List<TipoOperacionFinancieraDTO> resultados = jdbcTemplate.query(
                sql,
                this::mapear,
                moduloOrigen,
                procesoOrigen,
                tablaOrigen
        );

        if (resultados.size() > 1) {
            throw new IllegalStateException(
                    "Existen varias operaciones financieras activas para "
                            + moduloOrigen + " / "
                            + procesoOrigen + " / "
                            + tablaOrigen
                            + ". Debe especificarse el código de operación financiera."
            );
        }

        return resultados.stream().findFirst();
    }

    /**
     * Busca una operación financiera específica mediante su código funcional.
     * Valida la identificación completa del origen y no modifica registros.
     */
    public Optional<TipoOperacionFinancieraDTO> buscarActivoPorCodigo(
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen,
            String codigoOperacionFinanciera
    ) {
        String sql = """
                SELECT
                    id_tipo_operacion,
                    modulo_origen,
                    proceso_origen,
                    tabla_origen,
                    codigo_operacion_financiera,
                    nombre_operacion,
                    naturaleza,
                    permite_caja,
                    permite_tesoreria,
                    permite_parciales,
                    perfil_ejecucion,
                    activo
                FROM general.tipos_operaciones_financieras
                WHERE modulo_origen = ?
                  AND proceso_origen = ?
                  AND tabla_origen = ?
                  AND codigo_operacion_financiera = ?
                  AND activo = TRUE
                """;

        List<TipoOperacionFinancieraDTO> resultados = jdbcTemplate.query(
                sql,
                this::mapear,
                moduloOrigen,
                procesoOrigen,
                tablaOrigen,
                codigoOperacionFinanciera
        );

        return resultados.stream().findFirst();
    }

    /**
     * Lista las configuraciones activas habilitadas para Tesorería.
     * No consulta ni modifica operaciones financieras pendientes.
     */
    public List<TipoOperacionFinancieraDTO> listarParaTesoreria() {
        String sql = """
                SELECT
                    id_tipo_operacion,
                    modulo_origen,
                    proceso_origen,
                    tabla_origen,
                    codigo_operacion_financiera,
                    nombre_operacion,
                    naturaleza,
                    permite_caja,
                    permite_tesoreria,
                    permite_parciales,
                    perfil_ejecucion,
                    activo
                FROM general.tipos_operaciones_financieras
                WHERE activo = TRUE
                  AND permite_tesoreria = TRUE
                ORDER BY modulo_origen, nombre_operacion
                """;

        return jdbcTemplate.query(sql, this::mapear);
    }

    /**
     * Lista las configuraciones activas habilitadas para Caja.
     * No reemplaza el catálogo cajas.operaciones_caja.
     */
    public List<TipoOperacionFinancieraDTO> listarParaCaja() {
        String sql = """
                SELECT
                    id_tipo_operacion,
                    modulo_origen,
                    proceso_origen,
                    tabla_origen,
                    codigo_operacion_financiera,
                    nombre_operacion,
                    naturaleza,
                    permite_caja,
                    permite_tesoreria,
                    permite_parciales,
                    perfil_ejecucion,
                    activo
                FROM general.tipos_operaciones_financieras
                WHERE activo = TRUE
                  AND permite_caja = TRUE
                ORDER BY modulo_origen, nombre_operacion
                """;

        return jdbcTemplate.query(sql, this::mapear);
    }

    /**
     * Convierte cada registro de PostgreSQL al DTO del catálogo.
     * Conserva los valores financieros y el código funcional registrado.
     */
    private TipoOperacionFinancieraDTO mapear(
            ResultSet rs,
            int rowNum
    ) throws SQLException {
        TipoOperacionFinancieraDTO dto = new TipoOperacionFinancieraDTO();

        dto.setIdTipoOperacion(rs.getLong("id_tipo_operacion"));
        dto.setModuloOrigen(rs.getString("modulo_origen"));
        dto.setProcesoOrigen(rs.getString("proceso_origen"));
        dto.setTablaOrigen(rs.getString("tabla_origen"));
        dto.setCodigoOperacionFinanciera(
                rs.getString("codigo_operacion_financiera")
        );
        dto.setNombreOperacion(rs.getString("nombre_operacion"));
        dto.setNaturaleza(rs.getString("naturaleza"));
        dto.setPermiteCaja(rs.getBoolean("permite_caja"));
        dto.setPermiteTesoreria(rs.getBoolean("permite_tesoreria"));
        dto.setPermiteParciales(rs.getBoolean("permite_parciales"));
        dto.setPerfilEjecucion(rs.getString("perfil_ejecucion"));
        dto.setActivo(rs.getBoolean("activo"));

        return dto;
    }
}
