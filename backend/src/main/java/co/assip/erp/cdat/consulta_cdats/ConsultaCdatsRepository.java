package co.assip.erp.cdat.informes.consulta_cdats;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ConsultaCdatsRepository {

    private static final int LIMITE_RESULTADOS = 2000;

    private final JdbcTemplate jdbc;

    /**
     * Consulta principal de CDAT.
     *
     * Mantiene como fuente:
     * cdat.vw_cdat_cuentas_total_extendida
     */
    public List<Map<String, Object>> consultarCdats(
            String documento,
            String nombres,
            String primerApellido,
            String segundoApellido,
            String codigoCdat
    ) {

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM cdat.vw_cdat_cuentas_total_extendida
                WHERE 1 = 1
                """);

        List<Object> parametros = new ArrayList<>();

        if (tieneValor(documento)) {
            sql.append("""
                    
                    AND CAST(documento AS TEXT) ILIKE ?
                    """);
            parametros.add("%" + documento.trim() + "%");
        }

        if (tieneValor(nombres)) {
            sql.append("""
                    
                    AND LOWER(nombres) ILIKE LOWER(?)
                    """);
            parametros.add("%" + nombres.trim() + "%");
        }

        if (tieneValor(primerApellido)) {
            sql.append("""
                    
                    AND LOWER(primer_apellido) ILIKE LOWER(?)
                    """);
            parametros.add("%" + primerApellido.trim() + "%");
        }

        if (tieneValor(segundoApellido)) {
            sql.append("""
                    
                    AND LOWER(segundo_apellido) ILIKE LOWER(?)
                    """);
            parametros.add("%" + segundoApellido.trim() + "%");
        }

        if (tieneValor(codigoCdat)) {
            sql.append("""
                    
                    AND CAST(codigo_cdat AS TEXT) ILIKE ?
                    """);
            parametros.add("%" + codigoCdat.trim() + "%");
        }

        sql.append("""
                
                ORDER BY 1 DESC
                LIMIT ?
                """);

        parametros.add(LIMITE_RESULTADOS);

        return jdbc.queryForList(
                sql.toString(),
                parametros.toArray()
        );
    }

    /**
     * Consulta de movimientos/extracto de un CDAT.
     *
     * Mantiene como fuente:
     * cdat.vw_cdat_extractos_total
     */
    public List<Map<String, Object>> consultarExtracto(
            String codigoCdat,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {

        String sql = """
                SELECT *
                FROM cdat.vw_cdat_extractos_total
                WHERE CAST(codigo_cdat AS TEXT) = ?
                  AND fecha_movimiento >= ?
                  AND fecha_movimiento <= ?
                ORDER BY
                    fecha_movimiento ASC,
                    hora_movimiento ASC
                LIMIT ?
                """;

        return jdbc.queryForList(
                sql,
                codigoCdat,
                fechaInicial,
                fechaFinal,
                LIMITE_RESULTADOS
        );
    }

    private boolean tieneValor(String valor) {
        return valor != null && !valor.isBlank();
    }
}