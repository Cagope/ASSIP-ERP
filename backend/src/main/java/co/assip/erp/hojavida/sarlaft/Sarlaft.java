package co.assip.erp.hojavida.sarlaft;

import co.assip.erp.shared.domain.BaseAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

/**
 * 🧩 Entidad: Sarlaft
 * ------------------------------------------------------------
 * Gestiona la información SARLAFT asociada a cada persona (Hoja de Vida).
 * Incluye controles de exoneración, PEPS, familiares PEPS y operaciones
 * en moneda extranjera o cuentas en el exterior.
 */
@Getter
@Setter
@Entity
@Table(name = "sarlaft", schema = "hoja_vida")
public class Sarlaft extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sarlaft")
    private Integer idSarlaft;

    @Column(name = "id_datos_personal", nullable = false)
    private Integer idDatosPersonal;

    // ============================================================
    // 🟩 Exonerado de transacciones en efectivo
    // ============================================================
    @Column(name = "exoneracion_uiaf")
    private Boolean exoneracionUiaf = false;

    @Column(name = "fecha_exoneracion")
    private LocalDate fechaExoneracion;

    // ============================================================
    // 🟦 Asociado PEPS
    // ============================================================
    @Column(name = "asociado_peps")
    private Boolean asociadoPeps = false;

    @Column(name = "tipo_peps", length = 5)
    private String tipoPeps;

    @Column(name = "observaciones_peps", length = 300)
    private String observacionesPeps;

    @Column(name = "fecha_inicial_peps")
    private LocalDate fechaInicialPeps;

    @Column(name = "fecha_final_peps")
    private LocalDate fechaFinalPeps;

    // ============================================================
    // 🟨 Familiares PEPS
    // ============================================================
    @Column(name = "familia_peps")
    private Boolean familiaPeps = false;

    @Column(name = "tipo_familia_peps", length = 5)
    private String tipoFamiliaPeps;

    @Column(name = "cedula_familia_peps", length = 20)
    private String cedulaFamiliaPeps;

    @Column(name = "codigo_parentesco", length = 2)
    private String codigoParentesco;

    @Column(name = "nombre_familia_peps", length = 100)
    private String nombreFamiliaPeps;

    // ============================================================
    // 💱 Moneda extranjera
    // ============================================================
    @Column(name = "moneda_extranjera")
    private Boolean monedaExtranjera = false;

    @Column(name = "observacion_moneda_extranjera", length = 200)
    private String observacionMonedaExtranjera;

    // ============================================================
    // 🌎 Cuentas en el extranjero
    // ============================================================
    @Column(name = "cuenta_extranjero")
    private Boolean cuentaExtranjero = false;

    @Column(name = "tipo_moneda_extranjera", length = 20)
    private String tipoMonedaExtranjera;

    @Column(name = "numero_cuenta_extranjero", length = 30)
    private String numeroCuentaExtranjero;

    @Column(name = "nombre_banco_extranjero", length = 100)
    private String nombreBancoExtranjero;

    @Column(name = "ciudad_cuenta_extranjero", length = 50)
    private String ciudadCuentaExtranjero;

    @Column(name = "pais_cuenta_extranjero", length = 50)
    private String paisCuentaExtranjero;
}
