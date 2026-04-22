package co.assip.erp.nomina.eventos_liquidacion;

import co.assip.erp.nomina.eventos_liquidacion.dto.EventoLiquidacionFormDTO;
import co.assip.erp.nomina.eventos_liquidacion.dto.EventoLiquidacionListDTO;
import co.assip.erp.nomina.eventos_liquidacion.dto.EventoLiquidacionSaveDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EventosLiquidacionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<EventoLiquidacionListDTO> listar() {
        String sql = """
            SELECT
                el.id_evento_liquidacion,
                el.id_contrato,
                ec.id_empleado,
                e.id_agencia,
                dp.documento AS documento_empleado,
                CASE
                    WHEN dp.tipo_persona = '2' THEN COALESCE(dp.nombres, '')
                    ELSE TRIM(
                        COALESCE(dp.primer_apellido, '') || ' ' ||
                        COALESCE(dp.segundo_apellido, '') || ' ' ||
                        COALESCE(dp.nombres, '')
                    )
                END AS nombre_empleado,
                el.fecha_documento,
                el.fecha_inicio,
                el.fecha_fin,
                el.total_dias,
                el.tipo_evento,
                el.numero_soporte,
                el.responsable_pago,
                el.porcentaje_responsable,
                el.porcentaje_empresa,
                el.dias_empresa_100,
                el.genera_cxc,
                el.liquida_arl,
                el.es_remunerado,
                el.observacion,
                el.estado,
                el.fk_seguridad_creacion,
                el.fecha_creacion,
                el.fk_seguridad_edicion,
                el.fecha_edicion
            FROM nomina.eventos_liquidacion el
            JOIN nomina.empleado_contratos ec
                ON ec.id_contrato = el.id_contrato
            JOIN nomina.empleados e
                ON e.id_empleado = ec.id_empleado
            JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal = e.id_datos_personal
            ORDER BY el.id_evento_liquidacion DESC
            """;

        return jdbc.query(sql, (rs, rowNum) -> {
            Timestamp fechaCreacionTs = rs.getTimestamp("fecha_creacion");
            Timestamp fechaEdicionTs = rs.getTimestamp("fecha_edicion");

            return EventoLiquidacionListDTO.builder()
                    .idEventoLiquidacion(rs.getLong("id_evento_liquidacion"))
                    .idContrato(rs.getInt("id_contrato"))
                    .idEmpleado(rs.getInt("id_empleado"))
                    .idAgencia(rs.getInt("id_agencia"))
                    .documentoEmpleado(rs.getString("documento_empleado"))
                    .nombreEmpleado(rs.getString("nombre_empleado"))
                    .fechaDocumento(rs.getDate("fecha_documento") != null ? rs.getDate("fecha_documento").toLocalDate() : null)
                    .fechaInicio(rs.getDate("fecha_inicio") != null ? rs.getDate("fecha_inicio").toLocalDate() : null)
                    .fechaFin(rs.getDate("fecha_fin") != null ? rs.getDate("fecha_fin").toLocalDate() : null)
                    .totalDias(rs.getObject("total_dias") != null ? rs.getInt("total_dias") : null)
                    .tipoEvento(rs.getString("tipo_evento"))
                    .numeroSoporte(rs.getString("numero_soporte"))
                    .responsablePago(rs.getString("responsable_pago"))
                    .porcentajeResponsable(rs.getObject("porcentaje_responsable") != null ? rs.getDouble("porcentaje_responsable") : null)
                    .porcentajeEmpresa(rs.getObject("porcentaje_empresa") != null ? rs.getDouble("porcentaje_empresa") : null)
                    .diasEmpresa100(rs.getObject("dias_empresa_100") != null ? rs.getInt("dias_empresa_100") : null)
                    .generaCxc(rs.getObject("genera_cxc") != null ? rs.getBoolean("genera_cxc") : null)
                    .liquidaArl(rs.getObject("liquida_arl") != null ? rs.getBoolean("liquida_arl") : null)
                    .esRemunerado(rs.getObject("es_remunerado") != null ? rs.getBoolean("es_remunerado") : null)
                    .observacion(rs.getString("observacion"))
                    .estado(rs.getString("estado"))
                    .fkSeguridadCreacion(rs.getObject("fk_seguridad_creacion") != null ? rs.getInt("fk_seguridad_creacion") : null)
                    .fechaCreacion(fechaCreacionTs != null ? fechaCreacionTs.toLocalDateTime() : null)
                    .fkSeguridadEdicion(rs.getObject("fk_seguridad_edicion") != null ? rs.getInt("fk_seguridad_edicion") : null)
                    .fechaEdicion(fechaEdicionTs != null ? fechaEdicionTs.toLocalDateTime() : null)
                    .build();
        });
    }

    public Optional<EventoLiquidacionFormDTO> obtenerPorId(Long idEventoLiquidacion) {
        String sql = """
            SELECT
                el.id_evento_liquidacion,
                el.id_contrato,
                ec.id_empleado,
                el.fecha_documento,
                el.fecha_inicio,
                el.fecha_fin,
                el.total_dias,
                el.tipo_evento,
                el.numero_soporte,
                el.responsable_pago,
                el.porcentaje_responsable,
                el.porcentaje_empresa,
                el.dias_empresa_100,
                el.genera_cxc,
                el.liquida_arl,
                el.es_remunerado,
                el.observacion,
                el.estado
            FROM nomina.eventos_liquidacion el
            JOIN nomina.empleado_contratos ec
                ON ec.id_contrato = el.id_contrato
            WHERE el.id_evento_liquidacion = :idEventoLiquidacion
            """;

        List<EventoLiquidacionFormDTO> rows = jdbc.query(
                sql,
                Map.of("idEventoLiquidacion", idEventoLiquidacion),
                (rs, rowNum) -> EventoLiquidacionFormDTO.builder()
                        .idEventoLiquidacion(rs.getLong("id_evento_liquidacion"))
                        .idEmpleado(rs.getInt("id_empleado"))
                        .idContrato(rs.getInt("id_contrato"))
                        .fechaDocumento(rs.getDate("fecha_documento") != null ? rs.getDate("fecha_documento").toLocalDate() : null)
                        .fechaInicio(rs.getDate("fecha_inicio") != null ? rs.getDate("fecha_inicio").toLocalDate() : null)
                        .fechaFin(rs.getDate("fecha_fin") != null ? rs.getDate("fecha_fin").toLocalDate() : null)
                        .totalDias(rs.getObject("total_dias") != null ? rs.getInt("total_dias") : null)
                        .tipoEvento(rs.getString("tipo_evento"))
                        .numeroSoporte(rs.getString("numero_soporte"))
                        .responsablePago(rs.getString("responsable_pago"))
                        .porcentajeResponsable(rs.getObject("porcentaje_responsable") != null ? rs.getDouble("porcentaje_responsable") : null)
                        .porcentajeEmpresa(rs.getObject("porcentaje_empresa") != null ? rs.getDouble("porcentaje_empresa") : null)
                        .diasEmpresa100(rs.getObject("dias_empresa_100") != null ? rs.getInt("dias_empresa_100") : null)
                        .generaCxc(rs.getObject("genera_cxc") != null ? rs.getBoolean("genera_cxc") : null)
                        .liquidaArl(rs.getObject("liquida_arl") != null ? rs.getBoolean("liquida_arl") : null)
                        .esRemunerado(rs.getObject("es_remunerado") != null ? rs.getBoolean("es_remunerado") : null)
                        .observacion(rs.getString("observacion"))
                        .estado(rs.getString("estado"))
                        .build()
        );

        return rows.stream().findFirst();
    }

    public Long crear(EventoLiquidacionSaveDTO dto, Integer idUsuario) {
        String sql = """
            INSERT INTO nomina.eventos_liquidacion (
                id_contrato,
                fecha_documento,
                fecha_inicio,
                fecha_fin,
                total_dias,
                tipo_evento,
                numero_soporte,
                responsable_pago,
                porcentaje_responsable,
                porcentaje_empresa,
                dias_empresa_100,
                genera_cxc,
                liquida_arl,
                es_remunerado,
                observacion,
                estado,
                fk_seguridad_creacion,
                fecha_creacion,
                fk_seguridad_edicion,
                fecha_edicion
            ) VALUES (
                :idContrato,
                :fechaDocumento,
                :fechaInicio,
                :fechaFin,
                :totalDias,
                :tipoEvento,
                :numeroSoporte,
                :responsablePago,
                :porcentajeResponsable,
                :porcentajeEmpresa,
                :diasEmpresa100,
                :generaCxc,
                :liquidaArl,
                :esRemunerado,
                :observacion,
                :estado,
                :idUsuario,
                CURRENT_TIMESTAMP,
                :idUsuario,
                CURRENT_TIMESTAMP
            )
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idContrato", dto.getIdContrato())
                .addValue("fechaDocumento", dto.getFechaDocumento() != null ? Date.valueOf(dto.getFechaDocumento()) : null)
                .addValue("fechaInicio", dto.getFechaInicio() != null ? Date.valueOf(dto.getFechaInicio()) : null)
                .addValue("fechaFin", dto.getFechaFin() != null ? Date.valueOf(dto.getFechaFin()) : null)
                .addValue("totalDias", dto.getTotalDias())
                .addValue("tipoEvento", dto.getTipoEvento())
                .addValue("numeroSoporte", dto.getNumeroSoporte())
                .addValue("responsablePago", dto.getResponsablePago())
                .addValue("porcentajeResponsable", dto.getPorcentajeResponsable())
                .addValue("porcentajeEmpresa", dto.getPorcentajeEmpresa())
                .addValue("diasEmpresa100", dto.getDiasEmpresa100())
                .addValue("generaCxc", dto.getGeneraCxc())
                .addValue("liquidaArl", dto.getLiquidaArl())
                .addValue("esRemunerado", dto.getEsRemunerado())
                .addValue("observacion", dto.getObservacion())
                .addValue("estado", dto.getEstado())
                .addValue("idUsuario", idUsuario);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"id_evento_liquidacion"});

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public void actualizar(EventoLiquidacionSaveDTO dto, Integer idUsuario) {
        String sql = """
            UPDATE nomina.eventos_liquidacion
            SET
                id_contrato = :idContrato,
                fecha_documento = :fechaDocumento,
                fecha_inicio = :fechaInicio,
                fecha_fin = :fechaFin,
                total_dias = :totalDias,
                tipo_evento = :tipoEvento,
                numero_soporte = :numeroSoporte,
                responsable_pago = :responsablePago,
                porcentaje_responsable = :porcentajeResponsable,
                porcentaje_empresa = :porcentajeEmpresa,
                dias_empresa_100 = :diasEmpresa100,
                genera_cxc = :generaCxc,
                liquida_arl = :liquidaArl,
                es_remunerado = :esRemunerado,
                observacion = :observacion,
                estado = :estado,
                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_evento_liquidacion = :idEventoLiquidacion
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idEventoLiquidacion", dto.getIdEventoLiquidacion())
                .addValue("idContrato", dto.getIdContrato())
                .addValue("fechaDocumento", dto.getFechaDocumento() != null ? Date.valueOf(dto.getFechaDocumento()) : null)
                .addValue("fechaInicio", dto.getFechaInicio() != null ? Date.valueOf(dto.getFechaInicio()) : null)
                .addValue("fechaFin", dto.getFechaFin() != null ? Date.valueOf(dto.getFechaFin()) : null)
                .addValue("totalDias", dto.getTotalDias())
                .addValue("tipoEvento", dto.getTipoEvento())
                .addValue("numeroSoporte", dto.getNumeroSoporte())
                .addValue("responsablePago", dto.getResponsablePago())
                .addValue("porcentajeResponsable", dto.getPorcentajeResponsable())
                .addValue("porcentajeEmpresa", dto.getPorcentajeEmpresa())
                .addValue("diasEmpresa100", dto.getDiasEmpresa100())
                .addValue("generaCxc", dto.getGeneraCxc())
                .addValue("liquidaArl", dto.getLiquidaArl())
                .addValue("esRemunerado", dto.getEsRemunerado())
                .addValue("observacion", dto.getObservacion())
                .addValue("estado", dto.getEstado())
                .addValue("idUsuario", idUsuario);

        jdbc.update(sql, params);
    }

    public void cambiarEstado(Long idEventoLiquidacion, String estado, Integer idUsuario) {
        String sql = """
            UPDATE nomina.eventos_liquidacion
            SET
                estado = :estado,
                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_evento_liquidacion = :idEventoLiquidacion
            """;

        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("idEventoLiquidacion", idEventoLiquidacion)
                .addValue("estado", estado)
                .addValue("idUsuario", idUsuario));
    }

    public String obtenerEstadoPeriodoDelEvento(Long idEventoLiquidacion) {
        String sql = """
            SELECT pn.estado
            FROM nomina.eventos_liquidacion el
            JOIN nomina.empleado_contratos ec
                ON ec.id_contrato = el.id_contrato
            JOIN nomina.empleados e
                ON e.id_empleado = ec.id_empleado
            JOIN nomina.periodos_nomina pn
                ON pn.id_agencia = e.id_agencia
               AND el.fecha_inicio <= pn.fecha_fin
               AND el.fecha_fin >= pn.fecha_inicio
            WHERE el.id_evento_liquidacion = :idEventoLiquidacion
            ORDER BY pn.id_periodo ASC
            LIMIT 1
            """;

        List<String> rows = jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idEventoLiquidacion", idEventoLiquidacion),
                (rs, rowNum) -> rs.getString("estado")
        );

        return rows.isEmpty() ? null : rows.get(0);
    }

    public Integer obtenerPeriodoQueCruzaEvento(Long idEventoLiquidacion) {
        String sql = """
            SELECT pn.id_periodo
            FROM nomina.eventos_liquidacion el
            JOIN nomina.empleado_contratos ec
                ON ec.id_contrato = el.id_contrato
            JOIN nomina.empleados e
                ON e.id_empleado = ec.id_empleado
            JOIN nomina.periodos_nomina pn
                ON pn.id_agencia = e.id_agencia
               AND el.fecha_inicio <= pn.fecha_fin
               AND el.fecha_fin >= pn.fecha_inicio
            WHERE el.id_evento_liquidacion = :idEventoLiquidacion
            ORDER BY pn.id_periodo ASC
            LIMIT 1
            """;

        List<Integer> rows = jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idEventoLiquidacion", idEventoLiquidacion),
                (rs, rowNum) -> rs.getInt("id_periodo")
        );

        return rows.isEmpty() ? null : rows.get(0);
    }

    public Integer obtenerContratoPorEvento(Long idEventoLiquidacion) {
        String sql = """
            SELECT id_contrato
            FROM nomina.eventos_liquidacion
            WHERE id_evento_liquidacion = :idEventoLiquidacion
            """;

        List<Integer> rows = jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idEventoLiquidacion", idEventoLiquidacion),
                (rs, rowNum) -> rs.getInt("id_contrato")
        );

        return rows.isEmpty() ? null : rows.get(0);
    }

    public void eliminarNovedadesPreviewPorPeriodoYContrato(Integer idPeriodo, Integer idContrato) {
        String sql = """
        DELETE FROM nomina.novedades_nomina
        WHERE id_periodo = :idPeriodo
          AND id_contrato = :idContrato
          AND origen = 'CALCULO'
        """;

        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("idPeriodo", idPeriodo)
                .addValue("idContrato", idContrato));
    }

    public void eliminar(Long idEventoLiquidacion) {
        String sql = """
            DELETE FROM nomina.eventos_liquidacion
            WHERE id_evento_liquidacion = :idEventoLiquidacion
            """;

        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("idEventoLiquidacion", idEventoLiquidacion));
    }
}