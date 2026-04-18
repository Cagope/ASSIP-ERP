package co.assip.erp.nomina.liquidacion_v2;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionDetalleDTO;
import co.assip.erp.nomina.liquidacion_v2.calculo.DeduccionesV2Calculator;
import co.assip.erp.nomina.liquidacion_v2.calculo.DevengadosV2Calculator;
import co.assip.erp.nomina.liquidacion_v2.calculo.IbcV2Calculator;
import co.assip.erp.nomina.liquidacion_v2.calculo.ResumenTiempoCalculator;
import co.assip.erp.nomina.liquidacion_v2.dto.LiquidacionV2DetalleDTO;
import co.assip.erp.nomina.liquidacion_v2.dto.LiquidacionV2PreviewItemDTO;
import co.assip.erp.nomina.liquidacion_v2.dto.LiquidacionV2PreviewResponseDTO;
import co.assip.erp.nomina.liquidacion_v2.dto.LiquidacionV2RequestDTO;
import co.assip.erp.nomina.liquidacion_v2.dto.ResumenTiempoDTO;
import co.assip.erp.nomina.periodos_nomina.PeriodosNominaRepository;
import co.assip.erp.seguridad.repository.UsuarioAgenciaRepository;
import co.assip.erp.seguridad.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LiquidacionV2PreviewService {

    private final PeriodosNominaRepository periodosRepo;
    private final UsuarioAgenciaRepository usuarioAgenciaRepo;
    private final LiquidacionV2Repository liquidacionRepo;

    private final ResumenTiempoCalculator resumenTiempoCalculator;
    private final DevengadosV2Calculator devengadosCalculator;
    private final IbcV2Calculator ibcV2Calculator;
    private final DeduccionesV2Calculator deduccionesV2Calculator;

    public PeriodosNominaRepository.PeriodoLabel obtenerPrimerPeriodoDisponible(Integer fkAgencia) {

        validarAgencia(fkAgencia);

        Integer idPeriodo = liquidacionRepo.obtenerPeriodoOperativo(fkAgencia);

        if (idPeriodo == null) {
            return null;
        }

        return periodosRepo.obtenerPeriodoLabel(idPeriodo);
    }

    public LiquidacionV2PreviewResponseDTO preview(LiquidacionV2RequestDTO request) {

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
        periodosRepo.validarPeriodoAbierto(idPeriodo, request.getFkAgencia());

        List<EmpleadoContratoDTO> contratos =
                liquidacionRepo.obtenerContratosParaLiquidacion(
                        idPeriodo,
                        request.getFkAgencia()
                );

        if (contratos.isEmpty()) {
            throw new IllegalStateException(
                    "No existen contratos activos para liquidar"
            );
        }

        List<LiquidacionV2PreviewItemDTO> items = new ArrayList<>();

        BigDecimal totalDev = BigDecimal.ZERO;
        BigDecimal totalDed = BigDecimal.ZERO;
        BigDecimal totalProv = BigDecimal.ZERO;
        BigDecimal totalNeto = BigDecimal.ZERO;

        for (EmpleadoContratoDTO contrato : contratos) {

            ResumenTiempoDTO resumenTiempo = resumenTiempoCalculator.calcular(
                    idPeriodo,
                    contrato
            );

            List<LiquidacionDetalleDTO> devengados = devengadosCalculator.calcular(
                    idPeriodo,
                    contrato
            );

            BigDecimal ibc = ibcV2Calculator.calcular(devengados);

            List<LiquidacionDetalleDTO> deducciones = deduccionesV2Calculator.calcular(
                    idPeriodo,
                    contrato,
                    ibc
            );

            List<LiquidacionDetalleDTO> detalleCompleto = new ArrayList<>();
            detalleCompleto.addAll(devengados);
            detalleCompleto.addAll(deducciones);

            BigDecimal subtotalDev = sumarPorTipo(detalleCompleto, "DEVENGADO");
            BigDecimal subtotalDed = sumarPorTipo(detalleCompleto, "DEDUCCION");
            BigDecimal subtotalProv = BigDecimal.ZERO;
            BigDecimal neto = subtotalDev.subtract(subtotalDed);

            totalDev = totalDev.add(subtotalDev);
            totalDed = totalDed.add(subtotalDed);
            totalProv = totalProv.add(subtotalProv);
            totalNeto = totalNeto.add(neto);

            items.add(
                    LiquidacionV2PreviewItemDTO.builder()
                            .idContrato(contrato.getIdContrato())
                            .idEmpleado(contrato.getIdEmpleado())
                            .documentoEmpleado(contrato.getDocumentoEmpleado())
                            .nombreEmpleado(contrato.getNombreEmpleado())
                            .salarioBase(contrato.getSalarioBase())
                            .diasLaborados(resumenTiempo.getDiasLaboradosSafe())
                            .ibc(ibc)
                            .totalDevengados(subtotalDev)
                            .totalDeducciones(subtotalDed)
                            .totalProvisiones(subtotalProv)
                            .netoPagar(neto)
                            .detalle(mapearDetalle(detalleCompleto))
                            .build()
            );
        }

        return LiquidacionV2PreviewResponseDTO.builder()
                .idPeriodoNomina(idPeriodo)
                .fkAgencia(request.getFkAgencia())
                .totalContratos(items.size())
                .totalDevengados(totalDev)
                .totalDeducciones(totalDed)
                .totalProvisiones(totalProv)
                .totalNetoPagar(totalNeto)
                .items(items)
                .build();
    }

    private void validarAgencia(Integer fkAgencia) {
        Integer idUsuario = SecurityUtils.getIdUsuario();

        if (!usuarioAgenciaRepo.existsByIdUsuarioAndIdAgencia(idUsuario, fkAgencia)) {
            throw new IllegalStateException(
                    "El usuario no tiene acceso a la agencia"
            );
        }
    }

    private BigDecimal sumarPorTipo(List<LiquidacionDetalleDTO> detalle, String tipo) {
        BigDecimal total = BigDecimal.ZERO;

        for (LiquidacionDetalleDTO d : detalle) {
            if (tipo.equalsIgnoreCase(d.getTipo())) {
                total = total.add(
                        d.getValorTotal() != null ? d.getValorTotal() : BigDecimal.ZERO
                );
            }
        }

        return total;
    }

    private List<LiquidacionV2DetalleDTO> mapearDetalle(List<LiquidacionDetalleDTO> detalle) {
        List<LiquidacionV2DetalleDTO> out = new ArrayList<>();

        for (LiquidacionDetalleDTO d : detalle) {
            out.add(
                    LiquidacionV2DetalleDTO.builder()
                            .tipo(d.getTipo())
                            .codigoConcepto(d.getCodigoConcepto())
                            .cantidad(d.getCantidad())
                            .valorUnitario(d.getValorUnitario())
                            .valorTotal(d.getValorTotal())
                            .baseCalculo(d.getBaseCalculo())
                            .origen(d.getOrigen())
                            .idNovedad(d.getIdNovedadNomina())
                            .build()
            );
        }

        return out;
    }
}