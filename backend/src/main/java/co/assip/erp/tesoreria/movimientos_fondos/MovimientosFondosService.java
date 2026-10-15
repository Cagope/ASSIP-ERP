package co.assip.erp.tesoreria.movimientos_fondos;

import co.assip.erp.tesoreria.traslados_cajas.TrasladosCajasService;
import co.assip.erp.tesoreria.traslados_cajas.TrasladosCajasRepository;
import co.assip.erp.tesoreria.consignaciones_bancarias.ConsignacionesBancariasRepository;
import co.assip.erp.tesoreria.consignaciones_bancarias.ConsignacionesBancariasService;

import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoPreviewDTO;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoRequestDTO;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoResponseDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientosFondosService {

    private final TrasladosCajasRepository trasladosCajasRepository;
    private final ConsignacionesBancariasService consignacionesBancariasService;
    private final TrasladosCajasService trasladosCajasService;

    public List<ConsignacionesBancariasRepository.CuentaBancariaConsignacion> listarCuentasBancarias(Integer idAgencia) {
        return consignacionesBancariasService.listarCuentasBancarias(idAgencia);
    }

    public List<ConsignacionesBancariasRepository.ChequeDisponible> listarChequesRecibidos(Integer idAgencia) {
        return consignacionesBancariasService.listarChequesRecibidos(idAgencia);
    }

    public List<ConsignacionesBancariasRepository.CajaEfectivoDisponible> listarCajasConProvision(
            Integer idAgencia, LocalDate fechaConsignacion) {
        return consignacionesBancariasService.listarCajasConProvision(idAgencia, fechaConsignacion);
    }

    public List<TrasladosCajasRepository.CajaMovimientoDisponible> listarCajasMovimiento(Integer idAgencia) {
        validarAgencia(idAgencia);
        return trasladosCajasRepository.listarCajasMovimiento(idAgencia);
    }

    public MovimientoFondoPreviewDTO preview(MovimientoFondoRequestDTO request) {
        if (esTrasladoCajas(request)) {
            return trasladosCajasService.previewTrasladoCajas(request);
        }
        return consignacionesBancariasService.previewConsignacion(request);
    }

    public MovimientoFondoResponseDTO aplicar(MovimientoFondoRequestDTO request) {
        if (esTrasladoCajas(request)) {
            return trasladosCajasService.aplicarTrasladoCajas(request);
        }
        return consignacionesBancariasService.aplicarConsignacion(request);
    }

    private boolean esTrasladoCajas(MovimientoFondoRequestDTO request) {
        return request != null
                && request.getTipoMovimiento() == MovimientoFondoRequestDTO.TipoMovimiento.TRASLADO_CAJAS;
    }

    private void validarAgencia(Integer idAgencia) {
        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException("La agencia es obligatoria.");
        }
    }
}
