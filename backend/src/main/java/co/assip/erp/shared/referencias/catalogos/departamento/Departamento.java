package co.assip.erp.shared.referencias.catalogos.departamento;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "departamentos", schema = "catalogos")
@Getter @Setter
public class Departamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departamento")
    private Integer idDepartamento;

    @Column(name = "codigo_departamento", length = 5)
    private String codigoDepartamento;

    @Column(name = "nombre_departamento", length = 100, nullable = false)
    private String nombreDepartamento;
}
