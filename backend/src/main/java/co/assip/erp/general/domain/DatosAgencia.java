package co.assip.erp.general.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "datos_agencias", schema = "general",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_general_agencia_codigo", columnNames = {"codigo_agencia"})
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DatosAgencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agencia")
    private Integer idAgencia; // int4 en DB

    @Column(name = "codigo_agencia", nullable = false, length = 2)
    private String codigoAgencia;

    @Column(name = "nombre_agencia", nullable = false, length = 100)
    private String nombreAgencia;

    @Column(name = "sigla_agencia", nullable = false, length = 100)
    private String siglaAgencia;

    @Column(name = "direccion_agencia", nullable = false, length = 100)
    private String direccionAgencia;

    @Column(name = "id_departamento")
    private Integer idDepartamento;

    @Column(name = "id_ciudad")
    private Integer idCiudad;

    @Column(name = "correo_agencia", length = 100)
    private String correoAgencia;

    @Column(name = "celular_agencia", length = 10)
    private String celularAgencia;

    @Column(name = "telefono_agencia", length = 7)
    private String telefonoAgencia;

    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion; // int4 en DB

    @Column(name = "fecha_creacion", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private java.time.LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_actualizacion", nullable = false)
    private Integer fkSeguridadActualizacion; // int4 en DB

    @Column(name = "fecha_actualizacion", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private java.time.LocalDateTime fechaActualizacion;
}
