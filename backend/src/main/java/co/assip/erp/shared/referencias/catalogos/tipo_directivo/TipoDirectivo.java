package co.assip.erp.shared.referencias.catalogos.tipo_directivo;

import jakarta.persistence.*;

@Entity
@Table(name = "tipos_directivos", schema = "catalogos")
public class TipoDirectivo {

    @Id
    @Column(name = "codigo_tipo_directivo", length = 2)
    private String codigoTipoDirectivo;

    @Column(name = "nombre_tipo_directivo", length = 50)
    private String nombreTipoDirectivo;

    // Getters y Setters
    public String getCodigoTipoDirectivo() {
        return codigoTipoDirectivo;
    }

    public void setCodigoTipoDirectivo(String codigoTipoDirectivo) {
        this.codigoTipoDirectivo = codigoTipoDirectivo;
    }

    public String getNombreTipoDirectivo() {
        return nombreTipoDirectivo;
    }

    public void setNombreTipoDirectivo(String nombreTipoDirectivo) {
        this.nombreTipoDirectivo = nombreTipoDirectivo;
    }
}
