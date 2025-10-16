package co.assip.erp.general.service.impl;

import co.assip.erp.general.domain.Empresa;
import co.assip.erp.general.dto.EmpresaDTOs.EmpresaHeader;
import co.assip.erp.general.dto.EmpresaDTOs.EmpresaResponse;
import co.assip.erp.general.repository.EmpresaRepository;
import co.assip.erp.general.service.EmpresaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository repo;

    public EmpresaServiceImpl(EmpresaRepository repo) {
        this.repo = repo;
    }

    @Override
    public EmpresaHeader getEmpresaHeader() {
        Empresa e = repo.findAll().stream().findFirst().orElse(null);
        if (e == null) {
            return EmpresaHeader.builder()
                    .nombre("Empresa")
                    .documentoEmpresa(null)
                    .digitoVerificacion(null)
                    .telefono(null)
                    .celular(null)
                    .direccion(null)
                    .logoUrl(null)
                    .build();
        }
        return EmpresaHeader.builder()
                .nombre(resolveNombre(e))
                .documentoEmpresa(e.getDocumentoEmpresa())
                .digitoVerificacion(e.getDigitoVerificacion())
                .telefono(e.getTelefono())
                .celular(e.getCelular())
                .direccion(null) // no hay columna explícita "direccion" en tu tabla
                .logoUrl(e.getLogoUrl())
                .build();
    }

    @Override
    public EmpresaResponse getEmpresaPrincipal() {
        Empresa e = repo.findAll().stream().findFirst().orElse(null);
        if (e == null) return null;
        return toResponse(e);
    }

    @Override
    public List<EmpresaResponse> listEmpresas() {
        return repo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private String resolveNombre(Empresa e) {
        String rs = safe(e.getRazonSocial());
        if (!rs.isBlank()) return rs;
        String sigla = safe(e.getSiglaEmpresa());
        if (!sigla.isBlank()) return sigla;
        return "Empresa";
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }

    private EmpresaResponse toResponse(Empresa e) {
        return EmpresaResponse.builder()
                .nombre(resolveNombre(e))
                .tipoDocumento(e.getTipoDocumento())
                .documentoEmpresa(e.getDocumentoEmpresa())
                .digitoVerificacion(e.getDigitoVerificacion())
                .razonSocial(e.getRazonSocial())
                .siglaEmpresa(e.getSiglaEmpresa())
                .fechaConstitucion(e.getFechaConstitucion())
                .idPaisDocumento(e.getIdPaisDocumento())
                .idDepartamento(e.getIdDepartamento())
                .idCiudad(e.getIdCiudad())
                .correoCorporativo(e.getCorreoCorporativo())
                .telefono(e.getTelefono())
                .celular(e.getCelular())
                .sitioWeb(e.getSitioWeb())
                .logoUrl(e.getLogoUrl())
                .fkSeguridadCreacion(e.getFkSeguridadCreacion())
                .fechaCreacion(e.getFechaCreacion())
                .fkSeguridadActualizacion(e.getFkSeguridadActualizacion())
                .fechaActualizacion(e.getFechaActualizacion())
                .build();
    }
}
