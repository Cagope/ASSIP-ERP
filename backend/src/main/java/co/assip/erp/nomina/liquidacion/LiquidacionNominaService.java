package co.assip.erp.nomina.liquidacion;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.liquidacion.calculo.DeduccionesCalculator;
import co.assip.erp.nomina.liquidacion.calculo.DevengadosCalculator;
import co.assip.erp.nomina.liquidacion.calculo.IbcCalculator;
import co.assip.erp.nomina.liquidacion.calculo.ProvisionesCalculator;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionDetalleDTO;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionRequestDTO;
import co.assip.erp.nomina.liquidacion.dto.TotalesLiquidacionDTO;
import co.assip.erp.nomina.novedades_nomina.NovedadesNominaRepository;
import co.assip.erp.nomina.periodos_nomina.PeriodosNominaRepository;
import co.assip.erp.nomina.periodos_nomina.PeriodosNominaRepository.PeriodoFechas;
import co.assip.erp.seguridad.repository.UsuarioAgenciaRepository;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LiquidacionNominaService {

    private final PeriodosNominaRepository periodosRepo;
    private final UsuarioAgenciaRepository usuarioAgenciaRepo;
    private final UsuarioSesionService usuarioSesionService;

    private final LiquidacionNominaRepository liquidacionRepo;
    private final LiquidacionDetalleRepository detalleRepo;
    private final NovedadesNominaRepository novedadesRepo;

    private final IbcCalculator ibcCalculator;
    private final DevengadosCalculator devengadosCalculator;
    private final DeduccionesCalculator deduccionesCalculator;
    private final ProvisionesCalculator provisionesCalculator;

    // =========================================================
    // 🔄 PROCESO PRINCIPAL
    // =========================================================
    @Transactional
    public void liquidarPeriodo(LiquidacionRequestDTO request) {

        Integer idPeriodo = request.getIdPeriodoNomina();

        if (idPeriodo == null) {
            idPeriodo = liquidacionRepo.obtenerPeriodoOperativo(request.getFkAgencia());
        }

        if (idPeriodo == null) {
            throw new IllegalStateException(
                    "No existe un período de nómina ABIERTO para la agencia"
            );
        }

        // 🔒 Validación fuerte
        periodosRepo.validarPeriodoAbierto(idPeriodo, request.getFkAgencia());
        validarAgencia(request.getFkAgencia());

        // 📅 Fechas reales del período
        PeriodoFechas fechasPeriodo = periodosRepo.obtenerFechas(idPeriodo);
        if (fechasPeriodo == null) {
            throw new IllegalStateException("No se pudieron obtener fechas del período");
        }

        // 1️⃣ Contratos activos
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

        // 2️⃣ Liquidación contrato a contrato (IDEMPOTENTE)
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

        // ✅ 3️⃣ Marcar período como LIQUIDADO (estándar)
        periodosRepo.cambiarEstado(
                idPeriodo,
                "CERRADO",
                usuarioSesionService.idUsuario()
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

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Integer idLiquidacion = liquidacionRepo.insertarCabecera(
                idPeriodoNomina,
                contrato,
                idUsuario
        );

        BigDecimal ibc = ibcCalculator.calcular(
                idPeriodoNomina,
                contrato
        );

        List<LiquidacionDetalleDTO> devengados =
                devengadosCalculator.calcular(
                        idPeriodoNomina,
                        contrato,
                        ibc
                );
        detalleRepo.insertar(idLiquidacion, devengados, idUsuario);

        List<LiquidacionDetalleDTO> deducciones =
                deduccionesCalculator.calcular(
                        idPeriodoNomina,
                        contrato,
                        ibc
                );
        detalleRepo.insertar(idLiquidacion, deducciones, idUsuario);

        // ✅ Si el cálculo genera deducciones automáticas (SALUD/PENSIÓN), se registran como NOVEDAD CALCULO en CERRADO
        for (LiquidacionDetalleDTO d : deducciones) {

            if (!"DEDUCCION".equals(d.getTipo())) continue;
            if (!List.of("SALUD_EMP", "PENSION_EMP").contains(d.getCodigoConcepto())) continue;

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

        List<LiquidacionDetalleDTO> provisiones =
                provisionesCalculator.calcular(
                        idPeriodoNomina,
                        contrato,
                        ibc
                );
        detalleRepo.insertar(idLiquidacion, provisiones, idUsuario);

        // ✅ Marcar novedades ABIERTO como CERRADO (aplicadas)
        novedadesRepo.marcarNovedadesAplicadas(
                idPeriodoNomina,
                contrato.getIdContrato(),
                idUsuario
        );

        liquidacionRepo.actualizarTotales(
                idLiquidacion,
                ibc,
                idUsuario
        );

        validarTotales(idLiquidacion);
    }

    // =========================================================
    // 🧹 ELIMINAR LIQUIDACIONES DE UN PERÍODO (para ABRIR)
    // =========================================================
    @Transactional
    public void eliminarPorPeriodo(Integer idPeriodoNomina) {

        if (idPeriodoNomina == null) {
            throw new IllegalArgumentException("El id del período es obligatorio");
        }

        liquidacionRepo.eliminarPorPeriodo(idPeriodoNomina);
    }

    // =========================================================
    // 🔓 ABRIR PERÍODO (compatibilidad con tu método anterior)
    //     - Solo borra ejecución (cabecera + detalle)
    //     - Los estados (periodo/novedades) los maneja PeriodosNominaService
    // =========================================================
    @Transactional
    public int abrirPeriodoNomina(Integer idPeriodoNomina) {

        if (idPeriodoNomina == null) {
            throw new IllegalArgumentException("El id del período es obligatorio");
        }

        return liquidacionRepo.abrirPeriodoEliminarEjecucion(
                idPeriodoNomina,
                usuarioSesionService.idUsuario()
        );
    }

    // =========================================================
    // ✅ VALIDACIONES
    // =========================================================
    private void validarAgencia(Integer fkAgencia) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        if (!usuarioAgenciaRepo.existsByIdUsuarioAndIdAgencia(idUsuario, fkAgencia)) {
            throw new IllegalStateException(
                    "El usuario no tiene acceso a la agencia seleccionada"
            );
        }
    }

    private void validarTotales(Integer idLiquidacion) {

        TotalesLiquidacionDTO t = liquidacionRepo.obtenerTotales(idLiquidacion);

        if (t.getTotalDevengados().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Total devengados inválido");
        }

        if (t.getTotalDeducciones().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Total deducciones inválido");
        }

        if (t.getNetoPagar().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "El neto a pagar no puede ser negativo"
            );
        }
    }
}