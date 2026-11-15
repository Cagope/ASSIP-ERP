package co.assip.erp.contabilidad.informes.auxiliares.auxiliar_exportacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuxiliarExportacionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AuxiliarExportacionRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> consultar(int agencia, LocalDate inicial,
                                                LocalDate fin, boolean resumido) {
        String tercero = String.join(", ",
                "dp.tipo_persona AS tipo_persona",
                "dp.tipo_documento AS tipo_documento",
                "dp.documento AS documento",
                "dp.digito_verificacion AS dv",
                "dp.nombres AS nombres_originales",
                "dp.primer_apellido AS apellido1",
                "dp.segundo_apellido AS apellido2",
                "u.direccion AS direccion",
                "p.codigo_pais AS pais",
                "d.codigo_departamento AS departamento",
                "c.codigo_ciudad AS municipio",
                "u.id_ubicacion AS id_ubicacion",
                "cc.codigo_cuenta AS cuenta",
                "cc.nombre_cuenta AS nombre_cuenta",
                "cc.naturaleza_cuenta AS naturaleza"
        );

        String comun = String.join(" ",
                "FROM contabilidad.auxiliares_contables ac",
                "JOIN contabilidad.catalogo_cuentas cc",
                "ON cc.id_catalogo_cuenta = ac.id_catalogo_cuenta",
                "AND cc.id_agencia = ac.id_agencia",
                "LEFT JOIN hoja_vida.datos_personales dp",
                "ON dp.id_datos_personal = ac.id_datos_personal",
                "LEFT JOIN hoja_vida.ubicaciones u",
                "ON u.id_datos_personal = ac.id_datos_personal",
                "LEFT JOIN catalogos.paises p ON p.id_pais = u.id_pais",
                "LEFT JOIN catalogos.departamentos d ON d.id_departamento = u.id_departamento",
                "LEFT JOIN catalogos.ciudades c ON c.id_ciudad = u.id_ciudad",
                "WHERE ac.id_agencia = :agencia",
                "AND ac.fecha_auxiliar BETWEEN :inicial AND :final",
                "AND ac.estado_movimiento in ('A', 'C') "
        );

        String sql;
        if (resumido) {
            sql = "SELECT " + tercero
                    + ", SUM(ac.valor_debito) AS debito"
                    + ", SUM(ac.valor_credito) AS credito "
                    + comun
                    + " GROUP BY dp.tipo_persona, dp.tipo_documento, dp.documento,"
                    + " dp.digito_verificacion, dp.nombres, dp.primer_apellido,"
                    + " dp.segundo_apellido, u.direccion, p.codigo_pais,"
                    + " d.codigo_departamento, c.codigo_ciudad, u.id_ubicacion,"
                    + " cc.codigo_cuenta, cc.nombre_cuenta, cc.naturaleza_cuenta"
                    + " ORDER BY cc.codigo_cuenta, dp.documento";
        } else {
            sql = "SELECT " + tercero
                    + ", ac.fecha_auxiliar AS fecha"
                    + ", ac.tipo_comprobante AS tipo_comprobante"
                    + ", ac.numero_comprobante AS numero_comprobante"
                    + ", ac.detalle_movimiento AS detalle"
                    + ", ac.valor_debito AS debito"
                    + ", ac.valor_credito AS credito "
                    + comun
                    + " ORDER BY ac.fecha_auxiliar, cc.codigo_cuenta,"
                    + " ac.tipo_comprobante, ac.numero_comprobante, ac.id_auxiliar_contable";
        }

        MapSqlParameterSource parametros = new MapSqlParameterSource()
                .addValue("agencia", agencia)
                .addValue("inicial", inicial)
                .addValue("final", fin);
        return jdbc.queryForList(sql, parametros);
    }
}
