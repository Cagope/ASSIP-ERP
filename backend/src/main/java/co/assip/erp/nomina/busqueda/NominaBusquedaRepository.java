package co.assip.erp.nomina.busqueda;

import co.assip.erp.nomina.busqueda.dto.EmpleadoBusquedaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NominaBusquedaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =====================================================
    // 🔍 BUSCAR EMPLEADOS NÓMINA
    // =====================================================
    public List<EmpleadoBusquedaDTO> buscarEmpleados(String q, int limit) {

        String sql = """
            SELECT
                e.id_empleado,
                dp.id_datos_personal,
                dp.documento,

                TRIM(
                    COALESCE(dp.primer_apellido,'') || ' ' ||
                    COALESCE(dp.segundo_apellido,'') || ' ' ||
                    COALESCE(dp.nombres,'')
                ) AS nombre_completo,

                a.id_agencia,
                a.nombre_agencia,

                c.id_contrato AS id_contrato_activo,
                cg.nombre_cargo,

                e.activo

            FROM nomina.empleados e
            JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal = e.id_datos_personal

            LEFT JOIN general.datos_agencias a
                ON a.id_agencia = e.id_agencia

            LEFT JOIN nomina.empleado_contratos c
                ON c.id_empleado = e.id_empleado
                AND c.activo = true

            LEFT JOIN nomina.cargos cg
                ON cg.id_cargo = c.id_cargo

            WHERE
                e.activo = true
                AND (
                    :q IS NULL OR
                    dp.documento ILIKE :qLike OR
                    dp.nombres ILIKE :qLike OR
                    dp.primer_apellido ILIKE :qLike OR
                    dp.segundo_apellido ILIKE :qLike
                )

            ORDER BY dp.documento
            LIMIT :limit
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("q", q)
                .addValue("qLike", q == null ? null : "%" + q.trim() + "%")
                .addValue("limit", Math.min(limit, 50));

        return jdbc.query(sql, params, (rs, i) ->
                EmpleadoBusquedaDTO.builder()
                        .idEmpleado(rs.getInt("id_empleado"))
                        .idDatosPersonal(rs.getLong("id_datos_personal"))
                        .documento(rs.getString("documento"))
                        .nombreCompleto(rs.getString("nombre_completo"))
                        .idAgencia(rs.getInt("id_agencia"))
                        .nombreAgencia(rs.getString("nombre_agencia"))
                        .idContratoActivo(rs.getObject("id_contrato_activo", Integer.class))
                        .nombreCargo(rs.getString("nombre_cargo"))
                        .activo(rs.getBoolean("activo"))
                        .build()
        );
    }

    // =====================================================
    // 📄 OBTENER POR ID
    // =====================================================
    public EmpleadoBusquedaDTO obtenerPorId(Integer idEmpleado) {

        String sql = """
        SELECT
            e.id_empleado,
            dp.id_datos_personal,
            dp.documento,

            TRIM(
                COALESCE(dp.primer_apellido,'') || ' ' ||
                COALESCE(dp.segundo_apellido,'') || ' ' ||
                COALESCE(dp.nombres,'')
            ) AS nombre_completo,

            a.id_agencia,
            a.nombre_agencia,

            c.id_contrato AS id_contrato_activo,
            cg.nombre_cargo,

            e.activo

        FROM nomina.empleados e
        JOIN hoja_vida.datos_personales dp
            ON dp.id_datos_personal = e.id_datos_personal

        LEFT JOIN general.datos_agencias a
            ON a.id_agencia = e.id_agencia

        LEFT JOIN nomina.empleado_contratos c
            ON c.id_empleado = e.id_empleado
            AND c.activo = true

        LEFT JOIN nomina.cargos cg
            ON cg.id_cargo = c.id_cargo

        WHERE e.id_empleado = :idEmpleado
    """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource("idEmpleado", idEmpleado),
                rs -> {
                    if (!rs.next()) return null;

                    return EmpleadoBusquedaDTO.builder()
                            .idEmpleado(rs.getInt("id_empleado"))
                            .idDatosPersonal(rs.getLong("id_datos_personal"))
                            .documento(rs.getString("documento"))
                            .nombreCompleto(rs.getString("nombre_completo"))
                            .idAgencia(rs.getInt("id_agencia"))
                            .nombreAgencia(rs.getString("nombre_agencia"))
                            .idContratoActivo(rs.getObject("id_contrato_activo", Integer.class)) // 🔥 CORREGIDO
                            .nombreCargo(rs.getString("nombre_cargo"))
                            .activo(rs.getBoolean("activo"))
                            .build();
                }
        );

    }

}
