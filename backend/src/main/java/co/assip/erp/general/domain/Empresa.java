package co.assip.erp.general.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "empresas", schema = "general")
public class Empresa {

    // No mostraste PK numérica; uso documento_empresa como PK
    @Id
    @Column(name = "documento_empresa", nullable = false, length = 50)
    private String documentoEmpresa;

    @Column(name = "tipo_documento", nullable = false, length = 5)
    private String tipoDocumento;

    @Column(name = "digito_verificacion", length = 2)
    private String digitoVerificacion;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "sigla_empresa", nullable = false, length = 100)
    private String siglaEmpresa;

    @Column(name = "fecha_constitucion")
    private LocalDate fechaConstitucion;

    @Column(name = "id_pais_documento")
    private Integer idPaisDocumento;

    @Column(name = "id_departamento")
    private Integer idDepartamento;

    @Column(name = "id_ciudad")
    private Integer idCiudad;

    @Column(name = "correo_corporativo", length = 150)
    private String correoCorporativo;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "celular", length = 50)
    private String celular;

    @Column(name = "sitio_web", length = 200)
    private String sitioWeb;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_actualizacion", nullable = false)
    private Integer fkSeguridadActualizacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
