package co.assip.erp.contabilidad.plan_cuentas;

import co.assip.erp.shared.domain.BaseAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(
        name = "catalogo_cuentas",
        schema = "contabilidad",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_catalogo_cuentas", columnNames = {"id_agencia", "codigo_cuenta"})
        }
)
public class PlanCuenta extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_catalogo_cuenta")
    private Integer id;

    @Column(name = "id_agencia", nullable = false)
    private Integer idAgencia;

    @Column(name = "codigo_cuenta", length = 11, nullable = false)
    private String codigoCuenta;

    @Column(name = "nombre_cuenta", length = 100, nullable = false)
    private String nombre;

    @Column(name = "naturaleza_cuenta", length = 1, nullable = false)
    private String naturaleza; // D o C

    @Column(name = "nivel_cuenta", nullable = false)
    private Integer nivel;

    @Column(name = "cuenta_operable", nullable = false)
    private Boolean operable = false;

    @Column(name = "id_tipo_especial_cuenta")
    private Integer tipoEspecial;

    @Column(name = "control_entrada_salida")
    private Boolean controlEntradaSalida = false;
}
