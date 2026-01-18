package co.assip.erp.general.empresas;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EmpresaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public EmpresaRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * ✅ Retorna los datos de la empresa (único registro).
     */
    public EmpresaDTO obtenerEmpresa() {

        String sql = """
            SELECT
              tipo_documento,
              documento_empresa,
              digito_verificacion,
              razon_social,
              sigla_empresa,
              fecha_constitucion,
              id_pais_documento,
              id_departamento,
              id_ciudad,
              correo_corporativo,
              telefono,
              celular,
              sitio_web,
              logo_url,
              id_datos_personal_empresa
            FROM general.empresas
            LIMIT 1
            """;

        return jdbc.queryForObject(sql, new MapSqlParameterSource(), (rs, rowNum) -> {

            EmpresaDTO e = new EmpresaDTO();
            e.setTipoDocumento(rs.getString("tipo_documento"));
            e.setDocumentoEmpresa(rs.getString("documento_empresa"));
            e.setDigitoVerificacion(rs.getString("digito_verificacion"));
            e.setRazonSocial(rs.getString("razon_social"));
            e.setSiglaEmpresa(rs.getString("sigla_empresa"));

            java.sql.Date fc = rs.getDate("fecha_constitucion");
            if (fc != null) {
                e.setFechaConstitucion(fc.toLocalDate());
            }

            e.setIdPaisDocumento((Integer) rs.getObject("id_pais_documento"));
            e.setIdDepartamento((Integer) rs.getObject("id_departamento"));
            e.setIdCiudad((Integer) rs.getObject("id_ciudad"));

            e.setCorreoCorporativo(rs.getString("correo_corporativo"));
            e.setTelefono(rs.getString("telefono"));
            e.setCelular(rs.getString("celular"));
            e.setSitioWeb(rs.getString("sitio_web"));
            e.setLogoUrl(rs.getString("logo_url"));

            e.setIdDatosPersonalEmpresa(rs.getObject("id_datos_personal_empresa", Long.class));

            return e;
        });
    }

    /**
     * ✅ Retorna SOLO el tercero contable de la empresa (único registro).
     * Útil para procesos masivos (depreciación, cierres, etc).
     */
    public Long obtenerIdDatosPersonalEmpresa() {

        String sql = """
            SELECT id_datos_personal_empresa
            FROM general.empresas
            LIMIT 1
            """;

        Long id = jdbc.queryForObject(sql, new MapSqlParameterSource(), Long.class);

        if (id == null) {
            throw new IllegalStateException(
                    "No se encontró id_datos_personal_empresa en general.empresas."
            );
        }

        return id;
    }
}
