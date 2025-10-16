package co.assip.erp.hojavida.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class FinancierosDTOs {
    private FinancierosDTOs() {}

    // REQUEST: todos obligatorios; validaciones numéricas >= 0; condicionales las maneja el service también.
    public static class FinancieroRequest {
        @NotNull @DecimalMin("0") @JsonAlias({"valor_salario","valorSalario"})
        public BigDecimal valorSalario;

        @NotNull @DecimalMin("0") @JsonAlias({"valor_pension","valorPension"})
        public BigDecimal valorPension;

        @NotNull @DecimalMin("0") @JsonAlias({"ingresos_arriendo","ingresosArriendo"})
        public BigDecimal ingresosArriendo;

        @NotNull @DecimalMin("0") @JsonAlias({"ingresos_comisiones","ingresosComisiones"})
        public BigDecimal ingresosComisiones;

        @NotNull @DecimalMin("0") @JsonAlias({"otros_ingresos","otrosIngresos"})
        public BigDecimal otrosIngresos;

        @NotBlank @Size(max=100) @JsonAlias({"comentario_otros_ingresos","comentarioOtrosIngresos"})
        public String comentarioOtrosIngresos;

        @NotNull @DecimalMin("0") @JsonAlias({"egresos_familiares","egresosFamiliares"})
        public BigDecimal egresosFamiliares;

        @NotNull @DecimalMin("0") @JsonAlias({"egresos_arriendo","egresosArriendo"})
        public BigDecimal egresosArriendo;

        @NotNull @DecimalMin("0") @JsonAlias({"egresos_credito","egresosCredito"})
        public BigDecimal egresosCredito;

        @NotNull @DecimalMin("0") @JsonAlias({"otros_egresos","otrosEgresos"})
        public BigDecimal otrosEgresos;

        @NotBlank @Size(max=100) @JsonAlias({"comentario_otros_egresos","comentarioOtrosEgresos"})
        public String comentarioOtrosEgresos;

        @NotNull @DecimalMin("0") @JsonAlias({"total_activos","totalActivos"})
        public BigDecimal totalActivos;

        @NotNull @DecimalMin("0") @JsonAlias({"total_pasivos","totalPasivos"})
        public BigDecimal totalPasivos;

        @NotBlank @Size(max=100) @JsonAlias({"origen_fondos","origenFondos"})
        public String origenFondos;

        @NotBlank @Size(max=100) @JsonAlias({"relacion_financiera","relacionFinanciera"})
        public String relacionFinanciera;

        @NotNull @DecimalMin("0") @JsonAlias({"deuda_relacion_financiera","deudaRelacionFinanciera"})
        public BigDecimal deudaRelacionFinanciera;
    }

    // RESPONSE: snake_case para el front
    public static class FinancieroResponse {
        public Integer id_financiero;
        public Integer id_datos_personal;
        public BigDecimal valor_salario;
        public BigDecimal valor_pension;
        public BigDecimal ingresos_arriendo;
        public BigDecimal ingresos_comisiones;
        public BigDecimal otros_ingresos;
        public String comentario_otros_ingresos;
        public BigDecimal egresos_familiares;
        public BigDecimal egresos_arriendo;
        public BigDecimal egresos_credito;
        public BigDecimal otros_egresos;
        public String comentario_otros_egresos;
        public BigDecimal total_activos;
        public BigDecimal total_pasivos;
        public String origen_fondos;
        public String relacion_financiera;
        public BigDecimal deuda_relacion_financiera;
    }
}
