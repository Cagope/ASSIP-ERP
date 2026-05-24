package co.assip.erp.depositos.documentos_soporte;

import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteBusquedaDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteCuentaDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteFormDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteHistoricoDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteGuardarDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DocumentosSoporteRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<DocumentosSoporteCuentaDTO> buscarCuentas(
            DocumentosSoporteBusquedaDTO filtros
    ) {

        String sql = """
        SELECT
            c.id_cuenta_ahorro,
            c.id_forma_ahorro,
            c.codigo_cuenta,

            hv.documento,

            CASE
                WHEN hv.tipo_persona = '2'
                    THEN hv.nombre_completo_nombres
                ELSE TRIM(
                    COALESCE(hv.nombre_completo_apellidos, '') || ' ' ||
                    COALESCE(hv.nombre_completo_nombres, '')
                )
            END AS nombre_completo,

            f.nombre_forma,
            f.documento_forma,

            tds.descripcion_soporte,
            tds.cantidad_soporte,

            COALESCE(c.saldo_actual_cuenta, 0)::numeric(18,2)
                AS saldo_actual,

            ea.descripcion_estado_ahorro
                AS estado_cuenta,

            EXISTS (
                SELECT 1
                FROM depositos.documentos_soporte ds
                INNER JOIN depositos.estados_documentos_soporte eds
                    ON eds.codigo_estado_documento_soporte = ds.estado_documento
                WHERE ds.id_cuenta_ahorro = c.id_cuenta_ahorro
                  AND eds.operativo = true
            ) AS tiene_documento_activo

        FROM depositos.cuentas_ahorro c

        INNER JOIN depositos.formas_ahorro f
            ON f.id_forma_ahorro = c.id_forma_ahorro

        INNER JOIN depositos.tipos_documentos_soporte tds
            ON tds.codigo_soporte = f.documento_forma

        LEFT JOIN depositos.estados_ahorros ea
            ON ea.codigo_estado_ahorro = c.estado_cuenta_cuenta

        LEFT JOIN LATERAL (
            SELECT
                hv1.*
            FROM reporting.vw_hoja_vida_general_total_extendida hv1
            WHERE hv1.id_datos_personal = c.id_datos_personal
            LIMIT 1
        ) hv ON true

        WHERE c.id_agencia = ?
          AND c.estado_cuenta_cuenta = 'A'
          AND f.documento_forma IN ('L', 'O')
    """;

        StringBuilder filtrosSql = new StringBuilder();

        List<Object> params = new java.util.ArrayList<>();

        params.add(filtros.getIdAgencia());

        if (filtros.getDocumento() != null
                && !filtros.getDocumento().isBlank()) {

            filtrosSql.append("""
            
              AND hv.documento ILIKE ?
        """);

            params.add("%" + filtros.getDocumento().trim() + "%");
        }

        if (filtros.getNombres() != null
                && !filtros.getNombres().isBlank()) {

            filtrosSql.append("""
        
              AND hv.nombre_completo_nombres ILIKE ?
        """);

            params.add("%" + filtros.getNombres().trim() + "%");
        }

        if (filtros.getPrimerApellido() != null
                && !filtros.getPrimerApellido().isBlank()) {

            filtrosSql.append("""
        
              AND hv.primer_apellido ILIKE ?
        """);

            params.add("%" + filtros.getPrimerApellido().trim() + "%");
        }

        if (filtros.getSegundoApellido() != null
                && !filtros.getSegundoApellido().isBlank()) {

            filtrosSql.append("""
        
              AND hv.segundo_apellido ILIKE ?
        """);

            params.add("%" + filtros.getSegundoApellido().trim() + "%");
        }

        sql += filtrosSql;

        sql += """
        
        ORDER BY
            hv.nombre_completo_apellidos,
            hv.nombre_completo_nombres,
            c.codigo_cuenta
        LIMIT 100
    """;

        return jdbcTemplate.query(
                sql,
                params.toArray(),
                (rs, rowNum) ->
                        DocumentosSoporteCuentaDTO.builder()
                                .idCuentaAhorro(
                                        rs.getInt("id_cuenta_ahorro")
                                )
                                .idFormaAhorro(
                                        rs.getInt("id_forma_ahorro")
                                )
                                .codigoCuenta(
                                        rs.getString("codigo_cuenta")
                                )
                                .documento(
                                        rs.getString("documento")
                                )
                                .nombreCompleto(
                                        rs.getString("nombre_completo")
                                )
                                .nombreForma(
                                        rs.getString("nombre_forma")
                                )
                                .documentoForma(
                                        rs.getString("documento_forma")
                                )
                                .descripcionSoporte(
                                        rs.getString("descripcion_soporte")
                                )
                                .cantidadSoporte(
                                        rs.getInt("cantidad_soporte")
                                )
                                .saldoActual(
                                        rs.getBigDecimal("saldo_actual")
                                )
                                .estadoCuenta(
                                        rs.getString("estado_cuenta")
                                )
                                .tieneDocumentoActivo(
                                        rs.getBoolean("tiene_documento_activo")
                                )
                                .build()
        );

    }

    public Optional<DocumentosSoporteFormDTO> obtenerDocumentoActivo(
            Integer idCuentaAhorro
    ) {

        String sql = """
        SELECT
            ds.id_documento_soporte,
            ds.tipo_documento_soporte,
            ds.numero_inicial,
            ds.numero_final,
            ds.fecha_entrega,
            ds.estado_documento,
            ds.fecha_estado

        FROM depositos.documentos_soporte ds

        INNER JOIN depositos.estados_documentos_soporte eds
            ON eds.codigo_estado_documento_soporte =
               ds.estado_documento

        WHERE ds.id_cuenta_ahorro = ?
          AND eds.operativo = true

        ORDER BY
            ds.fecha_entrega DESC,
            ds.id_documento_soporte DESC

        LIMIT 1
    """;

        List<DocumentosSoporteFormDTO> lista =
                jdbcTemplate.query(
                        sql,
                        new Object[]{
                                idCuentaAhorro
                        },
                        (rs, rowNum) ->
                                DocumentosSoporteFormDTO.builder()
                                        .idDocumentoSoporte(
                                                rs.getInt(
                                                        "id_documento_soporte"
                                                )
                                        )
                                        .tipoDocumentoSoporte(
                                                rs.getString(
                                                        "tipo_documento_soporte"
                                                )
                                        )
                                        .numeroInicial(
                                                rs.getString(
                                                        "numero_inicial"
                                                )
                                        )
                                        .numeroFinal(
                                                rs.getString(
                                                        "numero_final"
                                                )
                                        )
                                        .fechaEntrega(
                                                rs.getDate(
                                                        "fecha_entrega"
                                                ).toLocalDate()
                                        )
                                        .estadoDocumento(
                                                rs.getString(
                                                        "estado_documento"
                                                )
                                        )
                                        .fechaEstado(
                                                rs.getDate(
                                                        "fecha_estado"
                                                ).toLocalDate()
                                        )
                                        .build()
                );

        if (lista.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(lista.get(0));

    }

    public List<DocumentosSoporteHistoricoDTO> obtenerHistorico(
            Integer idCuentaAhorro
    ) {

        String sql = """
        SELECT
            ds.id_documento_soporte,
            ds.tipo_documento_soporte,
            ds.numero_inicial,
            ds.numero_final,
            ds.estado_documento,
            ds.fecha_entrega,
            ds.fecha_estado,
            ds.fk_seguridad_creacion,
            ds.fecha_creacion

        FROM depositos.documentos_soporte ds

        WHERE ds.id_cuenta_ahorro = ?

        ORDER BY
            ds.fecha_entrega DESC,
            ds.id_documento_soporte DESC
    """;

        return jdbcTemplate.query(
                sql,
                new Object[]{
                        idCuentaAhorro
                },
                (rs, rowNum) ->
                        DocumentosSoporteHistoricoDTO.builder()
                                .idDocumentoSoporte(
                                        rs.getInt(
                                                "id_documento_soporte"
                                        )
                                )
                                .tipoDocumentoSoporte(
                                        rs.getString(
                                                "tipo_documento_soporte"
                                        )
                                )
                                .numeroInicial(
                                        rs.getString(
                                                "numero_inicial"
                                        )
                                )
                                .numeroFinal(
                                        rs.getString(
                                                "numero_final"
                                        )
                                )
                                .estadoDocumento(
                                        rs.getString(
                                                "estado_documento"
                                        )
                                )
                                .fechaEntrega(
                                        rs.getDate(
                                                "fecha_entrega"
                                        ).toLocalDate()
                                )
                                .fechaEstado(
                                        rs.getDate(
                                                "fecha_estado"
                                        ).toLocalDate()
                                )
                                .usuarioCreacion(
                                        rs.getInt(
                                                "fk_seguridad_creacion"
                                        )
                                )
                                .fechaCreacion(
                                        rs.getTimestamp(
                                                "fecha_creacion"
                                        ).toLocalDateTime()
                                )
                                .build()
        );

    }

    public boolean existeRangoActivo(
            Integer idFormaAhorro,
            String tipoDocumento,
            String numeroInicial,
            String numeroFinal
    ) {

        String sql = """
        SELECT COUNT(*)

        FROM depositos.documentos_soporte ds

        INNER JOIN depositos.cuentas_ahorro c
            ON c.id_cuenta_ahorro = ds.id_cuenta_ahorro

        INNER JOIN depositos.estados_documentos_soporte eds
            ON eds.codigo_estado_documento_soporte =
               ds.estado_documento

        WHERE c.id_forma_ahorro = ?
          AND ds.tipo_documento_soporte = ?
          AND eds.operativo = true

          AND (
                CAST(? AS bigint)
                    <= CAST(ds.numero_final AS bigint)

            AND CAST(? AS bigint)
                    >= CAST(ds.numero_inicial AS bigint)
          )
    """;

        Integer total =
                jdbcTemplate.queryForObject(
                        sql,
                        Integer.class,
                        idFormaAhorro,
                        tipoDocumento,
                        numeroInicial,
                        numeroFinal
                );

        return total != null && total > 0;

    }

    public Integer guardar(
            DocumentosSoporteGuardarDTO dto,
            String tipoDocumento,
            String numeroInicial,
            String numeroFinal
    ) {

        String sql = """
    INSERT INTO depositos.documentos_soporte (

        id_cuenta_ahorro,
        tipo_documento_soporte,

        numero_inicial,
        numero_final,

        fecha_entrega,

        estado_documento,
        fecha_estado,

        fk_seguridad_creacion,
        fecha_creacion,

        fk_seguridad_edicion,
        fecha_edicion

    )
    VALUES (

        ?,
        ?,

        ?,
        ?,

        ?,

        ?,
        ?,

        ?,
        now(),

        ?,
        now()

    )
    RETURNING id_documento_soporte
""";

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,

                dto.getIdCuentaAhorro(),

                tipoDocumento,

                numeroInicial,
                numeroFinal,

                dto.getFechaEntrega(),

                "A",
                dto.getFechaEntrega(),

                1,
                1
        );

    }

    public void actualizarEstadoDocumento(
            Integer idDocumentoSoporte,
            String estado
    ) {

        String sql = """
        UPDATE depositos.documentos_soporte
           SET estado_documento = ?,
               fecha_estado = CURRENT_DATE,
               fk_seguridad_edicion = ?,
               fecha_edicion = now()
         WHERE id_documento_soporte = ?
    """;

        jdbcTemplate.update(
                sql,
                estado,
                1,
                idDocumentoSoporte
        );

    }

    public Optional<DocumentosSoporteCuentaDTO> obtenerDatosCuenta(
            Integer idCuentaAhorro
    ) {

        String sql = """
        SELECT
            c.id_cuenta_ahorro,
            c.id_forma_ahorro,
            c.codigo_cuenta,

            hv.documento,

            CASE
                WHEN hv.tipo_persona = '2'
                    THEN hv.nombre_completo_nombres
                ELSE TRIM(
                    COALESCE(hv.nombre_completo_apellidos, '') || ' ' ||
                    COALESCE(hv.nombre_completo_nombres, '')
                )
            END AS nombre_completo,

            f.nombre_forma,
            f.documento_forma,

            tds.descripcion_soporte,
            tds.cantidad_soporte,

            COALESCE(c.saldo_actual_cuenta, 0)::numeric(18,2)
                AS saldo_actual,

            ea.descripcion_estado_ahorro
                AS estado_cuenta

        FROM depositos.cuentas_ahorro c

        INNER JOIN depositos.formas_ahorro f
            ON f.id_forma_ahorro = c.id_forma_ahorro

        INNER JOIN depositos.tipos_documentos_soporte tds
            ON tds.codigo_soporte = f.documento_forma

        LEFT JOIN depositos.estados_ahorros ea
            ON ea.codigo_estado_ahorro =
               c.estado_cuenta_cuenta

        LEFT JOIN LATERAL (
            SELECT
                hv1.*
            FROM reporting.vw_hoja_vida_general_total_extendida hv1
            WHERE hv1.id_datos_personal = c.id_datos_personal
            LIMIT 1
        ) hv ON true

        WHERE c.id_cuenta_ahorro = ?
          AND f.documento_forma IN ('L', 'O')

        LIMIT 1
    """;

        List<DocumentosSoporteCuentaDTO> lista =
                jdbcTemplate.query(
                        sql,
                        new Object[]{
                                idCuentaAhorro
                        },
                        (rs, rowNum) ->
                                DocumentosSoporteCuentaDTO.builder()
                                        .idCuentaAhorro(
                                                rs.getInt("id_cuenta_ahorro")
                                        )
                                        .idFormaAhorro(
                                                rs.getInt("id_forma_ahorro")
                                        )
                                        .codigoCuenta(
                                                rs.getString("codigo_cuenta")
                                        )
                                        .documento(
                                                rs.getString("documento")
                                        )
                                        .nombreCompleto(
                                                rs.getString("nombre_completo")
                                        )
                                        .nombreForma(
                                                rs.getString("nombre_forma")
                                        )
                                        .documentoForma(
                                                rs.getString("documento_forma")
                                        )
                                        .descripcionSoporte(
                                                rs.getString("descripcion_soporte")
                                        )
                                        .cantidadSoporte(
                                                rs.getInt("cantidad_soporte")
                                        )
                                        .saldoActual(
                                                rs.getBigDecimal("saldo_actual")
                                        )
                                        .estadoCuenta(
                                                rs.getString("estado_cuenta")
                                        )
                                        .build()
                );

        if (lista.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(lista.get(0));

    }

}