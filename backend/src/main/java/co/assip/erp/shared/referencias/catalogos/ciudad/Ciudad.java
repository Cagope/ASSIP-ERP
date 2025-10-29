package co.assip.erp.shared.referencias.catalogos.ciudad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import co.assip.erp.shared.referencias.catalogos.departamento.Departamento;

@Entity
@Table(name = "ciudades", schema = "catalogos")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Ciudad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ciudad")
    private Integer idCiudad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departamento", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Departamento departamento;

    @Column(name = "codigo_ciudad", length = 5, nullable = false)
    private String codigoCiudad;

    @Column(name = "nombre_ciudad", length = 100)
    private String nombreCiudad;
}
