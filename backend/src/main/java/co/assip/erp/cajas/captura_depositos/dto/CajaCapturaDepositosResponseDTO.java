package co.assip.erp.cajas.captura_depositos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CajaCapturaDepositosResponseDTO {

    private Long idCuentaAhorro;
    private String codigoCuenta;

    private String documento;
    private String nombreAsociado;

    private LocalDate fechaContable;

    private BigDecimal valorTotal;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoFinal;

    private List<Long> movimientosCaja;

    private String mensaje;

    private Boolean requiereFormatoLavadoActivos;
    private Long idFormatoLavadoActivos;
    private String mensajeLavadoActivos;

}