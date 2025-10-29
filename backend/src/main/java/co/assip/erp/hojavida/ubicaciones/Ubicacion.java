package co.assip.erp.hojavida.ubicaciones;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ubicaciones", schema = "hoja_vida")
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ubicacion")
    private Integer idUbicacion;

    @Column(name = "id_datos_personal", nullable = false)
    private Integer idDatosPersonal;

    @Column(length = 100, nullable = false)
    private String direccion;

    @Column(length = 100)
    private String barrio;

    @Column(length = 7)
    private String telefono;

    @Column(name = "celular_uno", length = 10)
    private String celularUno;

    @Column(name = "celular_dos", length = 10)
    private String celularDos;

    @Column(length = 100)
    private String correo;

    @Column(name = "id_pais")
    private Integer idPais;

    @Column(name = "id_departamento")
    private Integer idDepartamento;

    @Column(name = "id_ciudad")
    private Integer idCiudad;

    @Column(name = "id_zona")
    private Integer idZona; // ✅ Nuevo campo agregado

    @Column(name = "id_sub_zona")
    private Integer idSubZona;

    @Column(name = "FK_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "FK_seguridad_edicion", nullable = false)
    private Integer fkSeguridadEdicion;

    @Column(name = "fecha_edicion")
    private LocalDateTime fechaEdicion;
}
