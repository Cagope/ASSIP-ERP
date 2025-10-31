package co.assip.erp.hojavida.referenciaspersonales;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 👥 Entidad — Referencias Personales
 * Esquema: hoja_vida
 *
 * Representa las referencias personales asociadas a una persona.
 * Incluye nombre, dirección, ubicación y medios de contacto.
 */
@Getter
@Setter
@Entity
@Table(name = "referencias_personales", schema = "hoja_vida")
public class ReferenciaPersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_referencia_personal")
    private Integer idReferenciaPersonal;

    @Column(name = "id_datos_personal", nullable = false)
    private Integer idDatosPersonal;

    @Column(name = "nombre_referencia_personal", length = 100, nullable = false)
    private String nombreReferenciaPersonal;

    @Column(name = "direccion_referencia_personal", length = 100, nullable = false)
    private String direccionReferenciaPersonal;

    @Column(name = "id_departamento")
    private Integer idDepartamento;

    @Column(name = "id_ciudad")
    private Integer idCiudad;

    @Column(name = "telefono_referencia_personal", length = 7)
    private String telefonoReferenciaPersonal;

    @Column(name = "celular_referencia_personal", length = 10)
    private String celularReferenciaPersonal;

    @Column(name = "FK_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "FK_seguridad_edicion", nullable = false)
    private Integer fkSeguridadEdicion;

    @Column(name = "fecha_edicion")
    private LocalDateTime fechaEdicion;
}
