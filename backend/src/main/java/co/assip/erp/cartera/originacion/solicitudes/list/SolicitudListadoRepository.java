package co.assip.erp.cartera.originacion.solicitudes.list;

import co.assip.erp.cartera.originacion.solicitudes.list.dto.SolicitudListadoDTO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SolicitudListadoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private final BeanPropertyRowMapper<SolicitudListadoDTO> rowMapper =
            BeanPropertyRowMapper.newInstance(SolicitudListadoDTO.class);


    public SolicitudListadoRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    // =========================================================
    // LISTAR SOLICITUDES
    // =========================================================

    public List<SolicitudListadoDTO> listar(
            Integer idAgencia,
            String numeroSolicitud,
            String documento,
            String nombreSolicitante,
            Integer idAsesor,
            Integer idSolicitudProceso,
            Integer idSolicitudResultado
    ) {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    id_solicitud_credito,
                    numero_solicitud,
                    fecha_inicio_solicitud,
                    fecha_ultima_gestion,

                    id_agencia,
                    codigo_agencia,
                    nombre_agencia,

                    id_datos_personal,
                    tipo_documento,
                    documento,
                    nombre_solicitante,

                    id_asesor,
                    usuario_asesor,
                    nombre_asesor,

                    id_solicitud_proceso,
                    nombre_proceso,

                    id_solicitud_resultado,
                    nombre_resultado,

                    valor_solicitado,

                    id_linea_credito,
                    codigo_linea_credito,
                    nombre_linea_credito,

                    activo

                FROM cartera.vw_solicitudes_originacion_lista

                WHERE activo = TRUE
                """);

        MapSqlParameterSource params =
                new MapSqlParameterSource();


        // =====================================================
        // AGENCIA
        // =====================================================

        if (idAgencia != null && idAgencia > 0) {

            sql.append("""
                    
                    AND id_agencia = :idAgencia
                    """);

            params.addValue(
                    "idAgencia",
                    idAgencia
            );
        }


        // =====================================================
        // NÚMERO DE SOLICITUD
        // =====================================================

        if (tieneTexto(numeroSolicitud)) {

            sql.append("""
                    
                    AND numero_solicitud ILIKE :numeroSolicitud
                    """);

            params.addValue(
                    "numeroSolicitud",
                    "%" + numeroSolicitud.trim() + "%"
            );
        }


        // =====================================================
        // DOCUMENTO
        // =====================================================

        if (tieneTexto(documento)) {

            sql.append("""
                    
                    AND documento ILIKE :documento
                    """);

            params.addValue(
                    "documento",
                    "%" + documento.trim() + "%"
            );
        }


        // =====================================================
        // NOMBRE DEL SOLICITANTE
        // =====================================================

        if (tieneTexto(nombreSolicitante)) {

            sql.append("""
                    
                    AND nombre_solicitante ILIKE :nombreSolicitante
                    """);

            params.addValue(
                    "nombreSolicitante",
                    "%" + nombreSolicitante.trim() + "%"
            );
        }


        // =====================================================
        // ASESOR
        // =====================================================

        if (idAsesor != null && idAsesor > 0) {

            sql.append("""
                    
                    AND id_asesor = :idAsesor
                    """);

            params.addValue(
                    "idAsesor",
                    idAsesor
            );
        }


        // =====================================================
        // PROCESO
        // =====================================================

        if (
                idSolicitudProceso != null
                        && idSolicitudProceso > 0
        ) {

            sql.append("""
                    
                    AND id_solicitud_proceso = :idSolicitudProceso
                    """);

            params.addValue(
                    "idSolicitudProceso",
                    idSolicitudProceso
            );
        }


        // =====================================================
        // RESULTADO
        // =====================================================

        if (
                idSolicitudResultado != null
                        && idSolicitudResultado > 0
        ) {

            sql.append("""
                    
                    AND id_solicitud_resultado = :idSolicitudResultado
                    """);

            params.addValue(
                    "idSolicitudResultado",
                    idSolicitudResultado
            );
        }


        // =====================================================
        // ORDEN
        // MÁS RECIENTES PRIMERO
        // =====================================================

        sql.append("""
                
                ORDER BY
                    fecha_inicio_solicitud DESC,
                    id_solicitud_credito DESC
                """);


        return jdbc.query(
                sql.toString(),
                params,
                rowMapper
        );
    }


    // =========================================================
    // UTILIDADES
    // =========================================================

    private boolean tieneTexto(String valor) {

        return valor != null
                && !valor.trim().isEmpty();
    }
}