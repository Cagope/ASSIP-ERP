package co.assip.erp.shared.cuentas_ahorro;

import co.assip.erp.shared.cuentas_ahorro.dto.CuentaAhorroSelectDTO;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CuentasAhorroRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CuentasAhorroRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CuentaAhorroSelectDTO> listarPorDatosPersonal(
            Integer idDatosPersonal,
            List<Integer> agencias
    ) {

        StringBuilder sql = new StringBuilder("""
        SELECT
            ca.id_cuenta_ahorro          AS idCuentaAhorro,
            ca.id_datos_personal         AS idDatosPersonal,

            ca.id_forma_ahorro           AS codigoForma,
            f.nombre_forma               AS nombreForma,

            ca.codigo_cuenta             AS numeroCuenta,

            ea.codigo_estado_ahorro      AS estadoCodigo,
            ea.descripcion_estado_ahorro AS estadoNombre,

            (ca.id_forma_ahorro || '-' ||
             ca.codigo_cuenta || ' ' ||
             f.nombre_forma)             AS cuentaDisplay

        FROM depositos.cuentas_ahorro ca

        JOIN depositos.formas_ahorro f
          ON f.id_forma_ahorro = ca.id_forma_ahorro

        JOIN depositos.estados_ahorros ea
          ON ea.codigo_estado_ahorro = ca.estado_cuenta_cuenta

        WHERE ca.id_datos_personal = :idDatosPersonal
          AND ca.id_forma_ahorro <> 1
          AND ea.operativo = true
    """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idDatosPersonal", idDatosPersonal);

        if (agencias != null && !agencias.isEmpty()) {
            sql.append(" AND ca.id_agencia IN (:agencias) ");
            params.addValue("agencias", agencias);
        }

        sql.append(" ORDER BY ca.id_forma_ahorro, ca.codigo_cuenta ");

        return jdbc.query(sql.toString(), params, (rs, rowNum) ->
                new CuentaAhorroSelectDTO(
                        rs.getInt("idCuentaAhorro"),
                        rs.getInt("idDatosPersonal"),
                        rs.getInt("codigoForma"),
                        rs.getString("nombreForma"),
                        rs.getString("numeroCuenta"),
                        rs.getString("estadoCodigo"),
                        rs.getString("estadoNombre"),
                        rs.getString("cuentaDisplay")
                )
        );
    }

}
