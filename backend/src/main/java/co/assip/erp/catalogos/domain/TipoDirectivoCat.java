package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(schema = "catalogos", name = "tipos_directivos")
public class TipoDirectivoCat {

    @Id
    @Column(name = "codigo_tipo_directivo", length = 2)
    private String codigoTipoDirectivo;

    @Column(name = "nombre_tipo_directivo", length = 50, nullable = false)
    private String nombreTipoDirectivo;

    public String getCodigoTipoDirectivo() { return codigoTipoDirectivo; }
    public void setCodigoTipoDirectivo(String codigoTipoDirectivo) { this.codigoTipoDirectivo = codigoTipoDirectivo; }

    public String getNombreTipoDirectivo() { return nombreTipoDirectivo; }
    public void setNombreTipoDirectivo(String nombreTipoDirectivo) { this.nombreTipoDirectivo = nombreTipoDirectivo; }
}
