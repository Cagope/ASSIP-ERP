package co.assip.erp.general.service;

import co.assip.erp.general.dto.EmpresaDTOs.EmpresaHeader;
import co.assip.erp.general.dto.EmpresaDTOs.EmpresaResponse;

import java.util.List;

public interface EmpresaService {
    EmpresaHeader getEmpresaHeader();
    EmpresaResponse getEmpresaPrincipal();
    List<EmpresaResponse> listEmpresas();
}
