package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(schema = "catalogos", name = "departamentos")
public class DepartamentoCat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departamento")
    private Integer idDepartamento;

    @Column(name = "codigo_departamento", length = 5, unique = true)
    private String codigoDepartamento;

    @Column(name = "nombre_departamento", length = 100, nullable = false)
    private String nombreDepartamento;

    // Getters & Setters
    public Integer getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Integer idDepartamento) { this.idDepartamento = idDepartamento; }

    public String getCodigoDepartamento() { return codigoDepartamento; }
    public void setCodigoDepartamento(String codigoDepartamento) { this.codigoDepartamento = codigoDepartamento; }

    public String getNombreDepartamento() { return nombreDepartamento; }
    public void setNombreDepartamento(String nombreDepartamento) { this.nombreDepartamento = nombreDepartamento; }
}
