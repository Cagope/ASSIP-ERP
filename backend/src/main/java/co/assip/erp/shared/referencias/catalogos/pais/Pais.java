package co.assip.erp.shared.referencias.catalogos.pais;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "paises", schema = "catalogos")
@Getter @Setter
public class Pais {
    @Id
    @Column(name = "id_pais")
    private Integer idPais;

    @Column(name = "codigo_pais", length = 5)
    private String codigoPais;

    @Column(name = "codigo_dos", length = 5)
    private String codigoDos;

    @Column(name = "nombre_pais", length = 100, nullable = false)
    private String nombrePais;
}
