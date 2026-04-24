package co.assip.erp.nomina.informes.consolidado_conceptos;

import co.assip.erp.nomina.informes.consolidado_conceptos.dto.ConsolidadoConceptosInformeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConsolidadoConceptosInformeRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<ConsolidadoConceptosInformeDTO> consultar(
            String codigoConcepto,
            String tipoConcepto,
            Integer idPeriodo,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {

        StringBuilder sql = new StringBuilder("""
            SELECT
                codigo_concepto,
                nombre_concepto,
                tipo_concepto,
                COUNT(*) AS cantidad_registros,
                COALESCE(SUM(cantidad), 0) AS total_cantidad,
                COALESCE(SUM(valor), 0) AS total_valor
            FROM nomina.vw_novedades_nomina_detalle
            WHERE 1 = 1
        """);

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (codigoConcepto != null && !codigoConcepto.isBlank()) {
            sql.append(" AND codigo_concepto = :codigoConcepto ");
            params.addValue("codigoConcepto", codigoConcepto.trim());
        }

        if (tipoConcepto != null && !tipoConcepto.isBlank()) {
            sql.append(" AND tipo_concepto = :tipoConcepto ");
            params.addValue("tipoConcepto", tipoConcepto.trim());
        }

        if (idPeriodo != null) {
            sql.append(" AND id_periodo = :idPeriodo ");
            params.addValue("idPeriodo", idPeriodo);
        }

        if (fechaInicial != null && fechaFinal != null) {
            sql.append("""
                AND (fecha_inicial <= :fechaFinal
                     AND fecha_final >= :fechaInicial)
            """);
            params.addValue("fechaInicial", fechaInicial);
            params.addValue("fechaFinal", fechaFinal);

        } else if (fechaInicial != null) {
            sql.append(" AND fecha_final >= :fechaInicial ");
            params.addValue("fechaInicial", fechaInicial);

        } else if (fechaFinal != null) {
            sql.append(" AND fecha_inicial <= :fechaFinal ");
            params.addValue("fechaFinal", fechaFinal);
        }

        sql.append("""
            GROUP BY
                codigo_concepto,
                nombre_concepto,
                tipo_concepto
            ORDER BY
                tipo_concepto,
                codigo_concepto
        """);

        return jdbc.query(sql.toString(), params, (rs, rowNum) ->
                ConsolidadoConceptosInformeDTO.builder()
                        .codigoConcepto(rs.getString("codigo_concepto"))
                        .nombreConcepto(rs.getString("nombre_concepto"))
                        .tipoConcepto(rs.getString("tipo_concepto"))
                        .cantidadRegistros(rs.getLong("cantidad_registros"))
                        .totalCantidad(rs.getBigDecimal("total_cantidad"))
                        .totalValor(rs.getBigDecimal("total_valor"))
                        .build()
        );
    }
}