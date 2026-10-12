package co.assip.erp.cajas.cierre;

import co.assip.erp.cajas.cierre.dto.CajaCierreMovimientosDTO;
import co.assip.erp.cajas.cierre.dto.CajaCierreResumenDTO;
import co.assip.erp.cajas.cierre.dto.CajaCierreVentanillaDTO;
import co.assip.erp.cajas.cierre.dto.CajaCierreMovimientoResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import co.assip.erp.cajas.cierre.dto.CajaCierreComprobanteDTO;
import co.assip.erp.cajas.cierre.dto.CajaCierreComprobantesResumenDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CajasCierreRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public Optional<CajaCierreResumenDTO> obtenerCabeceraProvision(
            Long idProvision
    ) {

        String sql = """
            SELECT
                p.id_provision,
                p.id_caja,
                c.id_agencia,
                c.codigo_caja,
                c.descripcion AS descripcion_caja,
                c.tipo_caja,
                p.fecha_contable,
                p.estado,
                p.efectivo_inicio,
                p.cheques_inicio
            FROM cajas.provisiones_diarias p
            INNER JOIN cajas.cajas c
                    ON c.id_caja = p.id_caja
            WHERE p.id_provision = :idProvision
              AND c.activa = TRUE
            """;

        List<CajaCierreResumenDTO> result = jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idProvision", idProvision),
                (rs, rowNum) -> CajaCierreResumenDTO.builder()
                        .idProvision(rs.getLong("id_provision"))
                        .idCaja(rs.getLong("id_caja"))
                        .idAgencia(rs.getInt("id_agencia"))
                        .codigoCaja(rs.getString("codigo_caja"))
                        .descripcionCaja(rs.getString("descripcion_caja"))
                        .tipoCaja(rs.getString("tipo_caja"))
                        .fechaContable(
                                rs.getObject(
                                        "fecha_contable",
                                        java.time.LocalDate.class
                                )
                        )
                        .estado(rs.getString("estado"))
                        .efectivoInicio(
                                nvl(rs.getBigDecimal("efectivo_inicio"))
                        )
                        .chequesInicio(
                                nvl(rs.getBigDecimal("cheques_inicio"))
                        )
                        .build()
        );

        return result.stream().findFirst();
    }

    public CajaCierreMovimientosDTO calcularMovimientos(
            Long idProvision
    ) {

        String sql = """
            SELECT
                COALESCE(SUM(
                    CASE
                        WHEN estado = 'REGISTRADO'
                         AND naturaleza = 'INGRESO'
                        THEN valor
                        ELSE 0
                    END
                ), 0) AS total_ingresos,

                COALESCE(SUM(
                    CASE
                        WHEN estado = 'REGISTRADO'
                         AND naturaleza = 'EGRESO'
                        THEN valor
                        ELSE 0
                    END
                ), 0) AS total_egresos,

                COALESCE(SUM(
                    CASE
                        WHEN estado = 'REGISTRADO'
                         AND naturaleza = 'INGRESO'
                         AND medio_pago = 'EFECTIVO'
                        THEN valor
                        ELSE 0
                    END
                ), 0) AS entradas_efectivo,

                COALESCE(SUM(
                    CASE
                        WHEN estado = 'REGISTRADO'
                         AND naturaleza = 'EGRESO'
                         AND medio_pago = 'EFECTIVO'
                        THEN valor
                        ELSE 0
                    END
                ), 0) AS salidas_efectivo,

                COALESCE(SUM(
                    CASE
                        WHEN estado = 'REGISTRADO'
                         AND naturaleza = 'INGRESO'
                         AND medio_pago = 'CHEQUE'
                        THEN valor
                        ELSE 0
                    END
                ), 0) AS entradas_cheques,

                COALESCE(SUM(
                    CASE
                        WHEN estado = 'REGISTRADO'
                         AND naturaleza = 'EGRESO'
                         AND medio_pago = 'CHEQUE'
                        THEN valor
                        ELSE 0
                    END
                ), 0) AS salidas_cheques,

                COUNT(*) FILTER (
                    WHERE estado = 'REGISTRADO'
                ) AS cantidad_movimientos

            FROM cajas.movimientos_caja
            WHERE id_provision = :idProvision
            """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idProvision", idProvision),
                (rs, rowNum) -> CajaCierreMovimientosDTO.builder()
                        .totalIngresos(
                                nvl(rs.getBigDecimal("total_ingresos"))
                        )
                        .totalEgresos(
                                nvl(rs.getBigDecimal("total_egresos"))
                        )
                        .entradasEfectivo(
                                nvl(rs.getBigDecimal("entradas_efectivo"))
                        )
                        .salidasEfectivo(
                                nvl(rs.getBigDecimal("salidas_efectivo"))
                        )
                        .entradasCheques(
                                nvl(rs.getBigDecimal("entradas_cheques"))
                        )
                        .salidasCheques(
                                nvl(rs.getBigDecimal("salidas_cheques"))
                        )
                        .cantidadMovimientos(
                                rs.getInt("cantidad_movimientos")
                        )
                        .build()
        );
    }

    public List<CajaCierreMovimientoResumenDTO> obtenerResumenMovimientos(
            Long idProvision
    ) {

        String sql = """
        SELECT
            d.id_departamento_operativo,
            d.codigo_departamento,
            d.nombre_departamento,
            m.naturaleza,

            COUNT(*) AS cantidad_movimientos,

            COALESCE(SUM(
                CASE
                    WHEN m.medio_pago = 'EFECTIVO'
                    THEN m.valor
                    ELSE 0
                END
            ), 0) AS valor_efectivo,

            COALESCE(SUM(
                CASE
                    WHEN m.medio_pago = 'CHEQUE'
                    THEN m.valor
                    ELSE 0
                END
            ), 0) AS valor_cheques,

            COALESCE(SUM(
                CASE
                    WHEN m.medio_pago NOT IN ('EFECTIVO', 'CHEQUE')
                    THEN m.valor
                    ELSE 0
                END
            ), 0) AS valor_otros,

            COALESCE(SUM(m.valor), 0) AS valor_total

        FROM cajas.movimientos_caja m

        INNER JOIN cajas.departamentos_operativos d
                ON d.id_departamento_operativo =
                   m.id_departamento_operativo

        WHERE m.id_provision = :idProvision
          AND m.estado = 'REGISTRADO'

        GROUP BY
            d.id_departamento_operativo,
            d.codigo_departamento,
            d.nombre_departamento,
            m.naturaleza

        ORDER BY
            CASE
                WHEN m.naturaleza = 'INGRESO' THEN 1
                WHEN m.naturaleza = 'EGRESO' THEN 2
                ELSE 3
            END,
            d.codigo_departamento
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idProvision", idProvision),
                (rs, rowNum) -> CajaCierreMovimientoResumenDTO.builder()
                        .idDepartamentoOperativo(
                                rs.getLong("id_departamento_operativo")
                        )
                        .codigoDepartamento(
                                rs.getString("codigo_departamento")
                        )
                        .nombreDepartamento(
                                rs.getString("nombre_departamento")
                        )
                        .naturaleza(
                                rs.getString("naturaleza")
                        )
                        .cantidadMovimientos(
                                rs.getInt("cantidad_movimientos")
                        )
                        .valorEfectivo(
                                nvl(rs.getBigDecimal("valor_efectivo"))
                        )
                        .valorCheques(
                                nvl(rs.getBigDecimal("valor_cheques"))
                        )
                        .valorOtros(
                                nvl(rs.getBigDecimal("valor_otros"))
                        )
                        .valorTotal(
                                nvl(rs.getBigDecimal("valor_total"))
                        )
                        .build()
        );
    }

    public List<CajaCierreComprobantesResumenDTO> obtenerResumenComprobantes(
            Long idProvision
    ) {

        String sql = """
        SELECT DISTINCT
            d.id_departamento_operativo,
            d.codigo_departamento,
            d.nombre_departamento,
            m.tipo_documento,
            m.numero_documento,
            m.tipo_comprobante,
            m.numero_comprobante

        FROM cajas.movimientos_caja m

        INNER JOIN cajas.departamentos_operativos d
                ON d.id_departamento_operativo =
                   m.id_departamento_operativo

        WHERE m.id_provision = :idProvision
          AND m.estado = 'REGISTRADO'

        ORDER BY
            d.codigo_departamento,
            m.tipo_documento,
            m.numero_documento,
            m.tipo_comprobante,
            m.numero_comprobante
        """;

        List<Map<String, Object>> filas = jdbc.queryForList(
                sql,
                new MapSqlParameterSource()
                        .addValue("idProvision", idProvision)
        );

        Map<Long, AcumuladorComprobantes> agrupados =
                new LinkedHashMap<>();

        for (Map<String, Object> fila : filas) {

            Long idDepartamento =
                    ((Number) fila.get(
                            "id_departamento_operativo"
                    )).longValue();

            AcumuladorComprobantes acumulador =
                    agrupados.computeIfAbsent(
                            idDepartamento,
                            id -> new AcumuladorComprobantes(
                                    id,
                                    (String) fila.get(
                                            "codigo_departamento"
                                    ),
                                    (String) fila.get(
                                            "nombre_departamento"
                                    )
                            )
                    );

            agregarComprobante(
                    acumulador.documentosOrigen,
                    (String) fila.get("tipo_documento"),
                    (String) fila.get("numero_documento")
            );

            agregarComprobante(
                    acumulador.comprobantesCaja,
                    (String) fila.get("tipo_comprobante"),
                    (String) fila.get("numero_comprobante")
            );
        }

        return agrupados.values()
                .stream()
                .map(acumulador ->
                        CajaCierreComprobantesResumenDTO.builder()
                                .idDepartamentoOperativo(
                                        acumulador.idDepartamentoOperativo
                                )
                                .codigoDepartamento(
                                        acumulador.codigoDepartamento
                                )
                                .nombreDepartamento(
                                        acumulador.nombreDepartamento
                                )
                                .documentosOrigen(
                                        List.copyOf(
                                                acumulador.documentosOrigen
                                        )
                                )
                                .comprobantesCaja(
                                        List.copyOf(
                                                acumulador.comprobantesCaja
                                        )
                                )
                                .build()
                )
                .toList();
    }

    private void agregarComprobante(
            List<CajaCierreComprobanteDTO> destino,
            String tipo,
            String numero
    ) {

        if (tipo == null
                || tipo.isBlank()
                || numero == null
                || numero.isBlank()) {
            return;
        }

        String tipoNormalizado = tipo.trim();
        String numeroNormalizado = numero.trim();

        boolean existe =
                destino.stream()
                        .anyMatch(item ->
                                tipoNormalizado.equals(
                                        item.getTipo()
                                )
                                        && numeroNormalizado.equals(
                                        item.getNumero()
                                )
                        );

        if (existe) {
            return;
        }

        destino.add(
                CajaCierreComprobanteDTO.builder()
                        .tipo(tipoNormalizado)
                        .numero(numeroNormalizado)
                        .build()
        );
    }

    private static class AcumuladorComprobantes {

        private final Long idDepartamentoOperativo;
        private final String codigoDepartamento;
        private final String nombreDepartamento;

        private final List<CajaCierreComprobanteDTO>
                documentosOrigen = new ArrayList<>();

        private final List<CajaCierreComprobanteDTO>
                comprobantesCaja = new ArrayList<>();

        private AcumuladorComprobantes(
                Long idDepartamentoOperativo,
                String codigoDepartamento,
                String nombreDepartamento
        ) {
            this.idDepartamentoOperativo =
                    idDepartamentoOperativo;
            this.codigoDepartamento =
                    codigoDepartamento;
            this.nombreDepartamento =
                    nombreDepartamento;
        }
    }

    public CajaCierreVentanillaDTO calcularVentanilla(
            Long idProvision,
            BigDecimal efectivoFinCalculado
    ) {

        String sql = """
            SELECT
                COALESCE(SUM(valor_entrada), 0) AS caja_a_ventanilla,
                COALESCE(SUM(valor_salida), 0) AS ventanilla_a_caja
            FROM cajas.movimientos_ventanilla
            WHERE id_provision = :idProvision
            """;

        CajaCierreVentanillaDTO traslados = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idProvision", idProvision),
                (rs, rowNum) -> CajaCierreVentanillaDTO.builder()
                        .cajaAVentanilla(
                                nvl(rs.getBigDecimal("caja_a_ventanilla"))
                        )
                        .ventanillaACaja(
                                nvl(rs.getBigDecimal("ventanilla_a_caja"))
                        )
                        .build()
        );

        CajaCierreMovimientosDTO movimientos =
                calcularMovimientos(idProvision);

        BigDecimal saldoVentanilla =
                traslados.getCajaAVentanilla()
                        .subtract(traslados.getVentanillaACaja())
                        .add(movimientos.getEntradasEfectivo())
                        .subtract(movimientos.getSalidasEfectivo());

        BigDecimal saldoCajaInterna =
                efectivoFinCalculado.subtract(saldoVentanilla);

        return CajaCierreVentanillaDTO.builder()
                .cajaAVentanilla(traslados.getCajaAVentanilla())
                .ventanillaACaja(traslados.getVentanillaACaja())
                .entradasOperacionesEfectivo(
                        movimientos.getEntradasEfectivo()
                )
                .salidasOperacionesEfectivo(
                        movimientos.getSalidasEfectivo()
                )
                .saldoVentanilla(saldoVentanilla)
                .saldoCajaInterna(saldoCajaInterna)
                .build();
    }

    public int contarMovimientosPendientes(
            Long idProvision
    ) {

        String sql = """
            SELECT COUNT(*)
            FROM cajas.movimientos_caja
            WHERE id_provision = :idProvision
              AND estado = 'PENDIENTE'
            """;

        Integer cantidad = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idProvision", idProvision),
                Integer.class
        );

        return cantidad != null ? cantidad : 0;
    }

    public void bloquearProvisionParaCierre(
            Long idProvision
    ) {

        String sql = """
            SELECT id_provision
            FROM cajas.provisiones_diarias
            WHERE id_provision = :idProvision
              AND estado = 'ABIERTA'
            FOR UPDATE
            """;

        List<Long> result = jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idProvision", idProvision),
                (rs, rowNum) -> rs.getLong("id_provision")
        );

        if (result.isEmpty()) {
            throw new RuntimeException(
                    "La provisión no existe o la caja ya se encuentra cerrada."
            );
        }
    }


    // =========================================================
// DENOMINACIONES DE EFECTIVO
// =========================================================

    /**
     * Obtiene las denominaciones de billetes activas
     * configuradas para el arqueo de caja.
     *
     * El orden depende exclusivamente del valor de la
     * denominación, de mayor a menor.
     *
     * De esta forma una nueva denominación queda ubicada
     * automáticamente en la posición correspondiente.
     */
    public List<BigDecimal> listarDenominacionesBilletesActivas() {

        String sql = """
        SELECT
            denominacion
        FROM cajas.denominaciones_efectivo
        WHERE tipo = 'BILLETE'
          AND activa = TRUE
        ORDER BY denominacion DESC
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                (rs, rowNum) ->
                        rs.getBigDecimal("denominacion")
        );
    }

    public void insertarDetalleEfectivo(
            Long idProvision,
            String tipoDetalle,
            BigDecimal denominacion,
            Integer cantidad,
            BigDecimal valorTotal,
            Integer idUsuario
    ) {

        String sql = """
        INSERT INTO cajas.cierres_efectivo_detalle (
            id_provision,
            tipo_detalle,
            denominacion,
            cantidad,
            valor_total,
            fk_seguridad_creacion,
            fecha_creacion
        )
        VALUES (
            :idProvision,
            :tipoDetalle,
            :denominacion,
            :cantidad,
            :valorTotal,
            :idUsuario,
            CURRENT_TIMESTAMP
        )
        """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idProvision", idProvision)
                        .addValue("tipoDetalle", tipoDetalle)
                        .addValue("denominacion", denominacion)
                        .addValue("cantidad", cantidad)
                        .addValue("valorTotal", valorTotal)
                        .addValue("idUsuario", idUsuario)
        );
    }

    public int cerrarProvision(
            Long idProvision,
            BigDecimal efectivoFin,
            BigDecimal chequesFin,
            BigDecimal totalIngresos,
            BigDecimal totalEgresos,
            String observacion,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cajas.provisiones_diarias
            SET
                estado = 'CERRADA',
                efectivo_fin = :efectivoFin,
                cheques_fin = :chequesFin,
                total_ingresos = :totalIngresos,
                total_egresos = :totalEgresos,
                observacion_cierre = :observacion,
                fk_usuario_cierre = :idUsuario,
                fecha_cierre = CURRENT_TIMESTAMP,
                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_provision = :idProvision
              AND estado = 'ABIERTA'
            """;

        return jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idProvision", idProvision)
                        .addValue("efectivoFin", efectivoFin)
                        .addValue("chequesFin", chequesFin)
                        .addValue("totalIngresos", totalIngresos)
                        .addValue("totalEgresos", totalEgresos)
                        .addValue("observacion", observacion)
                        .addValue("idUsuario", idUsuario)
        );
    }

    private static BigDecimal nvl(
            BigDecimal valor
    ) {
        return valor != null
                ? valor
                : BigDecimal.ZERO;
    }
}
