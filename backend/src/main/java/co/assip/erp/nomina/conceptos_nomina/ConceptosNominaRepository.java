package co.assip.erp.nomina.conceptos_nomina;

import co.assip.erp.nomina.conceptos_nomina.dto.ConceptoNominaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConceptosNominaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<ConceptoNominaDTO> listar() {

        String sql = """
            SELECT
              c.codigo_concepto AS codigoConcepto,
              c.nombre_concepto AS nombreConcepto,
              c.tipo_concepto AS tipoConcepto,
              c.es_fijo AS esFijo,
              c.activo AS activo,
              c.tipo_calculo AS tipoCalculo,
              c.base_calculo AS baseCalculo,
              c.multiplicador AS multiplicador,
              c.afecta_ibc AS afectaIbc,
              c.afecta_base_cesantias AS afectaBaseCesantias,
              c.afecta_base_prima_legal AS afectaBasePrimaLegal,
              c.afecta_base_vacaciones AS afectaBaseVacaciones,
              c.afecta_base_prima_semestral AS afectaBasePrimaSemestral,
              c.afecta_base_arl AS afectaBaseArl,
              c.afecta_base_parafiscales AS afectaBaseParafiscales,
              c.smmlv_desde AS smmlvDesde,
              c.smmlv_hasta AS smmlvHasta
            FROM nomina.conceptos_nomina c
            ORDER BY c.codigo_concepto
        """;

        return jdbc.query(sql, (rs, rowNum) -> mapRow(rs));
    }

    public Optional<ConceptoNominaDTO> obtener(String codigoConcepto) {

        String sql = """
            SELECT
              c.codigo_concepto AS codigoConcepto,
              c.nombre_concepto AS nombreConcepto,
              c.tipo_concepto AS tipoConcepto,
              c.es_fijo AS esFijo,
              c.activo AS activo,
              c.tipo_calculo AS tipoCalculo,
              c.base_calculo AS baseCalculo,
              c.multiplicador AS multiplicador,
              c.afecta_ibc AS afectaIbc,
              c.afecta_base_cesantias AS afectaBaseCesantias,
              c.afecta_base_prima_legal AS afectaBasePrimaLegal,
              c.afecta_base_vacaciones AS afectaBaseVacaciones,
              c.afecta_base_prima_semestral AS afectaBasePrimaSemestral,
              c.afecta_base_arl AS afectaBaseArl,
              c.afecta_base_parafiscales AS afectaBaseParafiscales,
              c.smmlv_desde AS smmlvDesde,
              c.smmlv_hasta AS smmlvHasta
            FROM nomina.conceptos_nomina c
            WHERE c.codigo_concepto = :codigoConcepto
        """;

        var params = new MapSqlParameterSource()
                .addValue("codigoConcepto", codigoConcepto);

        List<ConceptoNominaDTO> rows = jdbc.query(sql, params, (rs, rowNum) -> mapRow(rs));

        return rows.stream().findFirst();
    }

    public void crear(ConceptoNominaDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO nomina.conceptos_nomina (
              codigo_concepto,
              nombre_concepto,
              tipo_concepto,
              es_fijo,
              activo,
              tipo_calculo,
              base_calculo,
              multiplicador,
              afecta_ibc,
              afecta_base_cesantias,
              afecta_base_prima_legal,
              afecta_base_vacaciones,
              afecta_base_prima_semestral,
              afecta_base_arl,
              afecta_base_parafiscales,
              smmlv_desde,
              smmlv_hasta,
              fk_seguridad_creacion,
              fk_seguridad_edicion
            )
            VALUES (
              :codigoConcepto,
              :nombreConcepto,
              :tipoConcepto,
              :esFijo,
              :activo,
              :tipoCalculo,
              :baseCalculo,
              :multiplicador,
              :afectaIbc,
              :afectaBaseCesantias,
              :afectaBasePrimaLegal,
              :afectaBaseVacaciones,
              :afectaBasePrimaSemestral,
              :afectaBaseArl,
              :afectaBaseParafiscales,
              :smmlvDesde,
              :smmlvHasta,
              :usr,
              :usr
            )
        """;

        jdbc.update(sql, params(dto, idUsuario).addValue("codigoConcepto", dto.getCodigoConcepto()));
    }

    public void actualizar(String codigoConcepto, ConceptoNominaDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE nomina.conceptos_nomina
            SET
              nombre_concepto = :nombreConcepto,
              tipo_concepto = :tipoConcepto,
              es_fijo = :esFijo,
              activo = :activo,
              tipo_calculo = :tipoCalculo,
              base_calculo = :baseCalculo,
              multiplicador = :multiplicador,
              afecta_ibc = :afectaIbc,
              afecta_base_cesantias = :afectaBaseCesantias,
              afecta_base_prima_legal = :afectaBasePrimaLegal,
              afecta_base_vacaciones = :afectaBaseVacaciones,
              afecta_base_prima_semestral = :afectaBasePrimaSemestral,
              afecta_base_arl = :afectaBaseArl,
              afecta_base_parafiscales = :afectaBaseParafiscales,
              smmlv_desde = :smmlvDesde,
              smmlv_hasta = :smmlvHasta,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE codigo_concepto = :codigoConcepto
        """;

        jdbc.update(sql, params(dto, idUsuario).addValue("codigoConcepto", codigoConcepto));
    }

    public void eliminar(String codigoConcepto) {

        String sql = """
            DELETE FROM nomina.conceptos_nomina
            WHERE codigo_concepto = :codigoConcepto
        """;

        jdbc.update(sql, new MapSqlParameterSource().addValue("codigoConcepto", codigoConcepto));
    }

    public java.util.Map<String, String> mapCodigoNombre(Boolean soloActivos) {

        String sql = """
            SELECT
              c.codigo_concepto AS codigo,
              c.nombre_concepto AS nombre
            FROM nomina.conceptos_nomina c
            WHERE (:soloActivos = FALSE OR c.activo = TRUE)
        """;

        var params = new MapSqlParameterSource()
                .addValue("soloActivos", soloActivos != null ? soloActivos : Boolean.TRUE);

        return jdbc.query(sql, params, rs -> {
            java.util.Map<String, String> map = new java.util.HashMap<>();
            while (rs.next()) {
                String codigo = rs.getString("codigo");
                String nombre = rs.getString("nombre");
                if (codigo != null) {
                    map.put(codigo, nombre);
                }
            }
            return map;
        });
    }

    private ConceptoNominaDTO mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        return ConceptoNominaDTO.builder()
                .codigoConcepto(rs.getString("codigoConcepto"))
                .nombreConcepto(rs.getString("nombreConcepto"))
                .tipoConcepto(rs.getString("tipoConcepto"))
                .esFijo((Boolean) rs.getObject("esFijo"))
                .activo((Boolean) rs.getObject("activo"))
                .tipoCalculo(rs.getString("tipoCalculo"))
                .baseCalculo(rs.getString("baseCalculo"))
                .multiplicador(rs.getBigDecimal("multiplicador"))
                .afectaIbc((Boolean) rs.getObject("afectaIbc"))
                .afectaBaseCesantias((Boolean) rs.getObject("afectaBaseCesantias"))
                .afectaBasePrimaLegal((Boolean) rs.getObject("afectaBasePrimaLegal"))
                .afectaBaseVacaciones((Boolean) rs.getObject("afectaBaseVacaciones"))
                .afectaBasePrimaSemestral((Boolean) rs.getObject("afectaBasePrimaSemestral"))
                .afectaBaseArl((Boolean) rs.getObject("afectaBaseArl"))
                .afectaBaseParafiscales((Boolean) rs.getObject("afectaBaseParafiscales"))
                .smmlvDesde(rs.getBigDecimal("smmlvDesde"))
                .smmlvHasta(rs.getBigDecimal("smmlvHasta"))
                .build();
    }

    private MapSqlParameterSource params(ConceptoNominaDTO dto, Integer idUsuario) {
        return new MapSqlParameterSource()
                .addValue("nombreConcepto", dto.getNombreConcepto())
                .addValue("tipoConcepto", dto.getTipoConcepto())
                .addValue("esFijo", dto.getEsFijo() != null ? dto.getEsFijo() : Boolean.FALSE)
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("tipoCalculo", dto.getTipoCalculo())
                .addValue("baseCalculo", dto.getBaseCalculo())
                .addValue("multiplicador", dto.getMultiplicador())
                .addValue("afectaIbc", dto.getAfectaIbc() != null ? dto.getAfectaIbc() : Boolean.FALSE)
                .addValue("afectaBaseCesantias", dto.getAfectaBaseCesantias() != null ? dto.getAfectaBaseCesantias() : Boolean.FALSE)
                .addValue("afectaBasePrimaLegal", dto.getAfectaBasePrimaLegal() != null ? dto.getAfectaBasePrimaLegal() : Boolean.FALSE)
                .addValue("afectaBaseVacaciones", dto.getAfectaBaseVacaciones() != null ? dto.getAfectaBaseVacaciones() : Boolean.FALSE)
                .addValue("afectaBasePrimaSemestral", dto.getAfectaBasePrimaSemestral() != null ? dto.getAfectaBasePrimaSemestral() : Boolean.FALSE)
                .addValue("afectaBaseArl", dto.getAfectaBaseArl() != null ? dto.getAfectaBaseArl() : Boolean.FALSE)
                .addValue("afectaBaseParafiscales", dto.getAfectaBaseParafiscales() != null ? dto.getAfectaBaseParafiscales() : Boolean.FALSE)
                .addValue("smmlvDesde", dto.getSmmlvDesde())
                .addValue("smmlvHasta", dto.getSmmlvHasta())
                .addValue("usr", idUsuario != null ? idUsuario : 1);
    }
}