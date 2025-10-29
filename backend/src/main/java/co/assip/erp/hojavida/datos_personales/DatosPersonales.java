package co.assip.erp.hojavida.datos_personales;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "datos_personales", schema = "hoja_vida")
public class DatosPersonales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_datos_personal")
    private Integer idDatosPersonal;

    @Column(name = "tipo_documento", length = 2)
    private String tipoDocumento;

    @Column(nullable = false, length = 20)
    private String documento;

    @Column(name = "tipo_persona", length = 1, nullable = false)
    private String tipoPersona;

    @Column(name = "tiene_rut", nullable = false)
    private Boolean tieneRut = false;

    @Column(name = "digito_verificacion", length = 2)
    private String digitoVerificacion;

    @Column(name = "fecha_documento")
    private LocalDate fechaDocumento;

    @Column(name = "id_pais_documento")
    private Integer idPaisDocumento;

    @Column(name = "id_departamento_expedicion")
    private Integer idDepartamentoExpedicion;

    @Column(name = "id_ciudad_expedicion")
    private Integer idCiudadExpedicion;

    @Column(length = 100, nullable = false)
    private String nombres;

    @Column(name = "primer_apellido", length = 50, nullable = false)
    private String primerApellido;

    @Column(name = "segundo_apellido", length = 50)
    private String segundoApellido;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "id_pais_nacimiento")
    private Integer idPaisNacimiento;

    @Column(name = "id_departamento_nacimiento")
    private Integer idDepartamentoNacimiento;

    @Column(name = "id_ciudad_nacimiento")
    private Integer idCiudadNacimiento;

    @Column(name = "fecha_apertura")
    private LocalDate fechaApertura = LocalDate.now();

    @Column(name = "fecha_actualizacion")
    private LocalDate fechaActualizacion = LocalDate.now();

    @Column(name = "codigo_genero", length = 1)
    private String codigoGenero;

    @Column(name = "codigo_estado_civil", length = 1)
    private String codigoEstadoCivil;

    @Column(name = "codigo_escolaridad", length = 2)
    private String codigoEscolaridad;

    @Column(name = "cabeza_familia", length = 1)
    private String cabezaFamilia;

    @Column(name = "estrato_social")
    private Integer estratoSocial;

    @Column(name = "codigo_tipo_vivienda", length = 2)
    private String codigoTipoVivienda;

    @Column(name = "numero_hijos")
    private Integer numeroHijos;

    @Column(name = "codigo_ocupacion", length = 2)
    private String codigoOcupacion;

    @Column(name = "codigo_sector_economico", length = 3)
    private String codigoSectorEconomico;

    @Column(name = "codigo_actividad_ses", length = 4)
    private String codigoActividadSes;

    @Column(name = "codigo_actividad_dian", length = 4)
    private String codigoActividadDian;

    @Column(length = 250, nullable = false)
    private String comentario;

    @Column(length = 100)
    private String foto;

    @Column(name = "firma_uno", length = 100)
    private String firmaUno;

    @Column(name = "firma_dos", length = 100)
    private String firmaDos;

    // Auditoría
    @Column(name = "FK_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion")
    private java.time.LocalDateTime fechaCreacion;

    @Column(name = "FK_seguridad_edicion", nullable = false)
    private Integer fkSeguridadEdicion;

    @Column(name = "fecha_edicion")
    private java.time.LocalDateTime fechaEdicion;
}
