package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(schema = "catalogos", name = "ciudades")
public class CiudadCat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ciudad")
    private Integer idCiudad;

    // FK simple (no necesitamos @ManyToOne para catálogos)
    @Column(name = "id_departamento", nullable = false)
    private Integer idDepartamento;

    @Column(name = "codigo_ciudad", length = 5, nullable = false)
    private String codigoCiudad;

    @Column(name = "nombre_ciudad", length = 100)
    private String nombreCiudad;

    // Getters & Setters
    public Integer getIdCiudad() { return idCiudad; }
    public void setIdCiudad(Integer idCiudad) { this.idCiudad = idCiudad; }

    public Integer getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Integer idDepartamento) { this.idDepartamento = idDepartamento; }

    public String getCodigoCiudad() { return codigoCiudad; }
    public void setCodigoCiudad(String codigoCiudad) { this.codigoCiudad = codigoCiudad; }

    public String getNombreCiudad() { return nombreCiudad; }
    public void setNombreCiudad(String nombreCiudad) { this.nombreCiudad = nombreCiudad; }
}
