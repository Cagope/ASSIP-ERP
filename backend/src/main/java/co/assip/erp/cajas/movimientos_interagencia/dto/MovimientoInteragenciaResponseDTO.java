package co.assip.erp.cajas.movimientos_interagencia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInteragenciaResponseDTO {

    private Long idCuentaAhorro;
    private String codigoCuenta;

    private Integer idAgenciaCaja;
    private Integer idAgenciaCuenta;

    private String documento;
    private String nombreAsociado;

    private LocalDate fechaContable;

    private String tipoMovimiento;
    private String naturaleza;

    private BigDecimal valorEfectivo;
    private BigDecimal valorCheques;
    private BigDecimal valorTotal;

    private BigDecimal saldoAnterior;
    private BigDecimal saldoFinal;

    @Builder.Default
    private List<Long> movimientosCaja = new ArrayList<>();

    private Boolean requiereFormatoLavadoActivos;

    private String mensaje;
}