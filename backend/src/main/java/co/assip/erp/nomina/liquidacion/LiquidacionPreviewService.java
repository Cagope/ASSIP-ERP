package co.assip.erp.nomina.liquidacion;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.liquidacion.calculo.DeduccionesCalculator;
import co.assip.erp.nomina.liquidacion.calculo.DevengadosCalculator;
import co.assip.erp.nomina.liquidacion.calculo.IbcCalculator;
import co.assip.erp.nomina.liquidacion.calculo.ProvisionesCalculator;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionDetalleDTO;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionPreviewExcelDTO;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionRequestDTO;
import co.assip.erp.nomina.liquidacion.dto.TotalesLiquidacionDTO;
import co.assip.erp.seguridad.repository.UsuarioAgenciaRepository;
import co.assip.erp.seguridad.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import co.assip.erp.nomina.periodos_nomina.PeriodosNominaRepository;

@Service
@RequiredArgsConstructor
public class LiquidacionPreviewService {

    private final UsuarioAgenciaRepository usuarioAgenciaRepo;
    private final LiquidacionNominaRepository liquidacionRepo;

    private final IbcCalculator ibcCalculator;
    private final DevengadosCalculator devengadosCalculator;
    private final DeduccionesCalculator deduccionesCalculator;
    private final ProvisionesCalculator provisionesCalculator;
    private final PeriodosNominaRepository periodosNominaRepo;

    // =========================================================
    // 👁️ PREVISUALIZAR LIQUIDACIÓN DE PERÍODO (SIN PERSISTIR)
    // =========================================================
    public List<PreviewContratoResult> previewPeriodo(LiquidacionRequestDTO request) {

        // 🔑 Resolver período operativo
        Integer idPeriodo = request.getIdPeriodoNomina();

        if (idPeriodo == null) {
            idPeriodo = liquidacionRepo.obtenerPeriodoOperativo(request.getFkAgencia());
        }

        if (idPeriodo == null) {
            throw new IllegalStateException(
                    "No existe un período de nómina ABIERTO para la agencia"
            );
        }

        validarAgencia(request.getFkAgencia());

        // 🔑 Obtener label del período (UNA sola vez)
        PeriodosNominaRepository.PeriodoLabel periodo =
                periodosNominaRepo.obtenerPeriodoLabel(idPeriodo);

        if (periodo == null) {
            throw new IllegalStateException(
                    "No se pudo resolver información del período operativo"
            );
        }

        // 1️⃣ Contratos a liquidar
        List<EmpleadoContratoDTO> contratos =
                liquidacionRepo.obtenerContratosParaLiquidacion(
                        idPeriodo,
                        request.getFkAgencia()
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
                    idPeriodo,
                    contrato
            );

            List<LiquidacionDetalleDTO> detalles = new ArrayList<>();

            detalles.addAll(
                    devengadosCalculator.calcular(
                            idPeriodo,
                            contrato,
                            ibc
                    )
            );

            detalles.addAll(
                    deduccionesCalculator.calcular(
                            idPeriodo,
                            contrato,
                            ibc
                    )
            );

            TotalesLiquidacionDTO totales = calcularTotales(detalles);

            resultado.add(
                    new PreviewContratoResult(
                            periodo,
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

        // 🔹 No se usa en preview
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
            PeriodosNominaRepository.PeriodoLabel periodo,
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

        Integer idPeriodo = request.getIdPeriodoNomina() != null
                ? request.getIdPeriodoNomina()
                : liquidacionRepo.obtenerPeriodoOperativo(request.getFkAgencia());

        for (PreviewContratoResult r : preview) {

            EmpleadoContratoDTO contrato = r.contrato();
            TotalesLiquidacionDTO totales = r.totales();

            for (LiquidacionDetalleDTO d : r.detalles()) {

                LiquidacionPreviewExcelDTO x = new LiquidacionPreviewExcelDTO();

                x.setIdEmpleado(contrato.getIdEmpleado());
                x.setNombreEmpleado(null);
                x.setDocumentoEmpleado(null);

                x.setIdContrato(contrato.getIdContrato());
                x.setFechaInicioContrato(contrato.getFechaInicio());
                x.setFechaFinContrato(contrato.getFechaFin());
                x.setContratoActivo(contrato.getActivo());

                x.setIdAgencia(request.getFkAgencia());
                x.setNombreAgencia(null);

                x.setIdSeccion(contrato.getIdSeccion());
                x.setNombreSeccion(null);

                x.setIdCargo(contrato.getIdCargo());
                x.setNombreCargo(null);

                x.setIdPeriodoNomina(idPeriodo);

                x.setCodigoConcepto(d.getCodigoConcepto());
                x.setNombreConcepto(null);
                x.setTipoConcepto(d.getTipo());
                x.setOrigen(d.getOrigen());

                x.setTipoCalculo(d.getTipoCalculo());
                x.setMultiplicador(d.getMultiplicador());

                x.setCantidad(d.getCantidad());
                x.setValorUnitario(d.getValorUnitario());
                x.setBaseCalculoValor(d.getBaseCalculo());
                x.setValorTotal(d.getValorTotal());

                x.setIdNovedadNomina(d.getIdNovedadNomina());

                x.setSalarioBase(contrato.getSalarioBase());
                x.setIbc(r.ibc());
                x.setTotalDevengados(totales.getTotalDevengados());
                x.setTotalDeducciones(totales.getTotalDeducciones());
                x.setTotalProvisiones(totales.getTotalProvisiones());
                x.setNetoPagar(totales.getNetoPagar());

                x.setEstadoLiquidacion("PREVIEW");

                filas.add(x);
            }
        }

        return filas;
    }
}