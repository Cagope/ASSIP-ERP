package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(schema = "catalogos", name = "paises")
public class PaisCat {

    @Id
    @Column(name = "id_pais")
    private Integer idPais; // PK ya viene sin identity en tu DDL

    @Column(name = "codigo_pais", length = 5)
    private String codigoPais;

    @Column(name = "codigo_dos", length = 5)
    private String codigoDos;

    @Column(name = "nombre_pais", length = 100, nullable = false)
    private String nombrePais;

    // Getters & Setters
    public Integer getIdPais() { return idPais; }
    public void setIdPais(Integer idPais) { this.idPais = idPais; }
    public String getCodigoPais() { return codigoPais; }
    public void setCodigoPais(String codigoPais) { this.codigoPais = codigoPais; }
    public String getCodigoDos() { return codigoDos; }
    public void setCodigoDos(String codigoDos) { this.codigoDos = codigoDos; }
    public String getNombrePais() { return nombrePais; }
    public void setNombrePais(String nombrePais) { this.nombrePais = nombrePais; }
}
