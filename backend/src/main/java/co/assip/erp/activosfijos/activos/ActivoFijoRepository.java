package co.assip.erp.activosfijos.activos;

import co.assip.erp.activosfijos.activos.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ActivoFijoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // 1. LISTADO
    // =========================================================

    public List<ActivoFijoListDTO> listar() {

        String sql = """
            SELECT
                af.id_activo_fijo,
                af.placa_activo,
                af.nombre_activo,
                af.fecha_ingreso,
                af.meses_depreciacion,
                af.valor_adquisicion,
                af.valor_mensual,
                af.id_agencia,
                ag.nombre_agencia,
                af.id_estado_activo,
                ea.nombre_estado
            FROM activos_fijos.activos_fijos af
            JOIN general.datos_agencias ag
                ON ag.id_agencia = af.id_agencia
            JOIN activos_fijos.estados_activo ea
                ON ea.id_estado_activo = af.id_estado_activo
            ORDER BY af.id_activo_fijo DESC
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            ActivoFijoListDTO dto = new ActivoFijoListDTO();
            dto.setIdActivoFijo(rs.getLong("id_activo_fijo"));
            dto.setPlacaActivo(rs.getString("placa_activo"));
            dto.setNombreActivo(rs.getString("nombre_activo"));
            dto.setFechaIngreso(rs.getDate("fecha_ingreso").toLocalDate());
            dto.setMesesDepreciacion(rs.getInt("meses_depreciacion"));
            dto.setValorAdquisicion(rs.getBigDecimal("valor_adquisicion"));
            dto.setValorMensual(rs.getBigDecimal("valor_mensual"));
            dto.setIdAgencia(rs.getInt("id_agencia"));
            dto.setNombreAgencia(rs.getString("nombre_agencia"));
            dto.setIdEstadoActivo(rs.getInt("id_estado_activo"));
            dto.setNombreEstadoActivo(rs.getString("nombre_estado"));
            return dto;
        });
    }

    // =========================================================
    // 2. FORMULARIO (detalle completo)
    // =========================================================

    public ActivoFijoFormDTO obtenerFormulario(Long idActivoFijo) {

        String sql = """
            SELECT
               af.id_activo_fijo,
               af.placa_activo,
               af.nombre_activo,
               af.fecha_ingreso,
               af.fecha_garantia,
               af.fecha_baja,
               af.id_forma_depreciacion,
               af.meses_depreciacion,
               af.valor_adquisicion,
               af.valor_mensual,
               af.id_estado_activo,
               af.id_tipo_adquisicion,
               af.id_agencia,
               af.id_localizacion,
               af.id_bloque,
               af.id_datos_personal_responsable,
               af.id_datos_personal_proveedor,
               af.id_catalogo_cuenta_activo,
               af.id_catalogo_cuenta_depreciacion,
               af.id_catalogo_cuenta_gasto,
               af.id_catalogo_cuenta_control
              FROM activos_fijos.activos_fijos af
              WHERE af.id_activo_fijo = :id
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", idActivoFijo);

        return jdbc.queryForObject(sql, params, (rs, rowNum) -> {
            ActivoFijoFormDTO dto = new ActivoFijoFormDTO();

            dto.setIdActivoFijo(rs.getLong("id_activo_fijo"));
            dto.setPlacaActivo(rs.getString("placa_activo"));
            dto.setNombreActivo(rs.getString("nombre_activo"));
            dto.setFechaIngreso(rs.getDate("fecha_ingreso").toLocalDate());
            dto.setFechaGarantia(
                    rs.getDate("fecha_garantia") != null
                            ? rs.getDate("fecha_garantia").toLocalDate()
                            : null
            );

            dto.setFechaBaja(
                    rs.getDate("fecha_baja") != null
                            ? rs.getDate("fecha_baja").toLocalDate()
                            : null
            );

            dto.setIdFormaDepreciacion(rs.getInt("id_forma_depreciacion"));
            dto.setMesesDepreciacion(rs.getInt("meses_depreciacion"));
            dto.setValorAdquisicion(rs.getBigDecimal("valor_adquisicion"));
            dto.setValorMensual(rs.getBigDecimal("valor_mensual"));

            dto.setIdEstadoActivo(rs.getInt("id_estado_activo"));
            dto.setIdTipoAdquisicion(rs.getInt("id_tipo_adquisicion"));
            dto.setIdAgencia(rs.getInt("id_agencia"));
            dto.setIdUbicacion(rs.getLong("id_localizacion"));
            dto.setIdBloque(rs.getObject("id_bloque", Integer.class));

            dto.setIdDatosPersonalResponsable(
                    rs.getObject("id_datos_personal_responsable", Long.class));
            dto.setIdDatosPersonalProveedor(
                    rs.getObject("id_datos_personal_proveedor", Long.class));

            dto.setIdCatalogoCuentaActivo(
                    rs.getObject("id_catalogo_cuenta_activo", Long.class));
            dto.setIdCatalogoCuentaDepreciacion(
                    rs.getObject("id_catalogo_cuenta_depreciacion", Long.class));
            dto.setIdCatalogoCuentaGasto(
                    rs.getObject("id_catalogo_cuenta_gasto", Long.class));
            dto.setIdCatalogoCuentaControl(
                    rs.getObject("id_catalogo_cuenta_control", Long.class));
            return dto;
        });
    }

    // =========================================================
    // 3. INSERT
    // =========================================================

    public void insertar(ActivoFijoSaveDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO activos_fijos.activos_fijos (
                placa_activo,
                nombre_activo,
                fecha_ingreso,
                fecha_garantia,
                fecha_baja,
                id_forma_depreciacion,
                meses_depreciacion,
                valor_adquisicion,
                valor_mensual,
                id_estado_activo,
                id_tipo_adquisicion,
                id_agencia,
                id_localizacion,
                id_bloque,
                id_datos_personal_responsable,
                id_datos_personal_proveedor,
                id_catalogo_cuenta_activo,
                id_catalogo_cuenta_depreciacion,
                id_catalogo_cuenta_gasto,
                id_catalogo_cuenta_control,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            ) VALUES (
                :placa,
                :nombre,
                :fechaIngreso,
                :fechaGarantia,
                :fechaBaja,
                :formaDepreciacion,
                :meses,
                :valorAdq,
                :valorMensual,
                :estado,
                :tipoAdq,
                :agencia,
                :ubicacion,
                :bloque,
                :responsable,
                :proveedor,
                :ctaActivo,
                :ctaDep,
                :ctaGasto,
                :ctaControl,
                :usuario,
                :usuario
            )
        """;

        jdbc.update(sql, paramsSave(dto, idUsuario));
    }

    // =========================================================
    // 4. UPDATE
    // =========================================================

    public void actualizar(Long id, ActivoFijoSaveDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE activos_fijos.activos_fijos SET
                placa_activo = :placa,
                nombre_activo = :nombre,
                fecha_ingreso = :fechaIngreso,
                fecha_garantia = :fechaGarantia,
                fecha_baja = :fechaBaja,
                id_forma_depreciacion = :formaDepreciacion,
                meses_depreciacion = :meses,
                valor_adquisicion = :valorAdq,
                valor_mensual = :valorMensual,
                id_estado_activo = :estado,
                id_tipo_adquisicion = :tipoAdq,
                id_agencia = :agencia,
                id_localizacion = :ubicacion,
                id_bloque = :bloque,
                id_datos_personal_responsable = :responsable,
                id_datos_personal_proveedor = :proveedor,
                id_catalogo_cuenta_activo = :ctaActivo,
                id_catalogo_cuenta_depreciacion = :ctaDep,
                id_catalogo_cuenta_gasto = :ctaGasto,
                id_catalogo_cuenta_control = :ctaControl,
                fk_seguridad_edicion = :usuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_activo_fijo = :id
        """;

        MapSqlParameterSource params = paramsSave(dto, idUsuario);
        params.addValue("id", id);

        jdbc.update(sql, params);
    }

    // =========================================================
    // 5. DELETE
    // =========================================================

    public void eliminar(Long id) {

        String sql = """
            DELETE FROM activos_fijos.activos_fijos
            WHERE id_activo_fijo = :id
        """;

        jdbc.update(sql, new MapSqlParameterSource("id", id));
    }

    // =========================================================
    // Util
    // =========================================================

    private MapSqlParameterSource paramsSave(ActivoFijoSaveDTO dto, Integer usuario) {

        return new MapSqlParameterSource()
                .addValue("placa", dto.getPlacaActivo())
                .addValue("nombre", dto.getNombreActivo())
                .addValue("fechaIngreso", dto.getFechaIngreso())
                .addValue("fechaGarantia", dto.getFechaGarantia())
                .addValue("fechaBaja", dto.getFechaBaja())
                .addValue("formaDepreciacion", dto.getIdFormaDepreciacion())
                .addValue("meses", dto.getMesesDepreciacion())
                .addValue("valorAdq", dto.getValorAdquisicion())
                .addValue("valorMensual", dto.getValorMensual())
                .addValue("estado", dto.getIdEstadoActivo())
                .addValue("tipoAdq", dto.getIdTipoAdquisicion())
                .addValue("agencia", dto.getIdAgencia())
                .addValue("ubicacion", dto.getIdUbicacion())
                .addValue("bloque", dto.getIdBloque())
                .addValue("responsable", dto.getIdDatosPersonalResponsable())
                .addValue("proveedor", dto.getIdDatosPersonalProveedor())
                .addValue("ctaActivo", dto.getIdCatalogoCuentaActivo())
                .addValue("ctaDep", dto.getIdCatalogoCuentaDepreciacion())
                .addValue("ctaGasto", dto.getIdCatalogoCuentaGasto())
                .addValue("ctaControl", dto.getIdCatalogoCuentaControl())
                .addValue("usuario", usuario);
    }
}
