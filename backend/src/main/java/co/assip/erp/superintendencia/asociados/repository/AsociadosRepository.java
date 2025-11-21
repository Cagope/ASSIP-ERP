package co.assip.erp.superintendencia.asociados.repository;

import co.assip.erp.superintendencia.asociados.dto.AsociadoDTO;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AsociadosRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AsociadosRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<AsociadoDTO> consultar(String fechaCorte) {

        String sql = """
            WITH salario_minimo AS (
                SELECT valor_parametro::numeric AS smmlv
                FROM general.parametros
                WHERE id_agencia = 1 AND codigo_parametro = 50
                LIMIT 1
            ),

            mov AS (
                SELECT 
                    e.id_cuenta_ahorro,
                    SUM(e.valor_debito)  AS total_debitos,
                    SUM(e.valor_credito) AS total_credits
                FROM depositos.extractos_cuentas_ahorros e
                WHERE e.fecha_movimiento <= CAST(:fecha AS DATE)
                GROUP BY e.id_cuenta_ahorro
            ),

            aportes AS (
                SELECT 
                    c.id_datos_personal,
                    (COALESCE(m.total_debitos,0) - COALESCE(m.total_credits,0)) AS saldo_aportes
                FROM depositos.cuentas_ahorro c
                LEFT JOIN mov m
                    ON m.id_cuenta_ahorro = c.id_cuenta_ahorro
                WHERE c.codigo_forma = '01'
            ),

            ingresos AS (
                SELECT
                    fi.id_datos_personal,
                    (
                        COALESCE(fi.valor_salario,0) +
                        COALESCE(fi.valor_pension,0) +
                        COALESCE(fi.ingresos_arriendo,0) +
                        COALESCE(fi.ingresos_comisiones,0) +
                        COALESCE(fi.otros_ingresos,0)
                    ) AS total_ingresos
                FROM hoja_vida.financieros fi
            ),

            laboral AS (
              SELECT
                  id_datos_personal,
                  CASE
                      WHEN empleado_entidad = true THEN 1
                      ELSE 0
                  END AS empleado
              FROM hoja_vida.laborales
            ),

            clasificacion AS (
                SELECT 
                    ni.codigo_natural AS codigo,
                    ni.salarios
                FROM catalogos.nivel_ingresos ni
            )

            SELECT
                hv.tipo_documento  AS tipoIdentificacion,
                hv.documento        AS numeroIdentificacion,
                hv.digito_verificacion AS digitoVerificacion,
                hv.primer_apellido  AS primerApellido,
                hv.segundo_apellido AS segundoApellido,
                hv.nombres          AS nombres,
                hv.tipo_persona     AS tipoPersona,

                TO_CHAR(hv.fecha_apertura, 'DD/MM/YYYY') AS fechaIngreso,

                hv.telefono,
                hv.celular_uno AS celular,
                hv.direccion_residencia AS direccion,

                1 AS rolAsociado,
                1 AS activo,
                '0000' AS actividadEconomica,

                hv.codigo_dane_residencia AS codigoMunicipio,
                hv.correo_personal AS email,

                CASE hv.codigo_genero
                  WHEN '1' THEN 1   -- Masculino
                  WHEN '2' THEN 2   -- Femenino
                  WHEN '3' THEN 3   -- Jurídica
                  ELSE 3
                END AS genero,

                COALESCE(l.empleado, 0) AS empleado,
                0 AS tipoContrato,

                hv.codigo_escolaridad::int AS nivelEscolaridad,
                hv.estrato_social          AS estrato,

                (
                    SELECT codigo 
                    FROM clasificacion c
                    CROSS JOIN salario_minimo sm
                    WHERE 
                        (COALESCE(i.total_ingresos,0) / sm.smmlv) <= c.salarios
                    ORDER BY c.salarios
                    LIMIT 1
                ) AS nivelIngresos,

                TO_CHAR(hv.fecha_nacimiento, 'DD/MM/YYYY') AS fechaNacimiento,

                CASE hv.codigo_estado_civil
                    WHEN '1' THEN 1
                    WHEN '2' THEN 2
                    WHEN '3' THEN 3
                    WHEN '4' THEN 4
                    WHEN '5' THEN 5
                    WHEN '6' THEN 6
                    ELSE 0
                END AS estadoCivil,

                hv.cabeza_familia AS mujerCabezaFamilia,

                hv.codigo_ocupacion::int        AS ocupacion,
                hv.codigo_sector_economico::int AS sectorEconomico,
                1 AS jornadaLaboral,

                NULL AS fechaRetiro,
                0 AS asistioAsamblea,

                ap.saldo_aportes AS saldoAportes

            FROM reporting.vw_hoja_vida_general_total_extendida hv
            JOIN aportes ap
                ON ap.id_datos_personal = hv.id_datos_personal
            LEFT JOIN ingresos i
                ON i.id_datos_personal = hv.id_datos_personal
            LEFT JOIN laboral l
                ON l.id_datos_personal = hv.id_datos_personal
            WHERE ap.saldo_aportes > 0
            ORDER BY hv.primer_apellido, hv.segundo_apellido, hv.nombres
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("fecha", fechaCorte);

        return jdbc.query(sql, params, (rs, idx) -> {
            AsociadoDTO dto = new AsociadoDTO();

            dto.setTipoIdentificacion(rs.getString("tipoIdentificacion"));
            dto.setNumeroIdentificacion(rs.getString("numeroIdentificacion"));
            dto.setDigitoVerificacion(rs.getString("digitoVerificacion"));

            String tipoPersona = rs.getString("tipoPersona");
            dto.setTipoPersona(tipoPersona);

            // PERSONA JURIDICA → Apellidos vacíos
            if ("2".equals(tipoPersona)) {
                dto.setPrimerApellido("");
                dto.setSegundoApellido("");
                dto.setNombres(rs.getString("nombres"));
            } else {
                dto.setPrimerApellido(rs.getString("primerApellido"));
                dto.setSegundoApellido(rs.getString("segundoApellido"));
                dto.setNombres(rs.getString("nombres"));
            }

            dto.setFechaIngreso(rs.getString("fechaIngreso"));
            dto.setTelefono(rs.getString("telefono"));
            dto.setCelular(rs.getString("celular"));
            dto.setDireccion(rs.getString("direccion"));

            dto.setRolAsociado(rs.getInt("rolAsociado"));
            dto.setActivo(rs.getInt("activo"));
            dto.setActividadEconomica(rs.getString("actividadEconomica"));

            dto.setCodigoMunicipio(rs.getString("codigoMunicipio"));
            dto.setEmail(rs.getString("email"));

            dto.setGenero(rs.getInt("genero"));
            dto.setEmpleado(rs.getInt("empleado"));
            dto.setTipoContrato(rs.getInt("tipoContrato"));

            dto.setNivelEscolaridad(rs.getInt("nivelEscolaridad"));
            dto.setEstrato(rs.getInt("estrato"));
            dto.setNivelIngresos(rs.getInt("nivelIngresos"));

            dto.setFechaNacimiento(rs.getString("fechaNacimiento"));

            dto.setEstadoCivil(rs.getInt("estadoCivil"));
            dto.setMujerCabezaFamilia(rs.getInt("mujerCabezaFamilia"));
            dto.setOcupacion(rs.getInt("ocupacion"));
            dto.setSectorEconomico(rs.getInt("sectorEconomico"));
            dto.setJornadaLaboral(rs.getInt("jornadaLaboral"));

            dto.setFechaRetiro(rs.getString("fechaRetiro"));
            dto.setAsistioAsamblea(rs.getInt("asistioAsamblea"));
            dto.setSaldoAportes(rs.getDouble("saldoAportes"));

            return dto;
        });
    }
}
