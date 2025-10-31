package co.assip.erp.hojavida.financieros;

import co.assip.erp.shared.domain.BaseAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "financieros", schema = "hoja_vida")
public class Financiero extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_financiero")
    private Integer idFinanciero;

    @Column(name = "id_datos_personal", nullable = false)
    private Integer idDatosPersonal;

    // ==========================================================
    // 💰 GRUPO DE INGRESOS
    // ==========================================================
    @Column(name = "valor_salario", precision = 18, scale = 2)
    private BigDecimal valorSalario = BigDecimal.ZERO;

    @Column(name = "valor_pension", precision = 18, scale = 2)
    private BigDecimal valorPension = BigDecimal.ZERO;

    @Column(name = "ingresos_arriendo", precision = 18, scale = 2)
    private BigDecimal ingresosArriendo = BigDecimal.ZERO;

    @Column(name = "ingresos_comisiones", precision = 18, scale = 2)
    private BigDecimal ingresosComisiones = BigDecimal.ZERO;

    @Column(name = "otros_ingresos", precision = 18, scale = 2)
    private BigDecimal otrosIngresos = BigDecimal.ZERO;

    @Column(name = "comentario_otros_ingresos", length = 100)
    private String comentarioOtrosIngresos;

    @Column(name = "origen_fondos", length = 100)
    private String origenFondos;

    // ==========================================================
    // 💸 GRUPO DE EGRESOS
    // ==========================================================
    @Column(name = "egresos_familiares", precision = 18, scale = 2)
    private BigDecimal egresosFamiliares = BigDecimal.ZERO;

    @Column(name = "egresos_arriendo", precision = 18, scale = 2)
    private BigDecimal egresosArriendo = BigDecimal.ZERO;

    @Column(name = "egresos_credito", precision = 18, scale = 2)
    private BigDecimal egresosCredito = BigDecimal.ZERO;

    @Column(name = "otros_egresos", precision = 18, scale = 2)
    private BigDecimal otrosEgresos = BigDecimal.ZERO;

    @Column(name = "comentario_otros_egresos", length = 100)
    private String comentarioOtrosEgresos;

    // ==========================================================
    // 🏦 GRUPO DE PATRIMONIO
    // ==========================================================
    @Column(name = "total_activos", precision = 18, scale = 2)
    private BigDecimal totalActivos = BigDecimal.ZERO;

    @Column(name = "total_pasivos", precision = 18, scale = 2)
    private BigDecimal totalPasivos = BigDecimal.ZERO;

    @Column(name = "relacion_financiera", length = 100)
    private String relacionFinanciera;

    @Column(name = "deuda_relacion_financiera", precision = 18, scale = 2)
    private BigDecimal deudaRelacionFinanciera = BigDecimal.ZERO;

    // ==========================================================
    // 📊 CAMPOS DERIVADOS
    // ==========================================================

    @Transient
    public BigDecimal getTotalIngresos() {
        return safe(valorSalario)
                .add(safe(valorPension))
                .add(safe(ingresosArriendo))
                .add(safe(ingresosComisiones))
                .add(safe(otrosIngresos));
    }

    @Transient
    public BigDecimal getTotalEgresos() {
        return safe(egresosFamiliares)
                .add(safe(egresosArriendo))
                .add(safe(egresosCredito))
                .add(safe(otrosEgresos));
    }

    @Transient
    public BigDecimal getBalanceFinal() {
        return getTotalIngresos().subtract(getTotalEgresos());
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
