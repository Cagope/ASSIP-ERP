package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(schema = "catalogos", name = "tipos_empresas")
public class TiposEmpresaCat {

    @Id
    @Column(name = "codigo_tipo_empresa", length = 2)
    private String codigoTipoEmpresa;

    @Column(name = "nombre_tipo_empresa", length = 50)
    private String nombreTipoEmpresa;

    // Getters y Setters
    public String getCodigoTipoEmpresa() {
        return codigoTipoEmpresa;
    }

    public void setCodigoTipoEmpresa(String codigoTipoEmpresa) {
        this.codigoTipoEmpresa = codigoTipoEmpresa;
    }

    public String getNombreTipoEmpresa() {
        return nombreTipoEmpresa;
    }

    public void setNombreTipoEmpresa(String nombreTipoEmpresa) {
        this.nombreTipoEmpresa = nombreTipoEmpresa;
    }
}
