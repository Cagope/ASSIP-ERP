package co.assip.erp.depositos.informes.extracto_asociado;

import co.assip.erp.depositos.informes.extracto_asociado.dto.*;
import co.assip.erp.general.empresas.EmpresaDTO;
import co.assip.erp.general.empresas.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtractoAsociadoService {

    private final ExtractoAsociadoRepository repository;
    private final EmpresaRepository empresaRepository;

    public List<ExtractoAsociadoBusquedaDTO> buscarAsociados(
            String documento,
            String nombres,
            String primerApellido,
            String segundoApellido,
            String codigoCuenta
    ) {

        return repository.buscarAsociados(
                documento,
                nombres,
                primerApellido,
                segundoApellido,
                codigoCuenta
        );
    }

    public ExtractoAsociadoResponseDTO consultar(
            ExtractoAsociadoRequestDTO request
    ) {

        validar(request);

        EmpresaDTO empresa =
                empresaRepository.obtenerEmpresa();

        ExtractoAsociadoResumenDTO resumen =
                repository.obtenerResumen(request);

        resumen.setRazonSocial(empresa.getRazonSocial());
        resumen.setSiglaEmpresa(empresa.getSiglaEmpresa());
        resumen.setDocumentoEmpresa(empresa.getDocumentoEmpresa());
        resumen.setDigitoVerificacion(empresa.getDigitoVerificacion());
        resumen.setTelefonoEmpresa(empresa.getTelefono());
        resumen.setCelularEmpresa(empresa.getCelular());
        resumen.setSitioWebEmpresa(empresa.getSitioWeb());
        resumen.setLogoUrl(empresa.getLogoUrl());

        List<ExtractoAsociadoCuentaDTO> cuentas =
                repository.obtenerCuentas(request);

        List<ExtractoAsociadoMovimientoDTO> movimientos =
                repository.obtenerMovimientos(request);

        return ExtractoAsociadoResponseDTO.builder()
                .resumen(resumen)
                .cuentas(cuentas)
                .movimientos(movimientos)
                .build();
    }

    private void validar(
            ExtractoAsociadoRequestDTO request
    ) {

        if (request.getIdDatosPersonal() == null) {
            throw new RuntimeException("Debe seleccionar el asociado.");
        }

        if (request.getFechaInicial() == null || request.getFechaInicial().isBlank()) {
            throw new RuntimeException("Debe seleccionar la fecha inicial.");
        }

        if (request.getFechaFinal() == null || request.getFechaFinal().isBlank()) {
            throw new RuntimeException("Debe seleccionar la fecha final.");
        }
    }

}