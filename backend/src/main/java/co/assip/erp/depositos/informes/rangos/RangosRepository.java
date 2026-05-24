package co.assip.erp.depositos.informes.rangos;

import co.assip.erp.depositos.informes.rangos.dto.RangosFiltroDTO;
import co.assip.erp.depositos.informes.rangos.RangosRequestDTO;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RangosRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public RangosRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String BASE_SQL = """
        WITH mov AS (
            SELECT
                e.id_cuenta_ahorro,
                SUM(COALESCE(e.valor_debito, 0))  AS total_debitos,
                SUM(COALESCE(e.valor_credito, 0)) AS total_creditos
            FROM depositos.extractos_cuentas_ahorros e
            WHERE e.fecha_movimiento <= CAST(:fechaCorte AS DATE)
            GROUP BY e.id_cuenta_ahorro
        ),
        hv_unica AS (
            SELECT *
            FROM (
                SELECT
                    h.*,
                    ROW_NUMBER() OVER (
                        PARTITION BY h.id_datos_personal
                        ORDER BY h.id_datos_personal
                    ) AS rn
                FROM reporting.vw_hoja_vida_general_total_reciente h
            ) x
            WHERE x.rn = 1
        ),
        base AS (
            SELECT
                c.id_cuenta_ahorro,
                c.codigo_cuenta,

                c.id_agencia,
                a.codigo_agencia,
                a.nombre_agencia,

                c.id_forma_ahorro,
                LPAD(f.codigo_forma::text, 2, '0') AS codigo_forma_str,
                f.nombre_forma,

                c.estado_cuenta_cuenta AS estado_cuenta_codigo,
                ea.descripcion_estado_ahorro AS estado_cuenta_nombre,

                hv.documento,
                CONCAT_WS(' ', hv.primer_apellido, hv.segundo_apellido, hv.nombres) AS nombre_completo,
                hv.fecha_nacimiento,
                hv.nombre_genero,
                hv.nombre_estado_civil,
                hv.tipo_persona,

                hv.nombre_zona,
                hv.nombre_sub_zona,
                hv.ciudad_residencia AS nombre_ciudad,
                hv.departamento_residencia AS nombre_departamento,
                hv.pais_residencia AS nombre_pais,

                hv.telefono,
                hv.celular_uno AS celular,
                hv.correo_personal AS correo,

                c.fecha_apertura_cuenta,

                (
                    COALESCE(m.total_creditos, 0)
                    -
                    COALESCE(m.total_debitos, 0)
                ) AS saldo,

                EXTRACT(YEAR FROM age(CAST(:fechaCorte AS DATE), hv.fecha_nacimiento))::int AS edad_anios,

                (
                    EXTRACT(YEAR FROM age(CAST(:fechaCorte AS DATE), c.fecha_apertura_cuenta)) * 12 +
                    EXTRACT(MONTH FROM age(CAST(:fechaCorte AS DATE), c.fecha_apertura_cuenta))
                )::int AS antiguedad_meses

            FROM depositos.cuentas_ahorro c

            JOIN general.datos_agencias a
                ON a.id_agencia = c.id_agencia

            JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro

            LEFT JOIN depositos.estados_ahorros ea
                ON ea.codigo_estado_ahorro = c.estado_cuenta_cuenta

            LEFT JOIN hv_unica hv
                ON hv.id_datos_personal = c.id_datos_personal

            LEFT JOIN mov m
                ON m.id_cuenta_ahorro = c.id_cuenta_ahorro
        )
        SELECT *
        FROM base
        WHERE saldo > 0
          AND (:idAgencia = 0 OR id_agencia = :idAgencia)
          AND (
                :codigoForma = '0'
             OR codigo_forma_str = LPAD(:codigoForma, 2, '0')
          )
        """;

    public List<RangosItemDTO> consultarPorEdad(RangosRequestDTO req) {

        StringBuilder filtros = new StringBuilder(" AND (");
        Map<String, Object> params = baseParams(req);

        List<RangosFiltroDTO> rs = req.getRangos();

        for (int i = 0; i < rs.size(); i++) {
            filtros.append("(edad_anios BETWEEN :eMin").append(i)
                    .append(" AND :eMax").append(i).append(")");

            if (i < rs.size() - 1) {
                filtros.append(" OR ");
            }

            params.put("eMin" + i, rs.get(i).getDesde());
            params.put("eMax" + i, rs.get(i).getHasta());
        }

        filtros.append(")");

        return jdbc.query(
                BASE_SQL + filtros + " ORDER BY id_agencia, codigo_cuenta",
                params,
                this::mapRow
        );
    }

    public List<RangosItemDTO> consultarPorSaldo(RangosRequestDTO req) {

        StringBuilder filtros = new StringBuilder(" AND (");
        Map<String, Object> params = baseParams(req);

        List<RangosFiltroDTO> rs = req.getRangos();

        for (int i = 0; i < rs.size(); i++) {
            filtros.append("(saldo BETWEEN :sMin").append(i)
                    .append(" AND :sMax").append(i).append(")");

            if (i < rs.size() - 1) {
                filtros.append(" OR ");
            }

            params.put("sMin" + i, rs.get(i).getDesde());
            params.put("sMax" + i, rs.get(i).getHasta());
        }

        filtros.append(")");

        return jdbc.query(
                BASE_SQL + filtros + " ORDER BY id_agencia, codigo_cuenta",
                params,
                this::mapRow
        );
    }

    public List<RangosItemDTO> consultarPorAntiguedad(RangosRequestDTO req) {

        StringBuilder filtros = new StringBuilder(" AND (");
        Map<String, Object> params = baseParams(req);

        List<RangosFiltroDTO> rs = req.getRangos();

        for (int i = 0; i < rs.size(); i++) {
            filtros.append("(antiguedad_meses BETWEEN :aMin").append(i)
                    .append(" AND :aMax").append(i).append(")");

            if (i < rs.size() - 1) {
                filtros.append(" OR ");
            }

            params.put("aMin" + i, rs.get(i).getDesde() * 12);
            params.put("aMax" + i, rs.get(i).getHasta() * 12);
        }

        filtros.append(")");

        return jdbc.query(
                BASE_SQL + filtros + " ORDER BY id_agencia, codigo_cuenta",
                params,
                this::mapRow
        );
    }

    private Map<String, Object> baseParams(RangosRequestDTO req) {
        Map<String, Object> p = new HashMap<>();

        p.put(
                "idAgencia",
                req.getIdAgencia() == null ? 0 : req.getIdAgencia()
        );

        p.put(
                "codigoForma",
                req.getCodigoForma() == null || req.getCodigoForma().isBlank()
                        ? "0"
                        : req.getCodigoForma().trim()
        );

        p.put("fechaCorte", req.getFechaCorte());

        return p;
    }

    private RangosItemDTO mapRow(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {

        RangosItemDTO dto = new RangosItemDTO();

        dto.setCodigoCuenta(rs.getString("codigo_cuenta"));
        dto.setCodigoAgencia(String.format("%02d", rs.getInt("codigo_agencia")));
        dto.setNombreAgencia(rs.getString("nombre_agencia"));

        dto.setCodigoForma(rs.getString("codigo_forma_str"));
        dto.setNombreForma(rs.getString("nombre_forma"));

        dto.setEstadoCuentaCodigo(rs.getString("estado_cuenta_codigo"));
        dto.setEstadoCuentaNombre(rs.getString("estado_cuenta_nombre"));

        dto.setDocumento(rs.getString("documento"));
        dto.setNombreCompleto(rs.getString("nombre_completo"));
        dto.setGenero(rs.getString("nombre_genero"));
        dto.setEstadoCivil(rs.getString("nombre_estado_civil"));
        dto.setTipoPersona(rs.getString("tipo_persona"));

        dto.setZona(rs.getString("nombre_zona"));
        dto.setSubZona(rs.getString("nombre_sub_zona"));
        dto.setCiudad(rs.getString("nombre_ciudad"));
        dto.setDepartamento(rs.getString("nombre_departamento"));
        dto.setPais(rs.getString("nombre_pais"));

        dto.setTelefono(rs.getString("telefono"));
        dto.setCelular(rs.getString("celular"));
        dto.setCorreo(rs.getString("correo"));

        dto.setFechaNacimiento(
                rs.getDate("fecha_nacimiento") != null
                        ? rs.getDate("fecha_nacimiento").toString()
                        : null
        );

        dto.setFechaApertura(
                rs.getDate("fecha_apertura_cuenta") != null
                        ? rs.getDate("fecha_apertura_cuenta").toString()
                        : null
        );

        dto.setSaldo(rs.getDouble("saldo"));
        dto.setEdadAnios(rs.getInt("edad_anios"));

        int antigMeses = rs.getInt("antiguedad_meses");
        dto.setAntiguedadMeses(antigMeses);
        dto.setAntiguedadAnios(antigMeses / 12);

        return dto;
    }
}