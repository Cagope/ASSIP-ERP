package co.assip.erp.sarlaft.informes;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import co.assip.erp.sarlaft.informes.dto.InformePersonasDesactualizadasDTO;
import co.assip.erp.sarlaft.informes.dto.InformeMovimientosInusualesDTO;
import co.assip.erp.sarlaft.informes.dto.InformeRegla002DTO;
import co.assip.erp.sarlaft.informes.dto.InformeRegla003DTO;


@Service
public class InformeSarlaftService {

    private final NamedParameterJdbcTemplate jdbc;

    public InformeSarlaftService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Object resolverInforme(InformeSarlaftRequest request) {

        InformeSarlaftTipo tipo = request.getTipo();
        Map<String, Object> filtros = request.getFiltros();

        switch (tipo) {
            case PERSONAS_DESACTUALIZADAS:
                return informePersonasDesactualizadas(filtros != null ? filtros : Map.of());

            case DATOS_DEMOGRAFICOS:
                return informeDatosDemograficos(filtros != null ? filtros : Map.of());

            case MOVIMIENTOS_INUSUALES:   // 🟩 ESTE DEBE ESTAR
                return informeMovimientosInusuales(filtros != null ? filtros : Map.of());

            case REGLA_002_DOCUMENTO_EDAD:
                return informeRegla002(filtros != null ? filtros : Map.of());

            case REGLA_003_FORMA_PROHIBIDA:
                return informeRegla003(filtros != null ? filtros : Map.of());

            default:
                throw new IllegalArgumentException("Informe SARLAFT no soportado: " + tipo);

        }

    }

    // =====================================================
    //  INFORME: PERSONAS DESACTUALIZADAS
    // =====================================================
    public List<InformePersonasDesactualizadasDTO> informePersonasDesactualizadas(Map<String, Object> filtros) {

        String sql = """
            SELECT
                hv.id_datos_personal,
                hv.documento,
                (hv.nombres || ' ' || hv.primer_apellido || ' ' ||
                 COALESCE(hv.segundo_apellido, '')) AS nombre_completo,

                hv.fecha_actualizacion,
                hv.telefono,
                hv.celular_uno,
                hv.celular_dos,
                hv.correo_personal,

                hv.nombre_zona,
                hv.nombre_sub_zona,

                hv.recibe_llamadas,
                hv.recibe_msm,
                hv.recibe_emails,
                hv.recibe_cartas,
                hv.recibe_redes_sociales,

                COALESCE(ah.saldo_actual_cuenta, 0) AS saldo_aportes,
                ah.fecha_apertura_cuenta

            FROM reporting.vw_hoja_vida_general_total_reciente hv

            LEFT JOIN (
                SELECT
                    v.id_datos_personal,
                    v.saldo_actual_cuenta,
                    v.fecha_apertura_cuenta,
                    v.codigo_forma
                FROM depositos.vw_depositos_cuentas_ahorro_total v
                WHERE v.codigo_forma = '01'       -- 🔹 Aportes sociales
                  AND v.saldo_actual_cuenta > 0   -- 🔹 Solo con saldo
            ) ah
                ON ah.id_datos_personal = hv.id_datos_personal

            WHERE 1 = 1
        """;

        Map<String, Object> params = new HashMap<>();

        // 🔹 Filtros de fecha (sobre fecha_apertura_cuenta de aportes)
        Object desdeObj = filtros.get("desde");
        if (desdeObj != null && !desdeObj.toString().isBlank()) {
            sql += " AND ah.fecha_apertura_cuenta >= TO_DATE(:desde, 'YYYY-MM-DD') ";
            params.put("desde", desdeObj.toString());
        }

        Object hastaObj = filtros.get("hasta");
        if (hastaObj != null && !hastaObj.toString().isBlank()) {
            sql += " AND ah.fecha_apertura_cuenta <= TO_DATE(:hasta, 'YYYY-MM-DD') ";
            params.put("hasta", hastaObj.toString());
        }

        // Ejecutar SQL
        List<Map<String, Object>> rows = jdbc.queryForList(sql, params);

        List<InformePersonasDesactualizadasDTO> lista = new ArrayList<>();

        for (Map<String, Object> row : rows) {

            InformePersonasDesactualizadasDTO dto = new InformePersonasDesactualizadasDTO();

            dto.idDatosPersonal   = row.get("id_datos_personal") != null
                    ? ((Number) row.get("id_datos_personal")).longValue()
                    : null;

            dto.documento         = (String) row.get("documento");
            dto.nombreCompleto    = (String) row.get("nombre_completo");
            dto.fechaActualizacion = row.get("fecha_actualizacion") != null
                    ? row.get("fecha_actualizacion").toString()
                    : null;

            // 🔹 Cálculo días desactualizado
            if (dto.fechaActualizacion != null) {
                try {
                    LocalDate fecha = LocalDate.parse(dto.fechaActualizacion);
                    dto.diasDesactualizado =
                            (int) ChronoUnit.DAYS.between(fecha, LocalDate.now());
                } catch (Exception e) {
                    dto.diasDesactualizado = 9999;
                }
            } else {
                dto.diasDesactualizado = 9999;
            }

            // 🔹 Saldo aportes
            dto.saldoAportes = row.get("saldo_aportes") != null
                    ? ((Number) row.get("saldo_aportes")).doubleValue()
                    : 0.0;

            // 🔹 Estado según días
            dto.estado = dto.diasDesactualizado > 365
                    ? "DESACTUALIZADO"
                    : "ACTUALIZADO";

            // 🔹 Contacto
            dto.telefono        = (String) row.get("telefono");
            dto.celularUno      = (String) row.get("celular_uno");
            dto.celularDos      = (String) row.get("celular_dos");
            dto.correoPersonal  = (String) row.get("correo_personal");

            // 🔹 Zona
            dto.nombreZona      = (String) row.get("nombre_zona");
            dto.nombreSubZona   = (String) row.get("nombre_sub_zona");

            // 🔹 Permisos especiales
            dto.recibeLlamadas       = (Boolean) row.get("recibe_llamadas");
            dto.recibeMsm            = (Boolean) row.get("recibe_msm");
            dto.recibeEmails         = (Boolean) row.get("recibe_emails");
            dto.recibeCartas         = (Boolean) row.get("recibe_cartas");
            dto.recibeRedesSociales  = (Boolean) row.get("recibe_redes_sociales");

            // 🔹 Fecha apertura aportes
            dto.fechaAperturaCuenta = row.get("fecha_apertura_cuenta") != null
                    ? row.get("fecha_apertura_cuenta").toString()
                    : null;

            lista.add(dto);
        }

        // 🔹 Filtro por estado (opcional) a nivel de servicio
        Object estadoObj = filtros.get("estado");
        if (estadoObj != null && !estadoObj.toString().isBlank()) {
            String estadoFiltro = estadoObj.toString().toUpperCase(Locale.ROOT).trim();
            lista = lista.stream()
                    .filter(d -> d.estado != null &&
                            d.estado.toUpperCase(Locale.ROOT).equals(estadoFiltro))
                    .toList();
        }

        return lista;
    }

    // =====================================================
    //  INFORME: DATOS DEMOGRÁFICOS DE ASOCIADOS
    // =====================================================
    public List<Map<String, Object>> informeDatosDemograficos(Map<String, Object> filtros) {

        // Fecha de corte (si no viene, hoy)
        String fechaCorte = Optional.ofNullable(filtros.get("fecha"))
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .orElse(LocalDate.now().toString());

        Map<String, Object> params = new HashMap<>();
        params.put("fecha", fechaCorte);

        String sql = """
        SELECT
            hv.tipo_documento,
            hv.documento,
            hv.nombres,
            hv.primer_apellido,
            hv.segundo_apellido,

            v.fecha_apertura_cuenta     AS fecha_apertura,

            hv.direccion_residencia     AS direccion,
            hv.barrio,
            hv.telefono,
            hv.celular_uno,
            hv.celular_dos,
            hv.correo_personal          AS email,
            hv.ciudad_residencia        AS municipio,

            hv.nombre_genero,
            hv.nombre_escolaridad,
            hv.estrato_social,

            (
              COALESCE(hv.valor_salario,0) +
              COALESCE(hv.ingresos_arriendo,0) +
              COALESCE(hv.ingresos_comisiones,0) +
              COALESCE(hv.otros_ingresos,0)
            ) AS nivel_ingresos,

            hv.fecha_nacimiento,
            hv.nombre_estado_civil,

            cony.nombre_datos_familiar      AS nombre_conyuge,
            cony.documento_datos_familiar   AS cedula_conyuge,

            hv.cabeza_familia,
            hv.nombre_ocupacion,
            hv.nombre_sector_economico,
            hv.numero_hijos,

            hv.valor_salario,
            (
              COALESCE(hv.valor_salario,0) +
              COALESCE(hv.ingresos_arriendo,0) +
              COALESCE(hv.ingresos_comisiones,0) +
              COALESCE(hv.otros_ingresos,0)
            ) AS valor_ingresos,

            hv.otros_ingresos,

            (
              COALESCE(hv.egresos_familiares,0) +
              COALESCE(hv.egresos_arriendo,0) +
              COALESCE(hv.egresos_credito,0) +
              COALESCE(hv.otros_egresos,0)
            ) AS egresos,

            hv.total_activos,
            hv.total_pasivos,
            (COALESCE(hv.total_activos,0) - COALESCE(hv.total_pasivos,0)) AS valor_patrimonio,

            hv.nombre_tipo_vivienda,

            rp.nombre_referencia_personal   AS nombre_arrendador,
            rp.celular_referencia_personal  AS telefono_arrendador,

            hv.barrio                       AS ubicacion_vivienda,
            hv.celular_uno                 AS celular,
            hv.fecha_documento,
            hv.asociado_peps               AS peps,
            hv.fecha_inicial_peps          AS fecha_peps,
            hv.observaciones_peps          AS comentario_peps,

            hv.familia_peps,
            sar.cedula_familia_peps,
            sar.nombre_familia_peps,

            hv.recibe_llamadas,
            hv.recibe_msm,
            hv.recibe_redes_sociales,
            hv.recibe_emails,
            hv.recibe_cartas,

            hv.nombre_zona,
            hv.nombre_sub_zona,

            EXTRACT(YEAR FROM age(TO_DATE(:fecha,'YYYY-MM-DD'), hv.fecha_nacimiento))::int AS edad,
            EXTRACT(YEAR FROM age(TO_DATE(:fecha,'YYYY-MM-DD'), v.fecha_apertura_cuenta))::int AS antiguedad,

            v.nombre_agencia               AS agencia,
            COALESCE(v.saldo_actual_cuenta,0) AS saldo_aportes

        FROM reporting.vw_hoja_vida_general_total_reciente hv

        LEFT JOIN depositos.vw_depositos_cuentas_ahorro_total v
          ON v.id_datos_personal = hv.id_datos_personal
         AND v.codigo_forma = '01'
         AND v.saldo_actual_cuenta > 0

        LEFT JOIN hoja_vida.datos_familiares cony
          ON cony.id_datos_personal = hv.id_datos_personal
         AND cony.codigo_parentesco = '2'

        LEFT JOIN hoja_vida.referencias_personales rp
          ON rp.id_datos_personal = hv.id_datos_personal

        LEFT JOIN reporting.vw_hoja_vida_sarlaft_total sar
          ON sar.id_datos_personal = hv.id_datos_personal

        WHERE v.fecha_apertura_cuenta <= TO_DATE(:fecha, 'YYYY-MM-DD')
    """;

        return jdbc.queryForList(sql, params);
    }

    // =====================================================
    //  INFORME: MOVIMIENTOS INUSUALES
    // =====================================================
    public List<InformeMovimientosInusualesDTO> informeMovimientosInusuales(Map<String, Object> filtros) {

        // Validar fechas
        String inicio = Optional.ofNullable(filtros.get("fecha_inicio"))
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("fecha_inicio es obligatoria"));

        String fin = Optional.ofNullable(filtros.get("fecha_fin"))
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("fecha_fin es obligatoria"));

        Map<String, Object> params = new HashMap<>();
        params.put("inicio", inicio);
        params.put("fin", fin);

        // ===============================
        //   SQL PRINCIPAL
        // ===============================
        String sql = """
        WITH movimientos AS (
            SELECT
                dp.id_datos_personal,
                dp.documento,
                (dp.nombres || ' ' || dp.primer_apellido || ' ' ||
                 COALESCE(dp.segundo_apellido,'')) AS nombre_completo,

                fi.valor_salario +
                fi.ingresos_arriendo +
                fi.ingresos_comisiones +
                fi.otros_ingresos AS ingresos_mensuales,

                fi.egresos_familiares +
                fi.egresos_arriendo +
                fi.egresos_credito +
                fi.otros_egresos AS egresos_mensuales,

                z.nombre_zona,
                sz.nombre_sub_zona,

                v.nombre_agencia,
                v.saldo_actual_cuenta AS saldo_aportes,

                COALESCE(SUM(CASE 
                     WHEN tm.accion_movimiento = 'S' THEN e.valor_debito 
                     ELSE 0 END),0) AS total_consignaciones

            FROM reporting.vw_hoja_vida_general_total_reciente hv
            JOIN hoja_vida.datos_personales dp 
                ON dp.id_datos_personal = hv.id_datos_personal

            LEFT JOIN hoja_vida.financieros fi 
                ON fi.id_datos_personal = hv.id_datos_personal

            LEFT JOIN hoja_vida.ubicaciones ub 
                ON ub.id_datos_personal = dp.id_datos_personal
            LEFT JOIN general.sub_zonas sz 
                ON sz.id_sub_zona = ub.id_sub_zona
            LEFT JOIN general.zonas z 
                ON z.id_zona = sz.id_zona

            LEFT JOIN depositos.vw_depositos_cuentas_ahorro_total v
                ON v.id_datos_personal = dp.id_datos_personal
               AND v.codigo_forma = '01'

            LEFT JOIN depositos.extractos_cuentas_ahorros e
                ON e.id_cuenta_ahorro = v.id_cuenta_ahorro
               AND e.fecha_movimiento BETWEEN TO_DATE(:inicio,'YYYY-MM-DD')
                                          AND TO_DATE(:fin,'YYYY-MM-DD')

            LEFT JOIN depositos.tipo_movimiento tm 
                ON tm.codigo_movimiento = e.tipo_movimiento

            GROUP BY
                dp.id_datos_personal, dp.documento, nombre_completo,
                ingresos_mensuales, egresos_mensuales,
                z.nombre_zona, sz.nombre_sub_zona,
                v.nombre_agencia, v.saldo_actual_cuenta
        )

        SELECT
            *,
            CASE 
                WHEN total_consignaciones > ingresos_mensuales THEN 'INUSUAL'
                WHEN total_consignaciones > egresos_mensuales THEN 'INUSUAL'
                ELSE 'NORMAL'
            END AS estado_inusual,

            ROUND(
               (CASE WHEN ingresos_mensuales > 0\s
                     THEN (total_consignaciones / ingresos_mensuales) * 100\s
                     ELSE 0 END)::numeric
            , 2) AS pct_vs_ingresos,
            
            ROUND(
               (CASE WHEN egresos_mensuales > 0\s
                     THEN (total_consignaciones / egresos_mensuales) * 100\s
                     ELSE 0 END)::numeric
            , 2) AS pct_vs_egresos

        FROM movimientos
        ORDER BY total_consignaciones DESC
        """;

        List<Map<String, Object>> rows = jdbc.queryForList(sql, params);
        List<InformeMovimientosInusualesDTO> lista = new ArrayList<>();

        for (Map<String, Object> row : rows) {

            InformeMovimientosInusualesDTO dto = new InformeMovimientosInusualesDTO();

            dto.idDatosPersonal      = row.get("id_datos_personal") != null ? ((Number) row.get("id_datos_personal")).longValue() : null;
            dto.documento            = (String) row.get("documento");
            dto.nombreCompleto       = (String) row.get("nombre_completo");

            dto.ingresosMensuales    = row.get("ingresos_mensuales") != null ? ((Number) row.get("ingresos_mensuales")).doubleValue() : 0;
            dto.egresosMensuales     = row.get("egresos_mensuales") != null ? ((Number) row.get("egresos_mensuales")).doubleValue() : 0;

            dto.nombreZona           = (String) row.get("nombre_zona");
            dto.nombreSubZona        = (String) row.get("nombre_sub_zona");

            dto.nombreAgencia        = (String) row.get("nombre_agencia");
            dto.saldoAportes         = row.get("saldo_aportes") != null ? ((Number) row.get("saldo_aportes")).doubleValue() : 0;

            dto.totalConsignaciones  = row.get("total_consignaciones") != null ? ((Number) row.get("total_consignaciones")).doubleValue() : 0;

            dto.estadoInusual        = (String) row.get("estado_inusual");

            dto.pctVsIngresos        = row.get("pct_vs_ingresos") != null ? ((Number) row.get("pct_vs_ingresos")).doubleValue() : 0;
            dto.pctVsEgresos         = row.get("pct_vs_egresos") != null ? ((Number) row.get("pct_vs_egresos")).doubleValue() : 0;

            lista.add(dto);
        }

        return lista;
    }

    // =====================================================
    //  INFORME: MAYORES DE 7 CON REGISTRO CIVIL
    // =====================================================

    public List<InformeRegla002DTO> informeRegla002(Map<String, Object> filtros) {

        String sql = """
        SELECT
            d.id_datos_personal,
            d.documento,
            d.tipo_documento,
            d.nombre_tipo_documento,

            -- Construir nombre completo desde HV
            TRIM(CONCAT(hv.nombres, ' ', hv.primer_apellido, ' ', COALESCE(hv.segundo_apellido, '')))
                AS nombre_completo,

            -- Zona / Subzona
            hv.nombre_zona,
            hv.nombre_sub_zona,

            -- Contacto
            hv.telefono,
            hv.celular_uno,
            hv.celular_dos,
            hv.correo_personal,

            -- Permisos especiales
            hv.recibe_llamadas,
            hv.recibe_msm,
            hv.recibe_emails,
            hv.recibe_cartas,
            hv.recibe_redes_sociales,

            -- Cuenta aportes
            d.saldo_actual_cuenta AS saldo_aportes,
            d.fecha_apertura_cuenta,

            d.fecha_nacimiento,
            EXTRACT(YEAR FROM age(CURRENT_DATE, d.fecha_nacimiento))::int AS edad

        FROM depositos.vw_depositos_cuentas_ahorro_total d

        LEFT JOIN reporting.vw_hoja_vida_general_total_reciente hv
            ON hv.id_datos_personal = d.id_datos_personal

        WHERE 1 = 1
          AND CAST(d.tipo_persona AS TEXT) = '1'
          AND d.codigo_forma = '01'
          AND d.saldo_actual_cuenta > 0
          AND (
                (EXTRACT(YEAR FROM age(CURRENT_DATE, d.fecha_nacimiento)) >= 7
                 AND CAST(d.tipo_documento AS TEXT) = 'R')
               OR
                (EXTRACT(YEAR FROM age(CURRENT_DATE, d.fecha_nacimiento)) >= 18
                 AND CAST(d.tipo_documento AS TEXT) <> 'C')
              )

        ORDER BY hv.nombre_zona, hv.nombre_sub_zona, nombre_completo
        """;

        List<Map<String, Object>> rows = jdbc.queryForList(sql, Map.of());

        List<InformeRegla002DTO> lista = new ArrayList<>();

        for (Map<String, Object> row : rows) {

            InformeRegla002DTO dto = new InformeRegla002DTO();

            dto.idDatosPersonal    = row.get("id_datos_personal") != null
                    ? ((Number) row.get("id_datos_personal")).longValue() : null;

            dto.documento          = (String) row.get("documento");
            dto.tipoDocumento      = (String) row.get("tipo_documento");
            dto.nombreTipoDocumento = (String) row.get("nombre_tipo_documento");
            dto.nombreCompleto     = (String) row.get("nombre_completo");

            // Zona
            dto.nombreZona         = (String) row.get("nombre_zona");
            dto.nombreSubZona      = (String) row.get("nombre_sub_zona");

            // Contacto
            dto.telefono        = (String) row.get("telefono");
            dto.celularUno      = (String) row.get("celular_uno");
            dto.celularDos      = (String) row.get("celular_dos");
            dto.correoPersonal  = (String) row.get("correo_personal");

            // Permisos
            dto.recibeLlamadas       = (Boolean) row.get("recibe_llamadas");
            dto.recibeMsm            = (Boolean) row.get("recibe_msm");
            dto.recibeEmails         = (Boolean) row.get("recibe_emails");
            dto.recibeCartas         = (Boolean) row.get("recibe_cartas");
            dto.recibeRedesSociales  = (Boolean) row.get("recibe_redes_sociales");

            // Cuenta
            dto.saldoAportes = row.get("saldo_aportes") != null
                    ? ((Number) row.get("saldo_aportes")).doubleValue() : 0;

            dto.fechaAperturaCuenta = row.get("fecha_apertura_cuenta") != null
                    ? row.get("fecha_apertura_cuenta").toString() : null;

            // Edad
            dto.edad = row.get("edad") != null ? ((Number) row.get("edad")).intValue() : null;

            // Motivo
            String tipoDoc = dto.tipoDocumento;
            if (dto.edad != null && dto.edad >= 7 && "R".equals(tipoDoc)) {
                dto.motivo = ">= 7 años con Registro Civil (debería tener Tarjeta de Identidad)";
            }
            else if (dto.edad != null && dto.edad >= 18 && !"C".equals(tipoDoc)) {
                dto.motivo = ">= 18 años sin Cédula de Ciudadanía";
            } else {
                dto.motivo = "Documento no corresponde a la edad";
            }

            lista.add(dto);
        }

        return lista;
    }

    // =====================================================
//  INFORME: REGLA 003 — FORMA 03 PROHIBIDA PARA MAYORES
// =====================================================
    public List<InformeRegla003DTO> informeRegla003(Map<String, Object> filtros) {

        int edadLimite = 14;  // ✔ Valor oficial

        String sql = """
        SELECT
            d.id_datos_personal,
            d.documento,
            d.tipo_documento,
            d.nombre_tipo_documento,
            d.nombre_completo,

            -- Zona
            hv.nombre_zona,
            hv.nombre_sub_zona,

            -- Contacto
            hv.telefono,
            hv.celular_uno,
            hv.celular_dos,
            hv.correo_personal,

            -- Permisos especiales
            hv.recibe_llamadas,
            hv.recibe_msm,
            hv.recibe_emails,
            hv.recibe_cartas,
            hv.recibe_redes_sociales,

            -- Aportes
            d.saldo_actual_cuenta AS saldo_aportes,
            d.fecha_apertura_cuenta,
            d.fecha_nacimiento,

            EXTRACT(YEAR FROM age(CURRENT_DATE, d.fecha_nacimiento))::int AS edad

        FROM depositos.vw_depositos_cuentas_ahorro_total d

        LEFT JOIN reporting.vw_hoja_vida_general_total_reciente hv
            ON hv.id_datos_personal = d.id_datos_personal

        WHERE 1 = 1
          AND CAST(d.tipo_persona AS TEXT) = '1'
          AND d.codigo_forma = '03'
          AND d.saldo_actual_cuenta > 0
          AND EXTRACT(YEAR FROM age(CURRENT_DATE, d.fecha_nacimiento)) > :edadLimite

        ORDER BY hv.nombre_zona, hv.nombre_sub_zona, d.nombre_completo
        """;

        List<Map<String, Object>> rows =
                jdbc.queryForList(sql, Map.of("edadLimite", edadLimite));

        List<InformeRegla003DTO> lista = new ArrayList<>();

        for (Map<String, Object> row : rows) {

            InformeRegla003DTO dto = new InformeRegla003DTO();

            dto.idDatosPersonal = row.get("id_datos_personal") != null
                    ? ((Number) row.get("id_datos_personal")).longValue()
                    : null;

            dto.documento          = (String) row.get("documento");
            dto.tipoDocumento      = (String) row.get("tipo_documento");
            dto.nombreTipoDocumento = (String) row.get("nombre_tipo_documento");
            dto.nombreCompleto     = (String) row.get("nombre_completo");

            dto.nombreZona         = (String) row.get("nombre_zona");
            dto.nombreSubZona      = (String) row.get("nombre_sub_zona");

            dto.telefono           = (String) row.get("telefono");
            dto.celularUno         = (String) row.get("celular_uno");
            dto.celularDos         = (String) row.get("celular_dos");
            dto.correoPersonal     = (String) row.get("correo_personal");

            dto.recibeLlamadas       = (Boolean) row.get("recibe_llamadas");
            dto.recibeMsm            = (Boolean) row.get("recibe_msm");
            dto.recibeEmails         = (Boolean) row.get("recibe_emails");
            dto.recibeCartas         = (Boolean) row.get("recibe_cartas");
            dto.recibeRedesSociales  = (Boolean) row.get("recibe_redes_sociales");

            dto.saldoAportes = row.get("saldo_aportes") != null
                    ? ((Number) row.get("saldo_aportes")).doubleValue()
                    : 0.0;

            dto.fechaAperturaCuenta = row.get("fecha_apertura_cuenta") != null
                    ? row.get("fecha_apertura_cuenta").toString()
                    : null;

            dto.edad = row.get("edad") != null
                    ? ((Number) row.get("edad")).intValue()
                    : null;

            // Motivo
            dto.motivo = "Forma 03 prohibida para mayores de " + edadLimite +
                    " años. (Edad: " + dto.edad + ")";

            lista.add(dto);
        }

        return lista;
    }

}
