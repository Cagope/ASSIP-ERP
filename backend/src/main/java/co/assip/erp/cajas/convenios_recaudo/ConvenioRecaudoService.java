package co.assip.erp.cajas.convenios_recaudo;

import co.assip.erp.cajas.convenios_recaudo.dto.ConvenioRecaudoCuentaDTO;
import co.assip.erp.cajas.convenios_recaudo.dto.ConvenioRecaudoDTO;
import co.assip.erp.cajas.convenios_recaudo.dto.ConvenioRecaudoRequestDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import co.assip.erp.cajas.convenios_recaudo.dto.ConvenioRecaudoBusquedaDTO;

@Service
@RequiredArgsConstructor
public class ConvenioRecaudoService {

    private static final String CODIGO_FORMA_APORTES = "01";

    private final ConvenioRecaudoRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public List<ConvenioRecaudoDTO> listar() {
        return repository.listar();
    }

    public ConvenioRecaudoDTO obtener(Long idConvenio) {

        if (idConvenio == null) {
            throw new RuntimeException("El convenio es obligatorio.");
        }

        ConvenioRecaudoDTO dto =
                repository.obtener(idConvenio);

        if (dto == null) {
            throw new RuntimeException("No existe el convenio de recaudo.");
        }

        return dto;
    }

    public ConvenioRecaudoBusquedaDTO buscarCuentasPorDocumento(
            Integer idAgencia,
            String documento
    ) {

        if (idAgencia == null) {
            throw new RuntimeException("La agencia es obligatoria.");
        }

        if (isBlank(documento)) {
            throw new RuntimeException("El documento es obligatorio.");
        }

        if (!repository.existePersonaPorDocumento(documento)) {
            throw new RuntimeException(
                    "No existe una persona registrada con el documento indicado."
            );
        }

        ConvenioRecaudoBusquedaDTO resumenAportes =
                repository.obtenerResumenAportes(
                        idAgencia,
                        documento
                );

        if (resumenAportes == null) {
            throw new RuntimeException(
                    "La persona existe, pero no tiene cuenta de aportes activa en la agencia."
            );
        }

        if (resumenAportes.getSaldoAportes() == null
                || resumenAportes.getSaldoAportes().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "La persona tiene cuenta de aportes, pero no registra saldo."
            );
        }

        List<ConvenioRecaudoCuentaDTO> cuentas =
                repository.buscarCuentasPorDocumento(
                        idAgencia,
                        documento
                );

        if (cuentas == null || cuentas.isEmpty()) {
            throw new RuntimeException(
                    "La persona tiene cuenta de aportes válida, pero no tiene cuentas disponibles diferentes a aportes."
            );
        }

        resumenAportes.setValido(true);
        resumenAportes.setMensaje(
                "Persona validada correctamente. Seleccione la cuenta que se asociará al convenio."
        );
        resumenAportes.setCuentas(cuentas);

        return resumenAportes;
    }

    @Transactional
    public ConvenioRecaudoDTO crear(
            ConvenioRecaudoRequestDTO request
    ) {

        validarRequest(request, null);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Long id =
                repository.crear(
                        normalizarRequest(request),
                        idUsuario
                );

        return obtener(id);
    }

    @Transactional
    public ConvenioRecaudoDTO actualizar(
            Long idConvenio,
            ConvenioRecaudoRequestDTO request
    ) {

        if (idConvenio == null) {
            throw new RuntimeException("El convenio es obligatorio.");
        }

        obtener(idConvenio);

        validarRequest(request, idConvenio);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        repository.actualizar(
                idConvenio,
                normalizarRequest(request),
                idUsuario
        );

        return obtener(idConvenio);
    }

    @Transactional
    public void eliminar(
            Long idConvenio
    ) {

        if (idConvenio == null) {
            throw new RuntimeException("El convenio es obligatorio.");
        }

        obtener(idConvenio);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        repository.eliminar(
                idConvenio,
                idUsuario
        );
    }

    private void validarRequest(
            ConvenioRecaudoRequestDTO request,
            Long idExcluir
    ) {

        if (request == null) {
            throw new RuntimeException("No se recibió información del convenio.");
        }

        if (request.getIdAgencia() == null) {
            throw new RuntimeException("La agencia es obligatoria.");
        }

        if (isBlank(request.getCodigoConvenio())) {
            throw new RuntimeException("El código del convenio es obligatorio.");
        }

        if (isBlank(request.getNombreConvenio())) {
            throw new RuntimeException("El nombre del convenio es obligatorio.");
        }

        if (request.getIdCuentaAhorro() == null) {
            throw new RuntimeException("La cuenta de ahorro del convenio es obligatoria.");
        }

        String codigo =
                request.getCodigoConvenio().trim();

        String estado =
                normalizarEstado(request.getEstado());

        if (!"A".equals(estado) && !"I".equals(estado)) {
            throw new RuntimeException("El estado del convenio debe ser A o I.");
        }

        ConvenioRecaudoCuentaDTO cuenta =
                repository.obtenerCuenta(
                        request.getIdCuentaAhorro()
                );

        if (cuenta == null) {
            throw new RuntimeException("La cuenta seleccionada no existe.");
        }

        if (cuenta.getIdAgencia() == null
                || !cuenta.getIdAgencia().equals(request.getIdAgencia())) {
            throw new RuntimeException("La cuenta seleccionada no pertenece a la agencia.");
        }

        if (CODIGO_FORMA_APORTES.equals(
                trim(cuenta.getCodigoForma())
        )) {
            throw new RuntimeException("La cuenta del convenio no puede ser una cuenta de aportes.");
        }

        if (Boolean.FALSE.equals(cuenta.getEstadoOperativo())) {
            throw new RuntimeException(cuenta.getMensajeOperativo());
        }

        if (repository.existeCodigoConvenio(
                request.getIdAgencia(),
                codigo,
                idExcluir
        )) {
            throw new RuntimeException("Ya existe un convenio con ese código para la agencia.");
        }

        if (repository.existeCuentaConvenio(
                request.getIdAgencia(),
                request.getIdCuentaAhorro(),
                idExcluir
        )) {
            throw new RuntimeException("La cuenta seleccionada ya está asociada a otro convenio.");
        }

        if (!repository.tieneCuentaAportesValidaConSaldo(
                request.getIdAgencia(),
                cuenta.getDocumento()
        )) {
            throw new RuntimeException(
                    "El titular debe tener una cuenta de aportes válida y con saldo."
            );
        }
    }

    private ConvenioRecaudoRequestDTO normalizarRequest(
            ConvenioRecaudoRequestDTO request
    ) {

        return ConvenioRecaudoRequestDTO.builder()
                .idAgencia(request.getIdAgencia())
                .codigoConvenio(trim(request.getCodigoConvenio()))
                .nombreConvenio(trim(request.getNombreConvenio()))
                .idCuentaAhorro(request.getIdCuentaAhorro())
                .estado(normalizarEstado(request.getEstado()))
                .build();
    }

    private String normalizarEstado(
            String estado
    ) {
        return isBlank(estado)
                ? "A"
                : estado.trim().toUpperCase();
    }

    private String trim(
            String value
    ) {
        return value == null
                ? null
                : value.trim();
    }

    private boolean isBlank(
            String value
    ) {
        return value == null
                || value.trim().isEmpty();
    }
}