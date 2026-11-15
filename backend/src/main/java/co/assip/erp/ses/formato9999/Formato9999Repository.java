
package co.assip.erp.ses.formato9999;

import co.assip.erp.ses.formato9999.dto.Formato9999DTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class Formato9999Repository {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("ddMMyyyy");

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Consulta los asociados con saldo positivo de aportes sociales
     * a la fecha de corte, consolidando todas las agencias.
     */
    public List<Formato9999DTO> consultar(String fechaCorte) {

        LocalDate fecha = LocalDate.parse(fechaCorte);
        String fechaReporte = fecha.format(FORMATO_FECHA);

        String sql = """
            WITH salario_minimo AS (
                SELECT
                    valor_parametro::numeric AS smmlv
                FROM general.parametros
                WHERE id_agencia = 1
                  AND codigo_parametro = 50
                LIMIT 1
            ),

            mov AS (
                SELECT
                    e.id_cuenta_ahorro,
                    SUM(COALESCE(e.valor_debito, 0)) AS total_debitos,
                    SUM(COALESCE(e.valor_credito, 0)) AS total_creditos
                FROM depositos.extractos_cuentas_ahorros e
                WHERE e.fecha_movimiento <= :fecha
                GROUP BY e.id_cuenta_ahorro
            ),

            aportes AS (
                SELECT
                    c.id_datos_personal,
                    SUM(
                        COALESCE(m.total_creditos, 0)
                        - COALESCE(m.total_debitos, 0)
                    ) AS saldo_aportes
                FROM depositos.cuentas_ahorro c
                INNER JOIN depositos.formas_ahorro f
                    ON f.id_forma_ahorro = c.id_forma_ahorro
                   AND f.id_agencia = c.id_agencia
                LEFT JOIN mov m
                    ON m.id_cuenta_ahorro = c.id_cuenta_ahorro
                WHERE TRIM(f.tipo_captacion_forma) = '1'
                  AND c.fecha_apertura_cuenta <= :fecha
                GROUP BY c.id_datos_personal
                HAVING SUM(
                    COALESCE(m.total_creditos, 0)
                    - COALESCE(m.total_debitos, 0)
                ) > 0
            ),

            ingresos AS (
                SELECT
                    fi.id_datos_personal,
                    (
                        COALESCE(fi.valor_salario, 0)
                        + COALESCE(fi.valor_pension, 0)
                        + COALESCE(fi.ingresos_arriendo, 0)
                        + COALESCE(fi.ingresos_comisiones, 0)
                        + COALESCE(fi.otros_ingresos, 0)
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
            ),

            entidad AS (
                SELECT
                    TRIM(codigo_supersolidaria) AS codigo_entidad
                FROM general.empresas
                WHERE codigo_supersolidaria IS NOT NULL
                  AND TRIM(codigo_supersolidaria) <> ''
                ORDER BY documento_empresa
                LIMIT 1
            )

            SELECT
                entidad.codigo_entidad AS codigoEntidad,

                hv.tipo_documento AS tipoIdentificacion,
                hv.documento AS numeroIdentificacion,
                hv.digito_verificacion AS digitoVerificacion,

                hv.primer_apellido AS primerApellido,
                hv.segundo_apellido AS segundoApellido,
                hv.nombres AS nombres,
                hv.tipo_persona AS tipoPersona,

                TO_CHAR(
                    hv.fecha_apertura,
                    'DDMMYYYY'
                ) AS fechaIngreso,

                hv.telefono,
                hv.celular_uno AS celular,
                hv.direccion_residencia AS direccion,

                1 AS rolAsociado,
                1 AS activo,

                '0000' AS actividadEconomica,

                hv.codigo_dane_residencia AS codigoMunicipio,
                hv.correo_personal AS email,

                CASE hv.codigo_genero
                    WHEN '1' THEN 1
                    WHEN '2' THEN 2
                    WHEN '3' THEN 3
                    ELSE 3
                END AS genero,

                COALESCE(l.empleado, 0) AS empleado,

                0 AS tipoContrato,

                hv.codigo_escolaridad::int AS nivelEscolaridad,
                hv.estrato_social AS estrato,

                (
                    SELECT
                        c.codigo
                    FROM clasificacion c
                    CROSS JOIN salario_minimo sm
                    WHERE (
                        COALESCE(i.total_ingresos, 0)
                        / NULLIF(sm.smmlv, 0)
                    ) <= c.salarios
                    ORDER BY c.salarios
                    LIMIT 1
                ) AS nivelIngresos,

                TO_CHAR(
                    hv.fecha_nacimiento,
                    'DDMMYYYY'
                ) AS fechaNacimiento,

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

                hv.codigo_ocupacion::int AS ocupacion,
                hv.codigo_sector_economico::int AS sectorEconomico,

                1 AS jornadaLaboral,

                NULL AS fechaRetiro,
                0 AS asistioAsamblea,

                ap.saldo_aportes AS saldoAportes

            FROM reporting.vw_hoja_vida_general_total_extendida hv

            INNER JOIN aportes ap
                ON ap.id_datos_personal = hv.id_datos_personal

            LEFT JOIN ingresos i
                ON i.id_datos_personal = hv.id_datos_personal

            LEFT JOIN laboral l
                ON l.id_datos_personal = hv.id_datos_personal

            CROSS JOIN entidad

            WHERE ap.saldo_aportes > 0

            ORDER BY
                hv.primer_apellido,
                hv.segundo_apellido,
                hv.nombres
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue("fecha", fecha);

        return jdbc.query(
                sql,
                parametros,
                (rs, rowNum) -> {

                    Formato9999DTO dto = new Formato9999DTO();

                    // Identificación del reporte SES
                    dto.setCodigoEntidad(
                            rs.getString("codigoEntidad")
                    );

                    dto.setFechaCorte(fechaReporte);

                    // Identificación del asociado
                    dto.setTipoIdentificacion(
                            rs.getString("tipoIdentificacion")
                    );

                    dto.setNumeroIdentificacion(
                            rs.getString("numeroIdentificacion")
                    );

                    dto.setDigitoVerificacion(
                            rs.getString("digitoVerificacion")
                    );

                    // Información personal
                    String tipoPersona =
                            rs.getString("tipoPersona");

                    dto.setTipoPersona(tipoPersona);

                    if ("2".equals(tipoPersona)) {

                        dto.setPrimerApellido("");
                        dto.setSegundoApellido("");

                        dto.setNombres(
                                rs.getString("nombres")
                        );

                    } else {

                        dto.setPrimerApellido(
                                rs.getString("primerApellido")
                        );

                        dto.setSegundoApellido(
                                rs.getString("segundoApellido")
                        );

                        dto.setNombres(
                                rs.getString("nombres")
                        );
                    }

                    // Vinculación
                    dto.setFechaIngreso(
                            rs.getString("fechaIngreso")
                    );

                    dto.setRolAsociado(
                            rs.getInt("rolAsociado")
                    );

                    dto.setActivo(
                            rs.getInt("activo")
                    );

                    // Contacto
                    dto.setTelefono(
                            rs.getString("telefono")
                    );

                    dto.setCelular(
                            rs.getString("celular")
                    );

                    dto.setDireccion(
                            rs.getString("direccion")
                    );

                    dto.setCodigoMunicipio(
                            rs.getString("codigoMunicipio")
                    );

                    dto.setEmail(
                            rs.getString("email")
                    );

                    // Actividad económica
                    dto.setActividadEconomica(
                            rs.getString("actividadEconomica")
                    );

                    dto.setSectorEconomico(
                            rs.getInt("sectorEconomico")
                    );

                    dto.setOcupacion(
                            rs.getInt("ocupacion")
                    );

                    // Información laboral
                    dto.setEmpleado(
                            rs.getInt("empleado")
                    );

                    dto.setTipoContrato(
                            rs.getInt("tipoContrato")
                    );

                    dto.setJornadaLaboral(
                            rs.getInt("jornadaLaboral")
                    );

                    // Información socioeconómica
                    dto.setNivelEscolaridad(
                            rs.getInt("nivelEscolaridad")
                    );

                    dto.setEstrato(
                            rs.getInt("estrato")
                    );

                    dto.setNivelIngresos(
                            rs.getInt("nivelIngresos")
                    );

                    // Información demográfica
                    dto.setFechaNacimiento(
                            rs.getString("fechaNacimiento")
                    );

                    dto.setGenero(
                            rs.getInt("genero")
                    );

                    dto.setEstadoCivil(
                            rs.getInt("estadoCivil")
                    );

                    dto.setMujerCabezaFamilia(
                            rs.getInt("mujerCabezaFamilia")
                    );

                    // Retiro y participación
                    dto.setFechaRetiro(
                            rs.getString("fechaRetiro")
                    );

                    dto.setAsistioAsamblea(
                            rs.getInt("asistioAsamblea")
                    );

                    // Información financiera
                    dto.setSaldoAportes(
                            rs.getDouble("saldoAportes")
                    );

                    return dto;
                }
        );
    }
}
