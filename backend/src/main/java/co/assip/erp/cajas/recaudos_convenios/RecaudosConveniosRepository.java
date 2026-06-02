package co.assip.erp.cajas.recaudos_convenios;

import co.assip.erp.cajas.recaudos_convenios.dto.RecaudoConvenioConvenioDTO;
import co.assip.erp.cajas.recaudos_convenios.dto.RecaudoConvenioDTO;
import co.assip.erp.cajas.recaudos_convenios.dto.RecaudoConvenioRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecaudosConveniosRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<RecaudoConvenioConvenioDTO> listarConveniosActivos(
            Integer idAgencia
    ) {

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

                hv.documento AS documento_titular,

                CASE
                    WHEN hv.tipo_persona = '2' THEN COALESCE(hv.nombres, '')
                    ELSE TRIM(
                        COALESCE(hv.primer_apellido, '') || ' ' ||
                        COALESCE(hv.segundo_apellido, '') || ' ' ||
                        COALESCE(hv.nombres, '')
                    )
                END AS nombre_titular

            FROM cajas.convenios_recaudo cr

            INNER JOIN general.datos_agencias ag
                ON ag.id_agencia = cr.id_agencia

            INNER JOIN depositos.cuentas_ahorro ca
                ON ca.id_cuenta_ahorro = cr.id_cuenta_ahorro

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

            WHERE cr.id_agencia = ?
              AND cr.estado = 'A'

            ORDER BY cr.nombre_convenio
        """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> RecaudoConvenioConvenioDTO.builder()
                        .idConvenio(rs.getLong("id_convenio"))
                        .idAgencia(rs.getInt("id_agencia"))
                        .codigoAgencia(rs.getString("codigo_agencia"))
                        .nombreAgencia(rs.getString("nombre_agencia"))
                        .codigoConvenio(rs.getString("codigo_convenio"))
                        .nombreConvenio(rs.getString("nombre_convenio"))
                        .idCuentaAhorro(rs.getLong("id_cuenta_ahorro"))
                        .codigoCuenta(rs.getString("codigo_cuenta"))
                        .documentoTitular(rs.getString("documento_titular"))
                        .nombreTitular(rs.getString("nombre_titular"))
                        .build(),
                idAgencia
        );
    }

    public RecaudoConvenioConvenioDTO obtenerConvenioActivo(
            Long idConvenio
    ) {

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

                hv.documento AS documento_titular,

                CASE
                    WHEN hv.tipo_persona = '2' THEN COALESCE(hv.nombres, '')
                    ELSE TRIM(
                        COALESCE(hv.primer_apellido, '') || ' ' ||
                        COALESCE(hv.segundo_apellido, '') || ' ' ||
                        COALESCE(hv.nombres, '')
                    )
                END AS nombre_titular

            FROM cajas.convenios_recaudo cr

            INNER JOIN general.datos_agencias ag
                ON ag.id_agencia = cr.id_agencia

            INNER JOIN depositos.cuentas_ahorro ca
                ON ca.id_cuenta_ahorro = cr.id_cuenta_ahorro

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
              AND cr.estado = 'A'
        """;

        List<RecaudoConvenioConvenioDTO> datos =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) -> RecaudoConvenioConvenioDTO.builder()
                                .idConvenio(rs.getLong("id_convenio"))
                                .idAgencia(rs.getInt("id_agencia"))
                                .codigoAgencia(rs.getString("codigo_agencia"))
                                .nombreAgencia(rs.getString("nombre_agencia"))
                                .codigoConvenio(rs.getString("codigo_convenio"))
                                .nombreConvenio(rs.getString("nombre_convenio"))
                                .idCuentaAhorro(rs.getLong("id_cuenta_ahorro"))
                                .codigoCuenta(rs.getString("codigo_cuenta"))
                                .documentoTitular(rs.getString("documento_titular"))
                                .nombreTitular(rs.getString("nombre_titular"))
                                .build(),
                        idConvenio
                );

        return datos.isEmpty() ? null : datos.get(0);
    }

    public boolean existeProvisionAbierta(
            Long idProvision
    ) {

        String sql = """
            SELECT COUNT(1)
            FROM cajas.provisiones_diarias
            WHERE id_provision = ?
              AND estado = 'ABIERTA'
        """;

        Integer count =
                jdbcTemplate.queryForObject(
                        sql,
                        Integer.class,
                        idProvision
                );

        return count != null && count > 0;
    }

    public Long guardar(
            RecaudoConvenioRequestDTO request,
            Integer idUsuario
    ) {

        String sql = """
            INSERT INTO cajas.recaudos_convenios (
                id_provision,
                fecha_recaudo,
                id_convenio,
                documento_soporte,
                valor_recaudo,
                estado_recaudo,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            SELECT
                ?,
                p.fecha_contable,
                ?,
                ?,
                ?,
                'RECIBIDO',
                ?,
                ?
            FROM cajas.provisiones_diarias p
            WHERE p.id_provision = ?
            RETURNING id_recaudo_convenio
        """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                request.getIdProvision(),
                request.getIdConvenio(),
                trim(request.getDocumentoSoporte()),
                nvl(request.getValorRecaudo()),
                idUsuario,
                idUsuario,
                request.getIdProvision()
        );
    }

    public RecaudoConvenioDTO obtener(
            Long idRecaudoConvenio
    ) {

        String sql = """
            SELECT
                rc.id_recaudo_convenio,
                rc.id_provision,
                p.id_caja,
                cr.id_agencia,
                rc.fecha_recaudo,

                rc.id_convenio,
                cr.codigo_convenio,
                cr.nombre_convenio,

                cr.id_cuenta_ahorro,
                ca.codigo_cuenta,

                rc.documento_soporte,
                rc.valor_recaudo,
                rc.estado_recaudo

            FROM cajas.recaudos_convenios rc

            INNER JOIN cajas.provisiones_diarias p
                ON p.id_provision = rc.id_provision

            INNER JOIN cajas.convenios_recaudo cr
                ON cr.id_convenio = rc.id_convenio

            INNER JOIN depositos.cuentas_ahorro ca
                ON ca.id_cuenta_ahorro = cr.id_cuenta_ahorro

            WHERE rc.id_recaudo_convenio = ?
        """;

        List<RecaudoConvenioDTO> datos =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) -> RecaudoConvenioDTO.builder()
                                .idRecaudoConvenio(rs.getLong("id_recaudo_convenio"))
                                .idProvision(rs.getLong("id_provision"))
                                .idCaja(rs.getLong("id_caja"))
                                .idAgencia(rs.getInt("id_agencia"))
                                .fechaRecaudo(rs.getDate("fecha_recaudo").toLocalDate())
                                .idConvenio(rs.getLong("id_convenio"))
                                .codigoConvenio(rs.getString("codigo_convenio"))
                                .nombreConvenio(rs.getString("nombre_convenio"))
                                .idCuentaAhorro(rs.getLong("id_cuenta_ahorro"))
                                .codigoCuenta(rs.getString("codigo_cuenta"))
                                .documentoSoporte(rs.getString("documento_soporte"))
                                .valorRecaudo(rs.getBigDecimal("valor_recaudo"))
                                .estadoRecaudo(rs.getString("estado_recaudo"))
                                .build(),
                        idRecaudoConvenio
                );

        return datos.isEmpty() ? null : datos.get(0);
    }

    public List<RecaudoConvenioDTO> listarPorProvision(
            Long idProvision
    ) {

        String sql = """
            SELECT
                rc.id_recaudo_convenio,
                rc.id_provision,
                p.id_caja,
                cr.id_agencia,
                rc.fecha_recaudo,

                rc.id_convenio,
                cr.codigo_convenio,
                cr.nombre_convenio,

                cr.id_cuenta_ahorro,
                ca.codigo_cuenta,

                rc.documento_soporte,
                rc.valor_recaudo,
                rc.estado_recaudo

            FROM cajas.recaudos_convenios rc

            INNER JOIN cajas.provisiones_diarias p
                ON p.id_provision = rc.id_provision

            INNER JOIN cajas.convenios_recaudo cr
                ON cr.id_convenio = rc.id_convenio

            INNER JOIN depositos.cuentas_ahorro ca
                ON ca.id_cuenta_ahorro = cr.id_cuenta_ahorro

            WHERE rc.id_provision = ?

            ORDER BY
                rc.id_recaudo_convenio DESC
        """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> RecaudoConvenioDTO.builder()
                        .idRecaudoConvenio(rs.getLong("id_recaudo_convenio"))
                        .idProvision(rs.getLong("id_provision"))
                        .idCaja(rs.getLong("id_caja"))
                        .idAgencia(rs.getInt("id_agencia"))
                        .fechaRecaudo(rs.getDate("fecha_recaudo").toLocalDate())
                        .idConvenio(rs.getLong("id_convenio"))
                        .codigoConvenio(rs.getString("codigo_convenio"))
                        .nombreConvenio(rs.getString("nombre_convenio"))
                        .idCuentaAhorro(rs.getLong("id_cuenta_ahorro"))
                        .codigoCuenta(rs.getString("codigo_cuenta"))
                        .documentoSoporte(rs.getString("documento_soporte"))
                        .valorRecaudo(rs.getBigDecimal("valor_recaudo"))
                        .estadoRecaudo(rs.getString("estado_recaudo"))
                        .build(),
                idProvision
        );
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}