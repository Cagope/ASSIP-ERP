package co.assip.erp.nomina.novedades_nomina;

import co.assip.erp.nomina.novedades_nomina.dto.NovedadNominaFormDTO;
import co.assip.erp.nomina.novedades_nomina.dto.NovedadNominaListDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NovedadesNominaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // LISTAR
    // =========================================================
    public List<NovedadNominaListDTO> listar(Integer idPeriodo, Integer idEmpleado) {

        StringBuilder sql = new StringBuilder("""
        SELECT
          n.id_novedad,
          n.id_periodo,
          n.id_empleado,
          n.id_contrato,
          n.codigo_concepto,
          n.fecha_inicial,
          n.fecha_final,
          n.cantidad,
          n.valor,
          n.estado,
          n.observacion,
          n.fk_agencia,

          dp.documento,
          dp.primer_apellido,
          dp.segundo_apellido,
          dp.nombres,

          p.anio,
          p.mes,
          p.numero_periodo,
          p.tipo_periodo

        FROM nomina.novedades_nomina n
        JOIN nomina.empleado_contratos c
          ON n.id_contrato = c.id_contrato
        JOIN nomina.empleados e
          ON c.id_empleado = e.id_empleado
        JOIN hoja_vida.datos_personales dp
          ON e.id_datos_personal = dp.id_datos_personal
        JOIN nomina.periodos_nomina p
          ON n.id_periodo = p.id_periodo
        WHERE 1=1
    """);

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (idPeriodo != null) {
            sql.append(" AND n.id_periodo = :periodo");
            params.addValue("periodo", idPeriodo);
        }

        if (idEmpleado != null) {
            sql.append(" AND n.id_empleado = :empleado");
            params.addValue("empleado", idEmpleado);
        }

        sql.append(" ORDER BY n.id_novedad DESC");

        return jdbc.query(sql.toString(), params, (rs, rowNum) ->
                NovedadNominaListDTO.builder()
                        .idNovedad(rs.getInt("id_novedad"))
                        .idPeriodo(rs.getInt("id_periodo"))
                        .idEmpleado(rs.getInt("id_empleado"))
                        .idContrato((Integer) rs.getObject("id_contrato"))
                        .codigoConcepto(rs.getString("codigo_concepto"))
                        .fechaInicial(rs.getDate("fecha_inicial").toLocalDate())
                        .fechaFinal(rs.getDate("fecha_final").toLocalDate())
                        .cantidad(rs.getBigDecimal("cantidad"))
                        .valor(rs.getBigDecimal("valor"))
                        .estado(rs.getString("estado"))
                        .observacion(rs.getString("observacion"))
                        .fkAgencia((Integer) rs.getObject("fk_agencia"))

                        // 🔥 EMPLEADO
                        .documentoEmpleado(rs.getString("documento"))
                        .nombreEmpleado(
                                rs.getString("primer_apellido") + " " +
                                        (rs.getString("segundo_apellido") != null
                                                ? rs.getString("segundo_apellido") + " "
                                                : "") +
                                        rs.getString("nombres")
                        )

                        // 🔥 PERÍODO
                        .anio(rs.getInt("anio"))
                        .mes(rs.getInt("mes"))
                        .numeroPeriodo(rs.getInt("numero_periodo"))
                        .tipoPeriodo(rs.getString("tipo_periodo"))

                        .build()
        );
    }



    // =========================================================
    // OBTENER POR ID
    // =========================================================
    public NovedadNominaFormDTO obtener(Integer id) {

        String sql = """
            SELECT *
            FROM nomina.novedades_nomina
            WHERE id_novedad = :id
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("id", id),
                (rs, rowNum) -> NovedadNominaFormDTO.builder()
                        .idPeriodo(rs.getInt("id_periodo"))
                        .idEmpleado(rs.getInt("id_empleado"))
                        .idContrato((Integer) rs.getObject("id_contrato"))
                        .codigoConcepto(rs.getString("codigo_concepto"))
                        .fechaInicial(rs.getDate("fecha_inicial").toLocalDate())
                        .fechaFinal(rs.getDate("fecha_final").toLocalDate())
                        .cantidad(rs.getBigDecimal("cantidad"))
                        .valor(rs.getBigDecimal("valor"))
                        .observacion(rs.getString("observacion"))
                        .fkAgencia((Integer) rs.getObject("fk_agencia"))
                        .build()
        );
    }

    // =========================================================
    // CREAR
    // =========================================================
    public Integer crear(NovedadNominaFormDTO dto, Integer idUsuario) {

        String sql = """
    INSERT INTO nomina.novedades_nomina (
      id_periodo,
      id_empleado,
      id_contrato,
      codigo_concepto,
      fecha_inicial,
      fecha_final,
      cantidad,
      valor,
      observacion,
      estado,
      fk_agencia,
      origen,
      fk_seguridad_creacion,
      fk_seguridad_edicion
    )
    SELECT
      :periodo,
      e.id_empleado,
      :contrato,
      :concepto,
      :inicio,
      :fin,
      :cantidad,
      :valor,
      
      :obs,
      'ABIERTO',
      e.id_agencia,
      'INDIVIDUAL'
      :usr,
      :usr
    FROM nomina.empleados e
    WHERE e.id_empleado = :empleado
    RETURNING id_novedad
    """;

        var params = new MapSqlParameterSource()
                .addValue("periodo", dto.getIdPeriodo())
                .addValue("empleado", dto.getIdEmpleado())
                .addValue("contrato", dto.getIdContrato())
                .addValue("concepto", dto.getCodigoConcepto())
                .addValue("inicio", dto.getFechaInicial())
                .addValue("fin", dto.getFechaFinal())
                .addValue("cantidad", dto.getCantidad())
                .addValue("valor", dto.getValor())
                .addValue("obs", dto.getObservacion())
                // ⛔ NO fk_agencia desde el DTO
                .addValue("usr", idUsuario != null ? idUsuario : 1);

        return jdbc.queryForObject(sql, params, Integer.class);
    }

    // =========================================================
    // ACTUALIZAR (solo ABIERTO)
    // =========================================================
    public void actualizar(Integer id, NovedadNominaFormDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE nomina.novedades_nomina
            SET
              id_periodo = :periodo,
              id_empleado = :empleado,
              id_contrato = :contrato,
              codigo_concepto = :concepto,
              fecha_inicial = :inicio,
              fecha_final = :fin,
              cantidad = :cantidad,
              valor = :valor,
              observacion = :obs,
              fk_agencia = :agencia,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_novedad = :id
              AND estado = 'ABIERTO'
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("periodo", dto.getIdPeriodo())
                .addValue("empleado", dto.getIdEmpleado())
                .addValue("contrato", dto.getIdContrato())
                .addValue("concepto", dto.getCodigoConcepto())
                .addValue("inicio", dto.getFechaInicial())
                .addValue("fin", dto.getFechaFinal())
                .addValue("cantidad", dto.getCantidad())
                .addValue("valor", dto.getValor())
                .addValue("obs", dto.getObservacion())
                .addValue("agencia", dto.getFkAgencia())
                .addValue("usr", idUsuario != null ? idUsuario : 1);

        jdbc.update(sql, params);
    }

    // =========================================================
    // ELIMINAR (solo ABIERTO)
    // =========================================================
    public void eliminar(Integer id) {

        String sql = """
            DELETE FROM nomina.novedades_nomina
            WHERE id_novedad = :id
              AND estado = 'ABIERTO'
        """;

        jdbc.update(sql, new MapSqlParameterSource("id", id));
    }

    // =========================================================
    // OBTENER ESTADO PERÍODO
    // =========================================================
    public String obtenerEstadoPeriodo(Integer idPeriodo) {

        String sql = """
            SELECT estado
            FROM nomina.periodos_nomina
            WHERE id_periodo = :id
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("id", idPeriodo),
                String.class
        );
    }

    // =========================================================
    // OBTENER PERIODO DESDE NOVEDAD
    // =========================================================
    public Integer obtenerPeriodoDeNovedad(Integer idNovedad) {

        String sql = """
            SELECT id_periodo
            FROM nomina.novedades_nomina
            WHERE id_novedad = :id
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("id", idNovedad),
                Integer.class
        );
    }

    // =========================================================
    // OBTENER ESTADO NOVEDAD
    // =========================================================
    public String obtenerEstadoNovedad(Integer idNovedad) {

        String sql = """
            SELECT estado
            FROM nomina.novedades_nomina
            WHERE id_novedad = :id
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("id", idNovedad),
                String.class
        );
    }

    // =========================================================
    // VALIDAR CONTRATO VIGENTE EN PERÍODO
    // =========================================================
    public boolean contratoVigenteEnPeriodo(Integer idContrato, Integer idPeriodo) {

        String sql = """
        SELECT COUNT(1)
        FROM nomina.empleado_contratos c
        JOIN nomina.periodos_nomina p
          ON p.id_periodo = :idPeriodo
        WHERE c.id_contrato = :idContrato
          AND c.activo = true
          AND c.fecha_inicio <= p.fecha_fin
          AND (c.fecha_fin IS NULL OR c.fecha_fin >= p.fecha_inicio)
        """;

        Integer count = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idContrato", idContrato)
                        .addValue("idPeriodo", idPeriodo),
                Integer.class
        );

        return count != null && count > 0;
    }

    // =========================================================
    // CAMBIAR ESTADO NOVEDAD
    // =========================================================
    public void cambiarEstado(Integer idNovedad, String estado, Integer idUsuario) {

        String sql = """
            UPDATE nomina.novedades_nomina
            SET
              estado = :estado,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_novedad = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idNovedad)
                .addValue("estado", estado)
                .addValue("usr", idUsuario != null ? idUsuario : 1);

        jdbc.update(sql, params);
    }

    public List<Integer> obtenerContratosVigentes(Integer idPeriodo) {

        String sql = """
        SELECT c.id_contrato
        FROM nomina.empleado_contratos c
        JOIN nomina.periodos_nomina p
          ON p.id_periodo = :idPeriodo
        WHERE c.activo = true
          AND c.fecha_inicio <= p.fecha_fin
          AND (c.fecha_fin IS NULL OR c.fecha_fin >= p.fecha_inicio)
    """;

        return jdbc.queryForList(
                sql,
                new MapSqlParameterSource("idPeriodo", idPeriodo),
                Integer.class
        );
    }

    public boolean existeNovedad(Integer idPeriodo, Integer idContrato, String codigoConcepto) {

        String sql = """
        SELECT COUNT(1)
        FROM nomina.novedades_nomina
        WHERE id_periodo = :periodo
          AND id_contrato = :contrato
          AND codigo_concepto = :concepto
    """;

        Integer count = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("periodo", idPeriodo)
                        .addValue("contrato", idContrato)
                        .addValue("concepto", codigoConcepto),
                Integer.class
        );

        return count != null && count > 0;
    }

    // =========================================================
// 🔁 MARCAR NOVEDADES ABIERTO COMO APLICADAS
// =========================================================
    public void marcarNovedadesAplicadas(
            Integer idPeriodo,
            Integer idContrato,
            Integer idUsuario
    ) {

        String sql = """
        UPDATE nomina.novedades_nomina
        SET
          estado = 'CERRADO',
          fk_seguridad_edicion = :usr,
          fecha_edicion = CURRENT_TIMESTAMP
        WHERE id_periodo = :periodo
          AND id_contrato = :contrato
          AND estado = 'ABIERTO'
    """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("periodo", idPeriodo)
                        .addValue("contrato", idContrato)
                        .addValue("usr", idUsuario != null ? idUsuario : 1)
        );
    }

    // =========================================================
    // ➕ INSERTAR NOVEDAD AUTOMÁTICA (APLICADA)
    // =========================================================
    public void insertarNovedadAplicada(
            Integer idPeriodo,
            Integer idEmpleado,
            Integer idContrato,
            String codigoConcepto,
            java.time.LocalDate fechaInicial,
            java.time.LocalDate fechaFinal,
            java.math.BigDecimal cantidad,
            java.math.BigDecimal valor,
            Integer idUsuario
    ) {

        String sql = """
        INSERT INTO nomina.novedades_nomina (
          id_periodo,
          id_empleado,
          id_contrato,
          codigo_concepto,
          fecha_inicial,
          fecha_final,
          cantidad,
          valor,
          observacion,
          estado,
          fk_agencia,
          fk_seguridad_creacion,
          fk_seguridad_edicion,
          fecha_creacion,
          fecha_edicion,
          origen
        )
        SELECT
          :periodo,
          e.id_empleado,
          :contrato,
          :concepto,
          :inicio,
          :fin,
          :cantidad,
          :valor,
          'Generado automáticamente por proceso de liquidación',
          'CERRADO',
          e.id_agencia,
          :usr,
          :usr,
          CURRENT_TIMESTAMP,
          CURRENT_TIMESTAMP,
          'CALCULO'
        FROM nomina.empleados e
        WHERE e.id_empleado = :empleado
    """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("periodo", idPeriodo)
                        .addValue("empleado", idEmpleado)
                        .addValue("contrato", idContrato)
                        .addValue("concepto", codigoConcepto)
                        .addValue("inicio", fechaInicial)
                        .addValue("fin", fechaFinal)
                        .addValue("cantidad", cantidad)
                        .addValue("valor", valor)
                        .addValue("usr", idUsuario != null ? idUsuario : 1)
        );
    }

    public int eliminarNovedadesCalculoPorPeriodo(Integer idPeriodo) {

        String sql = """
        DELETE FROM nomina.novedades_nomina
        WHERE id_periodo = :periodo
          AND origen = 'CALCULO'
    """;

        return jdbc.update(
                sql,
                new MapSqlParameterSource("periodo", idPeriodo)
        );
    }

    // =========================================================
// 🧹 ELIMINAR NOVEDADES DE CÁLCULO AL ABRIR PERÍODO
// =========================================================
    public int eliminarNovedadesCalculoPorPeriodo(
            Integer idPeriodo,
            Integer idUsuario
    ) {

        String sql = """
        DELETE FROM nomina.novedades_nomina
        WHERE id_periodo = :periodo
          AND origen = 'CALCULO'
    """;

        return jdbc.update(
                sql,
                new MapSqlParameterSource("periodo", idPeriodo)
        );
    }

    // =========================================================
    // CAMBIAR ESTADO DE TODAS LAS NOVEDADES POR PERÍODO
    // =========================================================
    public void actualizarEstadoPorPeriodo(
            Integer idPeriodo,
            String estado,
            Integer idUsuario
    ) {

        String sql = """
        UPDATE nomina.novedades_nomina
        SET
          estado = :estado,
          fk_seguridad_edicion = :usr,
          fecha_edicion = CURRENT_TIMESTAMP
        WHERE id_periodo = :periodo
    """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("periodo", idPeriodo)
                        .addValue("estado", estado)
                        .addValue("usr", idUsuario != null ? idUsuario : 1)
        );
    }


}
