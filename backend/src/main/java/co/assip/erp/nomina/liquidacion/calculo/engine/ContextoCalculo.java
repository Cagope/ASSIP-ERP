package co.assip.erp.nomina.liquidacion.calculo.engine;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

public class ContextoCalculo {

    private final Map<BaseCalculo, BigDecimal> valores =
            new EnumMap<>(BaseCalculo.class);

    public void put(BaseCalculo base, BigDecimal valor) {
        valores.put(base, valor != null ? valor : BigDecimal.ZERO);
    }

    public Map<BaseCalculo, BigDecimal> asMap() {
        return valores;
    }
}
