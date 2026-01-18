package co.assip.erp.shared.config;

import co.assip.erp.general.empresas.EmpresaDTO;
import co.assip.erp.general.empresas.EmpresaRepository;
import org.springframework.stereotype.Service;

@Service
public class EmpresaConfigService {

    private final EmpresaRepository repo;

    // ✅ Cache simple (1 empresa en todo el ERP)
    private volatile EmpresaDTO cache;

    public EmpresaConfigService(EmpresaRepository repo) {
        this.repo = repo;
    }

    /**
     * ✅ Retorna la empresa (cacheada).
     * Si necesitas recargar manualmente: refresh()
     */
    public EmpresaDTO getEmpresa() {
        if (cache == null) {
            cache = repo.obtenerEmpresa();
        }
        return cache;
    }

    /**
     * ✅ Forzar recarga desde BD (por ejemplo si actualizas logo/nit desde admin)
     */
    public void refresh() {
        cache = repo.obtenerEmpresa();
    }

    // =========================================================
    // Helpers directos para uso en procesos e informes
    // =========================================================

    /**
     * ✅ Tercero contable de la empresa (obligatorio)
     * (usado para comprobantes automáticos como depreciación)
     */
    public Integer getIdTerceroEmpresa() {
        Long id = getEmpresa().getIdDatosPersonalEmpresa();

        if (id == null || id <= 0) {
            throw new IllegalStateException(
                    "No está configurado id_datos_personal_empresa en general.empresas."
            );
        }

        return id.intValue();
    }

    public String getRazonSocial() {
        return getEmpresa().getRazonSocial();
    }

    public String getSigla() {
        return getEmpresa().getSiglaEmpresa();
    }

    public String getDocumentoEmpresa() {
        return getEmpresa().getDocumentoEmpresa();
    }

    public String getDigitoVerificacion() {
        return getEmpresa().getDigitoVerificacion();
    }

    public String getTipoDocumento() {
        return getEmpresa().getTipoDocumento();
    }

    public String getCorreoCorporativo() {
        return getEmpresa().getCorreoCorporativo();
    }

    public String getTelefono() {
        return getEmpresa().getTelefono();
    }

    public String getCelular() {
        return getEmpresa().getCelular();
    }

    public String getSitioWeb() {
        return getEmpresa().getSitioWeb();
    }

    public String getLogoUrl() {
        return getEmpresa().getLogoUrl();
    }
}
