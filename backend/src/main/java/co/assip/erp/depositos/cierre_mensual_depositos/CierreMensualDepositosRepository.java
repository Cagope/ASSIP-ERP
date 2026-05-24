package co.assip.erp.depositos.cierre_mensual_depositos;

import co.assip.erp.depositos.cierre_mensual_depositos.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CierreMensualDepositosRepository {

    private final JdbcTemplate jdbc;

    public boolean existeCierre(
            Integer idAgencia,
            LocalDate fechaCierre
    ) {

        String sql = """
            SELECT COUNT(*)
            FROM depositos.cierres_mensuales
            WHERE id_agencia = ?
              AND fecha_cierre = ?
            """;

        Integer count = jdbc.queryForObject(
                sql,
                Integer.class,
                idAgencia,
                fechaCierre
        );

        return count != null && count > 0;
    }

    public List<CierreMensualDepositosDetalleDTO> generarDetalle(
            Integer idAgencia,
            LocalDate fechaCierre
    ) {

        String sql = """
        WITH movimientos AS (
            SELECT
                e.id_cuenta_ahorro,
                COALESCE(SUM(e.valor_debito), 0) AS total_debitos,
                COALESCE(SUM(e.valor_credito), 0) AS total_creditos,
                COUNT(*) AS cantidad_movimientos
            FROM depositos.extractos_cuentas_ahorros e
            WHERE e.fecha_movimiento <= ?
            GROUP BY e.id_cuenta_ahorro
        )
        SELECT
            ca.id_agencia,
            ca.id_forma_ahorro,
            fa.codigo_forma,
            fa.nombre_forma,

            ca.id_cuenta_ahorro,
            ca.codigo_cuenta,

            ca.id_datos_personal,

            COALESCE(dp.tipo_persona, '') AS tipo_persona,
            COALESCE(dp.codigo_genero, '') AS codigo_genero,
            COALESCE(dp.nombre_genero, '') AS nombre_genero,

            COALESCE(dp.documento, '') AS documento,

            CASE
                WHEN dp.tipo_persona = '2'
                    THEN COALESCE(dp.nombres, '')
                ELSE COALESCE(
                    TRIM(
                        COALESCE(dp.primer_apellido, '') || ' ' ||
                        COALESCE(dp.segundo_apellido, '') || ' ' ||
                        COALESCE(dp.nombres, '')
                    ),
                    ''
                )
            END AS nombre_completo,

            ca.estado_cuenta_cuenta,
            ca.fecha_apertura_cuenta,
            ca.fecha_estado_cuenta,

            CASE
                WHEN COALESCE(m.cantidad_movimientos, 0) = 0
                    THEN COALESCE(ca.saldo_actual_cuenta, 0)
                ELSE COALESCE(m.total_debitos, 0) - COALESCE(m.total_creditos, 0)
            END AS saldo_cierre,

            COALESCE(m.total_debitos, 0) AS total_debitos,
            COALESCE(m.total_creditos, 0) AS total_creditos,

            ca.gmf_cuenta_cuenta,
            ca.fecha_gmf_cuenta,
            ca.plazo_cuenta,
            ca.cuota_mensual_cuenta,
            ca.fecha_final_cuenta,
            ca.tasa,
            ca.cuenta_activa,
            ca.cuenta_conjunta,
            ca.accion_conjunta

        FROM depositos.cuentas_ahorro ca

        INNER JOIN depositos.formas_ahorro fa
            ON fa.id_forma_ahorro = ca.id_forma_ahorro

        LEFT JOIN movimientos m
            ON m.id_cuenta_ahorro = ca.id_cuenta_ahorro

        LEFT JOIN (
        
              SELECT DISTINCT ON (id_datos_personal)
                  id_datos_personal,
                  tipo_persona,
                  codigo_genero,
                  nombre_genero,
                  documento,
                  nombres,
                  primer_apellido,
                  segundo_apellido
              FROM reporting.vw_hoja_vida_general_total
              ORDER BY
                  id_datos_personal,
                  fecha_creacion DESC NULLS LAST
    
        ) dp
            ON dp.id_datos_personal = ca.id_datos_personal

        WHERE ca.id_agencia = ?
         AND (
             CASE
                 WHEN COALESCE(m.cantidad_movimientos, 0) = 0
                     THEN COALESCE(ca.saldo_actual_cuenta, 0)
                 ELSE COALESCE(m.total_debitos, 0) - COALESCE(m.total_creditos, 0)
             END
         ) <> 0

        ORDER BY
            fa.codigo_forma,
            ca.codigo_cuenta
        """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        CierreMensualDepositosDetalleDTO.builder()
                                .idAgencia(rs.getInt("id_agencia"))
                                .idFormaAhorro(rs.getInt("id_forma_ahorro"))
                                .codigoForma(rs.getString("codigo_forma"))
                                .nombreForma(rs.getString("nombre_forma"))
                                .idCuentaAhorro(rs.getInt("id_cuenta_ahorro"))
                                .codigoCuenta(rs.getString("codigo_cuenta"))
                                .idDatosPersonal(rs.getInt("id_datos_personal"))
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .tipoPersona(rs.getString("tipo_persona"))
                                .codigoGenero(rs.getString("codigo_genero"))
                                .nombreGenero(rs.getString("nombre_genero"))
                                .estadoCuenta(rs.getString("estado_cuenta_cuenta"))
                                .fechaAperturaCuenta(
                                        rs.getDate("fecha_apertura_cuenta") == null
                                                ? null
                                                : rs.getDate("fecha_apertura_cuenta").toLocalDate()
                                )
                                .fechaEstadoCuenta(
                                        rs.getDate("fecha_estado_cuenta") == null
                                                ? null
                                                : rs.getDate("fecha_estado_cuenta").toLocalDate()
                                )
                                .saldoCierre(rs.getBigDecimal("saldo_cierre"))
                                .totalDebitos(rs.getBigDecimal("total_debitos"))
                                .totalCreditos(rs.getBigDecimal("total_creditos"))
                                .gmfCuenta(rs.getString("gmf_cuenta_cuenta"))
                                .fechaGmf(
                                        rs.getDate("fecha_gmf_cuenta") == null
                                                ? null
                                                : rs.getDate("fecha_gmf_cuenta").toLocalDate()
                                )
                                .plazo(rs.getObject("plazo_cuenta", Integer.class))
                                .cuotaMensual(rs.getBigDecimal("cuota_mensual_cuenta"))
                                .fechaFinal(
                                        rs.getDate("fecha_final_cuenta") == null
                                                ? null
                                                : rs.getDate("fecha_final_cuenta").toLocalDate()
                                )
                                .tasa(rs.getBigDecimal("tasa"))
                                .cuentaActiva(rs.getString("cuenta_activa"))
                                .cuentaConjunta(rs.getString("cuenta_conjunta"))
                                .accionConjunta(rs.getString("accion_conjunta"))
                                .build(),
                fechaCierre,
                idAgencia
        );
    }

    public List<CierreMensualDepositosResumenFormaDTO> generarResumenFormas(
            List<CierreMensualDepositosDetalleDTO> detalle
    ) {

        Map<String, List<CierreMensualDepositosDetalleDTO>> grupos =
                detalle.stream()
                        .collect(Collectors.groupingBy(d ->
                                d.getIdFormaAhorro() + "-" + d.getCodigoForma()
                        ));

        return grupos.values()
                .stream()
                .map(items -> {

                    CierreMensualDepositosDetalleDTO base =
                            items.get(0);

                    BigDecimal saldoTotal = items.stream()
                            .map(CierreMensualDepositosDetalleDTO::getSaldoCierre)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalDebitos = items.stream()
                            .map(CierreMensualDepositosDetalleDTO::getTotalDebitos)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalCreditos = items.stream()
                            .map(CierreMensualDepositosDetalleDTO::getTotalCreditos)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return CierreMensualDepositosResumenFormaDTO.builder()
                            .idAgencia(base.getIdAgencia())
                            .idFormaAhorro(base.getIdFormaAhorro())
                            .codigoForma(base.getCodigoForma())
                            .nombreForma(base.getNombreForma())
                            .cantidadCuentas(items.size())
                            .saldoTotal(saldoTotal)
                            .totalDebitos(totalDebitos)
                            .totalCreditos(totalCreditos)
                            .hombres(
                                    "01".equals(base.getCodigoForma())
                                            ? (int) items.stream()
                                            .filter(i ->
                                                    "1".equals(i.getTipoPersona()) &&
                                                            "1".equals(i.getCodigoGenero())
                                            )
                                            .count()
                                            : 0
                            )

                            .mujeres(
                                    "01".equals(base.getCodigoForma())
                                            ? (int) items.stream()
                                            .filter(i ->
                                                    "1".equals(i.getTipoPersona()) &&
                                                            "2".equals(i.getCodigoGenero())
                                            )
                                            .count()
                                            : 0
                            )

                            .juridicas(
                                    "01".equals(base.getCodigoForma())
                                            ? (int) items.stream()
                                            .filter(i ->
                                                    "2".equals(i.getTipoPersona())
                                            )
                                            .count()
                                            : 0
                            )
                            .build();
                })
                .toList();
    }

    public CierreMensualDepositosResumenDTO generarResumenGeneral(
            List<CierreMensualDepositosDetalleDTO> detalle,
            List<CierreMensualDepositosResumenFormaDTO> resumenFormas
    ) {

        BigDecimal saldoTotal = detalle.stream()
                .map(CierreMensualDepositosDetalleDTO::getSaldoCierre)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitos = detalle.stream()
                .map(CierreMensualDepositosDetalleDTO::getTotalDebitos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCreditos = detalle.stream()
                .map(CierreMensualDepositosDetalleDTO::getTotalCreditos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CierreMensualDepositosResumenDTO.builder()
                .totalCuentas(detalle.size())
                .saldoTotal(saldoTotal)
                .totalDebitos(totalDebitos)
                .totalCreditos(totalCreditos)
                .totalFormas(resumenFormas.size())
                .hombres(
                        resumenFormas.stream()
                                .map(CierreMensualDepositosResumenFormaDTO::getHombres)
                                .reduce(0, Integer::sum)
                )

                .mujeres(
                        resumenFormas.stream()
                                .map(CierreMensualDepositosResumenFormaDTO::getMujeres)
                                .reduce(0, Integer::sum)
                )

                .juridicas(
                        resumenFormas.stream()
                                .map(CierreMensualDepositosResumenFormaDTO::getJuridicas)
                                .reduce(0, Integer::sum)
                )
                .build();
    }

    public Long crearCierre(
            CierreMensualDepositosRequestDTO request,
            CierreMensualDepositosResumenDTO resumen
    ) {

        String sql = """
            INSERT INTO depositos.cierres_mensuales (
                id_agencia,
                fecha_cierre,
                anio,
                mes,
                estado_cierre,
                total_cuentas,
                saldo_total,
                total_debitos,
                total_creditos,
                fk_seguridad_creacion
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id_cierre_mensual
            """;

        return jdbc.queryForObject(
                sql,
                Long.class,
                request.getIdAgencia(),
                request.getFechaCierre(),
                request.getFechaCierre().getYear(),
                request.getFechaCierre().getMonthValue(),
                "APLICADO",
                resumen.getTotalCuentas(),
                resumen.getSaldoTotal(),
                resumen.getTotalDebitos(),
                resumen.getTotalCreditos(),
                1
        );
    }

    public void guardarDetalle(
            Long idCierre,
            List<CierreMensualDepositosDetalleDTO> detalle
    ) {

        String sql = """
            INSERT INTO depositos.cierres_mensuales_detalle (
                id_cierre_mensual,
                id_agencia,
                id_forma_ahorro,
                codigo_forma,
                nombre_forma,
                id_cuenta_ahorro,
                codigo_cuenta,
                id_datos_personal,
                documento,
                nombre_completo,
                estado_cuenta,
                fecha_apertura_cuenta,
                fecha_estado_cuenta,
                saldo_cierre,
                total_debitos,
                total_creditos,
                gmf_cuenta,
                fecha_gmf,
                plazo,
                cuota_mensual,
                fecha_final,
                tasa,
                cuenta_activa,
                cuenta_conjunta,
                accion_conjunta
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbc.batchUpdate(
                sql,
                detalle,
                100,
                (ps, d) -> {
                    ps.setLong(1, idCierre);
                    ps.setObject(2, d.getIdAgencia());
                    ps.setObject(3, d.getIdFormaAhorro());
                    ps.setString(4, d.getCodigoForma());
                    ps.setString(5, d.getNombreForma());
                    ps.setObject(6, d.getIdCuentaAhorro());
                    ps.setString(7, d.getCodigoCuenta());
                    ps.setObject(8, d.getIdDatosPersonal());
                    ps.setString(9, d.getDocumento());
                    ps.setString(10, d.getNombreCompleto());
                    ps.setString(11, d.getEstadoCuenta());
                    ps.setObject(12, d.getFechaAperturaCuenta());
                    ps.setObject(13, d.getFechaEstadoCuenta());
                    ps.setBigDecimal(14, d.getSaldoCierre());
                    ps.setBigDecimal(15, d.getTotalDebitos());
                    ps.setBigDecimal(16, d.getTotalCreditos());
                    ps.setString(17, d.getGmfCuenta());
                    ps.setObject(18, d.getFechaGmf());
                    ps.setObject(19, d.getPlazo());
                    ps.setBigDecimal(20, d.getCuotaMensual());
                    ps.setObject(21, d.getFechaFinal());
                    ps.setBigDecimal(22, d.getTasa());
                    ps.setString(23, d.getCuentaActiva());
                    ps.setString(24, d.getCuentaConjunta());
                    ps.setString(25, d.getAccionConjunta());
                }
        );
    }

    public void guardarResumenFormas(
            Long idCierre,
            List<CierreMensualDepositosResumenFormaDTO> resumenFormas
    ) {

        String sql = """
            INSERT INTO depositos.cierres_mensuales_resumen (
                id_cierre_mensual,
                id_agencia,
                id_forma_ahorro,
                codigo_forma,
                nombre_forma,
                cantidad_cuentas,
                saldo_total,
                total_debitos,
                total_creditos,
                hombres,
                mujeres,
                juridicas
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbc.batchUpdate(
                sql,
                resumenFormas,
                100,
                (ps, r) -> {
                    ps.setLong(1, idCierre);
                    ps.setObject(2, r.getIdAgencia());
                    ps.setObject(3, r.getIdFormaAhorro());
                    ps.setString(4, r.getCodigoForma());
                    ps.setString(5, r.getNombreForma());
                    ps.setObject(6, r.getCantidadCuentas());
                    ps.setBigDecimal(7, r.getSaldoTotal());
                    ps.setBigDecimal(8, r.getTotalDebitos());
                    ps.setBigDecimal(9, r.getTotalCreditos());
                    ps.setObject(10, r.getHombres());
                    ps.setObject(11, r.getMujeres());
                    ps.setObject(12, r.getJuridicas());
                }
        );
    }

    public List<CierreMensualDepositosPreviewDTO> listar() {

        String sql = """
        SELECT
            id_cierre_mensual,
            id_agencia,
            fecha_cierre,
            anio,
            mes,
            estado_cierre,
            total_cuentas,
            saldo_total,
            total_debitos,
            total_creditos
        FROM depositos.cierres_mensuales
        ORDER BY
            fecha_cierre DESC,
            id_cierre_mensual DESC
        """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        CierreMensualDepositosPreviewDTO.builder()
                                .idCierreMensual(
                                        rs.getLong("id_cierre_mensual")
                                )
                                .idAgencia(
                                        rs.getInt("id_agencia")
                                )
                                .fechaCierre(
                                        rs.getDate("fecha_cierre")
                                                .toLocalDate()
                                )
                                .anio(
                                        rs.getInt("anio")
                                )
                                .mes(
                                        rs.getInt("mes")
                                )
                                .estado(
                                        rs.getString("estado_cierre")
                                )
                                .totalCuentas(
                                        rs.getInt("total_cuentas")
                                )
                                .saldoTotal(
                                        rs.getBigDecimal("saldo_total")
                                )
                                .totalDebitos(
                                        rs.getBigDecimal("total_debitos")
                                )
                                .totalCreditos(
                                        rs.getBigDecimal("total_creditos")
                                )
                                .build()
        );
    }

    public CierreMensualDepositosPreviewDTO obtenerPorId(
            Long idCierre
    ) {

        String sql = """
        SELECT
            id_cierre_mensual,
            id_agencia,
            fecha_cierre,
            anio,
            mes,
            estado_cierre,
            total_cuentas,
            saldo_total,
            total_debitos,
            total_creditos
        FROM depositos.cierres_mensuales
        WHERE id_cierre_mensual = ?
        """;

        CierreMensualDepositosPreviewDTO cierre =
                jdbc.queryForObject(
                        sql,
                        (rs, rowNum) ->
                                CierreMensualDepositosPreviewDTO.builder()
                                        .idCierreMensual(
                                                rs.getLong("id_cierre_mensual")
                                        )
                                        .idAgencia(
                                                rs.getInt("id_agencia")
                                        )
                                        .fechaCierre(
                                                rs.getDate("fecha_cierre")
                                                        .toLocalDate()
                                        )
                                        .anio(
                                                rs.getInt("anio")
                                        )
                                        .mes(
                                                rs.getInt("mes")
                                        )
                                        .estado(
                                                rs.getString("estado_cierre")
                                        )
                                        .totalCuentas(
                                                rs.getInt("total_cuentas")
                                        )
                                        .saldoTotal(
                                                rs.getBigDecimal("saldo_total")
                                        )
                                        .totalDebitos(
                                                rs.getBigDecimal("total_debitos")
                                        )
                                        .totalCreditos(
                                                rs.getBigDecimal("total_creditos")
                                        )
                                        .build(),
                        idCierre
                );

        List<CierreMensualDepositosDetalleDTO> detalle =
                obtenerDetallePorCierre(idCierre);

        List<CierreMensualDepositosResumenFormaDTO> resumenFormas =
                obtenerResumenFormasPorCierre(idCierre);

        cierre.setDetalle(detalle);
        cierre.setResumenFormas(resumenFormas);

        cierre.setResumen(
                generarResumenGeneral(
                        detalle,
                        resumenFormas
                )
        );

        return cierre;
    }

    public void eliminar(
            Long idCierre
    ) {

        String sql = """
        DELETE FROM depositos.cierres_mensuales
        WHERE id_cierre_mensual = ?
        """;

        jdbc.update(
                sql,
                idCierre
        );
    }

    public List<CierreMensualDepositosDetalleDTO> obtenerDetallePorCierre(
            Long idCierre
    ) {

        String sql = """
        SELECT
            id_agencia,
            id_forma_ahorro,
            codigo_forma,
            nombre_forma,
            id_cuenta_ahorro,
            codigo_cuenta,
            id_datos_personal,
            documento,
            nombre_completo,
            estado_cuenta,
            fecha_apertura_cuenta,
            fecha_estado_cuenta,
            saldo_cierre,
            total_debitos,
            total_creditos,
            gmf_cuenta,
            fecha_gmf,
            plazo,
            cuota_mensual,
            fecha_final,
            tasa,
            cuenta_activa,
            cuenta_conjunta,
            accion_conjunta
        FROM depositos.cierres_mensuales_detalle
        WHERE id_cierre_mensual = ?
        ORDER BY
            codigo_forma,
            codigo_cuenta
        """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        CierreMensualDepositosDetalleDTO.builder()
                                .idAgencia(rs.getInt("id_agencia"))
                                .idFormaAhorro(rs.getInt("id_forma_ahorro"))
                                .codigoForma(rs.getString("codigo_forma"))
                                .nombreForma(rs.getString("nombre_forma"))
                                .idCuentaAhorro(rs.getInt("id_cuenta_ahorro"))
                                .codigoCuenta(rs.getString("codigo_cuenta"))
                                .idDatosPersonal(rs.getInt("id_datos_personal"))
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .estadoCuenta(rs.getString("estado_cuenta"))
                                .fechaAperturaCuenta(
                                        rs.getDate("fecha_apertura_cuenta") == null
                                                ? null
                                                : rs.getDate("fecha_apertura_cuenta").toLocalDate()
                                )
                                .fechaEstadoCuenta(
                                        rs.getDate("fecha_estado_cuenta") == null
                                                ? null
                                                : rs.getDate("fecha_estado_cuenta").toLocalDate()
                                )
                                .saldoCierre(rs.getBigDecimal("saldo_cierre"))
                                .totalDebitos(rs.getBigDecimal("total_debitos"))
                                .totalCreditos(rs.getBigDecimal("total_creditos"))
                                .gmfCuenta(rs.getString("gmf_cuenta"))
                                .fechaGmf(
                                        rs.getDate("fecha_gmf") == null
                                                ? null
                                                : rs.getDate("fecha_gmf").toLocalDate()
                                )
                                .plazo(rs.getObject("plazo", Integer.class))
                                .cuotaMensual(rs.getBigDecimal("cuota_mensual"))
                                .fechaFinal(
                                        rs.getDate("fecha_final") == null
                                                ? null
                                                : rs.getDate("fecha_final").toLocalDate()
                                )
                                .tasa(rs.getBigDecimal("tasa"))
                                .cuentaActiva(rs.getString("cuenta_activa"))
                                .cuentaConjunta(rs.getString("cuenta_conjunta"))
                                .accionConjunta(rs.getString("accion_conjunta"))
                                .build(),
                idCierre
        );
    }

    public List<CierreMensualDepositosResumenFormaDTO> obtenerResumenFormasPorCierre(
            Long idCierre
    ) {

        String sql = """
        SELECT
            id_agencia,
            id_forma_ahorro,
            codigo_forma,
            nombre_forma,
            cantidad_cuentas,
            saldo_total,
            total_debitos,
            total_creditos,
            hombres,
            mujeres,
            juridicas
        FROM depositos.cierres_mensuales_resumen
        WHERE id_cierre_mensual = ?
        ORDER BY
            codigo_forma
        """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        CierreMensualDepositosResumenFormaDTO.builder()
                                .idAgencia(rs.getInt("id_agencia"))
                                .idFormaAhorro(rs.getInt("id_forma_ahorro"))
                                .codigoForma(rs.getString("codigo_forma"))
                                .nombreForma(rs.getString("nombre_forma"))
                                .cantidadCuentas(rs.getInt("cantidad_cuentas"))
                                .saldoTotal(rs.getBigDecimal("saldo_total"))
                                .totalDebitos(rs.getBigDecimal("total_debitos"))
                                .totalCreditos(rs.getBigDecimal("total_creditos"))
                                .hombres(rs.getInt("hombres"))
                                .mujeres(rs.getInt("mujeres"))
                                .juridicas(rs.getInt("juridicas"))
                                .build(),
                idCierre
        );
    }

}