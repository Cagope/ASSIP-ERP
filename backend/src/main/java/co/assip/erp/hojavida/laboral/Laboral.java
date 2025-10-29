package co.assip.erp.hojavida.laboral;

import co.assip.erp.shared.domain.BaseAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "laborales", schema = "hoja_vida")
public class Laboral extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_laboral")
    private Integer idLaboral;

    @Column(name = "id_datos_personal", nullable = false)
    private Integer idDatosPersonal;

    @Column(name = "nombre_empresa", nullable = false, length = 100)
    private String nombreEmpresa;

    @Column(length = 100)
    private String direccion;

    @Column(name = "id_pais")
    private Integer idPais;

    @Column(name = "id_departamento")
    private Integer idDepartamento;

    @Column(name = "id_ciudad")
    private Integer idCiudad;

    @Column(name = "telefono_empresa", length = 7)
    private String telefonoEmpresa;

    @Column(name = "celular_empresa", length = 10)
    private String celularEmpresa;

    @Column(name = "correo_empresa", length = 100)
    private String correoEmpresa;

    @Column(name = "codigo_tipo_empresa", length = 2)
    private String codigoTipoEmpresa;

    @Column(name = "empleado_entidad", nullable = false)
    private Boolean empleadoEntidad = false;

    @Column(name = "codigo_tipo_contrato", length = 2)
    private String codigoTipoContrato;

    @Column(name = "codigo_jornada", length = 2)
    private String codigoJornada;

    @Column(name = "nombre_contacto", length = 100)
    private String nombreContacto;

    @Column(name = "celular_contacto", length = 10)
    private String celularContacto;

    @Column(name = "fecha_vinculacion")
    private LocalDate fechaVinculacion;
}
