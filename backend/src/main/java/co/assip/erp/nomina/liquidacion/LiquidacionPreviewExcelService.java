package co.assip.erp.nomina.liquidacion;

import co.assip.erp.nomina.conceptos_nomina.ConceptosNominaRepository;
import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionDetalleDTO;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionPreviewExcelDTO;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionRequestDTO;
import co.assip.erp.nomina.liquidacion.dto.TotalesLiquidacionDTO;
import co.assip.erp.nomina.periodos_nomina.PeriodosNominaRepository;
import co.assip.erp.nomina.periodos_nomina.PeriodosNominaRepository.PeriodoFechas;
import co.assip.erp.shared.personas.PersonasBusquedaRepository.PersonaBasica;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;


import java.util.*;

/**
 * Servicio de generación de datos PLANO para exportación Excel
 * del PREVIEW de liquidación.
 *
 * ⚠️ NO recalcula
 * ⚠️ NO persiste
 * ⚠️ NO modifica estados
 *
 * ✅ Enriquecimiento SOLO para Excel (sin tocar procesos core)
 */
@Service
@RequiredArgsConstructor
public class LiquidacionPreviewExcelService {

    private final LiquidacionPreviewService previewService;
    private final ConceptosNominaRepository conceptosNominaRepository;
    private final PeriodosNominaRepository periodosNominaRepository;

    // Lookups SOLO para exportación
    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // 📊 GENERAR DATA EXCEL (PLANO)
    // =========================================================
    public List<LiquidacionPreviewExcelDTO> generarPreviewExcel(
            LiquidacionRequestDTO request
    ) {

        // 1️⃣ Preview oficial (misma lógica del sistema)
        List<LiquidacionPreviewService.PreviewContratoResult> preview =
                previewService.previewPeriodo(request);

        // 2️⃣ Conceptos (1 query)
        Map<String, String> conceptoNombreByCodigo =
                conceptosNominaRepository.mapCodigoNombre(true);

        // 3️⃣ Fechas del período (1 query)

        Integer idPeriodo = request.getIdPeriodoNomina();

        if (idPeriodo == null) {
            idPeriodo = preview.isEmpty()
                    ? null
                    : preview.get(0).periodo().idPeriodo();
        }

        PeriodoFechas fechasPeriodo =
                idPeriodo != null
                        ? periodosNominaRepository.obtenerFechas(idPeriodo)
                        : null;

        if (fechasPeriodo == null) {
            throw new IllegalStateException(
                    "No se encontraron fechas para el período de nómina id=" + idPeriodo
            );
        }

        // =========================================================
        // 🔎 LOOKUPS POR LOTE (EXPORTACIÓN)
        // =========================================================

        // 4️⃣ Agencia
        String nombreAgencia = null;
        if (request.getFkAgencia() != null) {
            String sqlAg = """
                SELECT nombre_agencia
                FROM general.datos_agencias
                WHERE id_agencia = :id
            """;
            List<String> ag = jdbc.query(
                    sqlAg,
                    new MapSqlParameterSource("id", request.getFkAgencia()),
                    (rs, i) -> rs.getString(1)
            );
            nombreAgencia = ag.isEmpty() ? null : ag.get(0);
        }

        // 5️⃣ Secciones
        Map<Integer, String> seccionById = jdbc.query(
                "SELECT id_seccion, nombre_seccion FROM nomina.secciones_nomina",
                rs -> {
                    Map<Integer, String> m = new HashMap<>();
                    while (rs.next()) {
                        m.put(rs.getInt(1), rs.getString(2));
                    }
                    return m;
                }
        );

        // 6️⃣ Cargos
        Map<Integer, String> cargoById = jdbc.query(
                "SELECT id_cargo, nombre_cargo FROM nomina.cargos",
                rs -> {
                    Map<Integer, String> m = new HashMap<>();
                    while (rs.next()) {
                        m.put(rs.getInt(1), rs.getString(2));
                    }
                    return m;
                }
        );

        // 7️⃣ Personas por EMPLEADO (1 query)
        Map<Integer, PersonaBasica> personaByEmpleado = new HashMap<>();

        Set<Integer> idsEmpleado = new HashSet<>();
        for (var p : preview) {
            if (p.contrato().getIdEmpleado() != null) {
                idsEmpleado.add(p.contrato().getIdEmpleado());
            }
        }

        if (!idsEmpleado.isEmpty()) {
            String sqlPer = """
                SELECT
                  e.id_empleado,
                  dp.documento,
                  TRIM(
                    COALESCE(dp.primer_apellido,'') || ' ' ||
                    COALESCE(dp.segundo_apellido,'') || ' ' ||
                    COALESCE(dp.nombres,'')
                  ) AS nombre
                FROM nomina.empleados e
                JOIN hoja_vida.datos_personales dp
                  ON dp.id_datos_personal = e.id_datos_personal
                WHERE e.id_empleado IN (:ids)
            """;

            jdbc.query(
                    sqlPer,
                    new MapSqlParameterSource("ids", idsEmpleado),
                    (rs, rowNum) -> {
                        personaByEmpleado.put(
                                rs.getInt("id_empleado"),
                                new PersonaBasica(
                                        rs.getString("documento"),
                                        rs.getString("nombre")
                                )
                        );
                        return null; // RowMapper requiere retorno
                    }
            );
        }

        // =========================================================
        // 🧾 CONSTRUIR FILAS
        // =========================================================
        List<LiquidacionPreviewExcelDTO> filas = new ArrayList<>();

        for (var p : preview) {

            EmpleadoContratoDTO contrato = p.contrato();
            TotalesLiquidacionDTO totales = p.totales();
            PersonaBasica pb = personaByEmpleado.get(contrato.getIdEmpleado());

            for (LiquidacionDetalleDTO d : p.detalles()) {

                // ❌ EXCLUIR PROVISIONES Y APORTES DEL PREVIEW / EXCEL
                if (!"DEVENGADO".equals(d.getTipo())
                        && !"DEDUCCION".equals(d.getTipo())) {
                    continue;
                }

                LiquidacionPreviewExcelDTO x = new LiquidacionPreviewExcelDTO();

                // =========================
                // 🧑 EMPLEADO
                // =========================
                x.setIdEmpleado(contrato.getIdEmpleado());
                if (pb != null) {
                    x.setDocumentoEmpleado(pb.documento());
                    x.setNombreEmpleado(pb.nombreCompleto());
                }

                // =========================
                // 📄 CONTRATO
                // =========================
                x.setIdContrato(contrato.getIdContrato());
                x.setFechaInicioContrato(contrato.getFechaInicio());
                x.setFechaFinContrato(contrato.getFechaFin());
                x.setContratoActivo(contrato.getActivo());

                // =========================
                // 🏢 ORGANIZACIÓN
                // =========================
                x.setIdAgencia(request.getFkAgencia());
                x.setNombreAgencia(nombreAgencia);

                x.setIdSeccion(contrato.getIdSeccion());
                x.setNombreSeccion(
                        contrato.getIdSeccion() != null
                                ? seccionById.get(contrato.getIdSeccion())
                                : null
                );

                x.setIdCargo(contrato.getIdCargo());
                x.setNombreCargo(
                        contrato.getIdCargo() != null
                                ? cargoById.get(contrato.getIdCargo())
                                : null
                );

                // =========================
                // 🗓 PERÍODO
                // =========================
                x.setIdPeriodoNomina(idPeriodo);
                x.setFechaInicioPeriodo(fechasPeriodo.fechaInicio());
                x.setFechaFinPeriodo(fechasPeriodo.fechaFin());

                // =========================
                // 💼 CONCEPTO
                // =========================
                x.setCodigoConcepto(d.getCodigoConcepto());
                x.setNombreConcepto(conceptoNombreByCodigo.get(d.getCodigoConcepto()));
                x.setTipoConcepto(d.getTipo());
                x.setOrigen(d.getOrigen());

                // =========================
                // ⚙️ MOTOR
                // =========================
                x.setTipoCalculo(d.getTipoCalculo());
                x.setMultiplicador(d.getMultiplicador());
                x.setBaseCalculo(null); // si tienes tipo explícito, aquí se conecta

                // =========================
                // 🧮 CÁLCULO
                // =========================
                x.setCantidad(d.getCantidad());
                x.setValorUnitario(d.getValorUnitario());
                x.setBaseCalculoValor(d.getBaseCalculo());
                x.setValorTotal(d.getValorTotal());

                // =========================
                // 🧾 NOVEDAD
                // =========================
                x.setIdNovedadNomina(d.getIdNovedadNomina());
                x.setObservacionNovedad(null);

                // =========================
                // 📊 TOTALES
                // =========================
                x.setSalarioBase(contrato.getSalarioBase());
                x.setIbc(p.ibc());
                x.setTotalDevengados(totales.getTotalDevengados());
                x.setTotalDeducciones(totales.getTotalDeducciones());
                x.setTotalProvisiones(totales.getTotalProvisiones());
                x.setNetoPagar(totales.getNetoPagar());

                // =========================
                // 🔎 CONTROL
                // =========================
                x.setEstadoLiquidacion("PREVIEW");

                filas.add(x);
            }
        }

        return filas;
    }
}