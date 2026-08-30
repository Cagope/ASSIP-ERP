package co.assip.erp.depositos.cierre_mensual_depositos;

import co.assip.erp.depositos.cierre_mensual_depositos.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CierreMensualDepositosRepository {

    private final JdbcTemplate jdbc;


    // =========================================================
    // EXISTE CIERRE POR FECHA
    // =========================================================

    public boolean existeCierre(
            LocalDate fechaCierre
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM depositos.cierres_mensuales
                WHERE fecha_cierre = ?
                """;

        Integer count =
                jdbc.queryForObject(
                        sql,
                        Integer.class,
                        fechaCierre
                );

        return count != null
                && count > 0;
    }


    // =========================================================
    // BUSCAR ID DE CIERRE POR FECHA
    // =========================================================

    public Optional<Long> buscarIdPorFecha(
            LocalDate fechaCierre
    ) {

        String sql = """
                SELECT
                    id_cierre_mensual
                FROM depositos.cierres_mensuales
                WHERE fecha_cierre = ?
                LIMIT 1
                """;

        List<Long> lista =
                jdbc.query(
                        sql,
                        (rs, rowNum) ->
                                rs.getLong(
                                        "id_cierre_mensual"
                                ),
                        fechaCierre
                );

        return lista.stream().findFirst();
    }


    // =========================================================
    // BUSCAR ESTADO
    // =========================================================

    public Optional<String> buscarEstado(
            Long idCierre
    ) {

        String sql = """
                SELECT
                    estado_cierre
                FROM depositos.cierres_mensuales
                WHERE id_cierre_mensual = ?
                """;

        List<String> lista =
                jdbc.query(
                        sql,
                        (rs, rowNum) ->
                                rs.getString(
                                        "estado_cierre"
                                ),
                        idCierre
                );

        return lista.stream().findFirst();
    }


    // =========================================================
    // GENERAR DETALLE GLOBAL DE LA FOTOGRAFÍA
    //
    // - incluye todas las agencias
    // - calcula saldo hasta fecha de corte
    // - solo incluye saldo distinto de cero
    // =========================================================

    public List<CierreMensualDepositosDetalleDTO> generarDetalle(
            LocalDate fechaCierre
    ) {

        String sql = """
                WITH movimientos AS
                (
                    SELECT
                        e.id_cuenta_ahorro,

                        COALESCE(
                            SUM(e.valor_debito),
                            0
                        ) AS total_debitos,

                        COALESCE(
                            SUM(e.valor_credito),
                            0
                        ) AS total_creditos

                    FROM depositos.extractos_cuentas_ahorros e

                    WHERE e.fecha_movimiento <= ?

                    GROUP BY
                        e.id_cuenta_ahorro
                )

                SELECT
                    ca.id_agencia,
                    ca.id_forma_ahorro,

                    fa.codigo_forma,
                    fa.nombre_forma,

                    ca.id_cuenta_ahorro,
                    ca.codigo_cuenta,

                    ca.id_datos_personal,

                    COALESCE(
                        dp.tipo_persona,
                        ''
                    ) AS tipo_persona,

                    COALESCE(
                        dp.codigo_genero,
                        ''
                    ) AS codigo_genero,

                    COALESCE(
                        dp.nombre_genero,
                        ''
                    ) AS nombre_genero,

                    COALESCE(
                        dp.documento,
                        ''
                    ) AS documento,

                    CASE
                        WHEN dp.tipo_persona = '2'
                            THEN COALESCE(
                                dp.nombres,
                                ''
                            )

                        ELSE COALESCE(
                            TRIM(
                                COALESCE(dp.primer_apellido, '')
                                || ' '
                                || COALESCE(dp.segundo_apellido, '')
                                || ' '
                                || COALESCE(dp.nombres, '')
                            ),
                            ''
                        )
                    END AS nombre_completo,

                    ca.estado_cuenta_cuenta,
                    ca.fecha_apertura_cuenta,
                    ca.fecha_estado_cuenta,

                    COALESCE(
                        m.total_creditos,
                        0
                    )
                    -
                    COALESCE(
                        m.total_debitos,
                        0
                    ) AS saldo_cierre,

                    COALESCE(
                        m.total_debitos,
                        0
                    ) AS total_debitos,

                    COALESCE(
                        m.total_creditos,
                        0
                    ) AS total_creditos,

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
                    ON fa.id_forma_ahorro =
                       ca.id_forma_ahorro

                LEFT JOIN movimientos m
                    ON m.id_cuenta_ahorro =
                       ca.id_cuenta_ahorro

                LEFT JOIN
                (
                    SELECT DISTINCT ON (
                        id_datos_personal
                    )
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
                    ON dp.id_datos_personal =
                       ca.id_datos_personal

                WHERE
                (
                    COALESCE(
                        m.total_creditos,
                        0
                    )
                    -
                    COALESCE(
                        m.total_debitos,
                        0
                    )
                ) <> 0

                ORDER BY
                    ca.id_agencia,
                    fa.codigo_forma,
                    ca.codigo_cuenta
                """;

        return jdbc.query(
                sql,

                (rs, rowNum) ->
                        CierreMensualDepositosDetalleDTO
                                .builder()

                                .idAgencia(
                                        rs.getInt(
                                                "id_agencia"
                                        )
                                )

                                .idFormaAhorro(
                                        rs.getInt(
                                                "id_forma_ahorro"
                                        )
                                )

                                .codigoForma(
                                        rs.getString(
                                                "codigo_forma"
                                        )
                                )

                                .nombreForma(
                                        rs.getString(
                                                "nombre_forma"
                                        )
                                )

                                .idCuentaAhorro(
                                        rs.getInt(
                                                "id_cuenta_ahorro"
                                        )
                                )

                                .codigoCuenta(
                                        rs.getString(
                                                "codigo_cuenta"
                                        )
                                )

                                .idDatosPersonal(
                                        rs.getInt(
                                                "id_datos_personal"
                                        )
                                )

                                .documento(
                                        rs.getString(
                                                "documento"
                                        )
                                )

                                .nombreCompleto(
                                        rs.getString(
                                                "nombre_completo"
                                        )
                                )

                                .tipoPersona(
                                        rs.getString(
                                                "tipo_persona"
                                        )
                                )

                                .codigoGenero(
                                        rs.getString(
                                                "codigo_genero"
                                        )
                                )

                                .nombreGenero(
                                        rs.getString(
                                                "nombre_genero"
                                        )
                                )

                                .estadoCuenta(
                                        rs.getString(
                                                "estado_cuenta_cuenta"
                                        )
                                )

                                .fechaAperturaCuenta(
                                        rs.getDate(
                                                "fecha_apertura_cuenta"
                                        ) == null
                                                ? null
                                                : rs.getDate(
                                                "fecha_apertura_cuenta"
                                        ).toLocalDate()
                                )

                                .fechaEstadoCuenta(
                                        rs.getDate(
                                                "fecha_estado_cuenta"
                                        ) == null
                                                ? null
                                                : rs.getDate(
                                                "fecha_estado_cuenta"
                                        ).toLocalDate()
                                )

                                .saldoCierre(
                                        rs.getBigDecimal(
                                                "saldo_cierre"
                                        )
                                )

                                .totalDebitos(
                                        rs.getBigDecimal(
                                                "total_debitos"
                                        )
                                )

                                .totalCreditos(
                                        rs.getBigDecimal(
                                                "total_creditos"
                                        )
                                )

                                .gmfCuenta(
                                        rs.getString(
                                                "gmf_cuenta_cuenta"
                                        )
                                )

                                .fechaGmf(
                                        rs.getDate(
                                                "fecha_gmf_cuenta"
                                        ) == null
                                                ? null
                                                : rs.getDate(
                                                "fecha_gmf_cuenta"
                                        ).toLocalDate()
                                )

                                .plazo(
                                        rs.getObject(
                                                "plazo_cuenta",
                                                Integer.class
                                        )
                                )

                                .cuotaMensual(
                                        rs.getBigDecimal(
                                                "cuota_mensual_cuenta"
                                        )
                                )

                                .fechaFinal(
                                        rs.getDate(
                                                "fecha_final_cuenta"
                                        ) == null
                                                ? null
                                                : rs.getDate(
                                                "fecha_final_cuenta"
                                        ).toLocalDate()
                                )

                                .tasa(
                                        rs.getBigDecimal(
                                                "tasa"
                                        )
                                )

                                .cuentaActiva(
                                        rs.getString(
                                                "cuenta_activa"
                                        )
                                )

                                .cuentaConjunta(
                                        rs.getString(
                                                "cuenta_conjunta"
                                        )
                                )

                                .accionConjunta(
                                        rs.getString(
                                                "accion_conjunta"
                                        )
                                )

                                .build(),

                fechaCierre
        );
    }


    // =========================================================
    // GENERAR RESUMEN POR AGENCIA + FORMA DE AHORRO
    // =========================================================

    public List<CierreMensualDepositosResumenFormaDTO>
    generarResumenFormas(
            List<CierreMensualDepositosDetalleDTO> detalle
    ) {

        Map<String, List<CierreMensualDepositosDetalleDTO>>
                grupos =
                detalle.stream()
                        .collect(
                                Collectors.groupingBy(
                                        d ->
                                                d.getIdAgencia()
                                                        + "-"
                                                        + d.getIdFormaAhorro()
                                                        + "-"
                                                        + d.getCodigoForma()
                                )
                        );

        return grupos.values()
                .stream()
                .map(items -> {

                    CierreMensualDepositosDetalleDTO base =
                            items.get(0);

                    BigDecimal saldoTotal =
                            items.stream()
                                    .map(
                                            CierreMensualDepositosDetalleDTO
                                                    ::getSaldoCierre
                                    )
                                    .reduce(
                                            BigDecimal.ZERO,
                                            BigDecimal::add
                                    );

                    BigDecimal totalDebitos =
                            items.stream()
                                    .map(
                                            CierreMensualDepositosDetalleDTO
                                                    ::getTotalDebitos
                                    )
                                    .reduce(
                                            BigDecimal.ZERO,
                                            BigDecimal::add
                                    );

                    BigDecimal totalCreditos =
                            items.stream()
                                    .map(
                                            CierreMensualDepositosDetalleDTO
                                                    ::getTotalCreditos
                                    )
                                    .reduce(
                                            BigDecimal.ZERO,
                                            BigDecimal::add
                                    );

                    return CierreMensualDepositosResumenFormaDTO
                            .builder()

                            .idAgencia(
                                    base.getIdAgencia()
                            )

                            .idFormaAhorro(
                                    base.getIdFormaAhorro()
                            )

                            .codigoForma(
                                    base.getCodigoForma()
                            )

                            .nombreForma(
                                    base.getNombreForma()
                            )

                            .cantidadCuentas(
                                    items.size()
                            )

                            .saldoTotal(
                                    saldoTotal
                            )

                            .totalDebitos(
                                    totalDebitos
                            )

                            .totalCreditos(
                                    totalCreditos
                            )

                            .hombres(
                                    "01".equals(
                                            base.getCodigoForma()
                                    )
                                            ? (int) items.stream()
                                            .filter(
                                                    i ->
                                                            "1".equals(
                                                                    i.getTipoPersona()
                                                            )
                                                                    &&
                                                                    "1".equals(
                                                                            i.getCodigoGenero()
                                                                    )
                                            )
                                            .count()
                                            : 0
                            )

                            .mujeres(
                                    "01".equals(
                                            base.getCodigoForma()
                                    )
                                            ? (int) items.stream()
                                            .filter(
                                                    i ->
                                                            "1".equals(
                                                                    i.getTipoPersona()
                                                            )
                                                                    &&
                                                                    "2".equals(
                                                                            i.getCodigoGenero()
                                                                    )
                                            )
                                            .count()
                                            : 0
                            )

                            .juridicas(
                                    "01".equals(
                                            base.getCodigoForma()
                                    )
                                            ? (int) items.stream()
                                            .filter(
                                                    i ->
                                                            "2".equals(
                                                                    i.getTipoPersona()
                                                            )
                                            )
                                            .count()
                                            : 0
                            )

                            .build();
                })
                .toList();
    }

    // =========================================================
// GENERAR RESUMEN POR AGENCIA
//
// Se genera a partir del detalle de la fotografía.
//
// No requiere tabla adicional.
//
// Totales:
// - cuentas
// - saldo
// - débitos
// - créditos
//
// Hombres / mujeres / jurídicas:
// se calculan sobre APORTES SOCIALES (forma 01)
// para no duplicar personas que tengan varias formas
// de ahorro.
// =========================================================

    public List<CierreMensualDepositosResumenAgenciaDTO>
    generarResumenAgencias(
            List<CierreMensualDepositosDetalleDTO> detalle
    ) {

        Map<Integer, List<CierreMensualDepositosDetalleDTO>>
                grupos =
                detalle.stream()
                        .collect(
                                Collectors.groupingBy(
                                        CierreMensualDepositosDetalleDTO
                                                ::getIdAgencia
                                )
                        );


        return grupos.entrySet()
                .stream()

                .sorted(
                        Map.Entry.comparingByKey()
                )

                .map(entry -> {

                    Integer idAgencia =
                            entry.getKey();


                    List<CierreMensualDepositosDetalleDTO> items =
                            entry.getValue();


                    BigDecimal saldoTotal =
                            items.stream()
                                    .map(
                                            CierreMensualDepositosDetalleDTO
                                                    ::getSaldoCierre
                                    )
                                    .reduce(
                                            BigDecimal.ZERO,
                                            BigDecimal::add
                                    );


                    BigDecimal totalDebitos =
                            items.stream()
                                    .map(
                                            CierreMensualDepositosDetalleDTO
                                                    ::getTotalDebitos
                                    )
                                    .reduce(
                                            BigDecimal.ZERO,
                                            BigDecimal::add
                                    );


                    BigDecimal totalCreditos =
                            items.stream()
                                    .map(
                                            CierreMensualDepositosDetalleDTO
                                                    ::getTotalCreditos
                                    )
                                    .reduce(
                                            BigDecimal.ZERO,
                                            BigDecimal::add
                                    );


                    int hombres =
                            (int) items.stream()
                                    .filter(
                                            i ->
                                                    "01".equals(
                                                            i.getCodigoForma()
                                                    )
                                                            &&
                                                            "1".equals(
                                                                    i.getTipoPersona()
                                                            )
                                                            &&
                                                            "1".equals(
                                                                    i.getCodigoGenero()
                                                            )
                                    )
                                    .count();


                    int mujeres =
                            (int) items.stream()
                                    .filter(
                                            i ->
                                                    "01".equals(
                                                            i.getCodigoForma()
                                                    )
                                                            &&
                                                            "1".equals(
                                                                    i.getTipoPersona()
                                                            )
                                                            &&
                                                            "2".equals(
                                                                    i.getCodigoGenero()
                                                            )
                                    )
                                    .count();


                    int juridicas =
                            (int) items.stream()
                                    .filter(
                                            i ->
                                                    "01".equals(
                                                            i.getCodigoForma()
                                                    )
                                                            &&
                                                            "2".equals(
                                                                    i.getTipoPersona()
                                                            )
                                    )
                                    .count();


                    return CierreMensualDepositosResumenAgenciaDTO
                            .builder()

                            .idAgencia(
                                    idAgencia
                            )

                            .totalCuentas(
                                    items.size()
                            )

                            .saldoTotal(
                                    saldoTotal
                            )

                            .totalDebitos(
                                    totalDebitos
                            )

                            .totalCreditos(
                                    totalCreditos
                            )

                            .hombres(
                                    hombres
                            )

                            .mujeres(
                                    mujeres
                            )

                            .juridicas(
                                    juridicas
                            )

                            .build();
                })

                .toList();
    }


    // =========================================================
    // GENERAR RESUMEN GENERAL
    // =========================================================

    // =========================================================
// GENERAR RESUMEN GENERAL DE LA ENTIDAD
// =========================================================

    public CierreMensualDepositosResumenDTO generarResumenGeneral(
            List<CierreMensualDepositosDetalleDTO> detalle,
            List<CierreMensualDepositosResumenFormaDTO> resumenFormas
    ) {

        BigDecimal saldoTotal =
                detalle.stream()
                        .map(
                                CierreMensualDepositosDetalleDTO
                                        ::getSaldoCierre
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal totalDebitos =
                detalle.stream()
                        .map(
                                CierreMensualDepositosDetalleDTO
                                        ::getTotalDebitos
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal totalCreditos =
                detalle.stream()
                        .map(
                                CierreMensualDepositosDetalleDTO
                                        ::getTotalCreditos
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        return CierreMensualDepositosResumenDTO
                .builder()

                .totalCuentas(
                        detalle.size()
                )

                .saldoTotal(
                        saldoTotal
                )

                .totalDebitos(
                        totalDebitos
                )

                .totalCreditos(
                        totalCreditos
                )

                .hombres(
                        resumenFormas.stream()
                                .map(
                                        CierreMensualDepositosResumenFormaDTO
                                                ::getHombres
                                )
                                .reduce(
                                        0,
                                        Integer::sum
                                )
                )

                .mujeres(
                        resumenFormas.stream()
                                .map(
                                        CierreMensualDepositosResumenFormaDTO
                                                ::getMujeres
                                )
                                .reduce(
                                        0,
                                        Integer::sum
                                )
                )

                .juridicas(
                        resumenFormas.stream()
                                .map(
                                        CierreMensualDepositosResumenFormaDTO
                                                ::getJuridicas
                                )
                                .reduce(
                                        0,
                                        Integer::sum
                                )
                )

                .build();
    }


    // =========================================================
    // CREAR CABECERA CENTRALIZADA
    // =========================================================

    public Long crearCierre(
            CierreMensualDepositosRequestDTO request,
            CierreMensualDepositosResumenDTO resumen,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO depositos.cierres_mensuales
                (
                    fecha_cierre,
                    anio,
                    mes,

                    estado_cierre,

                    total_cuentas,
                    saldo_total,
                    total_debitos,
                    total_creditos,

                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                )
                VALUES
                (
                    ?,
                    ?,
                    ?,

                    'P',

                    ?,
                    ?,
                    ?,
                    ?,

                    ?,
                    ?
                )

                RETURNING id_cierre_mensual
                """;

        return jdbc.queryForObject(
                sql,
                Long.class,

                request.getFechaCierre(),

                request.getFechaCierre()
                        .getYear(),

                request.getFechaCierre()
                        .getMonthValue(),

                resumen.getTotalCuentas(),
                resumen.getSaldoTotal(),
                resumen.getTotalDebitos(),
                resumen.getTotalCreditos(),

                idUsuario,
                idUsuario
        );
    }


    // =========================================================
    // ACTUALIZAR PRECierre
    // =========================================================

    public int actualizarPrecierre(
            Long idCierre,
            CierreMensualDepositosResumenDTO resumen,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE depositos.cierres_mensuales

                   SET total_cuentas = ?,
                       saldo_total = ?,
                       total_debitos = ?,
                       total_creditos = ?,
                       fk_seguridad_edicion = ?,
                       fecha_edicion = CURRENT_TIMESTAMP

                 WHERE id_cierre_mensual = ?
                   AND estado_cierre = 'P'
                """;

        return jdbc.update(
                sql,

                resumen.getTotalCuentas(),
                resumen.getSaldoTotal(),
                resumen.getTotalDebitos(),
                resumen.getTotalCreditos(),

                idUsuario,
                idCierre
        );
    }


    // =========================================================
    // GUARDAR DETALLE
    // =========================================================

    public void guardarDetalle(
            Long idCierre,
            List<CierreMensualDepositosDetalleDTO> detalle
    ) {

        String sql = """
                INSERT INTO depositos.cierres_mensuales_detalle
                (
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
                VALUES
                (
                    ?, ?, ?, ?, ?,
                    ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?,
                    ?, ?, ?, ?,
                    ?, ?, ?
                )
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


    // =========================================================
    // GUARDAR RESUMEN POR AGENCIA + FORMA
    // =========================================================

    public void guardarResumenFormas(
            Long idCierre,
            List<CierreMensualDepositosResumenFormaDTO> resumenFormas
    ) {

        String sql = """
                INSERT INTO depositos.cierres_mensuales_resumen
                (
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
                VALUES
                (
                    ?, ?, ?, ?, ?,
                    ?, ?, ?, ?,
                    ?, ?, ?
                )
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


    // =========================================================
    // ELIMINAR DETALLE
    // =========================================================

    public int eliminarDetalle(
            Long idCierre
    ) {

        return jdbc.update(
                """
                DELETE FROM depositos.cierres_mensuales_detalle
                WHERE id_cierre_mensual = ?
                """,
                idCierre
        );
    }


    // =========================================================
    // ELIMINAR RESUMEN
    // =========================================================

    public int eliminarResumen(
            Long idCierre
    ) {

        return jdbc.update(
                """
                DELETE FROM depositos.cierres_mensuales_resumen
                WHERE id_cierre_mensual = ?
                """,
                idCierre
        );
    }


    // =========================================================
    // CONTAR DETALLE
    // =========================================================

    public int contarDetalle(
            Long idCierre
    ) {

        Integer cantidad =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM depositos.cierres_mensuales_detalle
                        WHERE id_cierre_mensual = ?
                        """,
                        Integer.class,
                        idCierre
                );

        return cantidad != null
                ? cantidad
                : 0;
    }


    // =========================================================
    // CONTAR RESUMEN
    // =========================================================

    public int contarResumen(
            Long idCierre
    ) {

        Integer cantidad =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM depositos.cierres_mensuales_resumen
                        WHERE id_cierre_mensual = ?
                        """,
                        Integer.class,
                        idCierre
                );

        return cantidad != null
                ? cantidad
                : 0;
    }


    // =========================================================
    // CERRAR EN FIRME
    // =========================================================

    public int finalizar(
            Long idCierre,
            Integer idUsuario
    ) {

        return jdbc.update(
                """
                UPDATE depositos.cierres_mensuales
                   SET estado_cierre = 'C',
                       fk_seguridad_edicion = ?,
                       fecha_edicion = CURRENT_TIMESTAMP
                 WHERE id_cierre_mensual = ?
                   AND estado_cierre = 'P'
                """,
                idUsuario,
                idCierre
        );
    }


    // =========================================================
    // ELIMINAR PRECierre
    // =========================================================

    public int eliminar(
            Long idCierre
    ) {

        return jdbc.update(
                """
                DELETE FROM depositos.cierres_mensuales
                WHERE id_cierre_mensual = ?
                  AND estado_cierre = 'P'
                """,
                idCierre
        );
    }


    // =========================================================
    // LISTAR CIERRES
    // =========================================================

    public List<CierreMensualDepositosPreviewDTO> listar() {

        String sql = """
                SELECT
                    id_cierre_mensual,
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
                        CierreMensualDepositosPreviewDTO
                                .builder()

                                .idCierreMensual(
                                        rs.getLong(
                                                "id_cierre_mensual"
                                        )
                                )

                                .fechaCierre(
                                        rs.getDate(
                                                "fecha_cierre"
                                        ).toLocalDate()
                                )

                                .anio(
                                        rs.getInt(
                                                "anio"
                                        )
                                )

                                .mes(
                                        rs.getInt(
                                                "mes"
                                        )
                                )

                                .estado(
                                        rs.getString(
                                                "estado_cierre"
                                        )
                                )

                                .totalCuentas(
                                        rs.getInt(
                                                "total_cuentas"
                                        )
                                )

                                .saldoTotal(
                                        rs.getBigDecimal(
                                                "saldo_total"
                                        )
                                )

                                .totalDebitos(
                                        rs.getBigDecimal(
                                                "total_debitos"
                                        )
                                )

                                .totalCreditos(
                                        rs.getBigDecimal(
                                                "total_creditos"
                                        )
                                )

                                .build()
        );
    }


    // =========================================================
// OBTENER CIERRE POR ID
// =========================================================

    public CierreMensualDepositosPreviewDTO obtenerPorId(
            Long idCierre
    ) {

        String sql = """
            SELECT
                id_cierre_mensual,
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
                                CierreMensualDepositosPreviewDTO
                                        .builder()

                                        .idCierreMensual(
                                                rs.getLong(
                                                        "id_cierre_mensual"
                                                )
                                        )

                                        .fechaCierre(
                                                rs.getDate(
                                                        "fecha_cierre"
                                                ).toLocalDate()
                                        )

                                        .anio(
                                                rs.getInt(
                                                        "anio"
                                                )
                                        )

                                        .mes(
                                                rs.getInt(
                                                        "mes"
                                                )
                                        )

                                        .estado(
                                                rs.getString(
                                                        "estado_cierre"
                                                )
                                        )

                                        .totalCuentas(
                                                rs.getInt(
                                                        "total_cuentas"
                                                )
                                        )

                                        .saldoTotal(
                                                rs.getBigDecimal(
                                                        "saldo_total"
                                                )
                                        )

                                        .totalDebitos(
                                                rs.getBigDecimal(
                                                        "total_debitos"
                                                )
                                        )

                                        .totalCreditos(
                                                rs.getBigDecimal(
                                                        "total_creditos"
                                                )
                                        )

                                        .build(),

                        idCierre
                );


        // =====================================================
        // 1. RECUPERAR DETALLE
        // =====================================================

        List<CierreMensualDepositosDetalleDTO> detalle =
                obtenerDetallePorCierre(
                        idCierre
                );


        // =====================================================
        // 2. RECUPERAR RESUMEN POR AGENCIA + FORMA
        // =====================================================

        List<CierreMensualDepositosResumenFormaDTO>
                resumenFormas =
                obtenerResumenFormasPorCierre(
                        idCierre
                );


        // =====================================================
        // 3. GENERAR RESUMEN POR AGENCIA
        //
        // Se reconstruye desde la fotografía persistida.
        // No necesita tabla adicional.
        // =====================================================

        List<CierreMensualDepositosResumenAgenciaDTO>
                resumenAgencias =
                generarResumenAgencias(
                        detalle
                );


        // =====================================================
        // 4. ASIGNAR DETALLE
        // =====================================================

        cierre.setDetalle(
                detalle
        );


        // =====================================================
        // 5. ASIGNAR RESUMEN POR AGENCIA
        // =====================================================

        cierre.setResumenAgencias(
                resumenAgencias
        );


        // =====================================================
        // 6. ASIGNAR RESUMEN POR AGENCIA + FORMA
        // =====================================================

        cierre.setResumenFormas(
                resumenFormas
        );


        // =====================================================
        // 7. GENERAR RESUMEN GENERAL
        // =====================================================

        cierre.setResumen(
                generarResumenGeneral(
                        detalle,
                        resumenFormas
                )
        );


        return cierre;
    }


    // =========================================================
    // OBTENER DETALLE
    // =========================================================

    public List<CierreMensualDepositosDetalleDTO>
    obtenerDetallePorCierre(
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
                    id_agencia,
                    codigo_forma,
                    codigo_cuenta
                """;

        return jdbc.query(
                sql,

                (rs, rowNum) ->
                        CierreMensualDepositosDetalleDTO
                                .builder()

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


    // =========================================================
    // OBTENER RESUMEN POR AGENCIA + FORMA
    // =========================================================

    public List<CierreMensualDepositosResumenFormaDTO>
    obtenerResumenFormasPorCierre(
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
                    id_agencia,
                    codigo_forma
                """;

        return jdbc.query(
                sql,

                (rs, rowNum) ->
                        CierreMensualDepositosResumenFormaDTO
                                .builder()

                                .idAgencia(
                                        rs.getInt(
                                                "id_agencia"
                                        )
                                )

                                .idFormaAhorro(
                                        rs.getInt(
                                                "id_forma_ahorro"
                                        )
                                )

                                .codigoForma(
                                        rs.getString(
                                                "codigo_forma"
                                        )
                                )

                                .nombreForma(
                                        rs.getString(
                                                "nombre_forma"
                                        )
                                )

                                .cantidadCuentas(
                                        rs.getInt(
                                                "cantidad_cuentas"
                                        )
                                )

                                .saldoTotal(
                                        rs.getBigDecimal(
                                                "saldo_total"
                                        )
                                )

                                .totalDebitos(
                                        rs.getBigDecimal(
                                                "total_debitos"
                                        )
                                )

                                .totalCreditos(
                                        rs.getBigDecimal(
                                                "total_creditos"
                                        )
                                )

                                .hombres(
                                        rs.getInt(
                                                "hombres"
                                        )
                                )

                                .mujeres(
                                        rs.getInt(
                                                "mujeres"
                                        )
                                )

                                .juridicas(
                                        rs.getInt(
                                                "juridicas"
                                        )
                                )

                                .build(),

                idCierre
        );
    }
}