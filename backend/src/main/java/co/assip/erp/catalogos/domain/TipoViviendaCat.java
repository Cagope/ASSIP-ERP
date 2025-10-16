package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(schema = "catalogos", name = "tipos_viviendas")
public class TipoViviendaCat {

    @Id
    @Column(name = "codigo_tipo_vivienda", length = 2)
    private String codigoTipoVivienda;

    @Column(name = "nombre_tipo_vivienda", length = 50, nullable = false)
    private String nombreTipoVivienda;

    public String getCodigoTipoVivienda() { return codigoTipoVivienda; }
    public void setCodigoTipoVivienda(String codigoTipoVivienda) { this.codigoTipoVivienda = codigoTipoVivienda; }
    public String getNombreTipoVivienda() { return nombreTipoVivienda; }
    public void setNombreTipoVivienda(String nombreTipoVivienda) { this.nombreTipoVivienda = nombreTipoVivienda; }
}
