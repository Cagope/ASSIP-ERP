package co.assip.erp.hojavida.residencia_fiscal;

import co.assip.erp.shared.domain.BaseAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 🧩 Entidad: ResidenciaFiscal
 * ------------------------------------------------------------
 * Almacena la información de residencia fiscal del asociado
 * requerida para el cumplimiento de FATCA / CRS.
 *
 * Cada persona puede registrar únicamente una residencia fiscal
 * (constraint UNIQUE sobre id_datos_personal).
 */
@Getter
@Setter
@Entity
@Table(
        name = "sarlaft_residencia_fiscal",
        schema = "hoja_vida",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sarlaft_residencia_fiscal",
                        columnNames = "id_datos_personal"
                )
        }
)
public class ResidenciaFiscal extends BaseAudit {

    // ============================================================
    // 🔑 Identificación
    // ============================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_residencia_fiscal")
    private Long idResidenciaFiscal;

    @Column(name = "id_datos_personal", nullable = false)
    private Integer idDatosPersonal;

    // ============================================================
    // 🇺🇸 FATCA
    // ============================================================

    /**
     * ¿Es ciudadano de los Estados Unidos?
     */
    @Column(name = "ciudadano_estados_unidos", nullable = false)
    private Boolean ciudadanoEstadosUnidos = false;

    /**
     * ¿Es residente fiscal en los Estados Unidos?
     */
    @Column(name = "residente_fiscal_estados_unidos", nullable = false)
    private Boolean residenteFiscalEstadosUnidos = false;

    // ============================================================
    // 🌍 CRS - Residencia fiscal exterior
    // ============================================================

    /**
     * ¿Es residente fiscal en otro país?
     */
    @Column(name = "residente_fiscal_exterior", nullable = false)
    private Boolean residenteFiscalExterior = false;

    @Column(name = "pais_residencia_fiscal", length = 100)
    private String paisResidenciaFiscal;

    @Column(name = "ciudad_residencia_fiscal", length = 100)
    private String ciudadResidenciaFiscal;

    @Column(name = "direccion_residencia_fiscal", length = 200)
    private String direccionResidenciaFiscal;

    // ============================================================
    // 🆔 Identificación fiscal
    // ============================================================

    @Column(name = "tipo_identificacion_fiscal", length = 30)
    private String tipoIdentificacionFiscal;

    @Column(name = "numero_identificacion_fiscal", length = 50)
    private String numeroIdentificacionFiscal;

    // ============================================================
    // 📝 Observaciones
    // ============================================================

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    // ============================================================
    // 🕓 Auditoría heredada de BaseAudit
    // ------------------------------------------------------------
    // fkSeguridadCreacion
    // fkSeguridadEdicion
    // fechaCreacion
    // fechaEdicion
    // ============================================================

}