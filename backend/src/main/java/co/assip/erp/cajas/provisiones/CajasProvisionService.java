package co.assip.erp.cajas.provisiones;

import co.assip.erp.cajas.provisiones.dto.CajasProvisionCerrarDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionFormDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionListDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionSaveDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.assip.erp.cajas.provisiones.dto.CajaDisponibleDTO;
import co.assip.erp.cajas.provisiones.dto.CajaEstadoDTO;

import java.math.BigDecimal;
import java.util.List;

import co.assip.erp.cajas.provisiones.dto.CajaProvisionActivaDTO;

@Service
@RequiredArgsConstructor
public class CajasProvisionService {

    private final CajasProvisionRepository repository;
    private final UsuarioSesionService usuarioSesionService;

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

        Integer idUsuario =
                usuarioSesionService.idUsuario();

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

        Integer idUsuario =
                usuarioSesionService.idUsuario();

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

    @Transactional
    public Long vincularUsuarioCaja(CajasProvisionSaveDTO dto) {

        validar(dto);

        Integer idUsuario = usuarioSesionService.idUsuario();

        CajasProvisionFormDTO provision = repository
                .obtenerPorCajaYFecha(dto.getIdCaja(), dto.getFechaContable())
                .orElse(null);

        if (provision == null) {
            return repository.crear(dto, idUsuario);
        }

        if ("CERRADA".equalsIgnoreCase(provision.getEstado())) {
            throw new RuntimeException("La caja ya fue cerrada para esta fecha.");
        }

        if (!"ABIERTA".equalsIgnoreCase(provision.getEstado())) {
            throw new RuntimeException("La provisión de caja no está abierta.");
        }

        if (provision.getFkUsuarioApertura() != null
                && !provision.getFkUsuarioApertura().equals(idUsuario)) {
            throw new RuntimeException("Esta caja ya está vinculada a otro usuario.");
        }

        return provision.getIdProvision();
    }

    public List<CajaDisponibleDTO> listarCajasDisponibles(Integer idAgencia) {
        if (idAgencia == null) {
            throw new RuntimeException("La agencia es obligatoria.");
        }

        return repository.listarCajasDisponibles(idAgencia);
    }

    public List<CajaEstadoDTO> listarEstadoCajas(
            Integer idAgencia,
            java.time.LocalDate fechaContable
    ) {

        if (idAgencia == null) {
            throw new RuntimeException("La agencia es obligatoria.");
        }

        if (fechaContable == null) {
            throw new RuntimeException("La fecha contable es obligatoria.");
        }

        return repository.listarEstadoCajas(
                idAgencia,
                fechaContable
        );

    }

    public CajaProvisionActivaDTO obtenerProvisionActivaUsuario() {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        return repository
                .obtenerProvisionActivaUsuario(idUsuario)
                .orElseThrow(() ->
                        new RuntimeException(
                                "El usuario no tiene una caja/provisión abierta."
                        )
                );
    }
}