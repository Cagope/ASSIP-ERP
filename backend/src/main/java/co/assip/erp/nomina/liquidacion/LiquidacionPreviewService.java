package co.assip.erp.nomina.liquidacion;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.liquidacion.calculo.DeduccionesCalculator;
import co.assip.erp.nomina.liquidacion.calculo.DevengadosCalculator;
import co.assip.erp.nomina.liquidacion.calculo.IbcCalculator;
import co.assip.erp.nomina.liquidacion.calculo.ProvisionesCalculator;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionDetalleDTO;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionRequestDTO;
import co.assip.erp.nomina.liquidacion.dto.TotalesLiquidacionDTO;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionPreviewExcelDTO;
import co.assip.erp.seguridad.repository.UsuarioAgenciaRepository;
import co.assip.erp.seguridad.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LiquidacionPreviewService {

    private final UsuarioAgenciaRepository usuarioAgenciaRepo;
    private final LiquidacionNominaRepository liquidacionRepo;

    private final IbcCalculator ibcCalculator;
    private final DevengadosCalculator devengadosCalculator;
    private final DeduccionesCalculator deduccionesCalculator;
    private final ProvisionesCalculator provisionesCalculator;

    // =========================================================
    // 👁️ PREVISUALIZAR LIQUIDACIÓN DE PERÍODO (SIN PERSISTIR)
    // =========================================================
    public List<PreviewContratoResult> previewPeriodo(LiquidacionRequestDTO request) {

        if (request.getIdPeriodoNomina() == null) {
            throw new IllegalStateException("El período de nómina es obligatorio");
        }

        validarAgencia(request.getFkAgencia());

        // 1️⃣ Contratos a liquidar (MISMO QUERY oficial)
        List<EmpleadoContratoDTO> contratos =
                liquidacionRepo.obtenerContratosParaLiquidacion(
                        request.getIdPeriodoNomina()
                );

        if (contratos.isEmpty()) {
            throw new IllegalStateException(
                    "No existen contratos activos para liquidar en el período"
            );
        }

        List<PreviewContratoResult> resultado = new ArrayList<>();

        // 2️⃣ Simulación contrato a contrato
        for (EmpleadoContratoDTO contrato : contratos) {

            BigDecimal ibc = ibcCalculator.calcular(
                    request.getIdPeriodoNomina(),
                    contrato
            );

            List<LiquidacionDetalleDTO> detalles = new ArrayList<>();

            detalles.addAll(
                    devengadosCalculator.calcular(
                            request.getIdPeriodoNomina(),
                            contrato,
                            ibc
                    )
            );

            detalles.addAll(
                    deduccionesCalculator.calcular(
                            request.getIdPeriodoNomina(),
                            contrato,
                            ibc
                    )
            );

            TotalesLiquidacionDTO totales = calcularTotales(detalles);

            resultado.add(
                    new PreviewContratoResult(
                            contrato,
                            ibc,
                            totales,
                            detalles
                    )
            );
        }

        return resultado;
    }

    // =========================================================
    // 🧮 TOTALES EN MEMORIA (NO BD)
    // =========================================================
    private TotalesLiquidacionDTO calcularTotales(
            List<LiquidacionDetalleDTO> detalles
    ) {

        BigDecimal devengados = BigDecimal.ZERO;
        BigDecimal deducciones = BigDecimal.ZERO;

        for (LiquidacionDetalleDTO d : detalles) {
            if ("DEVENGADO".equals(d.getTipo())) {
                devengados = devengados.add(d.getValorTotal());
            } else if ("DEDUCCION".equals(d.getTipo())) {
                deducciones = deducciones.add(d.getValorTotal());
            }
        }

        TotalesLiquidacionDTO t = new TotalesLiquidacionDTO();
        t.setTotalDevengados(devengados);
        t.setTotalDeducciones(deducciones);

        // 🔹 NO se usa en preview, pero se deja en cero para compatibilidad
        t.setTotalProvisiones(BigDecimal.ZERO);

        t.setNetoPagar(devengados.subtract(deducciones));

        return t;
    }

    // =========================================================
    // 🔐 SEGURIDAD
    // =========================================================
    private void validarAgencia(Integer fkAgencia) {

        Integer idUsuario = SecurityUtils.getIdUsuario();

        if (!usuarioAgenciaRepo.existsByIdUsuarioAndIdAgencia(idUsuario, fkAgencia)) {
            throw new IllegalStateException(
                    "El usuario no tiene acceso a la agencia seleccionada"
            );
        }
    }

    // =========================================================
    // 📦 RESULTADO INTERNO DE PREVIEW POR CONTRATO
    // =========================================================
    public record PreviewContratoResult(
            EmpleadoContratoDTO contrato,
            BigDecimal ibc,
            TotalesLiquidacionDTO totales,
            List<LiquidacionDetalleDTO> detalles
    ) {}

    // =========================================================
// 📤 PREVIEW PLANO PARA EXPORTACIÓN EXCEL
// =========================================================
    public List<LiquidacionPreviewExcelDTO> previewPeriodoExcel(
            LiquidacionRequestDTO request
    ) {

        List<PreviewContratoResult> preview = previewPeriodo(request);
        List<LiquidacionPreviewExcelDTO> filas = new ArrayList<>();

        for (PreviewContratoResult r : preview) {

            EmpleadoContratoDTO contrato = r.contrato();
            TotalesLiquidacionDTO totales = r.totales();

            for (LiquidacionDetalleDTO d : r.detalles()) {

                LiquidacionPreviewExcelDTO x = new LiquidacionPreviewExcelDTO();

                // =========================
                // EMPLEADO
                // =========================
                x.setIdEmpleado(contrato.getIdEmpleado());
                x.setNombreEmpleado(null);      // se llena luego con vista
                x.setDocumentoEmpleado(null);

                // =========================
                // CONTRATO
                // =========================
                x.setIdContrato(contrato.getIdContrato());
                x.setFechaInicioContrato(contrato.getFechaInicio());
                x.setFechaFinContrato(contrato.getFechaFin());
                x.setContratoActivo(contrato.getActivo());

                // =========================
                // ORGANIZACIÓN
                // =========================
                x.setIdAgencia(request.getFkAgencia());
                x.setNombreAgencia(null);

                x.setIdSeccion(contrato.getIdSeccion());
                x.setNombreSeccion(null);

                x.setIdCargo(contrato.getIdCargo());
                x.setNombreCargo(null);

                // =========================
                // PERÍODO
                // =========================
                x.setIdPeriodoNomina(request.getIdPeriodoNomina());

                // =========================
                // CONCEPTO
                // =========================
                x.setCodigoConcepto(d.getCodigoConcepto());
                x.setNombreConcepto(null);
                x.setTipoConcepto(d.getTipo());
                x.setOrigen(d.getOrigen());


                // =========================
                // MOTOR
                // =========================
                x.setTipoCalculo(d.getTipoCalculo());
                x.setMultiplicador(d.getMultiplicador());

                // =========================
                // CÁLCULO
                // =========================
                x.setCantidad(d.getCantidad());
                x.setValorUnitario(d.getValorUnitario());
                x.setBaseCalculoValor(d.getBaseCalculo());
                x.setValorTotal(d.getValorTotal());

                // =========================
                // NOVEDAD
                // =========================
                x.setIdNovedadNomina(d.getIdNovedadNomina());

                // =========================
                // TOTALES
                // =========================
                x.setSalarioBase(contrato.getSalarioBase());
                x.setIbc(r.ibc());
                x.setTotalDevengados(totales.getTotalDevengados());
                x.setTotalDeducciones(totales.getTotalDeducciones());
                x.setTotalProvisiones(totales.getTotalProvisiones());
                x.setNetoPagar(totales.getNetoPagar());

                // =========================
                // CONTROL
                // =========================
                x.setEstadoLiquidacion("PREVIEW");

                filas.add(x);
            }
        }

        return filas;
    }

}
