package co.assip.erp.depositos.formasagencias;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class FormasAgenciasRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<FormaAgenciaDTO> listarPorAgencia(Integer idAgencia) {

        String sql = """
            SELECT
                f.id_forma_ahorro          AS idFormaAhorro,
                f.codigo_forma             AS codigoForma,
                f.nombre_forma             AS nombreForma,

                f.fecha_ultima_liquidacion AS fechaUltimaLiquidacion,
                f.tasa_interes_forma       AS tasaInteresForma,
                f.valor_minimo             AS valorMinimo,
                f.tiempo_liquidacion       AS tiempoLiquidacion,

                f.cuenta_gasto             AS idCuentaGasto,
                cg.codigo_cuenta           AS codigoCuentaGasto,
                cg.nombre_cuenta           AS nombreCuentaGasto,

                f.cuenta_forma_corto       AS idCuentaFormaCorto,
                cf.codigo_cuenta           AS codigoCuentaFormaCorto,
                cf.nombre_cuenta           AS nombreCuentaFormaCorto,

                f.cuenta_retencion_fuente  AS idCuentaRetencionFuente,
                cr.codigo_cuenta           AS codigoCuentaRetencionFuente,
                cr.nombre_cuenta           AS nombreCuentaRetencionFuente

            FROM depositos.formas_ahorro f

            LEFT JOIN contabilidad.catalogo_cuentas cg
                   ON cg.id_catalogo_cuenta = f.cuenta_gasto
                  AND cg.id_agencia = f.id_agencia

            LEFT JOIN contabilidad.catalogo_cuentas cf
                   ON cf.id_catalogo_cuenta = f.cuenta_forma_corto
                  AND cf.id_agencia = f.id_agencia

            LEFT JOIN contabilidad.catalogo_cuentas cr
                   ON cr.id_catalogo_cuenta = f.cuenta_retencion_fuente
                  AND cr.id_agencia = f.id_agencia

            WHERE f.id_agencia = :idAgencia
            ORDER BY f.codigo_forma;
        """;

        return jdbc.query(
                sql,
                Map.of("idAgencia", idAgencia),
                (rs, rowNum) -> new FormaAgenciaDTO(
                        rs.getInt("idFormaAhorro"),
                        rs.getString("codigoForma"),
                        rs.getString("nombreForma"),

                        rs.getDate("fechaUltimaLiquidacion") != null
                                ? rs.getDate("fechaUltimaLiquidacion").toLocalDate()
                                : null,

                        rs.getBigDecimal("tasaInteresForma"),
                        rs.getBigDecimal("valorMinimo"),

                        rs.getString("tiempoLiquidacion"),

                        rs.getInt("idCuentaGasto"),
                        rs.getString("codigoCuentaGasto"),
                        rs.getString("nombreCuentaGasto"),

                        rs.getInt("idCuentaFormaCorto"),
                        rs.getString("codigoCuentaFormaCorto"),
                        rs.getString("nombreCuentaFormaCorto"),

                        rs.getObject("idCuentaRetencionFuente") != null
                                ? rs.getInt("idCuentaRetencionFuente")
                                : null,
                        rs.getString("codigoCuentaRetencionFuente"),
                        rs.getString("nombreCuentaRetencionFuente")
                )
        );
    }
}