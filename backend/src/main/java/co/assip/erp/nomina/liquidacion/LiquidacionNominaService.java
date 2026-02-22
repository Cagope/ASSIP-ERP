package co.assip.erp.nomina.liquidacion;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.liquidacion.calculo.DeduccionesCalculator;
import co.assip.erp.nomina.liquidacion.calculo.DevengadosCalculator;
import co.assip.erp.nomina.liquidacion.calculo.IbcCalculator;
import co.assip.erp.nomina.liquidacion.calculo.ProvisionesCalculator;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionDetalleDTO;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionRequestDTO;
import co.assip.erp.nomina.liquidacion.dto.TotalesLiquidacionDTO;
import co.assip.erp.nomina.periodos_nomina.PeriodosNominaRepository;
import co.assip.erp.seguridad.repository.UsuarioAgenciaRepository;
import co.assip.erp.seguridad.utils.SecurityUtils;
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

    private final LiquidacionNominaRepository liquidacionRepo;
    private final LiquidacionDetalleRepository detalleRepo;

    private final IbcCalculator ibcCalculator;
    private final DevengadosCalculator devengadosCalculator;
    private final DeduccionesCalculator deduccionesCalculator;
    private final ProvisionesCalculator provisionesCalculator;

    // =========================================================
    // 🔄 PROCESO PRINCIPAL
    // =========================================================
    @Transactional
    public void liquidarPeriodo(LiquidacionRequestDTO request) {

        if (request.getIdPeriodoNomina() == null) {
            throw new IllegalStateException("El período de nómina es obligatorio");
        }

        validarAgencia(request.getFkAgencia());

        // 1️⃣ Contratos activos definidos por el query de liquidación
        List<EmpleadoContratoDTO> contratos =
                liquidacionRepo.obtenerContratosParaLiquidacion(
                        request.getIdPeriodoNomina()
                );

        if (contratos.isEmpty()) {
            throw new IllegalStateException(
                    "No existen contratos activos para liquidar en el período"
            );
        }

        // 2️⃣ Liquidar contrato a contrato (idempotente)
        for (EmpleadoContratoDTO contrato : contratos) {

            if (liquidacionRepo.existeLiquidacion(
                    request.getIdPeriodoNomina(),
                    contrato.getIdContrato()
            )) {
                continue;
            }

            liquidarContrato(
                    request.getIdPeriodoNomina(),
                    contrato
            );
        }

        // 3️⃣ Marcar período como liquidado
        periodosRepo.marcarLiquidado(
                request.getIdPeriodoNomina(),
                SecurityUtils.getIdUsuario()
        );
    }

    // =========================================================
    // 🔁 LIQUIDACIÓN INDIVIDUAL
    // =========================================================
    private void liquidarContrato(
            Integer idPeriodoNomina,
            EmpleadoContratoDTO contrato
    ) {

        Integer idUsuario = SecurityUtils.getIdUsuario();

        // 1️⃣ Cabecera
        Integer idLiquidacion = liquidacionRepo.insertarCabecera(
                idPeriodoNomina,
                contrato,
                idUsuario
        );

        // 2️⃣ IBC
        BigDecimal ibc = ibcCalculator.calcular(
                idPeriodoNomina,
                contrato
        );

        // 3️⃣ Devengados
        List<LiquidacionDetalleDTO> devengados =
                devengadosCalculator.calcular(
                        idPeriodoNomina,
                        contrato,
                        ibc
                );
        detalleRepo.insertar(idLiquidacion, devengados, idUsuario);

        // 4️⃣ Deducciones
        List<LiquidacionDetalleDTO> deducciones =
                deduccionesCalculator.calcular(
                        idPeriodoNomina,
                        contrato,
                        ibc
                );
        detalleRepo.insertar(idLiquidacion, deducciones, idUsuario);

        // 5️⃣ Provisiones
        List<LiquidacionDetalleDTO> provisiones =
                provisionesCalculator.calcular(
                        idPeriodoNomina,
                        contrato,
                        ibc
                );
        detalleRepo.insertar(idLiquidacion, provisiones, idUsuario);

        // 6️⃣ Totales
        liquidacionRepo.actualizarTotales(
                idLiquidacion,
                ibc,
                idUsuario
        );

        validarTotales(idLiquidacion);
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
