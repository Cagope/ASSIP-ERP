package co.assip.erp.hojavida.datosfamiliares;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "datos_familiares", schema = "hoja_vida")
public class DatosFamiliar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_datos_familiares")
    private Integer idDatosFamiliares;

    @Column(name = "id_datos_personal", nullable = false)
    private Integer idDatosPersonal;

    @Column(name = "codigo_parentesco", length = 2)
    private String codigoParentesco;

    @Column(name = "nombre_datos_familiar", length = 100, nullable = false)
    private String nombreDatosFamiliar;

    // ⚙️ Se hace opcional para permitir guardar sin documento
    @Column(name = "documento_datos_familiar", length = 20, nullable = true)
    private String documentoDatosFamiliar;

    @Column(name = "telefono_datos_familiar", length = 7)
    private String telefonoDatosFamiliar;

    @Column(name = "celular_datos_familiar", length = 10)
    private String celularDatosFamiliar;

    @Column(name = "direccion_datos_familiar", length = 100, nullable = false)
    private String direccionDatosFamiliar;

    @Column(name = "id_departamento")
    private Integer idDepartamento;

    @Column(name = "id_ciudad")
    private Integer idCiudad;

    // 💰 BigDecimal con valor por defecto
    @Column(name = "ingresos_datos_familiar", precision = 18, scale = 2)
    private BigDecimal ingresosDatosFamiliar = BigDecimal.ZERO;

    @Column(name = "egresos_datos_familiar", precision = 18, scale = 2)
    private BigDecimal egresosDatosFamiliar = BigDecimal.ZERO;

    // 🔘 Boolean simple, no objeto (no puede ser nulo)
    @Column(name = "referencia_familiar", nullable = false)
    private boolean referenciaFamiliar = false;

    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_edicion", nullable = false)
    private Integer fkSeguridadEdicion;

    @Column(name = "fecha_edicion")
    private LocalDateTime fechaEdicion;
}
