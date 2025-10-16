package co.assip.erp.seguridad.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "permisos", schema = "seguridad")
public class Permiso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Long id;
}
