package co.assip.erp.nomina.liquidacion_v2;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.liquidacion.LiquidacionDetalleRepository;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionDetalleDTO;
import co.assip.erp.nomina.liquidacion_v2.calculo.DeduccionesV2Calculator;
import co.assip.erp.nomina.liquidacion_v2.calculo.DevengadosV2Calculator;
import co.assip.erp.nomina.liquidacion_v2.calculo.IbcV2Calculator;
import co.assip.erp.nomina.liquidacion_v2.calculo.ResumenTiempoCalculator;
import co.assip.erp.nomina.liquidacion_v2.dto.LiquidacionV2RequestDTO;
import co.assip.erp.nomina.liquidacion_v2.dto.ResumenTiempoDTO;
import co.assip.erp.nomina.novedades_nomina.NovedadesNominaRepository;
import co.assip.erp.nomina.periodos_nomina.PeriodosNominaRepository;
import co.assip.erp.nomina.periodos_nomina.PeriodosNominaRepository.PeriodoFechas;
import co.assip.erp.seguridad.repository.UsuarioAgenciaRepository;
import co.assip.erp.seguridad.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LiquidacionV2Service {

    private final PeriodosNominaRepository periodosRepo;
    private final UsuarioAgenciaRepository usuarioAgenciaRepo;

    private final LiquidacionV2Repository liquidacionRepo;
    private final LiquidacionDetalleRepository detalleRepo;
    private final NovedadesNominaRepository novedadesRepo;

    private final ResumenTiempoCalculator resumenTiempoCalculator;
    private final DevengadosV2Calculator devengadosCalculator;
    private final IbcV2Calculator ibcCalculator;
    private final DeduccionesV2Calculator deduccionesCalculator;

    // =========================================================
    // 🔄 PROCESO PRINCIPAL
    // =========================================================
    @Transactional
    public void liquidarPeriodo(LiquidacionV2RequestDTO request) {

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

        PeriodoFechas fechasPeriodo = periodosRepo.obtenerFechas(idPeriodo);
        if (fechasPeriodo == null) {
            throw new IllegalStateException("No se pudieron obtener fechas del período");
        }

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

        for (EmpleadoContratoDTO contrato : contratos) {

            if (liquidacionRepo.existeLiquidacion(
                    idPeriodo,
                    contrato.getIdContrato()
            )) {
                continue;
            }

            liquidarContrato(
                    idPeriodo,
                    fechasPeriodo,
                    contrato
            );
        }

        periodosRepo.cambiarEstado(
                idPeriodo,
                "CERRADO",
                SecurityUtils.getIdUsuario()
        );
    }

    // =========================================================
    // 🔁 LIQUIDACIÓN INDIVIDUAL
    // =========================================================
    private void liquidarContrato(
            Integer idPeriodoNomina,
            PeriodoFechas fechasPeriodo,
            EmpleadoContratoDTO contrato
    ) {

        Integer idUsuario = SecurityUtils.getIdUsuario();

        Integer idLiquidacion = liquidacionRepo.insertarCabecera(
                idPeriodoNomina,
                contrato,
                idUsuario
        );

        ResumenTiempoDTO resumenTiempo = resumenTiempoCalculator.calcular(
                idPeriodoNomina,
                contrato
        );

        // =========================
        // DEVENGADOS
        // =========================
        List<LiquidacionDetalleDTO> devengados =
                devengadosCalculator.calcular(
                        idPeriodoNomina,
                        contrato
                );
        detalleRepo.insertar(idLiquidacion, devengados, idUsuario);

        // =========================
        // IBC
        // =========================
        BigDecimal ibc = ibcCalculator.calcular(devengados);

        // =========================
        // DEDUCCIONES
        // =========================
        List<LiquidacionDetalleDTO> deducciones =
                deduccionesCalculator.calcular(
                        idPeriodoNomina,
                        contrato,
                        ibc
                );
        detalleRepo.insertar(idLiquidacion, deducciones, idUsuario);

        // =====================================================
        // 🧾 GUARDAR NOVEDADES CALCULADAS (V2)
        // =====================================================
        guardarNovedadesCalculadas(
                idPeriodoNomina,
                fechasPeriodo,
                contrato,
                devengados,
                deducciones,
                idUsuario
        );

        actualizarNovedadesCalculadas(devengados, deducciones, idUsuario);

        // =====================================================
        // 🔒 MARCAR NOVEDADES ABIERTAS COMO APLICADAS
        // =====================================================
        novedadesRepo.marcarNovedadesAplicadas(
                idPeriodoNomina,
                contrato.getIdContrato(),
                idUsuario
        );

        // =========================
        // ACTUALIZACIONES
        // =========================
        liquidacionRepo.actualizarDiasLaborados(
                idLiquidacion,
                resumenTiempo.getDiasLaboradosSafe(),
                idUsuario
        );

        liquidacionRepo.actualizarTotales(
                idLiquidacion,
                ibc,
                idUsuario
        );
    }

    // =========================================================
    // 🧾 GUARDAR NOVEDADES CALCULADAS
    // - Solo AUTOMATICO
    // - Usa origen = CALCULO (compatible con sistema actual)
    // =========================================================
    private void guardarNovedadesCalculadas(
            Integer idPeriodoNomina,
            PeriodoFechas fechasPeriodo,
            EmpleadoContratoDTO contrato,
            List<LiquidacionDetalleDTO> devengados,
            List<LiquidacionDetalleDTO> deducciones,
            Integer idUsuario
    ) {

        // DEVENGADOS
        for (LiquidacionDetalleDTO d : devengados) {

            if (!"AUTOMATICO".equals(d.getOrigen())) continue;

            novedadesRepo.insertarNovedadAplicada(
                    idPeriodoNomina,
                    contrato.getIdEmpleado(),
                    contrato.getIdContrato(),
                    d.getCodigoConcepto(),
                    fechasPeriodo.fechaInicio(),
                    fechasPeriodo.fechaFin(),
                    d.getCantidad(),
                    d.getValorTotal(),
                    idUsuario
            );
        }

        // DEDUCCIONES
        for (LiquidacionDetalleDTO d : deducciones) {

            if (!"AUTOMATICO".equals(d.getOrigen())) continue;

            novedadesRepo.insertarNovedadAplicada(
                    idPeriodoNomina,
                    contrato.getIdEmpleado(),
                    contrato.getIdContrato(),
                    d.getCodigoConcepto(),
                    fechasPeriodo.fechaInicio(),
                    fechasPeriodo.fechaFin(),
                    d.getCantidad(),
                    d.getValorTotal(),
                    idUsuario
            );
        }
    }

    // =========================================================
    // ✅ VALIDACIONES
    // =========================================================
    private void validarAgencia(Integer fkAgencia) {

        Integer idUsuario = SecurityUtils.getIdUsuario();

        if (!usuarioAgenciaRepo.existsByIdUsuarioAndIdAgencia(idUsuario, fkAgencia)) {
            throw new IllegalStateException(
                    "El usuario no tiene acceso a la agencia seleccionada"
            );
        }
    }

    private void actualizarNovedadesCalculadas(
            List<LiquidacionDetalleDTO> devengados,
            List<LiquidacionDetalleDTO> deducciones,
            Integer idUsuario
    ) {

        // DEVENGADOS
        for (LiquidacionDetalleDTO d : devengados) {

            if (!"NOVEDAD".equals(d.getOrigen())) continue;
            if (d.getIdNovedadNomina() == null) continue;

            novedadesRepo.actualizarValorNovedad(
                    d.getIdNovedadNomina(),
                    d.getValorTotal(),
                    d.getCantidad(),
                    idUsuario
            );
        }

        // DEDUCCIONES (por si alguna aplica en futuro)
        for (LiquidacionDetalleDTO d : deducciones) {

            if (!"NOVEDAD".equals(d.getOrigen())) continue;
            if (d.getIdNovedadNomina() == null) continue;

            novedadesRepo.actualizarValorNovedad(
                    d.getIdNovedadNomina(),
                    d.getValorTotal(),
                    d.getCantidad(),
                    idUsuario
            );
        }
    }

}