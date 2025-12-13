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
    // 🔹 1. Validar existencia de aportes activos (forma 01)
    // ============================================================
    public boolean tieneAportesActivos(Integer idDatosPersonal) {

        String sql = """
            SELECT COUNT(*)
            FROM depositos.cuentas_ahorro ca
            JOIN depositos.formas_ahorro fa 
                    ON fa.id_forma_ahorro = ca.id_forma_ahorro
            WHERE ca.id_datos_personal = :id
              AND fa.codigo_forma = '01'
              AND ca.estado_cuenta_cuenta = 'A'
        """;

        Integer count = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource().addValue("id", idDatosPersonal),
                Integer.class
        );

        return count != null && count > 0;
    }

    // ============================================================
    // 🔹 1.1 NUEVO — validar si existe cualquier cuenta activa en la agencia
    // ============================================================
    public boolean existeCuentaActivaEnAgencia(Integer idPersona, Integer idAgencia) {

        String sql = """
            SELECT COUNT(*)
            FROM depositos.cuentas_ahorro
            WHERE id_datos_personal = :id
              AND id_agencia = :agencia
              AND estado_cuenta_cuenta = 'A'
        """;

        Integer count = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", idPersona)
                        .addValue("agencia", idAgencia),
                Integer.class
        );

        return count != null && count > 0;
    }

    // ============================================================
    // 🔹 2. Listar formas habilitadas
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
    // 🔹 3. Incrementar consecutivo por agencia
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
    // 🔹 4. Crear cuenta (INSERT final)
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
                fecha_apertura_cuenta,
                saldo_inicial_cuenta,
                saldo_actual_cuenta,
                estado_cuenta_cuenta,
                fecha_estado_cuenta,
                gmf_cuenta_cuenta,
                fecha_gmf_cuenta,
                libranza_cuenta,
                libranzatiempo_pago,
                cuota_mensual_cuenta,
                retencion_fuente_cuenta,
                plazo_cuenta,
                fecha_final_cuenta,
                cuenta_activa,
                cuenta_conjunta,
                accion_conjunta,
                tasa,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            VALUES (
                :agencia,
                :forma,
                :codigo,
                :idPer,
                CURRENT_DATE,
                0,
                0,
                'A',
                CURRENT_DATE,
                :gmf,
                CURRENT_DATE,
                false,
                'M',
                0,
                :retencion,
                0,
                CURRENT_DATE,   -- ✔ YA NO ES NULL
                'A',
                'N',
                'N',
                0,
                :user,
                :user
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
    // 🔹 5. Guardar apoderado
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
    // 🔹 6. Cuentas con saldo
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
