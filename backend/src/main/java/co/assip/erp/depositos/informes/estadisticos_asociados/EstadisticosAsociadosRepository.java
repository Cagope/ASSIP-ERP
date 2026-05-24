package co.assip.erp.depositos.informes.estadisticos_asociados;

import co.assip.erp.depositos.informes.estadisticos_asociados.dto.EstadisticosAsociadosDetalleDTO;
import co.assip.erp.depositos.informes.estadisticos_asociados.dto.EstadisticosAsociadosItemDTO;
import co.assip.erp.depositos.informes.estadisticos_asociados.dto.EstadisticosAsociadosRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EstadisticosAsociadosRepository {

    private final JdbcTemplate jdbc;

    public List<EstadisticosAsociadosItemDTO> consultar(
            EstadisticosAsociadosRequestDTO request
    ) {

        List<EstadisticosAsociadosRow> rows =
                consultarRows(request);

        List<EstadisticosAsociadosItemDTO> resultado =
                new ArrayList<>();

        agregarGrupo(resultado, rows, "Género");
        agregarGrupo(resultado, rows, "Estado civil");
        agregarGrupo(resultado, rows, "Cabeza familia");
        agregarGrupo(resultado, rows, "Escolaridad");
        agregarGrupo(resultado, rows, "Tipo vivienda");
        agregarGrupo(resultado, rows, "Ocupación");
        agregarGrupo(resultado, rows, "Sector económico");

        return resultado;
    }

    public List<EstadisticosAsociadosDetalleDTO> consultarDetalle(
            EstadisticosAsociadosRequestDTO request
    ) {

        List<EstadisticosAsociadosRow> rows =
                consultarRows(request);

        List<EstadisticosAsociadosDetalleDTO> detalle =
                new ArrayList<>();

        agregarDetalle(detalle, rows, "Género");
        agregarDetalle(detalle, rows, "Estado civil");
        agregarDetalle(detalle, rows, "Cabeza familia");
        agregarDetalle(detalle, rows, "Escolaridad");
        agregarDetalle(detalle, rows, "Tipo vivienda");
        agregarDetalle(detalle, rows, "Ocupación");
        agregarDetalle(detalle, rows, "Sector económico");

        return detalle;
    }

    private List<EstadisticosAsociadosRow> consultarRows(
            EstadisticosAsociadosRequestDTO request
    ) {

        String sql = """
            WITH hv_unica AS (
                SELECT DISTINCT ON (id_datos_personal)
                    *
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY id_datos_personal
            ),

            saldos AS (
                SELECT
                    c.id_datos_personal,

                    COALESCE(SUM(e.valor_credito), 0)
                    - COALESCE(SUM(e.valor_debito), 0) AS saldo_aportes

                FROM depositos.cuentas_ahorro c

                INNER JOIN depositos.formas_ahorro f
                    ON f.id_forma_ahorro = c.id_forma_ahorro

                LEFT JOIN depositos.extractos_cuentas_ahorros e
                    ON e.id_cuenta_ahorro = c.id_cuenta_ahorro
                   AND e.fecha_movimiento <= ?

                WHERE (? = 0 OR c.id_agencia = ?)
                  AND (
                        ? = '0'
                        OR f.codigo_forma = ?
                      )

                GROUP BY
                    c.id_datos_personal

                HAVING
                    COALESCE(SUM(e.valor_credito), 0)
                    - COALESCE(SUM(e.valor_debito), 0) > 0
            )

            SELECT
                dp.documento,

                CASE
                    WHEN dp.tipo_persona = '2'
                        THEN dp.nombres
                    ELSE TRIM(
                        CONCAT(
                            COALESCE(dp.primer_apellido, ''),
                            ' ',
                            COALESCE(dp.segundo_apellido, ''),
                            ' ',
                            COALESCE(dp.nombres, '')
                        )
                    )
                END AS nombre_completo,

                dp.nombre_genero,
                dp.nombre_estado_civil,
                dp.cabeza_familia,
                dp.nombre_escolaridad,
                dp.nombre_tipo_vivienda,
                dp.nombre_ocupacion,
                dp.nombre_sector_economico,

                dp.ciudad_residencia AS ciudad,
                dp.celular_uno AS celular,
                dp.correo_personal AS correo,

                s.saldo_aportes

            FROM hv_unica dp

            INNER JOIN saldos s
                ON s.id_datos_personal = dp.id_datos_personal
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        EstadisticosAsociadosRow.builder()
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .ciudad(rs.getString("ciudad"))
                                .celular(rs.getString("celular"))
                                .correo(rs.getString("correo"))
                                .genero(rs.getString("nombre_genero"))
                                .estadoCivil(rs.getString("nombre_estado_civil"))
                                .cabezaFamilia(rs.getString("cabeza_familia"))
                                .escolaridad(rs.getString("nombre_escolaridad"))
                                .tipoVivienda(rs.getString("nombre_tipo_vivienda"))
                                .ocupacion(rs.getString("nombre_ocupacion"))
                                .sectorEconomico(rs.getString("nombre_sector_economico"))
                                .saldoAportes(rs.getBigDecimal("saldo_aportes"))
                                .build(),
                request.getFechaCorte(),
                request.getIdAgencia(),
                request.getIdAgencia(),
                request.getCodigoForma(),
                request.getCodigoForma()
        );
    }

    private void agregarGrupo(
            List<EstadisticosAsociadosItemDTO> resultado,
            List<EstadisticosAsociadosRow> rows,
            String grupo
    ) {

        rows.stream()
                .map(row -> obtenerValor(row, grupo))
                .filter(v -> v != null && !v.isBlank())
                .distinct()
                .sorted()
                .forEach(categoria -> {

                    List<EstadisticosAsociadosRow> grupoRows =
                            rows.stream()
                                    .filter(r ->
                                            categoria.equals(
                                                    obtenerValor(r, grupo)
                                            )
                                    )
                                    .toList();

                    resultado.add(
                            EstadisticosAsociadosItemDTO.builder()
                                    .grupo(grupo)
                                    .categoria(categoria)
                                    .cantidad(grupoRows.size())
                                    .saldoTotal(
                                            grupoRows.stream()
                                                    .map(EstadisticosAsociadosRow::getSaldoAportes)
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                    )
                                    .build()
                    );

                });

    }

    private void agregarDetalle(
            List<EstadisticosAsociadosDetalleDTO> detalle,
            List<EstadisticosAsociadosRow> rows,
            String grupo
    ) {

        for (EstadisticosAsociadosRow row : rows) {

            String categoria =
                    obtenerValor(row, grupo);

            if (categoria == null || categoria.isBlank()) {
                continue;
            }

            detalle.add(
                    EstadisticosAsociadosDetalleDTO.builder()
                            .grupo(grupo)
                            .categoria(categoria)
                            .documento(row.getDocumento())
                            .nombreCompleto(row.getNombreCompleto())
                            .ciudad(row.getCiudad())
                            .celular(row.getCelular())
                            .correo(row.getCorreo())
                            .saldoAportes(row.getSaldoAportes())
                            .genero(row.getGenero())
                            .estadoCivil(row.getEstadoCivil())
                            .cabezaFamilia(row.getCabezaFamilia())
                            .escolaridad(row.getEscolaridad())
                            .tipoVivienda(row.getTipoVivienda())
                            .ocupacion(row.getOcupacion())
                            .sectorEconomico(row.getSectorEconomico())
                            .build()
            );
        }
    }

    private String obtenerValor(
            EstadisticosAsociadosRow row,
            String grupo
    ) {

        return switch (grupo) {

            case "Género" ->
                    row.getGenero();

            case "Estado civil" ->
                    row.getEstadoCivil();

            case "Cabeza familia" ->
                    row.getCabezaFamilia();

            case "Escolaridad" ->
                    row.getEscolaridad();

            case "Tipo vivienda" ->
                    row.getTipoVivienda();

            case "Ocupación" ->
                    row.getOcupacion();

            case "Sector económico" ->
                    row.getSectorEconomico();

            default ->
                    "";

        };

    }

}