package co.assip.erp.cajas.convenios_recaudo;

import co.assip.erp.cajas.convenios_recaudo.dto.ConvenioRecaudoCuentaDTO;
import co.assip.erp.cajas.convenios_recaudo.dto.ConvenioRecaudoDTO;
import co.assip.erp.cajas.convenios_recaudo.dto.ConvenioRecaudoRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import co.assip.erp.cajas.convenios_recaudo.dto.ConvenioRecaudoBusquedaDTO;

@Repository
@RequiredArgsConstructor
public class ConvenioRecaudoRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<ConvenioRecaudoDTO> listar() {

        String sql = """
            SELECT
                cr.id_convenio,
                cr.id_agencia,
                ag.codigo_agencia,
                ag.nombre_agencia,

                cr.codigo_convenio,
                cr.nombre_convenio,

                cr.id_cuenta_ahorro,
                ca.codigo_cuenta,
                fa.codigo_forma,
                fa.nombre_forma,

                ca.id_datos_personal,
                hv.documento,

                CASE
                    WHEN hv.tipo_persona = '2' THEN COALESCE(hv.nombres, '')
                    ELSE TRIM(
                        COALESCE(hv.primer_apellido, '') || ' ' ||
                        COALESCE(hv.segundo_apellido, '') || ' ' ||
                        COALESCE(hv.nombres, '')
                    )
                END AS nombre_titular,

                COALESCE(ca.saldo_actual_cuenta, 0) AS saldo_actual,

                cr.estado

            FROM cajas.convenios_recaudo cr

            INNER JOIN general.datos_agencias ag
                ON ag.id_agencia = cr.id_agencia

            INNER JOIN depositos.cuentas_ahorro ca
                ON ca.id_cuenta_ahorro = cr.id_cuenta_ahorro

            INNER JOIN depositos.formas_ahorro fa
                ON fa.id_forma_ahorro = ca.id_forma_ahorro

            LEFT JOIN (
                SELECT DISTINCT ON (id_datos_personal)
                    id_datos_personal,
                    tipo_persona,
                    documento,
                    nombres,
                    primer_apellido,
                    segundo_apellido
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY id_datos_personal
            ) hv
                ON hv.id_datos_personal = ca.id_datos_personal

            ORDER BY
                ag.codigo_agencia,
                cr.codigo_convenio
        """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapConvenio(rs)
        );
    }

    public ConvenioRecaudoDTO obtener(Long idConvenio) {

        String sql = """
            SELECT
                cr.id_convenio,
                cr.id_agencia,
                ag.codigo_agencia,
                ag.nombre_agencia,

                cr.codigo_convenio,
                cr.nombre_convenio,

                cr.id_cuenta_ahorro,
                ca.codigo_cuenta,
                fa.codigo_forma,
                fa.nombre_forma,

                ca.id_datos_personal,
                hv.documento,

                CASE
                    WHEN hv.tipo_persona = '2' THEN COALESCE(hv.nombres, '')
                    ELSE TRIM(
                        COALESCE(hv.primer_apellido, '') || ' ' ||
                        COALESCE(hv.segundo_apellido, '') || ' ' ||
                        COALESCE(hv.nombres, '')
                    )
                END AS nombre_titular,

                COALESCE(ca.saldo_actual_cuenta, 0) AS saldo_actual,

                cr.estado

            FROM cajas.convenios_recaudo cr

            INNER JOIN general.datos_agencias ag
                ON ag.id_agencia = cr.id_agencia

            INNER JOIN depositos.cuentas_ahorro ca
                ON ca.id_cuenta_ahorro = cr.id_cuenta_ahorro

            INNER JOIN depositos.formas_ahorro fa
                ON fa.id_forma_ahorro = ca.id_forma_ahorro

            LEFT JOIN (
                SELECT DISTINCT ON (id_datos_personal)
                    id_datos_personal,
                    tipo_persona,
                    documento,
                    nombres,
                    primer_apellido,
                    segundo_apellido
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY id_datos_personal
            ) hv
                ON hv.id_datos_personal = ca.id_datos_personal

            WHERE cr.id_convenio = ?
        """;

        List<ConvenioRecaudoDTO> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapConvenio(rs),
                idConvenio
        );

        return datos.isEmpty() ? null : datos.get(0);
    }

    public List<ConvenioRecaudoCuentaDTO> buscarCuentasPorDocumento(
            Integer idAgencia,
            String documento
    ) {

        String sql = """
            SELECT
                ca.id_cuenta_ahorro,
                ca.id_agencia,
                ag.codigo_agencia,
                ag.nombre_agencia,

                ca.id_datos_personal,
                hv.documento,

                CASE
                    WHEN hv.tipo_persona = '2' THEN COALESCE(hv.nombres, '')
                    ELSE TRIM(
                        COALESCE(hv.primer_apellido, '') || ' ' ||
                        COALESCE(hv.segundo_apellido, '') || ' ' ||
                        COALESCE(hv.nombres, '')
                    )
                END AS nombre_titular,

                ca.codigo_cuenta,
                fa.codigo_forma,
                fa.nombre_forma,

                COALESCE(ca.saldo_actual_cuenta, 0) AS saldo_actual,

                ca.estado_cuenta_cuenta AS estado_cuenta,

                COALESCE(ea.operativo, false) AS estado_operativo,

                CASE
                    WHEN COALESCE(ea.operativo, false) = false THEN
                        'Cuenta no operativa: ' ||
                        COALESCE(ea.descripcion_estado_ahorro, ca.estado_cuenta_cuenta)
                    ELSE
                        'Cuenta disponible'
                END AS mensaje_operativo

            FROM depositos.cuentas_ahorro ca

            INNER JOIN general.datos_agencias ag
                ON ag.id_agencia = ca.id_agencia

            INNER JOIN depositos.formas_ahorro fa
                ON fa.id_forma_ahorro = ca.id_forma_ahorro

            LEFT JOIN depositos.estados_ahorros ea
                ON TRIM(ea.codigo_estado_ahorro) = TRIM(ca.estado_cuenta_cuenta)

            INNER JOIN (
                SELECT DISTINCT ON (id_datos_personal)
                    id_datos_personal,
                    tipo_persona,
                    documento,
                    nombres,
                    primer_apellido,
                    segundo_apellido
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY id_datos_personal
            ) hv
                ON hv.id_datos_personal = ca.id_datos_personal

            WHERE ca.id_agencia = ?
              AND TRIM(hv.documento) = TRIM(?)
              AND TRIM(fa.codigo_forma) <> '01'
              AND COALESCE(ea.operativo, false) = true

            ORDER BY
                fa.codigo_forma,
                ca.codigo_cuenta
        """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapCuenta(rs),
                idAgencia,
                documento
        );
    }

    public boolean existePersonaPorDocumento(String documento) {

        String sql = """
            SELECT COUNT(1)
            FROM reporting.vw_hoja_vida_general_total_reciente
            WHERE TRIM(documento) = TRIM(?)
        """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                documento
        );

        return count != null && count > 0;
    }

    public ConvenioRecaudoBusquedaDTO obtenerResumenAportes(
            Integer idAgencia,
            String documento
    ) {

        String sql = """
        SELECT
            hv.documento,

            CASE
                WHEN hv.tipo_persona = '2' THEN COALESCE(hv.nombres, '')
                ELSE TRIM(
                    COALESCE(hv.primer_apellido, '') || ' ' ||
                    COALESCE(hv.segundo_apellido, '') || ' ' ||
                    COALESCE(hv.nombres, '')
                )
            END AS nombre_titular,

            ca.codigo_cuenta AS codigo_cuenta_aportes,
            COALESCE(ca.saldo_actual_cuenta, 0) AS saldo_aportes,
            ca.fecha_apertura_cuenta AS fecha_apertura_aportes

        FROM depositos.cuentas_ahorro ca

        INNER JOIN depositos.formas_ahorro fa
            ON fa.id_forma_ahorro = ca.id_forma_ahorro

        LEFT JOIN depositos.estados_ahorros ea
            ON TRIM(ea.codigo_estado_ahorro) = TRIM(ca.estado_cuenta_cuenta)

        INNER JOIN (
            SELECT DISTINCT ON (id_datos_personal)
                id_datos_personal,
                documento,
                tipo_persona,
                nombres,
                primer_apellido,
                segundo_apellido
            FROM reporting.vw_hoja_vida_general_total_reciente
            ORDER BY id_datos_personal
        ) hv
            ON hv.id_datos_personal = ca.id_datos_personal

        WHERE ca.id_agencia = ?
          AND TRIM(hv.documento) = TRIM(?)
          AND TRIM(fa.codigo_forma) = '01'
          AND COALESCE(ea.operativo, false) = true

        ORDER BY ca.fecha_apertura_cuenta DESC
        LIMIT 1
    """;

        List<ConvenioRecaudoBusquedaDTO> datos =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) -> ConvenioRecaudoBusquedaDTO.builder()
                                .documento(rs.getString("documento"))
                                .nombreTitular(rs.getString("nombre_titular"))
                                .codigoCuentaAportes(rs.getString("codigo_cuenta_aportes"))
                                .saldoAportes(nvl(rs.getBigDecimal("saldo_aportes")))
                                .fechaAperturaAportes(rs.getString("fecha_apertura_aportes"))
                                .build(),
                        idAgencia,
                        documento
                );

        return datos.isEmpty()
                ? null
                : datos.get(0);
    }

    public boolean tieneCuentaAportesValidaConSaldo(
            Integer idAgencia,
            String documento
    ) {

        String sql = """
            SELECT COUNT(1)
            FROM depositos.cuentas_ahorro ca

            INNER JOIN depositos.formas_ahorro fa
                ON fa.id_forma_ahorro = ca.id_forma_ahorro

            LEFT JOIN depositos.estados_ahorros ea
                ON TRIM(ea.codigo_estado_ahorro) = TRIM(ca.estado_cuenta_cuenta)

            INNER JOIN (
                SELECT DISTINCT ON (id_datos_personal)
                    id_datos_personal,
                    documento
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY id_datos_personal
            ) hv
                ON hv.id_datos_personal = ca.id_datos_personal

            WHERE ca.id_agencia = ?
              AND TRIM(hv.documento) = TRIM(?)
              AND TRIM(fa.codigo_forma) = '01'
              AND COALESCE(ea.operativo, false) = true
              AND COALESCE(ca.saldo_actual_cuenta, 0) > 0
        """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                idAgencia,
                documento
        );

        return count != null && count > 0;
    }

    public boolean existeCodigoConvenio(
            Integer idAgencia,
            String codigoConvenio,
            Long idExcluir
    ) {

        String sql = """
            SELECT COUNT(1)
            FROM cajas.convenios_recaudo
            WHERE id_agencia = ?
              AND TRIM(codigo_convenio) = TRIM(?)
              AND (CAST(? AS BIGINT) IS NULL OR id_convenio <> CAST(? AS BIGINT))
        """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                idAgencia,
                codigoConvenio,
                idExcluir,
                idExcluir
        );

        return count != null && count > 0;
    }

    public boolean existeCuentaConvenio(
            Integer idAgencia,
            Long idCuentaAhorro,
            Long idExcluir
    ) {

        String sql = """
            SELECT COUNT(1)
            FROM cajas.convenios_recaudo
            WHERE id_agencia = ?
              AND id_cuenta_ahorro = ?
              AND (CAST(? AS BIGINT) IS NULL OR id_convenio <> CAST(? AS BIGINT))
        """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                idAgencia,
                idCuentaAhorro,
                idExcluir,
                idExcluir
        );

        return count != null && count > 0;
    }

    public ConvenioRecaudoCuentaDTO obtenerCuenta(
            Long idCuentaAhorro
    ) {

        String sql = """
            SELECT
                ca.id_cuenta_ahorro,
                ca.id_agencia,
                ag.codigo_agencia,
                ag.nombre_agencia,

                ca.id_datos_personal,
                hv.documento,

                CASE
                    WHEN hv.tipo_persona = '2' THEN COALESCE(hv.nombres, '')
                    ELSE TRIM(
                        COALESCE(hv.primer_apellido, '') || ' ' ||
                        COALESCE(hv.segundo_apellido, '') || ' ' ||
                        COALESCE(hv.nombres, '')
                    )
                END AS nombre_titular,

                ca.codigo_cuenta,
                fa.codigo_forma,
                fa.nombre_forma,

                COALESCE(ca.saldo_actual_cuenta, 0) AS saldo_actual,

                ca.estado_cuenta_cuenta AS estado_cuenta,

                COALESCE(ea.operativo, false) AS estado_operativo,

                CASE
                    WHEN COALESCE(ea.operativo, false) = false THEN
                        'Cuenta no operativa: ' ||
                        COALESCE(ea.descripcion_estado_ahorro, ca.estado_cuenta_cuenta)
                    ELSE
                        'Cuenta disponible'
                END AS mensaje_operativo

            FROM depositos.cuentas_ahorro ca

            INNER JOIN general.datos_agencias ag
                ON ag.id_agencia = ca.id_agencia

            INNER JOIN depositos.formas_ahorro fa
                ON fa.id_forma_ahorro = ca.id_forma_ahorro

            LEFT JOIN depositos.estados_ahorros ea
                ON TRIM(ea.codigo_estado_ahorro) = TRIM(ca.estado_cuenta_cuenta)

            INNER JOIN (
                SELECT DISTINCT ON (id_datos_personal)
                    id_datos_personal,
                    tipo_persona,
                    documento,
                    nombres,
                    primer_apellido,
                    segundo_apellido
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY id_datos_personal
            ) hv
                ON hv.id_datos_personal = ca.id_datos_personal

            WHERE ca.id_cuenta_ahorro = ?
        """;

        List<ConvenioRecaudoCuentaDTO> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapCuenta(rs),
                idCuentaAhorro
        );

        return datos.isEmpty() ? null : datos.get(0);
    }

    public Long crear(
            ConvenioRecaudoRequestDTO dto,
            Integer idUsuario
    ) {

        String sql = """
            INSERT INTO cajas.convenios_recaudo (
                id_agencia,
                codigo_convenio,
                nombre_convenio,
                id_cuenta_ahorro,
                estado,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            VALUES (
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?
            )
            RETURNING id_convenio
        """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                dto.getIdAgencia(),
                trim(dto.getCodigoConvenio()),
                trim(dto.getNombreConvenio()),
                dto.getIdCuentaAhorro(),
                normalizarEstado(dto.getEstado()),
                idUsuario,
                idUsuario
        );
    }

    public void actualizar(
            Long idConvenio,
            ConvenioRecaudoRequestDTO dto,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cajas.convenios_recaudo
               SET id_agencia = ?,
                   codigo_convenio = ?,
                   nombre_convenio = ?,
                   id_cuenta_ahorro = ?,
                   estado = ?,
                   fk_seguridad_edicion = ?,
                   fecha_edicion = CURRENT_TIMESTAMP
             WHERE id_convenio = ?
        """;

        jdbcTemplate.update(
                sql,
                dto.getIdAgencia(),
                trim(dto.getCodigoConvenio()),
                trim(dto.getNombreConvenio()),
                dto.getIdCuentaAhorro(),
                normalizarEstado(dto.getEstado()),
                idUsuario,
                idConvenio
        );
    }

    public void eliminar(
            Long idConvenio,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cajas.convenios_recaudo
               SET estado = 'I',
                   fk_seguridad_edicion = ?,
                   fecha_edicion = CURRENT_TIMESTAMP
             WHERE id_convenio = ?
        """;

        jdbcTemplate.update(
                sql,
                idUsuario,
                idConvenio
        );
    }

    private ConvenioRecaudoDTO mapConvenio(
            java.sql.ResultSet rs
    ) throws java.sql.SQLException {

        return ConvenioRecaudoDTO.builder()
                .idConvenio(rs.getLong("id_convenio"))
                .idAgencia(rs.getInt("id_agencia"))
                .codigoAgencia(rs.getString("codigo_agencia"))
                .nombreAgencia(rs.getString("nombre_agencia"))
                .codigoConvenio(rs.getString("codigo_convenio"))
                .nombreConvenio(rs.getString("nombre_convenio"))
                .idCuentaAhorro(rs.getLong("id_cuenta_ahorro"))
                .codigoCuenta(rs.getString("codigo_cuenta"))
                .codigoForma(rs.getString("codigo_forma"))
                .nombreForma(rs.getString("nombre_forma"))
                .idDatosPersonal(rs.getLong("id_datos_personal"))
                .documento(rs.getString("documento"))
                .nombreTitular(rs.getString("nombre_titular"))
                .saldoActual(nvl(rs.getBigDecimal("saldo_actual")))
                .estado(rs.getString("estado"))
                .build();
    }

    private ConvenioRecaudoCuentaDTO mapCuenta(
            java.sql.ResultSet rs
    ) throws java.sql.SQLException {

        return ConvenioRecaudoCuentaDTO.builder()
                .idCuentaAhorro(rs.getLong("id_cuenta_ahorro"))
                .idAgencia(rs.getInt("id_agencia"))
                .codigoAgencia(rs.getString("codigo_agencia"))
                .nombreAgencia(rs.getString("nombre_agencia"))
                .idDatosPersonal(rs.getLong("id_datos_personal"))
                .documento(rs.getString("documento"))
                .nombreTitular(rs.getString("nombre_titular"))
                .codigoCuenta(rs.getString("codigo_cuenta"))
                .codigoForma(rs.getString("codigo_forma"))
                .nombreForma(rs.getString("nombre_forma"))
                .saldoActual(nvl(rs.getBigDecimal("saldo_actual")))
                .estadoCuenta(rs.getString("estado_cuenta"))
                .estadoOperativo(rs.getBoolean("estado_operativo"))
                .mensajeOperativo(rs.getString("mensaje_operativo"))
                .build();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizarEstado(String estado) {
        return estado == null || estado.trim().isEmpty()
                ? "A"
                : estado.trim();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}