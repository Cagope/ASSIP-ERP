package co.assip.erp.cajas.recaudos_convenios;

import co.assip.erp.cajas.recaudos_convenios.dto.RecaudoConvenioConvenioDTO;
import co.assip.erp.cajas.recaudos_convenios.dto.RecaudoConvenioDTO;
import co.assip.erp.cajas.recaudos_convenios.dto.RecaudoConvenioRequestDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecaudosConveniosService {

    private final RecaudosConveniosRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public List<RecaudoConvenioConvenioDTO> listarConveniosActivos(
            Integer idAgencia
    ) {

        if (idAgencia == null) {
            throw new RuntimeException("La agencia es obligatoria.");
        }

        return repository.listarConveniosActivos(idAgencia);
    }

    public List<RecaudoConvenioDTO> listarPorProvision(
            Long idProvision
    ) {

        if (idProvision == null) {
            throw new RuntimeException("La provisión es obligatoria.");
        }

        return repository.listarPorProvision(idProvision);
    }

    @Transactional
    public RecaudoConvenioDTO aplicar(
            RecaudoConvenioRequestDTO request
    ) {

        validar(request);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Long id =
                repository.guardar(
                        request,
                        idUsuario
                );

        return repository.obtener(id);
    }

    private void validar(
            RecaudoConvenioRequestDTO request
    ) {

        if (request == null) {
            throw new RuntimeException("No se recibió información del recaudo.");
        }

        if (request.getIdProvision() == null) {
            throw new RuntimeException("La provisión es obligatoria.");
        }

        if (!repository.existeProvisionAbierta(request.getIdProvision())) {
            throw new RuntimeException("La provisión no existe o no está abierta.");
        }

        if (request.getIdConvenio() == null) {
            throw new RuntimeException("El convenio es obligatorio.");
        }

        RecaudoConvenioConvenioDTO convenio =
                repository.obtenerConvenioActivo(
                        request.getIdConvenio()
                );

        if (convenio == null) {
            throw new RuntimeException("El convenio no existe o no está activo.");
        }

        if (isBlank(request.getDocumentoSoporte())) {
            throw new RuntimeException("El documento soporte es obligatorio.");
        }

        if (request.getValorRecaudo() == null
                || request.getValorRecaudo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El valor del recaudo debe ser mayor a cero.");
        }
    }

    private boolean isBlank(
            String value
    ) {
        return value == null
                || value.trim().isEmpty();
    }
}