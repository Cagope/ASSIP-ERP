package co.assip.erp.seguridad.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "roles", schema = "seguridad")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long id;
}
