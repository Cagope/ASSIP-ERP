package co.assip.erp.shared.referencias.catalogos.sector_economico;

import jakarta.persistence.*;

@Entity
@Table(name = "sectores_economicos", schema = "catalogos")
public class SectorEconomico {

    @Id
    @Column(name = "codigo_sector_economico", length = 3)
    private String codigoSectorEconomico;

    @Column(name = "nombre_sector_economico", length = 100)
    private String nombreSectorEconomico;

    // Getters y Setters
    public String getCodigoSectorEconomico() {
        return codigoSectorEconomico;
    }

    public void setCodigoSectorEconomico(String codigoSectorEconomico) {
        this.codigoSectorEconomico = codigoSectorEconomico;
    }

    public String getNombreSectorEconomico() {
        return nombreSectorEconomico;
    }

    public void setNombreSectorEconomico(String nombreSectorEconomico) {
        this.nombreSectorEconomico = nombreSectorEconomico;
    }
}
