package co.assip.erp.depositos.informes.extracto_cuenta;

import co.assip.erp.depositos.informes.extracto_cuenta.dto.*;
import co.assip.erp.general.empresas.EmpresaDTO;
import co.assip.erp.general.empresas.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtractoCuentaService {

    private final ExtractoCuentaRepository repository;
    private final EmpresaRepository empresaRepository;

    public List<ExtractoCuentaBusquedaDTO> buscarCuentas(
            String documento,
            String nombres,
            String primerApellido,
            String segundoApellido,
            String codigoCuenta
    ) {

        return repository.buscarCuentas(
                documento,
                nombres,
                primerApellido,
                segundoApellido,
                codigoCuenta
        );
    }

    public ExtractoCuentaResponseDTO consultar(
            ExtractoCuentaRequestDTO request
    ) {

        validar(request);

        EmpresaDTO empresa =
                empresaRepository.obtenerEmpresa();

        ExtractoCuentaResumenDTO resumen =
                repository.obtenerResumen(request);

        resumen.setRazonSocial(
                empresa.getRazonSocial()
        );

        resumen.setSiglaEmpresa(
                empresa.getSiglaEmpresa()
        );

        resumen.setDocumentoEmpresa(
                empresa.getDocumentoEmpresa()
        );

        resumen.setDigitoVerificacion(
                empresa.getDigitoVerificacion()
        );

        resumen.setTelefonoEmpresa(
                empresa.getTelefono()
        );

        resumen.setCelularEmpresa(
                empresa.getCelular()
        );

        resumen.setSitioWebEmpresa(
                empresa.getSitioWeb()
        );

        resumen.setLogoUrl(
                empresa.getLogoUrl()
        );

        List<ExtractoCuentaMovimientoDTO> movimientos =
                repository.obtenerMovimientos(
                        request,
                        resumen.getSaldoInicial()
                );

        return ExtractoCuentaResponseDTO.builder()
                .resumen(resumen)
                .movimientos(movimientos)
                .build();
    }

    private void validar(
            ExtractoCuentaRequestDTO request
    ) {

        if (request.getIdCuentaAhorro() == null) {
            throw new RuntimeException("Debe seleccionar la cuenta.");
        }

        if (request.getFechaInicial() == null
                || request.getFechaInicial().isBlank()) {

            throw new RuntimeException(
                    "Debe seleccionar la fecha inicial."
            );
        }

        if (request.getFechaFinal() == null
                || request.getFechaFinal().isBlank()) {

            throw new RuntimeException(
                    "Debe seleccionar la fecha final."
            );
        }

    }

}