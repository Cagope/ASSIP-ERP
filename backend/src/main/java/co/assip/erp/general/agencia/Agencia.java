package co.assip.erp.general.agencia;

import co.assip.erp.shared.domain.BaseAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "datos_agencias", schema = "general")
@Getter
@Setter
public class Agencia extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agencia")
    private Integer idAgencia;

    @Column(name = "codigo_agencia", length = 2, nullable = false)
    private String codigoAgencia;

    @Column(name = "nombre_agencia", length = 100, nullable = false)
    private String nombreAgencia;

    @Column(name = "sigla_agencia", length = 100, nullable = false)
    private String siglaAgencia;

    @Column(name = "direccion_agencia", length = 100, nullable = false)
    private String direccionAgencia;

    @Column(name = "correo_agencia", length = 100)
    private String correoAgencia;

    @Column(name = "celular_agencia", length = 10)
    private String celularAgencia;

    @Column(name = "telefono_agencia", length = 7)
    private String telefonoAgencia;

    @Column(name = "id_departamento")
    private Integer idDepartamento;

    @Column(name = "id_ciudad")
    private Integer idCiudad;
}
