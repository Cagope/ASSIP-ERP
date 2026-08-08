package co.assip.erp.cartera.evaluacion.proceso.motor;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EvaluacionMotorParametrosRepository {

    /*
     * La evaluación institucional utiliza la configuración
     * general registrada para la agencia 1.
     */
    private static final Integer ID_AGENCIA_INSTITUCIONAL = 1;

    /*
     * Parámetro:
     *
     * 210 = Puntaje mínimo resultado favorable
     *       de evaluación de cartera.
     */
    private static final Integer PARAMETRO_PUNTAJE_MINIMO_FAVORABLE = 210;

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // OBTENER PUNTAJE MÍNIMO PARA RESULTADO FAVORABLE
    // =========================================================

    public BigDecimal obtenerPuntajeMinimoResultadoFavorable() {

        String sql = """
                SELECT
                    p.valor_parametro

                FROM general.parametros p

                WHERE p.id_agencia =
                      :idAgencia

                  AND p.codigo_parametro =
                      :codigoParametro
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idAgencia",
                                ID_AGENCIA_INSTITUCIONAL
                        )
                        .addValue(
                                "codigoParametro",
                                PARAMETRO_PUNTAJE_MINIMO_FAVORABLE
                        );

        List<BigDecimal> resultados =
                jdbc.query(
                        sql,
                        parametros,
                        (
                                rs,
                                numeroFila
                        ) -> rs.getBigDecimal(
                                "valor_parametro"
                        )
                );

        return resultados.stream()
                .findFirst()
                .orElse(null);
    }
}