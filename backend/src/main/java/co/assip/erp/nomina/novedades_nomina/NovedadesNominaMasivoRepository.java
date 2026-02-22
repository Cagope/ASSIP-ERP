package co.assip.erp.nomina.novedades_nomina;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class NovedadesNominaMasivoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // OBTENER PERÍODO (fechas y agencia)
    // =========================================================
    public Map<String, Object> obtenerPeriodo(Integer idPeriodo) {

        String sql = """
            SELECT
              p.id_periodo,
              p.id_agencia,
              p.fecha_inicio,
              p.fecha_fin,
              p.estado
            FROM nomina.periodos_nomina p
            WHERE p.id_periodo = :id
        """;

        return jdbc.queryForMap(sql, new MapSqlParameterSource("id", idPeriodo));
    }

    // =========================================================
    // CONTAR CONTRATOS ELEGIBLES
    // (tipo contrato 1 y 2)
    // =========================================================
    public int contarContratosElegibles(
            Integer idAgencia,
            LocalDate inicio,
            LocalDate fin
    ) {

        String sql = """
            SELECT COUNT(1)
            FROM nomina.empleado_contratos c
            JOIN nomina.empleados e
              ON e.id_empleado = c.id_empleado
            WHERE c.activo = TRUE
              AND e.activo = TRUE
              AND e.id_agencia = :idAgencia
              AND c.id_tipo_contrato IN (1,2)
              AND c.fecha_inicio <= :fin
              AND (c.fecha_fin IS NULL OR c.fecha_fin >= :inicio)
        """;

        var params = new MapSqlParameterSource()
                .addValue("idAgencia", idAgencia)
                .addValue("inicio", inicio)
                .addValue("fin", fin);

        Integer n = jdbc.queryForObject(sql, params, Integer.class);
        return n != null ? n : 0;
    }

    // =========================================================
    // INSERT MASIVO (APLICAR)
    // =========================================================
    public int insertarMasivo(
            Integer idPeriodo,
            Integer idAgencia,
            String codigoConcepto,
            LocalDate fechaInicial,
            LocalDate fechaFinal,
            BigDecimal cantidad,
            BigDecimal valorManual,
            String observacion,
            String estado,
            Integer idUsuario
    ) {

        String sql = """
        INSERT INTO nomina.novedades_nomina (
          id_periodo,
          id_empleado,
          codigo_concepto,
          fecha_inicial,
          fecha_final,
          cantidad,
          valor,
          observacion,
          fk_seguridad_creacion,
          fecha_creacion,
          fk_seguridad_edicion,
          fecha_edicion,
          id_contrato,
          estado,
          fk_agencia
        )
        SELECT
          :idPeriodo,
          e.id_empleado,
          :codigoConcepto,
          :fechaInicial,
          :fechaFinal,
          :cantidad,

          -- 🔥 CÁLCULO AUTOMÁTICO
          CASE
          --    AUXILIO DE TRANSPORTE (POR DÍAS, BASE AUX)
            WHEN cn.tipo_calculo = 'AUX_TRANSPORTE'
               AND c.salario_base < (v.smmlv * 2) THEN
            ROUND((v.aux_transporte / v.dias_mes) * :cantidad, 0)
            WHEN cn.tipo_calculo = 'POR_DIAS' THEN
              ROUND((c.salario_base / v.dias_mes) * :cantidad * cn.multiplicador, 0)

            WHEN cn.tipo_calculo = 'POR_HORAS' THEN
              ROUND((c.salario_base / v.horas_mes) * :cantidad * cn.multiplicador, 0)

            WHEN cn.tipo_calculo = 'POR_PORCENTAJE' THEN
              ROUND(c.salario_base * (cn.multiplicador / 100), 0)

            WHEN cn.tipo_calculo = 'MANUAL' THEN
              :valor

            ELSE
              0
          END,

          :observacion,
          :usr,
          CURRENT_TIMESTAMP,
          :usr,
          CURRENT_TIMESTAMP,
          c.id_contrato,
          :estado,
          e.id_agencia

        FROM nomina.empleado_contratos c
        JOIN nomina.empleados e
          ON e.id_empleado = c.id_empleado

        JOIN nomina.conceptos_nomina cn
          ON cn.codigo_concepto = :codigoConcepto
         AND cn.activo = TRUE

        JOIN nomina.periodos_nomina p
          ON p.id_periodo = :idPeriodo

        JOIN nomina.variables_vigencia v
          ON p.fecha_fin BETWEEN v.fecha_inicial AND v.fecha_final
         AND v.activo = TRUE

        WHERE c.activo = TRUE
          AND e.activo = TRUE
          AND e.id_agencia = :idAgencia
          AND c.id_tipo_contrato IN (1,2)

          -- 🔥 AUXILIO SOLO < 2 SMMLV
          AND (
            cn.tipo_calculo <> 'AUX_TRANSPORTE'
            OR c.salario_base < (v.smmlv * 2)
          )

          AND c.fecha_inicio <= p.fecha_fin
          AND (c.fecha_fin IS NULL OR c.fecha_fin >= p.fecha_inicio)

          AND NOT EXISTS (
            SELECT 1
            FROM nomina.novedades_nomina n
            WHERE n.id_periodo = :idPeriodo
              AND n.id_contrato = c.id_contrato
              AND n.codigo_concepto = :codigoConcepto
          )
        """;

        var params = new MapSqlParameterSource()
                .addValue("idPeriodo", idPeriodo)
                .addValue("idAgencia", idAgencia)
                .addValue("codigoConcepto", codigoConcepto)
                .addValue("fechaInicial", fechaInicial)
                .addValue("fechaFinal", fechaFinal)
                .addValue("cantidad", cantidad != null ? cantidad : BigDecimal.ZERO)
                .addValue("valor", valorManual != null ? valorManual : BigDecimal.ZERO)
                .addValue("observacion", observacion)
                .addValue("estado", estado)
                .addValue("usr", idUsuario != null ? idUsuario : 1);

        return jdbc.update(sql, params);
    }

    // =========================================================
    // PREVIEW MASIVO (NO GRABA)
    // =========================================================
    public List<Map<String, Object>> previewMasivo(
            Integer idPeriodo,
            Integer idAgencia,
            String codigoConcepto,
            LocalDate fechaInicial,
            LocalDate fechaFinal,
            BigDecimal cantidad,
            BigDecimal valorManual
    ) {

        String sql = """
        SELECT
          c.id_contrato,
          c.id_empleado,
          dp.documento,
          TRIM(
            COALESCE(dp.primer_apellido,'') || ' ' ||
            COALESCE(dp.segundo_apellido,'') || ' ' ||
            COALESCE(dp.nombres,'')
          ) AS nombre_empleado,

          :cantidad AS cantidad,
          c.salario_base,
          cn.tipo_calculo,

          -- 🔥 VALOR CALCULADO (MISMA LÓGICA DEL INSERT)
          CASE
          --    AUXILIO DE TRANSPORTE (POR DÍAS, BASE AUX)
            WHEN cn.tipo_calculo = 'AUX_TRANSPORTE'
               AND c.salario_base < (v.smmlv * 2) THEN
            ROUND((v.aux_transporte / v.dias_mes) * :cantidad, 0)
            WHEN cn.tipo_calculo = 'POR_DIAS' THEN
              ROUND((c.salario_base / v.dias_mes) * :cantidad * cn.multiplicador, 0)

            WHEN cn.tipo_calculo = 'POR_HORAS' THEN
              ROUND((c.salario_base / v.horas_mes) * :cantidad * cn.multiplicador, 0)

            WHEN cn.tipo_calculo = 'POR_PORCENTAJE' THEN
              ROUND(c.salario_base * (cn.multiplicador / 100), 0)

            WHEN cn.tipo_calculo = 'MANUAL' THEN
              :valor

            ELSE
              0
          END AS valor_calculado,

          EXISTS (
            SELECT 1
            FROM nomina.novedades_nomina n
            WHERE n.id_periodo = :idPeriodo
              AND n.id_contrato = c.id_contrato
              AND n.codigo_concepto = :codigoConcepto
          ) AS ya_existe

        FROM nomina.empleado_contratos c
        JOIN nomina.empleados e
          ON e.id_empleado = c.id_empleado

        JOIN hoja_vida.datos_personales dp
          ON dp.id_datos_personal = e.id_datos_personal

        JOIN nomina.conceptos_nomina cn
          ON cn.codigo_concepto = :codigoConcepto
         AND cn.activo = TRUE

        JOIN nomina.periodos_nomina p
          ON p.id_periodo = :idPeriodo

        JOIN nomina.variables_vigencia v
          ON p.fecha_fin BETWEEN v.fecha_inicial AND v.fecha_final
         AND v.activo = TRUE

        WHERE c.activo = TRUE
          AND e.activo = TRUE
          AND e.id_agencia = :idAgencia
          AND c.id_tipo_contrato IN (1,2)

          -- 🔥 AUXILIO SOLO < 2 SMMLV
          AND (
            cn.tipo_calculo <> 'AUX_TRANSPORTE'
            OR c.salario_base < (v.smmlv * 2)
          )

          AND c.fecha_inicio <= p.fecha_fin
          AND (c.fecha_fin IS NULL OR c.fecha_fin >= p.fecha_inicio)

        ORDER BY dp.documento
        """;

        var params = new MapSqlParameterSource()
                .addValue("idPeriodo", idPeriodo)
                .addValue("idAgencia", idAgencia)
                .addValue("codigoConcepto", codigoConcepto)
                .addValue("fechaInicial", fechaInicial)
                .addValue("fechaFinal", fechaFinal)
                .addValue("cantidad", cantidad != null ? cantidad : BigDecimal.ZERO)
                .addValue("valor", valorManual != null ? valorManual : BigDecimal.ZERO);

        return jdbc.queryForList(sql, params);
    }
}
