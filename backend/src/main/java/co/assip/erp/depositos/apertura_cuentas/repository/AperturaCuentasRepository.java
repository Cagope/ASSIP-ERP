package co.assip.erp.depositos.apertura_cuentas.repository;

import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentaItemDTO;
import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentasEntradaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AperturaCuentasRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
    // 🔹 1. Validar existencia de cuenta de aportes
    // ============================================================
    public boolean tieneAportesActivos(Integer idDatosPersonal) {
        String sql = """
            SELECT COUNT(*)
            FROM depositos.cuentas_ahorro ca
            JOIN depositos.formas_ahorro fa ON fa.id_forma_ahorro = ca.id_forma_ahorro
            WHERE ca.id_datos_personal = :id
              AND fa.codigo_forma = '01'
              AND ca.saldo_actual_cuenta > 0
        """;

        Integer count = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource().addValue("id", idDatosPersonal),
                Integer.class
        );

        return count != null && count > 0;
    }

    // ============================================================
    // 🔹 2. Cuenta internas con saldo — agencia actual
    // ============================================================
    public int contarCuentasInternas(Integer idDatosPersonal, List<Integer> agenciasUsuario) {

        if (agenciasUsuario == null || agenciasUsuario.isEmpty()) {
            return 0;
        }

        String sql = """
            SELECT COUNT(*)
            FROM depositos.cuentas_ahorro
            WHERE id_datos_personal = :id
              AND saldo_actual_cuenta > 0
              AND id_agencia IN (:agenciasUsuario)
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", idDatosPersonal)
                        .addValue("agenciasUsuario", agenciasUsuario),
                Integer.class
        );
    }

    // ============================================================
    // 🔹 3. Cuentas externas con saldo — fuera de la agencia operativa
    // ============================================================
    public int contarCuentasExternas(Integer idDatosPersonal, List<Integer> agenciasUsuario) {

        if (agenciasUsuario == null || agenciasUsuario.isEmpty()) {
            return 0;
        }

        String sql = """
            SELECT COUNT(*)
            FROM depositos.cuentas_ahorro
            WHERE id_datos_personal = :id
              AND saldo_actual_cuenta > 0
              AND id_agencia NOT IN (:agenciasUsuario)
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", idDatosPersonal)
                        .addValue("agenciasUsuario", agenciasUsuario),
                Integer.class
        );
    }


    // ============================================================
    // 🔹 4. Listar formas habilitadas (con consecutivo)
    // ============================================================
    public List<AperturaCuentaItemDTO> listarFormasDisponibles(Integer idDatosPersonal, Integer idAgenciaUsuario) {

        String sql = """
        SELECT
            fa.id_forma_ahorro,
            fa.codigo_forma,
            fa.nombre_forma,
            fa.id_agencia,
            (fa.codigo_forma = '01')::boolean AS obligatoria,
            true::boolean AS permite_apoderado,
            (fa.codigo_forma <> '01')::boolean AS permite_gmf,
            (fa.codigo_forma <> '01')::boolean AS permite_retencion,
            CAST('' AS varchar) AS observacion,
            COALESCE(fa.consecutivo_forma,0) + 1 AS consecutivo
        FROM depositos.formas_ahorro fa
        WHERE fa.id_agencia = COALESCE(:idAgencia, fa.id_agencia)
        ORDER BY fa.codigo_forma
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource().addValue("idAgencia", idAgenciaUsuario),
                (rs, row) -> {
                    AperturaCuentaItemDTO dto = new AperturaCuentaItemDTO();
                    dto.setIdFormaAhorro(rs.getInt("id_forma_ahorro"));
                    dto.setCodigoForma(rs.getString("codigo_forma"));
                    dto.setNombreForma(rs.getString("nombre_forma"));
                    dto.setIdAgencia(rs.getInt("id_agencia"));
                    dto.setObligatoria(rs.getBoolean("obligatoria"));
                    dto.setPermiteApoderado(rs.getBoolean("permite_apoderado"));
                    dto.setPermiteGmf(rs.getBoolean("permite_gmf"));
                    dto.setPermiteRetencion(rs.getBoolean("permite_retencion"));
                    dto.setObservacion(rs.getString("observacion"));
                    dto.setConsecutivo(rs.getInt("consecutivo"));
                    return dto;
                }
        );

    }


    // ============================================================
    // 🔹 5. Consecutivo real
    // ============================================================
    public Integer obtenerConsecutivo(Integer idForma) {
        return jdbc.queryForObject(
                """
                SELECT COALESCE(consecutivo_forma,0) + 1
                FROM depositos.formas_ahorro
                WHERE id_forma_ahorro = :id
                """,
                new MapSqlParameterSource().addValue("id", idForma),
                Integer.class
        );
    }

    public void actualizarConsecutivo(Integer idForma) {
        jdbc.update("""
            UPDATE depositos.formas_ahorro
            SET consecutivo_forma = COALESCE(consecutivo_forma,0) + 1
            WHERE id_forma_ahorro = :id
        """, new MapSqlParameterSource().addValue("id", idForma));
    }


    // ============================================================
    // 🔹 6. Código forma
    // ============================================================
    public String obtenerCodigoForma(Integer idForma) {
        return jdbc.queryForObject(
                """
                SELECT codigo_forma
                FROM depositos.formas_ahorro
                WHERE id_forma_ahorro = :id
                LIMIT 1
                """,
                new MapSqlParameterSource().addValue("id", idForma),
                String.class
        );
    }

    // ============================================================
    // 🔹 7. Crear cuenta
    // ============================================================
    public Integer crearCuenta(
            Integer idForma,
            Integer idDatosPersonal,
            Integer idAgenciaUsuario,
            String codigoCuenta,
            String gmf,
            Boolean retencion,
            Integer usuarioId
    ) {
        String sql = """
            INSERT INTO depositos.cuentas_ahorro (
                id_agencia,
                id_forma_ahorro,
                codigo_cuenta,
                id_datos_personal,
                estado_cuenta_cuenta,
                gmf_cuenta_cuenta,
                retencion_fuente_cuenta,
                plazo_cuenta,
                cuota_mensual_cuenta,
                cuenta_conjunta,
                accion_conjunta,
                tasa,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            VALUES (
                :idAgencia,
                :idForma,
                :codigo,
                :idPer,
                'A',
                :gmf,
                :retencion,
                0,
                0,
                'N',
                'N',
                0,
                :usuario,
                :usuario
            )
            RETURNING id_cuenta_ahorro
        """;

        var params = new MapSqlParameterSource()
                .addValue("idAgencia", idAgenciaUsuario)
                .addValue("idForma", idForma)
                .addValue("codigo", codigoCuenta)
                .addValue("idPer", idDatosPersonal)
                .addValue("gmf", gmf)
                .addValue("retencion", retencion)
                .addValue("usuario", usuarioId);

        return jdbc.queryForObject(sql, params, Integer.class);
    }

// 🔹 8. Guardar un apoderado (reutilizable para aportes y ahorro)
// ============================================================
    public void guardarApoderadoBasico(
            Integer idCuenta,
            String documento,
            String nombre,
            String telefono,
            String celular,
            Integer usuarioId
    ) {

        if (documento == null || documento.isBlank()) {
            return;
        }

        jdbc.update("""
        INSERT INTO depositos.poderes_cuentas_ahorro (
            id_cuenta,
            documento_poder,
            nombre_poder,
            telefono_poder,
            celular_poder,
            fk_seguridad_creacion,
            fk_seguridad_edicion
        ) VALUES (
            :idCuenta,
            :doc,
            :nom,
            :tel,
            :cel,
            :user,
            :user
        )
    """, new MapSqlParameterSource()
                .addValue("idCuenta", idCuenta)
                .addValue("doc", documento)
                .addValue("nom", nombre)
                .addValue("tel", telefono)
                .addValue("cel", celular)
                .addValue("user", usuarioId)
        );
    }

    // ============================================================
    // 🔹 9. Cuenta global: ¿tiene saldos en cualquier agencia?
    // ============================================================
    public int contarCuentasConSaldo(Integer idDatosPersonal) {
        String sql = """
        SELECT COUNT(*)
        FROM depositos.cuentas_ahorro
        WHERE id_datos_personal = :id
          AND saldo_actual_cuenta > 0
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource().addValue("id", idDatosPersonal),
                Integer.class
        );
    }


    // ============================================================
    // 🔹 10. Formas opcionales válidas (02–03–05–06)
    // ============================================================
    public List<AperturaCuentaItemDTO> listarFormasAhorroValidas(Integer idAgenciaUsuario) {

        if (idAgenciaUsuario == null) {
            return Collections.emptyList();
        }

        String sql = """
        SELECT
            fa.id_forma_ahorro,
            fa.codigo_forma,
            fa.nombre_forma,
            fa.id_agencia,
            false AS obligatoria,
            true AS permite_apoderado,
            true AS permite_gmf,
            true AS permite_retencion,
            '' AS observacion,
            COALESCE(fa.consecutivo_forma,0) + 1 AS consecutivo
        FROM depositos.formas_ahorro fa
        WHERE fa.id_agencia = :age
          AND fa.codigo_forma IN ('02','03','05','06')
        ORDER BY fa.codigo_forma
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource().addValue("age", idAgenciaUsuario),
                (rs, row) -> {
                    AperturaCuentaItemDTO dto = new AperturaCuentaItemDTO();
                    dto.setIdFormaAhorro(rs.getInt("id_forma_ahorro"));
                    dto.setCodigoForma(rs.getString("codigo_forma"));
                    dto.setNombreForma(rs.getString("nombre_forma"));
                    dto.setIdAgencia(rs.getInt("id_agencia"));
                    dto.setObligatoria(rs.getBoolean("obligatoria"));
                    dto.setPermiteApoderado(rs.getBoolean("permite_apoderado"));
                    dto.setPermiteGmf(rs.getBoolean("permite_gmf"));
                    dto.setPermiteRetencion(rs.getBoolean("permite_retencion"));
                    dto.setObservacion(rs.getString("observacion"));
                    dto.setConsecutivo(rs.getInt("consecutivo"));
                    return dto;
                }
        );
    }

}
