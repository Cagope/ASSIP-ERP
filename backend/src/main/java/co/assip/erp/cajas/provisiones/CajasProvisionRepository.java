package co.assip.erp.cajas.provisiones;

import co.assip.erp.cajas.provisiones.dto.CajaAperturaDTO;
import co.assip.erp.cajas.provisiones.dto.CajaDisponibleDTO;
import co.assip.erp.cajas.provisiones.dto.CajaEstadoDTO;
import co.assip.erp.cajas.provisiones.dto.CajaProvisionActivaDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionFormDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionListDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionSaveDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CajasProvisionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<CajasProvisionListDTO> listar() {

        String sql = """
            SELECT
                p.id_provision,
                c.id_caja,
                c.codigo_caja,
                c.descripcion,
                p.fecha_contable,
                p.estado,
                p.efectivo_inicio,
                p.efectivo_fin,
                p.cheques_inicio,
                p.cheques_fin
            FROM cajas.provisiones_diarias p
            JOIN cajas.cajas c
              ON c.id_caja = p.id_caja
            ORDER BY
                p.fecha_contable DESC,
                c.codigo_caja
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) -> CajasProvisionListDTO.builder()
                        .idProvision(rs.getLong("id_provision"))
                        .idCaja(rs.getLong("id_caja"))
                        .codigoCaja(rs.getString("codigo_caja"))
                        .descripcionCaja(rs.getString("descripcion"))
                        .fechaContable(
                                rs.getObject(
                                        "fecha_contable",
                                        java.time.LocalDate.class
                                )
                        )
                        .estado(rs.getString("estado"))
                        .efectivoInicio(rs.getBigDecimal("efectivo_inicio"))
                        .efectivoFin(rs.getBigDecimal("efectivo_fin"))
                        .chequesInicio(rs.getBigDecimal("cheques_inicio"))
                        .chequesFin(rs.getBigDecimal("cheques_fin"))
                        .build()
        );
    }

    public Optional<CajasProvisionFormDTO> obtenerPorId(
            Long idProvision
    ) {

        String sql = """
            SELECT
                id_provision,
                id_caja,
                fecha_contable,
                estado,
                efectivo_inicio,
                cheques_inicio,
                fk_usuario_apertura
            FROM cajas.provisiones_diarias
            WHERE id_provision = :idProvision
            """;

        List<CajasProvisionFormDTO> result = jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idProvision", idProvision),
                (rs, rowNum) -> CajasProvisionFormDTO.builder()
                        .idProvision(rs.getLong("id_provision"))
                        .idCaja(rs.getLong("id_caja"))
                        .fechaContable(
                                rs.getObject(
                                        "fecha_contable",
                                        java.time.LocalDate.class
                                )
                        )
                        .estado(rs.getString("estado"))
                        .efectivoInicio(rs.getBigDecimal("efectivo_inicio"))
                        .chequesInicio(rs.getBigDecimal("cheques_inicio"))
                        .fkUsuarioApertura(
                                rs.getObject("fk_usuario_apertura") != null
                                        ? rs.getInt("fk_usuario_apertura")
                                        : null
                        )
                        .build()
        );

        return result.stream().findFirst();
    }

    public boolean existeProvision(
            Long idCaja,
            java.time.LocalDate fechaContable,
            Long idProvisionExcluir
    ) {

        String sql = """
            SELECT COUNT(*)
            FROM cajas.provisiones_diarias
            WHERE id_caja = :idCaja
              AND fecha_contable = :fechaContable
              AND (
                    :idProvisionExcluir IS NULL
                    OR id_provision <> :idProvisionExcluir
                  )
            """;

        Integer count = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCaja", idCaja)
                        .addValue("fechaContable", fechaContable)
                        .addValue(
                                "idProvisionExcluir",
                                idProvisionExcluir
                        ),
                Integer.class
        );

        return count != null && count > 0;
    }

    public Long crear(
            CajasProvisionSaveDTO dto,
            Integer idUsuario
    ) {

        String sql = """
            INSERT INTO cajas.provisiones_diarias (
                id_caja,
                fecha_contable,
                estado,
                efectivo_inicio,
                cheques_inicio,
                fk_usuario_apertura,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            VALUES (
                :idCaja,
                :fechaContable,
                'ABIERTA',
                :efectivoInicio,
                :chequesInicio,
                :idUsuario,
                :idUsuario,
                :idUsuario
            )
            RETURNING id_provision
            """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCaja", dto.getIdCaja())
                        .addValue(
                                "fechaContable",
                                dto.getFechaContable()
                        )
                        .addValue(
                                "efectivoInicio",
                                dto.getEfectivoInicio()
                        )
                        .addValue(
                                "chequesInicio",
                                dto.getChequesInicio()
                        )
                        .addValue("idUsuario", idUsuario),
                Long.class
        );
    }

    /**
     * Prepara la apertura de la caja para la jornada actual.
     *
     * La fecha de jornada proviene exclusivamente de PostgreSQL.
     *
     * Prioridad de valores:
     *
     * 1. Provisión existente hoy.
     * 2. Último cierre anterior.
     * 3. Cero si la caja nunca ha tenido cierre.
     */
    public Optional<CajaAperturaDTO> prepararApertura(
            Long idCaja
    ) {

        String sql = """
            SELECT
                c.id_caja,
                c.id_agencia,
                c.codigo_caja,
                c.descripcion AS descripcion_caja,

                CURRENT_DATE AS fecha_jornada,

                actual.id_provision,
                actual.estado,

                COALESCE(
                    actual.efectivo_inicio,
                    anterior.efectivo_fin,
                    0
                ) AS efectivo_inicio,

                COALESCE(
                    actual.cheques_inicio,
                    anterior.cheques_fin,
                    0
                ) AS cheques_inicio,

                (actual.id_provision IS NOT NULL)
                    AS tiene_provision,

                (anterior.id_provision IS NOT NULL)
                    AS tiene_cierre_anterior,

                anterior.fecha_contable
                    AS fecha_ultimo_cierre

            FROM cajas.cajas c

            LEFT JOIN LATERAL (
                SELECT
                    p.id_provision,
                    p.estado,
                    p.efectivo_inicio,
                    p.cheques_inicio
                FROM cajas.provisiones_diarias p
                WHERE p.id_caja = c.id_caja
                  AND p.fecha_contable = CURRENT_DATE
                LIMIT 1
            ) actual
                ON TRUE

            LEFT JOIN LATERAL (
                SELECT
                    p.id_provision,
                    p.fecha_contable,
                    p.efectivo_fin,
                    p.cheques_fin
                FROM cajas.provisiones_diarias p
                WHERE p.id_caja = c.id_caja
                  AND p.estado = 'CERRADA'
                  AND p.fecha_contable < CURRENT_DATE
                ORDER BY p.fecha_contable DESC
                LIMIT 1
            ) anterior
                ON TRUE

            WHERE c.id_caja = :idCaja
              AND c.activa = TRUE
            """;

        List<CajaAperturaDTO> result = jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCaja", idCaja),
                (rs, rowNum) -> CajaAperturaDTO.builder()
                        .idCaja(rs.getLong("id_caja"))
                        .idAgencia(rs.getInt("id_agencia"))
                        .codigoCaja(rs.getString("codigo_caja"))
                        .descripcionCaja(
                                rs.getString("descripcion_caja")
                        )
                        .fechaJornada(
                                rs.getObject(
                                        "fecha_jornada",
                                        java.time.LocalDate.class
                                )
                        )
                        .idProvision(
                                rs.getObject("id_provision") != null
                                        ? rs.getLong("id_provision")
                                        : null
                        )
                        .estado(rs.getString("estado"))
                        .efectivoInicio(
                                rs.getBigDecimal("efectivo_inicio")
                        )
                        .chequesInicio(
                                rs.getBigDecimal("cheques_inicio")
                        )
                        .tieneProvision(
                                rs.getBoolean("tiene_provision")
                        )
                        .tieneCierreAnterior(
                                rs.getBoolean(
                                        "tiene_cierre_anterior"
                                )
                        )
                        .fechaUltimoCierre(
                                rs.getObject(
                                        "fecha_ultimo_cierre",
                                        java.time.LocalDate.class
                                )
                        )
                        .build()
        );

        return result.stream().findFirst();
    }

    public Long crearApertura(
            Long idCaja,
            BigDecimal efectivoInicio,
            BigDecimal chequesInicio,
            Integer idUsuario
    ) {

        String sql = """
            INSERT INTO cajas.provisiones_diarias (
                id_caja,
                fecha_contable,
                estado,
                efectivo_inicio,
                cheques_inicio,
                fk_usuario_apertura,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            VALUES (
                :idCaja,
                CURRENT_DATE,
                'ABIERTA',
                :efectivoInicio,
                :chequesInicio,
                :idUsuario,
                :idUsuario,
                :idUsuario
            )
            RETURNING id_provision
            """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCaja", idCaja)
                        .addValue(
                                "efectivoInicio",
                                efectivoInicio
                        )
                        .addValue(
                                "chequesInicio",
                                chequesInicio
                        )
                        .addValue("idUsuario", idUsuario),
                Long.class
        );
    }

    public void actualizar(
            CajasProvisionSaveDTO dto,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cajas.provisiones_diarias
            SET
                id_caja = :idCaja,
                fecha_contable = :fechaContable,
                efectivo_inicio = :efectivoInicio,
                cheques_inicio = :chequesInicio,
                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_provision = :idProvision
              AND estado = 'ABIERTA'
            """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idProvision",
                                dto.getIdProvision()
                        )
                        .addValue("idCaja", dto.getIdCaja())
                        .addValue(
                                "fechaContable",
                                dto.getFechaContable()
                        )
                        .addValue(
                                "efectivoInicio",
                                dto.getEfectivoInicio()
                        )
                        .addValue(
                                "chequesInicio",
                                dto.getChequesInicio()
                        )
                        .addValue("idUsuario", idUsuario)
        );
    }

    public void cerrar(
            Long idProvision,
            BigDecimal efectivoFin,
            BigDecimal chequesFin,
            String observacion,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cajas.provisiones_diarias
            SET
                estado = 'CERRADA',
                efectivo_fin = :efectivoFin,
                cheques_fin = :chequesFin,
                observacion_cierre = :observacion,
                fk_usuario_cierre = :idUsuario,
                fecha_cierre = CURRENT_TIMESTAMP,
                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_provision = :idProvision
              AND estado = 'ABIERTA'
            """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idProvision",
                                idProvision
                        )
                        .addValue(
                                "efectivoFin",
                                efectivoFin
                        )
                        .addValue(
                                "chequesFin",
                                chequesFin
                        )
                        .addValue(
                                "observacion",
                                observacion
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        )
        );
    }

    public List<CajasProvisionListDTO> listarCajasAbiertas(
            Integer idAgencia,
            java.time.LocalDate fecha
    ) {

        String sql = """
            SELECT
                p.id_provision,
                c.id_caja,
                c.codigo_caja,
                c.descripcion,
                p.fecha_contable,
                p.estado,
                p.efectivo_inicio,
                p.efectivo_fin,
                p.cheques_inicio,
                p.cheques_fin
            FROM cajas.provisiones_diarias p
            JOIN cajas.cajas c
              ON c.id_caja = p.id_caja
            WHERE c.id_agencia = :idAgencia
              AND p.fecha_contable = :fecha
              AND p.estado = 'ABIERTA'
              AND c.activa = TRUE
            ORDER BY c.codigo_caja
            """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idAgencia", idAgencia)
                        .addValue("fecha", fecha),
                (rs, rowNum) -> CajasProvisionListDTO.builder()
                        .idProvision(rs.getLong("id_provision"))
                        .idCaja(rs.getLong("id_caja"))
                        .codigoCaja(rs.getString("codigo_caja"))
                        .descripcionCaja(
                                rs.getString("descripcion")
                        )
                        .fechaContable(
                                rs.getObject(
                                        "fecha_contable",
                                        java.time.LocalDate.class
                                )
                        )
                        .estado(rs.getString("estado"))
                        .efectivoInicio(
                                rs.getBigDecimal("efectivo_inicio")
                        )
                        .efectivoFin(
                                rs.getBigDecimal("efectivo_fin")
                        )
                        .chequesInicio(
                                rs.getBigDecimal("cheques_inicio")
                        )
                        .chequesFin(
                                rs.getBigDecimal("cheques_fin")
                        )
                        .build()
        );
    }

    public List<CajaDisponibleDTO> listarCajasDisponibles(
            Integer idAgencia
    ) {

        String sql = """
            SELECT
                id_caja,
                id_agencia,
                codigo_caja,
                descripcion,
                permite_efectivo,
                permite_cheques,
                permite_tarjetas,
                permite_transferencias
            FROM cajas.cajas
            WHERE id_agencia = :idAgencia
              AND activa = TRUE
            ORDER BY codigo_caja
            """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idAgencia", idAgencia),
                (rs, rowNum) -> CajaDisponibleDTO.builder()
                        .idCaja(rs.getLong("id_caja"))
                        .idAgencia(rs.getInt("id_agencia"))
                        .codigoCaja(
                                rs.getString("codigo_caja")
                        )
                        .descripcionCaja(
                                rs.getString("descripcion")
                        )
                        .permiteEfectivo(
                                rs.getBoolean("permite_efectivo")
                        )
                        .permiteCheques(
                                rs.getBoolean("permite_cheques")
                        )
                        .permiteTarjetas(
                                rs.getBoolean("permite_tarjetas")
                        )
                        .permiteTransferencias(
                                rs.getBoolean(
                                        "permite_transferencias"
                                )
                        )
                        .build()
        );
    }

    public List<CajaEstadoDTO> listarEstadoCajas(
            Integer idAgencia,
            java.time.LocalDate fechaContable
    ) {

        String sql = """
            SELECT
                c.id_caja,
                c.codigo_caja,
                c.descripcion,

                p.id_provision,
                p.estado,
                p.fk_usuario_apertura,
                p.fecha_apertura,

                CASE
                    WHEN p.id_provision IS NULL THEN TRUE
                    ELSE FALSE
                END AS disponible

            FROM cajas.cajas c

            LEFT JOIN cajas.provisiones_diarias p
                   ON p.id_caja = c.id_caja
                  AND p.fecha_contable = :fechaContable
                  AND p.estado = 'ABIERTA'

            WHERE c.id_agencia = :idAgencia
              AND c.activa = TRUE

            ORDER BY c.codigo_caja
            """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idAgencia",
                                idAgencia
                        )
                        .addValue(
                                "fechaContable",
                                fechaContable
                        ),
                (rs, rowNum) -> CajaEstadoDTO.builder()
                        .idCaja(rs.getLong("id_caja"))
                        .codigoCaja(
                                rs.getString("codigo_caja")
                        )
                        .descripcionCaja(
                                rs.getString("descripcion")
                        )
                        .idProvision(
                                rs.getObject("id_provision") != null
                                        ? rs.getLong("id_provision")
                                        : null
                        )
                        .estado(rs.getString("estado"))
                        .fkUsuarioApertura(
                                rs.getObject(
                                        "fk_usuario_apertura"
                                ) != null
                                        ? rs.getInt(
                                        "fk_usuario_apertura"
                                )
                                        : null
                        )
                        .fechaApertura(
                                rs.getObject(
                                        "fecha_apertura",
                                        java.time.LocalDateTime.class
                                )
                        )
                        .disponible(
                                rs.getBoolean("disponible")
                        )
                        .build()
        );
    }

    public Optional<CajaProvisionActivaDTO>
    obtenerProvisionActivaCaja(
            Long idCaja
    ) {

        String sql = """
            SELECT
                c.id_caja,
                c.codigo_caja,
                c.descripcion AS descripcion_caja,

                a.id_agencia,
                a.codigo_agencia,
                a.nombre_agencia,

                p.id_provision,
                p.fecha_contable,
                p.estado

            FROM cajas.provisiones_diarias p

            INNER JOIN cajas.cajas c
                    ON c.id_caja = p.id_caja

            INNER JOIN general.datos_agencias a
                    ON a.id_agencia = c.id_agencia

            WHERE p.estado = 'ABIERTA'
              AND p.id_caja = :idCaja
              AND p.fecha_contable = CURRENT_DATE

            LIMIT 1
            """;

        List<CajaProvisionActivaDTO> result = jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCaja", idCaja),
                (rs, rowNum) ->
                        CajaProvisionActivaDTO.builder()
                                .idCaja(
                                        rs.getLong("id_caja")
                                )
                                .codigoCaja(
                                        rs.getString(
                                                "codigo_caja"
                                        )
                                )
                                .descripcionCaja(
                                        rs.getString(
                                                "descripcion_caja"
                                        )
                                )
                                .idAgencia(
                                        rs.getInt("id_agencia")
                                )
                                .codigoAgencia(
                                        rs.getString(
                                                "codigo_agencia"
                                        )
                                )
                                .nombreAgencia(
                                        rs.getString(
                                                "nombre_agencia"
                                        )
                                )
                                .idProvision(
                                        rs.getLong(
                                                "id_provision"
                                        )
                                )
                                .fechaContable(
                                        rs.getObject(
                                                "fecha_contable",
                                                java.time.LocalDate.class
                                        )
                                )
                                .estado(
                                        rs.getString("estado")
                                )
                                .build()
        );

        return result.stream().findFirst();
    }
}