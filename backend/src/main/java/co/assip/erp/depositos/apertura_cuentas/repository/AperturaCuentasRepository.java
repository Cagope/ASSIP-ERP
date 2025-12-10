package co.assip.erp.depositos.apertura_cuentas.repository;

import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentaItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AperturaCuentasRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
    // 🔹 1. Validar existencia de aportes activos
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
    // 🔹 2. Cuentas internas con saldo (agencias usuario) – POR SI LUEGO LO USAS
    // ============================================================
    public int contarCuentasInternas(Integer idDatosPersonal, List<Integer> agenciasUsuario) {

        if (agenciasUsuario == null || agenciasUsuario.isEmpty()) return 0;

        String sql = """
            SELECT COUNT(*)
            FROM depositos.cuentas_ahorro
            WHERE id_datos_personal = :id
              AND saldo_actual_cuenta > 0
              AND id_agencia IN (:agencias)
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", idDatosPersonal)
                        .addValue("agencias", agenciasUsuario),
                Integer.class
        );
    }

    // ============================================================
    // 🔹 3. Cuentas externas con saldo (fuera agencias usuario) – POR SI LUEGO LO USAS
    // ============================================================
    public int contarCuentasExternas(Integer idDatosPersonal, List<Integer> agenciasUsuario) {

        if (agenciasUsuario == null || agenciasUsuario.isEmpty()) return 0;

        String sql = """
            SELECT COUNT(*)
            FROM depositos.cuentas_ahorro
            WHERE id_datos_personal = :id
              AND saldo_actual_cuenta > 0
              AND id_agencia NOT IN (:agencias)
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", idDatosPersonal)
                        .addValue("agencias", agenciasUsuario),
                Integer.class
        );
    }

    // ============================================================
    // 🔹 4. Listar formas habilitadas
    //    - Si idAgenciaUsuario != null → filtra por agencia (para consecutivos).
    //    - Si idAgenciaUsuario == null → trae TODAS (para no dañar validación).
    // ============================================================
    public List<AperturaCuentaItemDTO> listarFormasDisponibles(Integer idDatosPersonal, Integer idAgenciaUsuario) {

        String sql = """
        SELECT
            fa.id_forma_ahorro,
            fa.codigo_forma,
            fa.nombre_forma,
            fa.id_agencia,
            (fa.codigo_forma = '01')::boolean AS obligatoria,
            true AS permite_apoderado,
            (fa.codigo_forma <> '01')::boolean AS permite_gmf,
            (fa.codigo_forma <> '01')::boolean AS permite_retencion,
            '' AS observacion,
            (
                SELECT COALESCE(f2.consecutivo_forma,0) + 1
                FROM depositos.formas_ahorro f2
                WHERE f2.id_forma_ahorro = fa.id_forma_ahorro
                  AND f2.id_agencia = :idAgencia
                LIMIT 1
            ) AS consecutivo
        FROM depositos.formas_ahorro fa
        ORDER BY fa.codigo_forma
    """;

        var params = new MapSqlParameterSource()
                .addValue("idAgencia", idAgenciaUsuario);

        return jdbc.query(sql, params, (rs, row) -> {
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
        });
    }


    // ============================================================
    // 🔹 5. Incrementar consecutivo por agencia (para guardar)
    // ============================================================
    public Integer incrementarYObtenerConsecutivo(Integer idForma, Integer idAgencia) {

        return jdbc.queryForObject("""
            UPDATE depositos.formas_ahorro
            SET consecutivo_forma = COALESCE(consecutivo_forma,0) + 1
            WHERE id_forma_ahorro = :forma
              AND id_agencia      = :agencia
            RETURNING consecutivo_forma
        """,
                new MapSqlParameterSource()
                        .addValue("forma", idForma)
                        .addValue("agencia", idAgencia),
                Integer.class);
    }

    // ============================================================
    // 🔹 6. Obtener código forma
    // ============================================================
    public String obtenerCodigoForma(Integer idForma) {

        return jdbc.queryForObject("""
            SELECT codigo_forma
            FROM depositos.formas_ahorro
            WHERE id_forma_ahorro = :id
        """,
                new MapSqlParameterSource().addValue("id", idForma),
                String.class);
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
                id_agencia, id_forma_ahorro, codigo_cuenta,
                id_datos_personal, estado_cuenta_cuenta,
                gmf_cuenta_cuenta, retencion_fuente_cuenta,
                plazo_cuenta, cuota_mensual_cuenta,
                cuenta_conjunta, accion_conjunta, tasa,
                fk_seguridad_creacion, fk_seguridad_edicion
            )
            VALUES (
                :agencia, :forma, :codigo,
                :idPer, 'A',
                :gmf, :retencion,
                0, 0,
                'N', 'N', 0,
                :user, :user
            )
            RETURNING id_cuenta_ahorro
        """;

        var params = new MapSqlParameterSource()
                .addValue("agencia", idAgenciaUsuario)
                .addValue("forma", idForma)
                .addValue("codigo", codigoCuenta)
                .addValue("idPer", idDatosPersonal)
                .addValue("gmf", gmf)
                .addValue("retencion", retencion)
                .addValue("user", usuarioId);

        return jdbc.queryForObject(sql, params, Integer.class);
    }

    // ============================================================
    // 🔹 8. Guardar apoderado
    // ============================================================
    public void guardarApoderadoBasico(
            Integer idCuenta,
            String documento,
            String nombre,
            String telefono,
            String celular,
            Integer usuarioId
    ) {

        if (documento == null || documento.isBlank()) return;

        jdbc.update("""
            INSERT INTO depositos.poderes_cuentas_ahorro (
                id_cuenta, documento_poder, nombre_poder,
                telefono_poder, celular_poder,
                fk_seguridad_creacion, fk_seguridad_edicion
            ) VALUES (
                :idCuenta, :doc, :nom,
                :tel, :cel,
                :user, :user
            )
        """,
                new MapSqlParameterSource()
                        .addValue("idCuenta", idCuenta)
                        .addValue("doc", documento)
                        .addValue("nom", nombre)
                        .addValue("tel", telefono)
                        .addValue("cel", celular)
                        .addValue("user", usuarioId));
    }

    // ============================================================
    // 🔹 9. Cuentas con saldo (para validaciones)
    // ============================================================
    public int contarCuentasConSaldo(Integer idDatosPersonal) {

        return jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM depositos.cuentas_ahorro
            WHERE id_datos_personal = :id
              AND saldo_actual_cuenta > 0
        """,
                new MapSqlParameterSource().addValue("id", idDatosPersonal),
                Integer.class);
    }

}
