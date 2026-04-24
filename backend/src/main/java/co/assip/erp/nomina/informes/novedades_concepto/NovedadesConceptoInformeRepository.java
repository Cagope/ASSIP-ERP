package co.assip.erp.nomina.informes.novedades_concepto;

import co.assip.erp.nomina.informes.novedades_concepto.dto.NovedadesConceptoInformeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class NovedadesConceptoInformeRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<NovedadesConceptoInformeDTO> consultar(
            String codigoConcepto,
            Integer idEmpleado,
            Integer idPeriodo,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {

        StringBuilder sql = new StringBuilder("""
            SELECT
                id_novedad,
                id_periodo,
                anio,
                mes,
                numero_periodo,
                tipo_periodo,
                estado_periodo,
                id_empleado,
                id_contrato,
                documento,
                nombre_empleado,
                id_agencia,
                codigo_agencia,
                nombre_agencia,
                codigo_concepto,
                nombre_concepto,
                tipo_concepto,
                fecha_inicial,
                fecha_final,
                cantidad,
                valor,
                estado,
                origen,
                observacion
            FROM nomina.vw_novedades_nomina_detalle
            WHERE 1 = 1
        """);

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (codigoConcepto != null && !codigoConcepto.isBlank()) {
            sql.append(" AND codigo_concepto = :codigoConcepto ");
            params.addValue("codigoConcepto", codigoConcepto);
        }

        if (idEmpleado != null) {
            sql.append(" AND id_empleado = :idEmpleado ");
            params.addValue("idEmpleado", idEmpleado);
        }

        if (idPeriodo != null) {
            sql.append(" AND id_periodo = :idPeriodo ");
            params.addValue("idPeriodo", idPeriodo);
        }

        // 🔥 CRUCE DE FECHAS
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
            ORDER BY
                fecha_inicial ASC,
                fecha_final ASC,
                id_novedad ASC
        """);

        return jdbc.query(sql.toString(), params, (rs, rowNum) ->
                NovedadesConceptoInformeDTO.builder()
                        .idNovedad(rs.getInt("id_novedad"))
                        .idPeriodo(rs.getInt("id_periodo"))
                        .anio(rs.getInt("anio"))
                        .mes(rs.getInt("mes"))
                        .numeroPeriodo(rs.getInt("numero_periodo"))
                        .tipoPeriodo(rs.getString("tipo_periodo"))
                        .estadoPeriodo(rs.getString("estado_periodo"))
                        .idEmpleado(rs.getInt("id_empleado"))
                        .idContrato(rs.getInt("id_contrato"))
                        .documento(rs.getString("documento"))
                        .nombreEmpleado(rs.getString("nombre_empleado"))
                        .idAgencia((Integer) rs.getObject("id_agencia"))
                        .codigoAgencia(rs.getString("codigo_agencia"))
                        .nombreAgencia(rs.getString("nombre_agencia"))
                        .codigoConcepto(rs.getString("codigo_concepto"))
                        .nombreConcepto(rs.getString("nombre_concepto"))
                        .tipoConcepto(rs.getString("tipo_concepto"))
                        .fechaInicial(rs.getDate("fecha_inicial") != null ? rs.getDate("fecha_inicial").toLocalDate() : null)
                        .fechaFinal(rs.getDate("fecha_final") != null ? rs.getDate("fecha_final").toLocalDate() : null)
                        .cantidad(rs.getBigDecimal("cantidad"))
                        .valor(rs.getBigDecimal("valor"))
                        .estado(rs.getString("estado"))
                        .origen(rs.getString("origen"))
                        .observacion(rs.getString("observacion"))
                        .build()
        );
    }
}