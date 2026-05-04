package co.assip.erp.cajas.provisiones;

import co.assip.erp.cajas.provisiones.dto.CajasProvisionCerrarDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionFormDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionListDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionSaveDTO;
import co.assip.erp.seguridad.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CajasProvisionService {

    private final CajasProvisionRepository repository;

    public List<CajasProvisionListDTO> listar() {
        return repository.listar();
    }

    public CajasProvisionFormDTO obtenerPorId(Long idProvision) {
        return repository.obtenerPorId(idProvision)
                .orElseThrow(() -> new RuntimeException("No se encontró la provisión de caja."));
    }

    @Transactional
    public Long guardar(CajasProvisionSaveDTO dto) {
        validar(dto);

        Integer idUsuario = SecurityUtils.getIdUsuario();

        if (repository.existeProvision(dto.getIdCaja(), dto.getFechaContable(), dto.getIdProvision())) {
            throw new RuntimeException("Ya existe una provisión para esta caja y fecha contable.");
        }

        if (dto.getIdProvision() == null) {
            return repository.crear(dto, idUsuario);
        }

        repository.actualizar(dto, idUsuario);
        return dto.getIdProvision();
    }

    @Transactional
    public void cerrar(CajasProvisionCerrarDTO dto) {
        if (dto.getIdProvision() == null) {
            throw new RuntimeException("Debe indicar la provisión a cerrar.");
        }

        if (dto.getEfectivoFin() == null || dto.getEfectivoFin().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El efectivo final no puede ser negativo.");
        }

        if (dto.getChequesFin() == null || dto.getChequesFin().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El valor final de cheques no puede ser negativo.");
        }

        Integer idUsuario = SecurityUtils.getIdUsuario();

        repository.cerrar(
                dto.getIdProvision(),
                dto.getEfectivoFin(),
                dto.getChequesFin(),
                dto.getObservacion(),
                idUsuario
        );
    }

    private void validar(CajasProvisionSaveDTO dto) {
        if (dto.getIdCaja() == null) {
            throw new RuntimeException("Debe seleccionar la caja.");
        }

        if (dto.getFechaContable() == null) {
            throw new RuntimeException("Debe ingresar la fecha contable.");
        }

        if (dto.getEfectivoInicio() == null || dto.getEfectivoInicio().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El efectivo inicial no puede ser negativo.");
        }

        if (dto.getChequesInicio() == null || dto.getChequesInicio().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El valor inicial de cheques no puede ser negativo.");
        }
    }

    public List<CajasProvisionListDTO> listarCajasAbiertas(Integer idAgencia, java.time.LocalDate fecha) {
        if (idAgencia == null || fecha == null) {
            throw new RuntimeException("Agencia y fecha son obligatorias.");
        }

        return repository.listarCajasAbiertas(idAgencia, fecha);
    }
}