package co.assip.erp.cartera.analisis.reciprocidadaportes;

import co.assip.erp.cartera.analisis.reciprocidadaportes.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReciprocidadAportesRepository {

    private final JdbcTemplate jdbc;

    public List<LocalDate> listarCortes() {
        return jdbc.query(
                "SELECT DISTINCT fecha_corte FROM cartera.vw_reciprocidad_aportes_resumen ORDER BY fecha_corte DESC",
                (r, n) -> r.getObject("fecha_corte", LocalDate.class)
        );
    }

    public Optional<ReciprocidadAportesResumenDTO> obtenerResumen(LocalDate fechaCorte) {
        return jdbc.query(
                "SELECT * FROM cartera.vw_reciprocidad_aportes_resumen WHERE fecha_corte = ?",
                this::mapResumen,
                fechaCorte
        ).stream().findFirst();
    }

    public List<ReciprocidadAportesPersonaDTO> listarPersonas(LocalDate fechaCorte) {
        return jdbc.query(
                "SELECT * FROM cartera.vw_reciprocidad_aportes_personas " +
                "WHERE fecha_corte = ? " +
                "ORDER BY porcentaje_reciprocidad ASC, saldo_cartera DESC, id_datos_personal",
                this::mapPersona,
                fechaCorte
        );
    }

    public List<ReciprocidadAportesDetalleDTO> listarDetalle(LocalDate fechaCorte) {
        return jdbc.query(
                "SELECT * FROM cartera.vw_reciprocidad_aportes_detalle " +
                "WHERE fecha_corte = ? " +
                "ORDER BY nombre_completo, documento, saldo_credito_fecha_corte DESC, id_cartera_credito",
                this::mapDetalle,
                fechaCorte
        );
    }

    public List<ReciprocidadAportesDetalleDTO> listarDetallePorPersona(LocalDate fechaCorte, Long idDatosPersonal) {
        return jdbc.query(
                "SELECT * FROM cartera.vw_reciprocidad_aportes_detalle " +
                "WHERE fecha_corte = ? AND id_datos_personal = ? " +
                "ORDER BY saldo_credito_fecha_corte DESC, id_cartera_credito",
                this::mapDetalle,
                fechaCorte,
                idDatosPersonal
        );
    }

    private ReciprocidadAportesResumenDTO mapResumen(ResultSet r, int n) throws SQLException {
        return new ReciprocidadAportesResumenDTO(
                integer(r, "id_cierre_cartera"),
                r.getObject("fecha_corte", LocalDate.class),
                longValue(r, "cantidad_personas"),
                r.getBigDecimal("cantidad_creditos"),
                r.getBigDecimal("saldo_cartera"),
                r.getBigDecimal("saldo_aportes"),
                r.getBigDecimal("porcentaje_reciprocidad_global"),
                r.getBigDecimal("apalancamiento_global"),
                r.getBigDecimal("exposicion_neta_aportes"),
                r.getBigDecimal("excedente_aportes"),
                longValue(r, "personas_sin_aportes"),
                longValue(r, "personas_aportes_menores_cartera"),
                longValue(r, "personas_aportes_cubren_cartera"),
                longValue(r, "personas_reciprocidad_menor_5"),
                longValue(r, "personas_reciprocidad_5_10"),
                longValue(r, "personas_reciprocidad_10_20"),
                longValue(r, "personas_reciprocidad_20_50"),
                longValue(r, "personas_reciprocidad_50_100"),
                longValue(r, "personas_reciprocidad_100_mas")
        );
    }

    private ReciprocidadAportesPersonaDTO mapPersona(ResultSet r, int n) throws SQLException {
        return new ReciprocidadAportesPersonaDTO(
                integer(r, "id_cierre_cartera"),
                r.getObject("fecha_corte", LocalDate.class),
                longValue(r, "id_datos_personal"),
                r.getString("tipo_documento"),
                r.getString("documento"),
                r.getString("nombre_completo"),
                longValue(r, "cantidad_creditos"),
                r.getBigDecimal("saldo_cartera"),
                r.getBigDecimal("saldo_aportes"),
                r.getBigDecimal("aportes_distribuidos"),
                r.getBigDecimal("diferencia_distribucion_aportes"),
                r.getBigDecimal("porcentaje_reciprocidad"),
                r.getBigDecimal("apalancamiento"),
                r.getBigDecimal("exposicion_neta_aportes"),
                r.getBigDecimal("excedente_aportes"),
                r.getObject("aportes_cubren_cartera", Boolean.class),
                r.getString("rango_reciprocidad")
        );
    }

    private ReciprocidadAportesDetalleDTO mapDetalle(ResultSet r, int n) throws SQLException {
        return new ReciprocidadAportesDetalleDTO(
                integer(r, "id_cierre_cartera"),
                r.getObject("fecha_corte", LocalDate.class),
                longValue(r, "id_datos_personal"),
                r.getString("tipo_documento"),
                r.getString("documento"),
                r.getString("nombre_completo"),
                longValue(r, "cantidad_creditos_persona"),
                r.getBigDecimal("saldo_cartera_persona"),
                r.getBigDecimal("saldo_aportes_persona"),
                r.getBigDecimal("porcentaje_reciprocidad_persona"),
                r.getBigDecimal("apalancamiento_persona"),
                integer(r, "id_cierre_cartera_credito"),
                longValue(r, "id_cartera_credito"),
                r.getString("pagare_cartera"),
                integer(r, "id_agencia"),
                longValue(r, "id_linea_credito"),
                r.getString("codigo_linea_credito"),
                r.getString("nombre_linea_credito"),
                r.getString("codigo_clasificacion_credito"),
                r.getString("descripcion_clasificacion_credito"),
                r.getString("codigo_destino_economico"),
                r.getString("descripcion_destino_economico"),
                r.getObject("fecha_desembolso", LocalDate.class),
                r.getBigDecimal("valor_inicial_credito"),
                r.getBigDecimal("valor_desembolsado"),
                r.getBigDecimal("saldo_credito_fecha_corte"),
                r.getBigDecimal("porcentaje_aportes_credito"),
                r.getBigDecimal("valor_aportes_credito"),
                r.getBigDecimal("exposicion_neta_credito"),
                r.getBigDecimal("excedente_aportes_credito"),
                integer(r, "dias_mora"),
                r.getString("edad_contable_resultado"),
                r.getBigDecimal("deterioro_capital"),
                r.getBigDecimal("deterioro_intereses"),
                r.getBigDecimal("deterioro_otros"),
                r.getBigDecimal("deterioro_total")
        );
    }

    private Long longValue(ResultSet r, String column) throws SQLException {
        long value = r.getLong(column);
        return r.wasNull() ? null : value;
    }

    private Integer integer(ResultSet r, String column) throws SQLException {
        int value = r.getInt(column);
        return r.wasNull() ? null : value;
    }
}
