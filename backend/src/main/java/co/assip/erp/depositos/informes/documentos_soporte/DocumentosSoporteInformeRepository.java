package co.assip.erp.depositos.informes.documentos_soporte;

import co.assip.erp.depositos.informes.documentos_soporte.dto.DocumentoSoporteInformeItemDTO;
import co.assip.erp.depositos.informes.documentos_soporte.dto.DocumentoSoporteResumenAgenciaFormaDTO;
import co.assip.erp.depositos.informes.documentos_soporte.dto.DocumentoSoporteResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DocumentosSoporteInformeRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<DocumentoSoporteInformeItemDTO> consultar(
            String agencia,
            String fechaDesde,
            String fechaHasta
    ) {

        String sql = """
        WITH hv_unica AS (
            SELECT DISTINCT ON (id_datos_personal)
                *
            FROM reporting.vw_hoja_vida_general_total_reciente
            ORDER BY
                id_datos_personal
        )

        SELECT
            ds.id_documento_soporte,

            a.id_agencia,
            LPAD(a.codigo_agencia, 2, '0') AS codigo_agencia,
            a.nombre_agencia,

            f.id_forma_ahorro,
            LPAD(f.codigo_forma, 2, '0') AS codigo_forma,
            f.nombre_forma,

            c.id_cuenta_ahorro,
            TRIM(c.codigo_cuenta) AS codigo_cuenta,

            hv.documento,

            CASE
               WHEN hv.tipo_persona = '2'
                   THEN COALESCE(hv.nombres, '')
               ELSE TRIM(
                   COALESCE(hv.primer_apellido, '') || ' ' ||
                   COALESCE(hv.segundo_apellido, '') || ' ' ||
                   COALESCE(hv.nombres, '')
               )
            END AS nombre_completo,

            ds.tipo_documento_soporte,
            tds.descripcion_soporte,

            ds.numero_inicial,
            ds.numero_final,

            (
                CAST(ds.numero_final AS bigint)
                - CAST(ds.numero_inicial AS bigint)
                + 1
            ) AS cantidad_documentos,

            ds.estado_documento,
            eds.descripcion_codigo_documento_soporte AS descripcion_estado,

            ds.fecha_entrega::text AS fecha_entrega,
            ds.fecha_estado::text AS fecha_estado,
            ds.fecha_creacion::text AS fecha_creacion

        FROM depositos.documentos_soporte ds

        INNER JOIN depositos.cuentas_ahorro c
            ON c.id_cuenta_ahorro = ds.id_cuenta_ahorro

        INNER JOIN general.datos_agencias a
            ON a.id_agencia = c.id_agencia

        INNER JOIN depositos.formas_ahorro f
            ON f.id_forma_ahorro = c.id_forma_ahorro

        LEFT JOIN depositos.tipos_documentos_soporte tds
            ON tds.codigo_soporte = ds.tipo_documento_soporte

        LEFT JOIN depositos.estados_documentos_soporte eds
            ON eds.codigo_estado_documento_soporte = ds.estado_documento

        LEFT JOIN hv_unica hv
            ON hv.id_datos_personal = c.id_datos_personal

        WHERE (:agencia = '0' OR c.id_agencia = CAST(:agencia AS integer))
          AND ds.fecha_entrega >= CAST(:fechaDesde AS date)
          AND ds.fecha_entrega <= CAST(:fechaHasta AS date)

        ORDER BY
            codigo_agencia,
            codigo_forma,
            ds.fecha_entrega,
            ds.numero_inicial
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("agencia", agencia)
                        .addValue("fechaDesde", fechaDesde)
                        .addValue("fechaHasta", fechaHasta),
                (rs, i) -> {
                    DocumentoSoporteInformeItemDTO dto =
                            new DocumentoSoporteInformeItemDTO();

                    dto.setIdDocumentoSoporte(rs.getInt("id_documento_soporte"));

                    dto.setIdAgencia(rs.getInt("id_agencia"));
                    dto.setCodigoAgencia(rs.getString("codigo_agencia"));
                    dto.setNombreAgencia(rs.getString("nombre_agencia"));

                    dto.setIdFormaAhorro(rs.getInt("id_forma_ahorro"));
                    dto.setCodigoForma(rs.getString("codigo_forma"));
                    dto.setNombreForma(rs.getString("nombre_forma"));

                    dto.setIdCuentaAhorro(rs.getInt("id_cuenta_ahorro"));
                    dto.setCodigoCuenta(rs.getString("codigo_cuenta"));

                    dto.setDocumento(rs.getString("documento"));
                    dto.setNombreCompleto(rs.getString("nombre_completo"));

                    dto.setTipoDocumentoSoporte(rs.getString("tipo_documento_soporte"));
                    dto.setDescripcionSoporte(rs.getString("descripcion_soporte"));

                    dto.setNumeroInicial(rs.getString("numero_inicial"));
                    dto.setNumeroFinal(rs.getString("numero_final"));
                    dto.setCantidadDocumentos(rs.getLong("cantidad_documentos"));

                    dto.setEstadoDocumento(rs.getString("estado_documento"));
                    dto.setDescripcionEstado(rs.getString("descripcion_estado"));

                    dto.setFechaEntrega(rs.getString("fecha_entrega"));
                    dto.setFechaEstado(rs.getString("fecha_estado"));
                    dto.setFechaCreacion(rs.getString("fecha_creacion"));

                    return dto;
                }
        );

    }

    public DocumentoSoporteResumenDTO resumen(
            String agencia,
            String fechaDesde,
            String fechaHasta
    ) {

        String sql = """
        SELECT
            COUNT(*) AS total_documentos,

            SUM(CASE WHEN ds.estado_documento = 'A' THEN 1 ELSE 0 END) AS activos,
            SUM(CASE WHEN ds.estado_documento = 'I' THEN 1 ELSE 0 END) AS inactivos,
            SUM(CASE WHEN ds.estado_documento = 'P' THEN 1 ELSE 0 END) AS perdidos,
            SUM(CASE WHEN ds.estado_documento = 'R' THEN 1 ELSE 0 END) AS robados,

            COALESCE(SUM(
                CAST(ds.numero_final AS bigint)
                - CAST(ds.numero_inicial AS bigint)
                + 1
            ), 0) AS total_documentos_fisicos

        FROM depositos.documentos_soporte ds

        INNER JOIN depositos.cuentas_ahorro c
            ON c.id_cuenta_ahorro = ds.id_cuenta_ahorro

        WHERE (:agencia = '0' OR c.id_agencia = CAST(:agencia AS integer))
          AND ds.fecha_entrega >= CAST(:fechaDesde AS date)
          AND ds.fecha_entrega <= CAST(:fechaHasta AS date)
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("agencia", agencia)
                        .addValue("fechaDesde", fechaDesde)
                        .addValue("fechaHasta", fechaHasta),
                (rs, i) -> {
                    DocumentoSoporteResumenDTO dto =
                            new DocumentoSoporteResumenDTO();

                    dto.setTotalDocumentos(rs.getLong("total_documentos"));
                    dto.setActivos(rs.getLong("activos"));
                    dto.setInactivos(rs.getLong("inactivos"));
                    dto.setPerdidos(rs.getLong("perdidos"));
                    dto.setRobados(rs.getLong("robados"));
                    dto.setTotalDocumentosFisicos(rs.getLong("total_documentos_fisicos"));

                    return dto;
                }
        );

    }

    public List<DocumentoSoporteResumenAgenciaFormaDTO> resumenPorAgenciaForma(
            String agencia,
            String fechaDesde,
            String fechaHasta
    ) {

        String sql = """
        SELECT
            a.id_agencia,
            LPAD(a.codigo_agencia, 2, '0') AS codigo_agencia,
            a.nombre_agencia,

            f.id_forma_ahorro,
            LPAD(f.codigo_forma, 2, '0') AS codigo_forma,
            f.nombre_forma,

            ds.tipo_documento_soporte,
            tds.descripcion_soporte,

            COUNT(*) AS total_documentos,

            SUM(CASE WHEN ds.estado_documento = 'A' THEN 1 ELSE 0 END) AS activos,
            SUM(CASE WHEN ds.estado_documento = 'I' THEN 1 ELSE 0 END) AS inactivos,
            SUM(CASE WHEN ds.estado_documento = 'P' THEN 1 ELSE 0 END) AS perdidos,
            SUM(CASE WHEN ds.estado_documento = 'R' THEN 1 ELSE 0 END) AS robados,

            COALESCE(SUM(
                CAST(ds.numero_final AS bigint)
                - CAST(ds.numero_inicial AS bigint)
                + 1
            ), 0) AS total_documentos_fisicos

        FROM depositos.documentos_soporte ds

        INNER JOIN depositos.cuentas_ahorro c
            ON c.id_cuenta_ahorro = ds.id_cuenta_ahorro

        INNER JOIN general.datos_agencias a
            ON a.id_agencia = c.id_agencia

        INNER JOIN depositos.formas_ahorro f
            ON f.id_forma_ahorro = c.id_forma_ahorro

        LEFT JOIN depositos.tipos_documentos_soporte tds
            ON tds.codigo_soporte = ds.tipo_documento_soporte

        WHERE (:agencia = '0' OR c.id_agencia = CAST(:agencia AS integer))
          AND ds.fecha_entrega >= CAST(:fechaDesde AS date)
          AND ds.fecha_entrega <= CAST(:fechaHasta AS date)

        GROUP BY
            a.id_agencia,
            a.codigo_agencia,
            a.nombre_agencia,
            f.id_forma_ahorro,
            f.codigo_forma,
            f.nombre_forma,
            ds.tipo_documento_soporte,
            tds.descripcion_soporte

        ORDER BY
            codigo_agencia,
            codigo_forma,
            ds.tipo_documento_soporte
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("agencia", agencia)
                        .addValue("fechaDesde", fechaDesde)
                        .addValue("fechaHasta", fechaHasta),
                (rs, i) -> {
                    DocumentoSoporteResumenAgenciaFormaDTO dto =
                            new DocumentoSoporteResumenAgenciaFormaDTO();

                    dto.setIdAgencia(rs.getInt("id_agencia"));
                    dto.setCodigoAgencia(rs.getString("codigo_agencia"));
                    dto.setNombreAgencia(rs.getString("nombre_agencia"));

                    dto.setIdFormaAhorro(rs.getInt("id_forma_ahorro"));
                    dto.setCodigoForma(rs.getString("codigo_forma"));
                    dto.setNombreForma(rs.getString("nombre_forma"));

                    dto.setTipoDocumentoSoporte(rs.getString("tipo_documento_soporte"));
                    dto.setDescripcionSoporte(rs.getString("descripcion_soporte"));

                    dto.setTotalDocumentos(rs.getLong("total_documentos"));
                    dto.setActivos(rs.getLong("activos"));
                    dto.setInactivos(rs.getLong("inactivos"));
                    dto.setPerdidos(rs.getLong("perdidos"));
                    dto.setRobados(rs.getLong("robados"));
                    dto.setTotalDocumentosFisicos(rs.getLong("total_documentos_fisicos"));

                    return dto;
                }
        );

    }

}