package co.assip.erp.shared.referencias.catalogos.tipo_vivienda;

import jakarta.persistence.*;

@Entity
@Table(name = "tipos_viviendas", schema = "catalogos")
public class TipoVivienda {

    @Id
    @Column(name = "codigo_tipo_vivienda", length = 2)
    private String codigoTipoVivienda;

    @Column(name = "nombre_tipo_vivienda", length = 50, nullable = false)
    private String nombreTipoVivienda;

    // Getters y Setters
    public String getCodigoTipoVivienda() {
        return codigoTipoVivienda;
    }

    public void setCodigoTipoVivienda(String codigoTipoVivienda) {
        this.codigoTipoVivienda = codigoTipoVivienda;
    }

    public String getNombreTipoVivienda() {
        return nombreTipoVivienda;
    }

    public void setNombreTipoVivienda(String nombreTipoVivienda) {
        this.nombreTipoVivienda = nombreTipoVivienda;
    }
}
