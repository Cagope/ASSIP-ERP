package co.assip.erp.shared.referencias.catalogos.tipo_contrato;

import jakarta.persistence.*;

@Entity
@Table(name = "tipos_contratos", schema = "catalogos")
public class TipoContrato {

    @Id
    @Column(name = "codigo_tipo_contrato", length = 2)
    private String codigoTipoContrato;

    @Column(name = "nombre_tipo_contrato", length = 50)
    private String nombreTipoContrato;

    // Getters y Setters
    public String getCodigoTipoContrato() {
        return codigoTipoContrato;
    }

    public void setCodigoTipoContrato(String codigoTipoContrato) {
        this.codigoTipoContrato = codigoTipoContrato;
    }

    public String getNombreTipoContrato() {
        return nombreTipoContrato;
    }

    public void setNombreTipoContrato(String nombreTipoContrato) {
        this.nombreTipoContrato = nombreTipoContrato;
    }
}
