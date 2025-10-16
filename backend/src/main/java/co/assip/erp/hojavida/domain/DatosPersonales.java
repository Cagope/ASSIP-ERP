package co.assip.erp.hojavida.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "datos_personales", schema = "hoja_vida")
public class DatosPersonales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_datos_personal")
    private Integer idDatosPersonal;

    // Identificación
    @Column(name = "tipo_documento", length = 2, nullable = false)
    private String tipoDocumento;

    @Column(name = "documento", length = 20, nullable = false)
    private String documento;

    @Column(name = "tiene_rut", nullable = false)
    private Boolean tieneRut;

    @Column(name = "digito_verificacion", length = 2)
    private String digitoVerificacion;

    @Column(name = "tipo_persona", length = 2, nullable = false)
    private String tipoPersona; // "1" natural, "2" jurídica

    // Fechas de negocio
    @Column(name = "fecha_documento", nullable = false)
    private LocalDate fechaDocumento;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDate fechaApertura;

    @Column(name = "fecha_actualiza", nullable = false)
    private LocalDate fechaActualiza;

    // Ubicación (derivadas por Service a partir de la ciudad)
    @Column(name = "id_pais_documento")
    private Integer idPaisDocumento;

    @Column(name = "id_departamento_expedicion")
    private Integer idDepartamentoExpedicion;

    @Column(name = "id_ciudad_expedicion", nullable = false)
    private Integer idCiudadExpedicion;

    @Column(name = "id_pais_nacimiento")
    private Integer idPaisNacimiento;

    @Column(name = "id_departamento_nacimiento")
    private Integer idDepartamentoNacimiento;

    @Column(name = "id_ciudad_nacimiento", nullable = false)
    private Integer idCiudadNacimiento;

    // Nombres
    @Column(name = "nombres", length = 100, nullable = false)
    private String nombres;

    @Column(name = "primer_apellido", length = 50, nullable = false)
    private String primerApellido;

    @Column(name = "segundo_apellido", length = 50, nullable = false)
    private String segundoApellido;

    // Catálogos / otros
    @Column(name = "comentario", length = 250, nullable = false)
    private String comentario;

    @Column(name = "codigo_genero", length = 1, nullable = false)
    private String codigoGenero;

    @Column(name = "codigo_estado_civil", length = 1, nullable = false)
    private String codigoEstadoCivil;

    @Column(name = "codigo_escolaridad", length = 2, nullable = false)
    private String codigoEscolaridad;

    @Column(name = "codigo_tipo_vivienda", length = 2, nullable = false)
    private String codigoTipoVivienda;

    @Column(name = "estrato_social", nullable = false)
    private Integer estratoSocial;

    @Column(name = "numero_hijos", nullable = false)
    private Integer numeroHijos;

    @Column(name = "codigo_ocupacion", length = 2, nullable = false)
    private String codigoOcupacion;

    @Column(name = "codigo_sector_economico", length = 3, nullable = false)
    private String codigoSectorEconomico;

    @Column(name = "codigo_actividad_ses", length = 4, nullable = false)
    private String codigoActividadSes;

    @Column(name = "codigo_actividad_dian", length = 4, nullable = false)
    private String codigoActividadDian;

    @Column(name = "codigo_retencion", length = 2, nullable = false)
    private String codigoRetencion;

    @Column(name = "cabeza_familia", length = 1, nullable = false)
    private String cabezaFamilia; // "0" o "1"

    // Auditoría
    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fk_seguridad_actualizacion", nullable = false)
    private Integer fkSeguridadActualizacion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
}
