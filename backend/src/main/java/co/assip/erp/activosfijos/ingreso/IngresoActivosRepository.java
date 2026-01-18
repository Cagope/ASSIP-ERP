package co.assip.erp.activosfijos.ingreso;

import co.assip.erp.seguridad.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class IngresoActivosRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // VALIDACIONES CONTABLES
    // =========================================================

    public boolean existeComprobante(Long idAgencia, String tipo, String numero10) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM contabilidad.auxiliares_contables a
                WHERE a.id_agencia = :idAgencia
                  AND a.tipo_comprobante = :tipo
                  AND a.numero_comprobante = :numero
            )
        """;

        return Boolean.TRUE.equals(
                jdbc.queryForObject(sql, Map.of(
                        "idAgencia", idAgencia,
                        "tipo", tipo,
                        "numero", numero10
                ), Boolean.class)
        );
    }

    public void validarAnoAbierto(Long idAgencia, int ano) {
        String sql = """
            SELECT COUNT(1)
            FROM contabilidad.anos_cerrados ac
            WHERE ac.id_agencia = :idAgencia
              AND ac.ano = :ano
              AND ac.estado = 'C'
        """;

        Integer n = jdbc.queryForObject(sql, Map.of(
                "idAgencia", idAgencia,
                "ano", ano
        ), Integer.class);

        if (n != null && n > 0) {
            throw new IllegalStateException("El año contable está cerrado para la agencia.");
        }
    }

    public void validarMesAbierto(Long idAgencia, int ano, int mes) {
        String sql = """
            SELECT COUNT(1)
            FROM contabilidad.meses_cerrados mc
            WHERE mc.id_agencia = :idAgencia
              AND mc.ano = :ano
              AND mc.mes = :mes
              AND mc.estado = 'C'
        """;

        Integer n = jdbc.queryForObject(sql, Map.of(
                "idAgencia", idAgencia,
                "ano", ano,
                "mes", mes
        ), Integer.class);

        if (n != null && n > 0) {
            throw new IllegalStateException("El mes contable está cerrado para la agencia.");
        }
    }

    // =========================================================
    // CONSECUTIVO COMPROBANTES
    // =========================================================

    public Integer obtenerYActualizarConsecutivo(Long idAgencia, String tipoComprobante) {

        String sqlSelect = """
            SELECT csc_comprobante
            FROM contabilidad.tipos_comprobantes
            WHERE tipo_comprobante = :tipo
              AND (id_agencia = :idAgencia OR id_agencia IS NULL)
            FOR UPDATE
        """;

        Integer actual = jdbc.queryForObject(sqlSelect, Map.of(
                "tipo", tipoComprobante,
                "idAgencia", idAgencia
        ), Integer.class);

        if (actual == null) {
            throw new IllegalStateException("No existe tipo de comprobante: " + tipoComprobante);
        }

        String sqlUpdate = """
            UPDATE contabilidad.tipos_comprobantes
            SET csc_comprobante = csc_comprobante + 1
            WHERE tipo_comprobante = :tipo
              AND (id_agencia = :idAgencia OR id_agencia IS NULL)
        """;

        jdbc.update(sqlUpdate, Map.of(
                "tipo", tipoComprobante,
                "idAgencia", idAgencia
        ));

        return actual + 1;
    }

    // =========================================================
    // HEADER CONTABLE
    // =========================================================

    public void insertarConceptoContable(
            String tipoComprobante,
            String numeroComprobante,
            String concepto
    ) {
        String sql = """
        INSERT INTO contabilidad.conceptos_contables (
            tipo_comprobante,
            numero_comprobante,
            concepto_comprobante,
            fk_seguridad_creacion,
            fk_seguridad_edicion
        ) VALUES (
            :tipo,
            :numero,
            :concepto,
            :idUsuario,
            :idUsuario
        )
    """;

        Integer idUsuario = SecurityUtils.getIdUsuario();
        if (idUsuario == null) {
            throw new IllegalStateException("No hay usuario autenticado.");
        }

        jdbc.update(sql, Map.of(
                "tipo", tipoComprobante,
                "numero", numeroComprobante,
                "concepto", concepto,
                "idUsuario", idUsuario
        ));
    }

    // =========================================================
    // VALIDACIONES CATÁLOGOS / AGENCIA
    // =========================================================

    public boolean existePlacaEnAgencia(Long idAgencia, String placa) {
        String sql = """
            SELECT EXISTS(
              SELECT 1
              FROM activos_fijos.activos_fijos a
              WHERE a.id_agencia = :idAgencia
                AND a.placa_activo = :placa
            )
        """;
        return Boolean.TRUE.equals(
                jdbc.queryForObject(sql, Map.of(
                        "idAgencia", idAgencia,
                        "placa", placa
                ), Boolean.class)
        );
    }

    public void validarProveedorActivoEnAgencia(Long idAgencia, Long idProveedor) {
        String sql = """
            SELECT COUNT(1)
            FROM hoja_vida.datos_personales dp
            WHERE dp.id_datos_personal = :id
        """;
        Integer n = jdbc.queryForObject(sql, Map.of("id", idProveedor), Integer.class);
        if (n == null || n <= 0) {
            throw new IllegalArgumentException("Proveedor no existe: " + idProveedor);
        }
    }

    public void validarCuentaActivaEnAgencia(Long idAgencia, Long idCuenta) {

        String sql = """
        SELECT COUNT(1)
        FROM contabilidad.catalogo_cuentas cc
        WHERE cc.id_catalogo_cuenta = :idCuenta
          AND cc.cuenta_operable = true
          AND (cc.id_agencia = :idAgencia OR cc.id_agencia IS NULL)
    """;

        Integer n = jdbc.queryForObject(sql, Map.of(
                "idCuenta", idCuenta,
                "idAgencia", idAgencia
        ), Integer.class);

        if (n == null || n <= 0) {
            throw new IllegalArgumentException(
                    "Cuenta no válida para agencia. idAgencia=" + idAgencia + ", idCuenta=" + idCuenta
            );
        }
    }

    public void validarUbicacionEnAgencia(Long idAgencia, Long idUbicacion) {

        if (idUbicacion == null) {
            throw new IllegalArgumentException("La localización es obligatoria.");
        }

        String sql = """
        SELECT COUNT(1)
        FROM activos_fijos.localizaciones l
        WHERE l.id_localizacion = :idUbicacion
          AND l.id_agencia = :idAgencia
    """;

        Integer n = jdbc.queryForObject(sql, Map.of(
                "idUbicacion", idUbicacion,
                "idAgencia", idAgencia
        ), Integer.class);

        if (n == null || n <= 0) {
            throw new IllegalArgumentException(
                    "Localización no válida para la agencia: " + idUbicacion
            );
        }
    }

    public void validarBloqueActivo(Long idBloque) {
        validarExistencia("activos_fijos.bloques", "id_bloque", idBloque, "Bloque");
    }

    public void validarFormaDepreciacionActiva(Long idForma) {
        validarExistencia("activos_fijos.formas_depreciacion", "id_forma_depreciacion", idForma, "Forma depreciación");
    }

    public void validarTipoAdquisicionActivo(Long idTipo) {
        validarExistencia("activos_fijos.tipos_adquisicion", "id_tipo_adquisicion", idTipo, "Tipo adquisición");
    }

    public void validarEstadoActivo(Long idEstado) {
        validarExistencia("activos_fijos.estados_activo", "id_estado_activo", idEstado, "Estado activo");
    }

    private void validarExistencia(String tabla, String columna, Long id, String nombre) {
        String sql = "SELECT COUNT(1) FROM " + tabla + " WHERE " + columna + " = :id";
        Integer n = jdbc.queryForObject(sql, Map.of("id", id), Integer.class);
        if (n == null || n <= 0) {
            throw new IllegalArgumentException(nombre + " no existe: " + id);
        }
    }

    // =========================================================
    // AUXILIARES CONTABLES
    // =========================================================

    public void insertarAuxiliar(
            Long idAgencia,
            Long idCuenta,
            LocalDate fecha,
            String tipoComprobante,
            String numeroComprobante,
            BigDecimal debito,
            BigDecimal credito,
            BigDecimal base,
            String detalle
    ) {

        Integer idUsuario = SecurityUtils.getIdUsuario();
        if (idUsuario == null) {
            throw new IllegalStateException("No hay usuario autenticado.");
        }

        String sql = """
        INSERT INTO contabilidad.auxiliares_contables (
            id_catalogo_cuenta,
            id_agencia,
            fecha_auxiliar,
            tipo_comprobante,
            numero_comprobante,
            detalle_movimiento,
            estado_movimiento,
            valor_debito,
            valor_credito,
            valor_base,
            fk_seguridad_creacion,
            fk_seguridad_edicion
        ) VALUES (
            :idCuenta,
            :idAgencia,
            :fecha,
            :tipo,
            :numero,
            :detalle,
            'A',
            :debito,
            :credito,
            :base,
            :idUsuario,
            :idUsuario
        )
    """;

        jdbc.update(sql, Map.of(
                "idCuenta", idCuenta,
                "idAgencia", idAgencia,
                "fecha", Date.valueOf(fecha),
                "tipo", tipoComprobante,
                "numero", numeroComprobante,
                "detalle", detalle,
                "debito", debito,
                "credito", credito,
                "base", base,
                "idUsuario", idUsuario
        ));
    }

    // =========================================================
    // ACTIVOS FIJOS
    // =========================================================

    public Long insertarActivoFijo(
            Long idAgencia,
            Integer idUsuario,
            String placa,
            String nombre,
            LocalDate fechaIngreso,
            LocalDate fechaGarantia,
            Long idFormaDepreciacion,
            Integer mesesDepreciacion,
            BigDecimal valorAdquisicion,
            BigDecimal valorMensual,
            Long idEstadoActivo,
            Long idTipoAdquisicion,
            Long idUbicacion,
            Long idBloque,
            Long idResponsable,
            Long idProveedor,
            Long idCuentaActivo,
            Long idCuentaDepreciacion,
            Long idCuentaGasto,
            Long idCuentaControl
    ) {

        String sql = """
        INSERT INTO activos_fijos.activos_fijos(
          placa_activo, nombre_activo,
          fecha_ingreso, fecha_garantia,
          id_forma_depreciacion, meses_depreciacion,
          valor_adquisicion, valor_mensual,
          id_estado_activo, id_tipo_adquisicion,
          id_agencia, id_localizacion, id_bloque,
          id_datos_personal_responsable,
          id_datos_personal_proveedor,
          id_catalogo_cuenta_activo,
          id_catalogo_cuenta_depreciacion,
          id_catalogo_cuenta_gasto,
          id_catalogo_cuenta_control,
          fk_seguridad_creacion,
          fk_seguridad_edicion
        ) VALUES (
          :placa, :nombre,
          :fechaIngreso, :fechaGarantia,
          :idFormaDepreciacion, :mesesDepreciacion,
          :valorAdquisicion, :valorMensual,
          :idEstadoActivo, :idTipoAdquisicion,
          :idAgencia, :idLocalizacion, :idBloque,
          :idResponsable,
          :idProveedor,
          :idCuentaActivo,
          :idCuentaDepreciacion,
          :idCuentaGasto,
          :idCuentaControl,
          :usuario,
          :usuario
        )
        RETURNING id_activo_fijo
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("placa", placa)
                        .addValue("nombre", nombre)
                        .addValue("fechaIngreso", Date.valueOf(fechaIngreso))
                        .addValue("fechaGarantia", fechaGarantia != null ? Date.valueOf(fechaGarantia) : null)
                        .addValue("idFormaDepreciacion", idFormaDepreciacion)
                        .addValue("mesesDepreciacion", mesesDepreciacion)
                        .addValue("valorAdquisicion", valorAdquisicion)
                        .addValue("valorMensual", valorMensual)
                        .addValue("idEstadoActivo", idEstadoActivo)
                        .addValue("idTipoAdquisicion", idTipoAdquisicion)
                        .addValue("idAgencia", idAgencia)
                        .addValue("idLocalizacion", idUbicacion)
                        .addValue("idBloque", idBloque)
                        .addValue("idResponsable", idResponsable)
                        .addValue("idProveedor", idProveedor)
                        .addValue("idCuentaActivo", idCuentaActivo)
                        .addValue("idCuentaDepreciacion", idCuentaDepreciacion)
                        .addValue("idCuentaGasto", idCuentaGasto)
                        .addValue("idCuentaControl", idCuentaControl)
                        .addValue("usuario", idUsuario),   // 🔥 SOLUCIÓN
                Long.class
        );
    }


    public void vincularActivoAComprobante(Long idActivo, Long idComprobante) {
        // NO-OP (modelo actual no define relación directa)
    }
}
