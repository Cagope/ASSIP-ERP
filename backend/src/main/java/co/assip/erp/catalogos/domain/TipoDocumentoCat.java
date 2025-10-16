package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "tipos_documentos", schema = "catalogos")
public class TipoDocumentoCat {

    @Id
    @Column(name = "tipo_documento", length = 2, nullable = false)
    private String tipoDocumento;

    @Column(name = "nombre_tipo_documento", length = 50)
    private String nombreTipoDocumento;

    @Column(name = "tipo_dv")
    private Boolean tipoDv;

    @Column(name = "tipo_documento_dian", length = 2)
    private String tipoDocumentoDian;

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNombreTipoDocumento() { return nombreTipoDocumento; }
    public void setNombreTipoDocumento(String nombreTipoDocumento) { this.nombreTipoDocumento = nombreTipoDocumento; }

    public Boolean getTipoDv() { return tipoDv; }
    public void setTipoDv(Boolean tipoDv) { this.tipoDv = tipoDv; }

    public String getTipoDocumentoDian() { return tipoDocumentoDian; }
    public void setTipoDocumentoDian(String tipoDocumentoDian) { this.tipoDocumentoDian = tipoDocumentoDian; }
}
