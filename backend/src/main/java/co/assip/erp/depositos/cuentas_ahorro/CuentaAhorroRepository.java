package co.assip.erp.depositos.cuentas_ahorro;

import co.assip.erp.depositos.cuentas_ahorro.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CuentaAhorroRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
// 🟦 1. DETALLE COMPLETO (Incluye documento)  ⭐ CORREGIDO
// ============================================================
    // ============================================================
// 🟦 1. DETALLE COMPLETO (Incluye documento y estado CORRECTO)
// ============================================================
    public CuentaAhorroDetalleDTO obtenerDetalle(Integer id) {

        String sql = """
        SELECT 
            ca.*,
            fa.nombre_forma,
            ag.nombre_agencia,

            -- ESTADO CORRECTO
            ea.codigo_estado_ahorro AS codigo_estado,
            ea.descripcion_estado_ahorro AS nombre_estado,

            dp.documento,
            dp.nombres || ' ' || dp.primer_apellido AS nombre_titular
        FROM depositos.cuentas_ahorro ca
        JOIN depositos.formas_ahorro fa  ON fa.id_forma_ahorro = ca.id_forma_ahorro
        JOIN general.datos_agencias ag   ON ag.id_agencia = ca.id_agencia
        JOIN hoja_vida.datos_personales dp 
              ON dp.id_datos_personal = ca.id_datos_personal
        LEFT JOIN depositos.estados_ahorros ea 
              ON ea.codigo_estado_ahorro = ca.estado_cuenta_cuenta
        WHERE ca.id_cuenta_ahorro = :id
    """;

        var params = new MapSqlParameterSource().addValue("id", id);

        return jdbc.query(sql, params, rs -> {
            if (!rs.next()) return null;

            CuentaAhorroDetalleDTO dto = new CuentaAhorroDetalleDTO();

            dto.setIdCuentaAhorro(rs.getInt("id_cuenta_ahorro"));
            dto.setIdAgencia(rs.getInt("id_agencia"));
            dto.setIdFormaAhorro(rs.getInt("id_forma_ahorro"));
            dto.setCodigoCuenta(rs.getString("codigo_cuenta"));
            dto.setIdDatosPersonal(rs.getInt("id_datos_personal"));
            dto.setFechaAperturaCuenta(String.valueOf(rs.getDate("fecha_apertura_cuenta")));
            dto.setSaldoInicialCuenta(rs.getBigDecimal("saldo_inicial_cuenta"));
            dto.setSaldoActualCuenta(rs.getBigDecimal("saldo_actual_cuenta"));
            dto.setEstadoCuenta(rs.getString("estado_cuenta_cuenta"));
            dto.setFechaEstadoCuenta(String.valueOf(rs.getDate("fecha_estado_cuenta")));
            dto.setGmfCuentaCuenta(rs.getString("gmf_cuenta_cuenta"));
            dto.setFechaGmfCuenta(String.valueOf(rs.getDate("fecha_gmf_cuenta")));
            dto.setLibranzaCuenta(rs.getBoolean("libranza_cuenta"));
            dto.setLibranzaTiempoPago(rs.getString("libranzatiempo_pago"));
            dto.setCuotaMensualCuenta(rs.getBigDecimal("cuota_mensual_cuenta"));
            dto.setRetencionFuenteCuenta(rs.getBoolean("retencion_fuente_cuenta"));
            dto.setPlazoCuenta(rs.getInt("plazo_cuenta"));
            dto.setFechaFinalCuenta(String.valueOf(rs.getDate("fecha_final_cuenta")));
            dto.setCuentaActiva(rs.getString("cuenta_activa"));
            dto.setCuentaConjunta(rs.getString("cuenta_conjunta"));
            dto.setAccionConjunta(rs.getString("accion_conjunta"));
            dto.setTasa(rs.getDouble("tasa"));

            dto.setNombreTitular(rs.getString("nombre_titular"));
            dto.setNombreForma(rs.getString("nombre_forma"));
            dto.setNombreAgencia(rs.getString("nombre_agencia"));

            // 🟦 ESTADO CORRECTO — AHORA SÍ
            dto.setCodigoEstado(rs.getString("codigo_estado"));
            dto.setNombreEstado(rs.getString("nombre_estado"));

            // 🟦 Documento
            dto.setDocumento(rs.getString("documento"));

            return dto;
        });
    }


    // ============================================================
    // 🟦 2. Validar si una cuenta pertenece a las agencias del usuario
    // ============================================================
    public boolean cuentaPerteneceAgencias(Integer idCuenta, List<Integer> agencias) {

        String sql = """
            SELECT COUNT(*) 
            FROM depositos.cuentas_ahorro 
            WHERE id_cuenta_ahorro = :id
              AND id_agencia IN (:ags)
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idCuenta)
                .addValue("ags", agencias);

        Integer count = jdbc.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    // ============================================================
    // 🟦 3. BENEFICIARIOS
    // ============================================================
    public List<BeneficiarioDTO> listarBeneficiarios(Integer idCuenta) {

        String sql = """
            SELECT *
            FROM depositos.beneficiarios_cuenta_ahorros
            WHERE id_cuenta_ahorro = :id
        """;

        return jdbc.query(sql,
                new MapSqlParameterSource().addValue("id", idCuenta),
                (rs, row) -> {
                    BeneficiarioDTO b = new BeneficiarioDTO();
                    b.setIdBeneficiario(rs.getInt("id_beneficiario"));
                    b.setIdCuentaAhorro(rs.getInt("id_cuenta_ahorro"));
                    b.setDocumentoBeneficiario(rs.getString("documento_beneficiario"));
                    b.setNombreBeneficiario(rs.getString("nombre_beneficiario"));
                    b.setTelefonoBeneficiario(rs.getString("telefono_beneficiario"));
                    b.setCelularBeneficiario(rs.getString("celular_beneficiario"));
                    b.setTipoParentesco(rs.getString("tipo_parentesco"));
                    return b;
                });
    }

    // ============================================================
    // 🟦 4. PODERES
    // ============================================================
    public List<PoderDTO> listarPoderes(Integer idCuenta) {

        String sql = """
            SELECT *
            FROM depositos.poderes_cuentas_ahorro
            WHERE id_cuenta_ahorro = :id
        """;

        return jdbc.query(sql,
                new MapSqlParameterSource().addValue("id", idCuenta),
                (rs, row) -> {
                    PoderDTO p = new PoderDTO();
                    p.setIdPoder(rs.getInt("id_poder"));
                    p.setIdCuenta(rs.getInt("id_cuenta_ahorro"));
                    p.setDocumentoPoder(rs.getString("documento_poder"));
                    p.setNombrePoder(rs.getString("nombre_poder"));
                    p.setTelefonoPoder(rs.getString("telefono_poder"));
                    p.setCelularPoder(rs.getString("celular_poder"));
                    p.setCorreoPoder(rs.getString("correo_poder"));
                    return p;
                });
    }

    // ============================================================
    // 🟦 5. BUSCAR CUENTA DE APORTES (FORMA 01)
    // ============================================================
    public CuentaAhorroDetalleDTO buscarCuentaAportes(Integer idDatosPersonal) {

        String sql = """
            SELECT ca.id_cuenta_ahorro,
                   ca.saldo_actual_cuenta,
                   ca.id_agencia,
                   ca.codigo_cuenta
            FROM depositos.cuentas_ahorro ca
            WHERE ca.id_datos_personal = :id
              AND ca.id_forma_ahorro = (
                    SELECT id_forma_ahorro
                    FROM depositos.formas_ahorro
                    WHERE codigo_forma = '01'
                )
            ORDER BY ca.id_cuenta_ahorro DESC
            LIMIT 1
        """;

        return jdbc.query(sql,
                new MapSqlParameterSource().addValue("id", idDatosPersonal),
                rs -> {
                    if (!rs.next()) return null;
                    CuentaAhorroDetalleDTO dto = new CuentaAhorroDetalleDTO();
                    dto.setIdCuentaAhorro(rs.getInt("id_cuenta_ahorro"));
                    dto.setSaldoActualCuenta(rs.getBigDecimal("saldo_actual_cuenta"));
                    dto.setIdAgencia(rs.getInt("id_agencia"));
                    dto.setCodigoCuenta(rs.getString("codigo_cuenta"));
                    return dto;
                });
    }

    // ============================================================
    // 🟦 6. CONTAR MOVIMIENTOS
    // ============================================================
    public int contarMovimientos(Integer idCuenta) {
        return jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM depositos.extractos_cuentas_ahorros
                WHERE id_cuenta_ahorro = :id
                """,
                new MapSqlParameterSource().addValue("id", idCuenta),
                Integer.class);
    }

    // ============================================================
    // 🟦 7. OBTENER CONSECUTIVO
    // ============================================================
    public Integer obtenerConsecutivo(Integer idForma) {
        return jdbc.queryForObject("""
                SELECT COALESCE(consecutivo_forma,0) + 1
                FROM depositos.formas_ahorro
                WHERE id_forma_ahorro = :id
                """,
                new MapSqlParameterSource().addValue("id", idForma),
                Integer.class);
    }

    // ============================================================
    // 🟦 7B. ACTUALIZAR CONSECUTIVO
    // ============================================================
    public void actualizarConsecutivo(Integer idForma) {
        jdbc.update("""
            UPDATE depositos.formas_ahorro
            SET consecutivo_forma = COALESCE(consecutivo_forma,0) + 1
            WHERE id_forma_ahorro = :id
        """, new MapSqlParameterSource().addValue("id", idForma));
    }

    // ============================================================
    // 🟦 8. GUARDAR CUENTA
    // ============================================================
    public Integer guardarCuenta(
            CuentaAhorroGuardarDTO dto,
            String codigoCuenta,
            String fechaFinal,
            Integer idUsuario
    ) {

        String sql = """
        INSERT INTO depositos.cuentas_ahorro (
            id_agencia, id_forma_ahorro, codigo_cuenta, id_datos_personal,
            estado_cuenta_cuenta, gmf_cuenta_cuenta, libranza_cuenta,
            libranzatiempo_pago, cuota_mensual_cuenta,
            retencion_fuente_cuenta, plazo_cuenta, fecha_final_cuenta,
            cuenta_activa, cuenta_conjunta, accion_conjunta, tasa,
            fk_seguridad_creacion, fk_seguridad_edicion
        ) VALUES (
            :idAgencia, :idForma, :codigoCuenta, :idDatosPersonal,
            'A', :gmf, false,
            'M', :cuota,
            :retencion, :plazo, :fechaFinal,
            'A', :conjunta, :accion, :tasa,
            :usuario, :usuario
        )
        RETURNING id_cuenta_ahorro
    """;

        var params = new MapSqlParameterSource()
                .addValue("idAgencia", dto.getIdAgencia())
                .addValue("idForma", dto.getIdFormaAhorro())
                .addValue("codigoCuenta", codigoCuenta)
                .addValue("idDatosPersonal", dto.getIdDatosPersonal())
                .addValue("gmf", dto.getGmfCuentaCuenta())
                .addValue("cuota", dto.getCuotaMensualCuenta())
                .addValue("retencion", dto.getRetencionFuenteCuenta())
                .addValue("plazo", dto.getPlazoCuenta())
                .addValue("fechaFinal", fechaFinal)
                .addValue("conjunta", dto.getCuentaConjunta())
                .addValue("accion", dto.getAccionConjunta())
                .addValue("tasa", dto.getTasa())
                .addValue("usuario", idUsuario);

        return jdbc.queryForObject(sql, params, Integer.class);
    }

    // ============================================================
    // 🟦 9. GUARDAR BENEFICIARIOS
    // ============================================================
    public void guardarBeneficiarios(Integer idCuenta, List<BeneficiarioDTO> lista, Integer usuarioId) {

        jdbc.update("""
            DELETE FROM depositos.beneficiarios_cuenta_ahorros
            WHERE id_cuenta_ahorro = :id
        """, new MapSqlParameterSource().addValue("id", idCuenta));

        if (lista == null || lista.isEmpty()) return;

        String insert = """
            INSERT INTO depositos.beneficiarios_cuenta_ahorros (
               id_cuenta_ahorro, documento_beneficiario, nombre_beneficiario,
               telefono_beneficiario, celular_beneficiario, tipo_parentesco,
               fk_seguridad_creacion, fk_seguridad_edicion
            ) VALUES (
               :idCuenta, :doc, :nom,
               :tel, :cel, :par,
               :usuario, :usuario
            )
        """;

        for (BeneficiarioDTO b : lista) {
            var p = new MapSqlParameterSource()
                    .addValue("idCuenta", idCuenta)
                    .addValue("doc", b.getDocumentoBeneficiario())
                    .addValue("nom", b.getNombreBeneficiario())
                    .addValue("tel", b.getTelefonoBeneficiario())
                    .addValue("cel", b.getCelularBeneficiario())
                    .addValue("par", b.getTipoParentesco())
                    .addValue("usuario", usuarioId);
            jdbc.update(insert, p);
        }
    }

    // ============================================================
    // 🟦 10. GUARDAR PODERES
    // ============================================================
    public void guardarPoderes(Integer idCuenta, List<PoderDTO> lista, Integer usuarioId) {

        jdbc.update("""
            DELETE FROM depositos.poderes_cuentas_ahorro
            WHERE id_cuenta_ahorro = :id
        """, new MapSqlParameterSource().addValue("id", idCuenta));

        if (lista == null || lista.isEmpty()) return;

        String insert = """
            INSERT INTO depositos.poderes_cuentas_ahorro (
                id_cuenta_ahorro, documento_poder, nombre_poder,
                telefono_poder, celular_poder,
                fk_seguridad_creacion, fk_seguridad_edicion
            ) VALUES (
                :idCuenta, :doc, :nom,
                :tel, :cel,
                :usuario, :usuario
            )
        """;

        for (PoderDTO pwr : lista) {
            var p = new MapSqlParameterSource()
                    .addValue("idCuenta", idCuenta)
                    .addValue("doc", pwr.getDocumentoPoder())
                    .addValue("nom", pwr.getNombrePoder())
                    .addValue("tel", pwr.getTelefonoPoder())
                    .addValue("cel", pwr.getCelularPoder())
                    .addValue("usuario", usuarioId);
            jdbc.update(insert, p);
        }
    }

    // ============================================================
    // 🟦 11. ELIMINAR CUENTA + RELACIONES
    // ============================================================
    public void eliminar(Integer idCuenta) {

        jdbc.update("""
            DELETE FROM depositos.beneficiarios_cuenta_ahorros
            WHERE id_cuenta_ahorro = :id
        """, new MapSqlParameterSource().addValue("id", idCuenta));

        jdbc.update("""
            DELETE FROM depositos.poderes_cuentas_ahorro
            WHERE id_cuenta_ahorro = :id
        """, new MapSqlParameterSource().addValue("id", idCuenta));

        jdbc.update("""
            DELETE FROM depositos.cuentas_ahorro
            WHERE id_cuenta_ahorro = :id
        """, new MapSqlParameterSource().addValue("id", idCuenta));
    }

    // ============================================================
    // 🟦 12. LISTAR SOLO CUENTAS DE UNA AGENCIA  ⭐ NUEVO
    // ============================================================
    public List<CuentaAhorroDTO> listarPorAgencia(Integer idAgencia) {

        String sql = """
            SELECT 
                ca.id_cuenta_ahorro,
                ca.codigo_cuenta,
                ca.id_agencia,
                ag.nombre_agencia,
                dp.nombres || ' ' || dp.primer_apellido AS nombre_titular,
                ca.saldo_actual_cuenta
            FROM depositos.cuentas_ahorro ca
            JOIN general.datos_agencias ag ON ag.id_agencia = ca.id_agencia
            JOIN hoja_vida.datos_personales dp ON dp.id_datos_personal = ca.id_datos_personal
            WHERE ca.id_agencia = :idAgencia
            ORDER BY ca.id_cuenta_ahorro DESC
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource().addValue("idAgencia", idAgencia),
                (rs, row) -> {
                    CuentaAhorroDTO dto = new CuentaAhorroDTO();
                    dto.setIdCuentaAhorro(rs.getInt("id_cuenta_ahorro"));
                    dto.setCodigoCuenta(rs.getString("codigo_cuenta"));
                    dto.setIdAgencia(rs.getInt("id_agencia"));
                    dto.setNombreAgencia(rs.getString("nombre_agencia"));
                    dto.setNombreTitular(rs.getString("nombre_titular"));
                    dto.setSaldoActual(rs.getBigDecimal("saldo_actual_cuenta"));
                    return dto;
                }
        );
    }

    // ============================================================
    // 🟦 LISTAR TODAS LAS CUENTAS (solo admin o multiagencia)
    // ============================================================
    public List<CuentaAhorroDTO> listarTodas() {

        String sql = """
        SELECT 
            ca.id_cuenta_ahorro,
            ca.codigo_cuenta,
            ca.id_agencia,
            ag.nombre_agencia,
            dp.nombres || ' ' || dp.primer_apellido AS nombre_titular,
            ca.saldo_actual_cuenta
        FROM depositos.cuentas_ahorro ca
        JOIN general.datos_agencias ag ON ag.id_agencia = ca.id_agencia
        JOIN hoja_vida.datos_personales dp ON dp.id_datos_personal = ca.id_datos_personal
        ORDER BY ca.id_cuenta_ahorro DESC
    """;

        return jdbc.query(sql, (rs, row) -> {
            CuentaAhorroDTO dto = new CuentaAhorroDTO();
            dto.setIdCuentaAhorro(rs.getInt("id_cuenta_ahorro"));
            dto.setCodigoCuenta(rs.getString("codigo_cuenta"));
            dto.setIdAgencia(rs.getInt("id_agencia"));
            dto.setNombreAgencia(rs.getString("nombre_agencia"));
            dto.setNombreTitular(rs.getString("nombre_titular"));
            dto.setSaldoActual(rs.getBigDecimal("saldo_actual_cuenta"));
            return dto;
        });
    }


    // ============================================================
    // 🟦 13. BUSCAR CUENTA SOLO SI PERTENECE A LA AGENCIA  ⭐ NUEVO
    // ============================================================
    public CuentaAhorroDetalleDTO buscarPorIdYAgencia(Integer idCuenta, Integer idAgencia) {

        String sql = """
            SELECT
                 ca.*,
                 fa.nombre_forma,
                 ag.nombre_agencia,
                 dp.documento,
                 dp.nombres || ' ' || dp.primer_apellido AS nombre_titular,
                 ca.estado_cuenta_cuenta AS codigo_estado,
                 ea.descripcion_estado_ahorro AS nombre_estado
             FROM depositos.cuentas_ahorro ca
             JOIN depositos.formas_ahorro fa  ON fa.id_forma_ahorro = ca.id_forma_ahorro
             JOIN general.datos_agencias ag   ON ag.id_agencia = ca.id_agencia
             JOIN hoja_vida.datos_personales dp ON dp.id_datos_personal = ca.id_datos_personal
             LEFT JOIN depositos.estados_ahorros ea ON ca.estado_cuenta_cuenta = ea.codigo_estado_ahorro
             WHERE ca.id_cuenta_ahorro = :id
              AND ca.id_agencia = :idAgencia
        """;

        var params = new MapSqlParameterSource()
                .addValue("idCuenta", idCuenta)
                .addValue("idAgencia", idAgencia);

        return jdbc.query(sql, params, rs -> {
            if (!rs.next()) return null;

            CuentaAhorroDetalleDTO dto = new CuentaAhorroDetalleDTO();

            dto.setIdCuentaAhorro(rs.getInt("id_cuenta_ahorro"));
            dto.setIdAgencia(rs.getInt("id_agencia"));
            dto.setIdFormaAhorro(rs.getInt("id_forma_ahorro"));
            dto.setCodigoCuenta(rs.getString("codigo_cuenta"));
            dto.setIdDatosPersonal(rs.getInt("id_datos_personal"));
            dto.setFechaAperturaCuenta(String.valueOf(rs.getDate("fecha_apertura_cuenta")));
            dto.setSaldoInicialCuenta(rs.getBigDecimal("saldo_inicial_cuenta"));
            dto.setSaldoActualCuenta(rs.getBigDecimal("saldo_actual_cuenta"));
            dto.setEstadoCuenta(rs.getString("estado_cuenta_cuenta"));
            dto.setFechaEstadoCuenta(String.valueOf(rs.getDate("fecha_estado_cuenta")));
            dto.setGmfCuentaCuenta(rs.getString("gmf_cuenta_cuenta"));
            dto.setFechaGmfCuenta(String.valueOf(rs.getDate("fecha_gmf_cuenta")));
            dto.setLibranzaCuenta(rs.getBoolean("libranza_cuenta"));
            dto.setLibranzaTiempoPago(rs.getString("libranzatiempo_pago"));
            dto.setCuotaMensualCuenta(rs.getBigDecimal("cuota_mensual_cuenta"));
            dto.setRetencionFuenteCuenta(rs.getBoolean("retencion_fuente_cuenta"));
            dto.setPlazoCuenta(rs.getInt("plazo_cuenta"));
            dto.setFechaFinalCuenta(String.valueOf(rs.getDate("fecha_final_cuenta")));
            dto.setCuentaActiva(rs.getString("cuenta_activa"));
            dto.setCuentaConjunta(rs.getString("cuenta_conjunta"));
            dto.setAccionConjunta(rs.getString("accion_conjunta"));
            dto.setTasa(rs.getDouble("tasa"));

            dto.setNombreTitular(rs.getString("nombre_titular"));
            dto.setNombreForma(rs.getString("nombre_forma"));
            dto.setNombreAgencia(rs.getString("nombre_agencia"));


            return dto;
        });
    }

    public List<CuentaConjuntaDTO> listarCuentasConjuntas(Integer idCuenta) {

        String sql = """
        SELECT
            id_cuenta_conjunta,
            id_cuenta_ahorro,
            id_datos_personal,
            documento,
            nombre_completo,
            codigo_accion,
            descripcion_accion
        FROM depositos.vw_depositos_cuentas_conjuntas_detalle
        WHERE id_cuenta_ahorro = :idCuenta
        ORDER BY nombre_completo
    """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCuenta", idCuenta),
                (rs, row) -> {
                    CuentaConjuntaDTO dto = new CuentaConjuntaDTO();

                    dto.setIdCuentaConjunta(rs.getInt("id_cuenta_conjunta"));
                    dto.setIdCuentaAhorro(rs.getInt("id_cuenta_ahorro"));
                    dto.setIdDatosPersonal(rs.getInt("id_datos_personal"));
                    dto.setDocumento(rs.getString("documento"));
                    dto.setNombreCompleto(rs.getString("nombre_completo"));
                    dto.setCodigoAccion(rs.getString("codigo_accion"));
                    dto.setDescripcionAccion(rs.getString("descripcion_accion"));

                    return dto;
                }
        );
    }

    public void agregarCuentaConjunta(
            Integer idCuenta,
            CuentaConjuntaDTO dto,
            Integer idUsuario
    ) {

        String sql = """
        INSERT INTO depositos.cuentas_ahorro_conjuntas (
            id_cuenta_ahorro,
            id_datos_personal,
            codigo_accion,
            fk_seguridad_creacion,
            fk_seguridad_edicion
        ) VALUES (
            :idCuenta,
            :idDatosPersonal,
            :codigoAccion,
            :idUsuario,
            :idUsuario
        )
    """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCuenta", idCuenta)
                        .addValue("idDatosPersonal", dto.getIdDatosPersonal())
                        .addValue("codigoAccion", dto.getCodigoAccion())
                        .addValue("idUsuario", idUsuario)
        );
    }

    public void eliminarCuentaConjunta(
            Integer idCuenta,
            Integer idCuentaConjunta
    ) {

        String sql = """
        DELETE FROM depositos.cuentas_ahorro_conjuntas
        WHERE id_cuenta_ahorro = :idCuenta
          AND id_cuenta_conjunta = :idCuentaConjunta
    """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCuenta", idCuenta)
                        .addValue("idCuentaConjunta", idCuentaConjunta)
        );
    }

    public int contarCuentasConjuntas(Integer idCuenta) {

        String sql = """
        SELECT COUNT(*)
        FROM depositos.cuentas_ahorro_conjuntas
        WHERE id_cuenta_ahorro = :idCuenta
    """;

        Integer total = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCuenta", idCuenta),
                Integer.class
        );

        return total == null ? 0 : total;
    }

    public void agregarPoder(
            Integer idCuenta,
            PoderDTO dto,
            Integer idUsuario
    ) {

        String sql = """
        INSERT INTO depositos.poderes_cuentas_ahorro (
            id_cuenta_ahorro,
            documento_poder,
            nombre_poder,
            telefono_poder,
            celular_poder,
            correo_poder,
            fk_seguridad_creacion,
            fk_seguridad_edicion
        )
        VALUES (
            :idCuenta,
            :documento,
            :nombre,
            :telefono,
            :celular,
            :correo,
            :usuario,
            :usuario
        )
    """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCuenta", idCuenta)
                        .addValue("documento", dto.getDocumentoPoder())
                        .addValue("nombre", dto.getNombrePoder())
                        .addValue("telefono", dto.getTelefonoPoder())
                        .addValue("celular", dto.getCelularPoder())
                        .addValue("correo", dto.getCorreoPoder())
                        .addValue("usuario", idUsuario)
        );
    }

    public void actualizarPoder(
            Integer idCuenta,
            Integer idPoder,
            PoderDTO dto,
            Integer idUsuario
    ) {

        String sql = """
        UPDATE depositos.poderes_cuentas_ahorro
        SET documento_poder = :documento,
            nombre_poder = :nombre,
            telefono_poder = :telefono,
            celular_poder = :celular,
            correo_poder = :correo,
            fk_seguridad_edicion = :usuario
        WHERE id_cuenta_ahorro = :idCuenta
          AND id_poder = :idPoder
    """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCuenta", idCuenta)
                        .addValue("idPoder", idPoder)
                        .addValue("documento", dto.getDocumentoPoder())
                        .addValue("nombre", dto.getNombrePoder())
                        .addValue("telefono", dto.getTelefonoPoder())
                        .addValue("celular", dto.getCelularPoder())
                        .addValue("correo", dto.getCorreoPoder())
                        .addValue("usuario", idUsuario)
        );
    }

    public void eliminarPoder(
            Integer idCuenta,
            Integer idPoder
    ) {

        String sql = """
        DELETE
        FROM depositos.poderes_cuentas_ahorro
        WHERE id_cuenta_ahorro = :idCuenta
          AND id_poder = :idPoder
    """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCuenta", idCuenta)
                        .addValue("idPoder", idPoder)
        );
    }

    public void actualizarIndicadorCuentaConjunta(
            Integer idCuenta,
            String valor
    ) {

        String sql = """
        UPDATE depositos.cuentas_ahorro
        SET cuenta_conjunta = :valor
        WHERE id_cuenta_ahorro = :idCuenta
    """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCuenta", idCuenta)
                        .addValue("valor", valor)
        );
    }

    public void agregarBeneficiario(
            Integer idCuenta,
            BeneficiarioDTO dto,
            Integer idUsuario
    ) {

        String sql = """
        INSERT INTO depositos.beneficiarios_cuenta_ahorros (
            id_cuenta_ahorro,
            documento_beneficiario,
            nombre_beneficiario,
            telefono_beneficiario,
            celular_beneficiario,
            tipo_parentesco,
            fk_seguridad_creacion,
            fk_seguridad_edicion
        )
        VALUES (
            :idCuenta,
            :documento,
            :nombre,
            :telefono,
            :celular,
            :parentesco,
            :usuario,
            :usuario
        )
    """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCuenta", idCuenta)
                        .addValue("documento", dto.getDocumentoBeneficiario())
                        .addValue("nombre", dto.getNombreBeneficiario())
                        .addValue("telefono", dto.getTelefonoBeneficiario())
                        .addValue("celular", dto.getCelularBeneficiario())
                        .addValue("parentesco", dto.getTipoParentesco())
                        .addValue("usuario", idUsuario)
        );
    }

    public void actualizarBeneficiario(
            Integer idCuenta,
            Integer idBeneficiario,
            BeneficiarioDTO dto,
            Integer idUsuario
    ) {

        String sql = """
        UPDATE depositos.beneficiarios_cuenta_ahorros
        SET documento_beneficiario = :documento,
            nombre_beneficiario = :nombre,
            telefono_beneficiario = :telefono,
            celular_beneficiario = :celular,
            tipo_parentesco = :parentesco,
            fk_seguridad_edicion = :usuario
        WHERE id_cuenta_ahorro = :idCuenta
          AND id_beneficiario = :idBeneficiario
    """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCuenta", idCuenta)
                        .addValue("idBeneficiario", idBeneficiario)
                        .addValue("documento", dto.getDocumentoBeneficiario())
                        .addValue("nombre", dto.getNombreBeneficiario())
                        .addValue("telefono", dto.getTelefonoBeneficiario())
                        .addValue("celular", dto.getCelularBeneficiario())
                        .addValue("parentesco", dto.getTipoParentesco())
                        .addValue("usuario", idUsuario)
        );
    }

    public void eliminarBeneficiario(
            Integer idCuenta,
            Integer idBeneficiario
    ) {

        String sql = """
        DELETE
        FROM depositos.beneficiarios_cuenta_ahorros
        WHERE id_cuenta_ahorro = :idCuenta
          AND id_beneficiario = :idBeneficiario
    """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCuenta", idCuenta)
                        .addValue("idBeneficiario", idBeneficiario)
        );
    }

    public String obtenerCodigoForma(Integer idForma) {

        String sql = """
        SELECT codigo_forma
        FROM depositos.formas_ahorro
        WHERE id_forma_ahorro = :idForma
    """;

        return jdbc.query(sql,
                new MapSqlParameterSource()
                        .addValue("idForma", idForma),
                rs -> {
                    if (!rs.next()) return null;
                    return rs.getString("codigo_forma");
                });
    }
}
