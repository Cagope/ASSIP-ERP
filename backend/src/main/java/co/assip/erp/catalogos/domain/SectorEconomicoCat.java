package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(schema = "catalogos", name = "sectores_economicos")
public class SectorEconomicoCat {

    @Id
    @Column(name = "codigo_sector_economico", length = 3)
    private String codigoSectorEconomico;

    @Column(name = "nombre_sector_economico", length = 100)
    private String nombreSectorEconomico;

    public String getCodigoSectorEconomico() { return codigoSectorEconomico; }
    public void setCodigoSectorEconomico(String codigoSectorEconomico) { this.codigoSectorEconomico = codigoSectorEconomico; }
    public String getNombreSectorEconomico() { return nombreSectorEconomico; }
    public void setNombreSectorEconomico(String nombreSectorEconomico) { this.nombreSectorEconomico = nombreSectorEconomico; }
}
